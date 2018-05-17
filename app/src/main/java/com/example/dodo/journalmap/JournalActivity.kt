package com.example.dodo.journalmap

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Log
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_journal.*
import permissions.dispatcher.*
import android.content.pm.ActivityInfo
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import com.example.dodo.journalmap.JournalLocation_.journal
import com.example.dodo.journalmap.R.id.*
import com.google.android.gms.maps.model.Marker
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import com.zhihu.matisse.engine.impl.PicassoEngine
import io.objectbox.Box
import io.objectbox.kotlin.boxFor
import io.objectbox.query.Query
import java.util.*
import kotlin.collections.ArrayList


@RuntimePermissions
class JournalActivity : AppCompatActivity(), OnMapReadyCallback {

    private val REQUEST_CODE_CHOOSE = 23
    private val EDITOR_CODE = 100

    private var lat = 0.0
    private var lng = 0.0
    private var name = ""
    private var mId: Long = 0

    private lateinit var mMap: GoogleMap
    private lateinit var mAdapter: JournalLocationAdapter

    private lateinit var journalQuery: Query<Journal>
    private lateinit var journalBox: Box<Journal>
    private lateinit var journalLocationBox: Box<JournalLocation>


    private var journalLocationList: ArrayList<JournalLocation> = ArrayList()
    private var mMarkers: ArrayList<Marker> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Set up for View
        super.onCreate(savedInstanceState)


        lat = intent.getDoubleExtra("latitude", 0.0)
        lng = intent.getDoubleExtra("longitude", 0.0)
        name = intent.getStringExtra("name")
        mId = intent.getLongExtra("id", 0)

        setContentView(R.layout.activity_journal)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.activity_journal_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Set up for DB
        journalBox = (application as App).boxStore.boxFor<Journal>()
        journalLocationBox = (application as App).boxStore.boxFor<JournalLocation>()
        journalQuery = journalBox.query().build()

        /*
        Log.v("id", "id $mId")
        val q = journalQuery.find()
        q.forEach {
            Log.v("id", "id is ${it.id}")
        }
        */

        // Set up for List View
        activity_journal_list_view_itself.emptyView = findViewById(android.R.id.empty)
        mAdapter = JournalLocationAdapter(this@JournalActivity, R.layout.activity_journal_list_view, journalLocationList)
        activity_journal_list_view_itself.adapter = mAdapter
        updateJournalLocation()

        // Set up for FAB
        activity_journal_fab.setOnClickListener {
            photoPickerWithPermissionCheck()
        }

        // Set up for default Home Button
        activity_journal_home_button.setOnClickListener {
            moveToDefaultLocation()
        }


        // Set up for View Model
        /*
        val model: JournalLocationViewModel = ViewModelProviders.of(this).get(JournalLocationViewModel::class.java)
        model.getJournalLocationLiveData(journalLocationBox).observe(this, android.arch.lifecycle.Observer<List<JournalLocation>> {
            journalLocationList = ArrayList(it)
            mAdapter.notifyDataSetChanged()
        })
        */
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == Activity.RESULT_OK) {
            try {
                Matisse.obtainResult(data).forEach {
                    // Temp Location Name Title for now
                    var random = Random().nextFloat()
                    random = (random + 10000.0f) / 10000
                    //
                    val journalLocation = JournalLocation(mLng = lng * random,
                            mLat = lat * random,
                            mImageUri = it.toString(),
                            mName = "",
                            mText = "")
                    val journalLocations = journalBox.get(mId)
                    journalLocations.mJournalLocations?.add(journalLocation)

                    //TODO()
                    val marker = mMap.addMarker(MarkerOptions()
                                    .position(LatLng(journalLocation.mLat,journalLocation.mLng))
                                    .title("Marker in ${journalLocation.mName}"))
                    mMarkers.add(marker)
                    journalBox.put(journalLocations)
                    //Log.v("journalbox", "size is ${journalBox.get(mId).mJournalLocations?.size}")
                    //Log.v("journalbox", "journalbox ${journalBox.get(mId).mJournalLocations?.get(0)}")
                }
                journalBox.get(mId).mJournalLocations?.forEach {
                    journalLocationList.add(it)
                }
                //Log.v("", "journallocation ${journalBox.get(mId).mJournalLocations?.get(0)?.mImageUri}")
                mAdapter.notifyDataSetChanged()
            } catch (e : Exception) {
                e.printStackTrace()
            }
        }
        if(requestCode == EDITOR_CODE){
            if(resultCode == Activity.RESULT_OK) {
                updateJournalLocation()
            }
        }
        mAdapter.notifyDataSetChanged()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val curLoc = LatLng(lat, lng)
        mMap.moveCamera(CameraUpdateFactory.newLatLng(curLoc))
    }

    @NeedsPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun photoPicker() {
        Matisse.from(this@JournalActivity)
                .choose(MimeType.allOf())
                .countable(true)
                .maxSelectable(9)
                .gridExpectedSize(resources.getDimensionPixelSize(R.dimen.grid_expected_size))
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                .thumbnailScale(0.85f)
                .imageEngine(PicassoEngine())
                .forResult(REQUEST_CODE_CHOOSE)
    }

    @OnShowRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun showRationaleForPhotoPicker(request: PermissionRequest) {
        AlertDialog.Builder(this)
                .setMessage("Permission for Storage")
                .setPositiveButton("allow", { _, _ -> request.proceed() })
                .setNegativeButton("deny", { _, _ -> request.cancel()})
                .show()
    }

    @OnPermissionDenied(android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun showDeniedForPhotoPicker() {
        Toast.makeText(this, "denied", Toast.LENGTH_SHORT).show()
    }

    @OnNeverAskAgain(android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun showNeverAskForCamera() {
        Toast.makeText(this, "never ask again", Toast.LENGTH_SHORT).show()
    }

    private fun updateJournalLocation() {
        val journalLocations = journalBox.get(mId).mJournalLocations

        if (!journalLocationList.isEmpty()) {
            journalLocationList.clear()
        }

        journalLocations?.forEach {
            journalLocationList.add(it)
        }
        mAdapter.notifyDataSetChanged()
    }

    fun moveMapCamera(latLng: LatLng) {
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
    }

    fun moveToDefaultLocation(){
        moveMapCamera(LatLng(lat, lng))
    }

    fun openEditor() {
        Log.v("openEditor", "openEditorActivity")
        val editorIntent = Intent(this, EditorActivity::class.java)
        editorIntent.putExtra("id", mId)
        startActivityForResult(editorIntent, EDITOR_CODE)
    }




/*
    class JournalLocationViewModel : ViewModel() {

        private lateinit var journalLocationLiveData: ObjectBoxLiveData<JournalLocation>

        fun getJournalLocationLiveData(journalLocationBox: Box<JournalLocation>?) : ObjectBoxLiveData<JournalLocation> {
            if (journalLocationLiveData == null) {
                journalLocationLiveData = ObjectBoxLiveData<JournalLocation>(journalLocationBox?.query()?.build())
            }
            return journalLocationLiveData
        }
    }
*/
}
