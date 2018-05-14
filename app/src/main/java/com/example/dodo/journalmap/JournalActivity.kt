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

    private var lat = 0.0
    private var lng = 0.0

    private lateinit var mMap: GoogleMap
    private lateinit var mAdapter: JournalLocationAdapter

    private lateinit var journalLocationQuery: Query<JournalLocation>
    private lateinit var journalLocationBox: Box<JournalLocation>
    private var journalLocationList: ArrayList<JournalLocation> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Set up for View
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_journal)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.activity_journal_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Set up for DB
        journalLocationBox = (application as App).boxStore.boxFor<JournalLocation>()
        journalLocationQuery = journalLocationBox.query().build()
        updateJournalLocation()

        // Set up for List View
        activity_journal_list_view_itself.emptyView = findViewById(android.R.id.empty)
        mAdapter = JournalLocationAdapter(this@JournalActivity, R.layout.activity_journal_list_view, journalLocationList)
        activity_journal_list_view_itself.adapter = mAdapter
        activity_journal_fab.setOnClickListener {
            photoPickerWithPermissionCheck()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == Activity.RESULT_OK) {
            try {
                Matisse.obtainResult(data).forEach {
                    // Temp Location Name Title for now
                    var random = Random().nextFloat()
                    random = (random + 2.0f) / 3
                    //
                    val journalLocation = JournalLocation(mLng = lat * random,
                            mLat = lat * random,
                            mImageUri = it.toString(),
                            mName = "",
                            mText = "")
                    journalLocationBox.put(journalLocation)
                    journalLocationList.add(journalLocation)
                }
                mAdapter.notifyDataSetChanged()
            } catch (e : Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val mapIntent = intent
        lat = mapIntent.getDoubleExtra("latitude", 0.0)
        lng = mapIntent.getDoubleExtra("longitude", 0.0)
        val name = mapIntent.getStringExtra("name")
        Log.v("lat, lng", "$lat   $lng")
        val curLoc = LatLng(lat, lng)
        mMap.addMarker(MarkerOptions().position(curLoc).title("Marker in $name"))
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
        val journalLocations = journalLocationQuery.find()

        if (!journalLocationList.isEmpty()) {
            journalLocationList.clear()
        }

        journalLocations.forEach {
            journalLocationList.add(JournalLocation(
                    id = it.id,
                    mText = it.mText,
                    mName = it.mName,
                    mImageUri = it.mImageUri,
                    mLat = it.mLat,
                    mLng = it.mLng
            ))
        }
    }
}
