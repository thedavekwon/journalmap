package com.example.dodo.journalmap

import android.app.Activity
import android.content.Context
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
import com.google.maps.android.clustering.ClusterManager
import permissions.dispatcher.*
import android.content.pm.ActivityInfo
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.ExifInterface
import android.net.Uri
import android.view.ViewGroup
import android.widget.ImageView
import com.akexorcist.googledirection.DirectionCallback
import com.akexorcist.googledirection.GoogleDirection
import com.akexorcist.googledirection.constant.TransportMode
import com.akexorcist.googledirection.model.Direction
import com.google.android.gms.maps.model.*
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ui.IconGenerator
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import com.zhihu.matisse.engine.impl.PicassoEngine
import io.objectbox.Box
import io.objectbox.kotlin.boxFor
import io.objectbox.query.Query
import kotlin.collections.ArrayList
import kotlinx.android.synthetic.main.activity_journal.*
import java.io.File
import java.io.FileInputStream


@RuntimePermissions
class JournalActivity : AppCompatActivity(),
        OnMapReadyCallback,
        ClusterManager.OnClusterClickListener<JournalLocation>,
        ClusterManager.OnClusterInfoWindowClickListener<JournalLocation>,
        ClusterManager.OnClusterItemInfoWindowClickListener<JournalLocation>,
        ClusterManager.OnClusterItemClickListener<JournalLocation> {

    private val REQUEST_CODE_CHOOSE = 23
    private val EDITOR_CODE = 100

    private var lat = 0.0
    private var lng = 0.0
    private var name = ""
    private var mId: Long = 0

    private lateinit var mMap: GoogleMap
    private lateinit var mAdapter: JournalLocationAdapter
    private lateinit var mapFragment: SupportMapFragment

    private lateinit var journalQuery: Query<Journal>
    private lateinit var journalBox: Box<Journal>
    private lateinit var journalLocationBox: Box<JournalLocation>

    private lateinit var mClusterManager: ClusterManager<JournalLocation>

    private var journalLocationList: ArrayList<JournalLocation> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Set up for View
        super.onCreate(savedInstanceState)

        lat = intent.getDoubleExtra("latitude", 0.0)
        lng = intent.getDoubleExtra("longitude", 0.0)
        name = intent.getStringExtra("name")
        mId = intent.getLongExtra("id", 0)

        setContentView(R.layout.activity_journal)
        mapFragment = supportFragmentManager.findFragmentById(R.id.activity_journal_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Set up for DB
        journalBox = (application as App).boxStore.boxFor<Journal>()
        journalLocationBox = (application as App).boxStore.boxFor<JournalLocation>()
        journalQuery = journalBox.query().build()

        // Set up for List View
        mAdapter = JournalLocationAdapter(this@JournalActivity, R.layout.activity_journal_list_view, journalLocationList)
        activity_journal_list_view_itself.adapter = mAdapter

        // Set up for FAB
        activity_journal_fab.setOnClickListener {
            photoPickerWithPermissionCheck()
        }

        // Set up for default Home Button
        activity_journal_home_button.setOnClickListener {
            moveToDefaultLocation()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.v("onActivityResult", "$requestCode, $resultCode")
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == Activity.RESULT_OK) {
            try {
                Matisse.obtainResult(data).forEach {
                    val filepath = getPathFromUri(it.toString())
                    val exif = ExifInterface(filepath)
                    val latLng = exifLatLng(exif)
                    val journalLocation = JournalLocation(
                            mLat = latLng[0],
                            mLng = latLng[1],
                            mImageUri = it.toString(),
                            mName = "",
                            mText = "",
                            mDate = "")
                    val journal = journalBox.get(mId)
                    journal.mJournalLocations?.add(journalLocation)
                    journalBox.put(journal)
                }
                updateJournalLocation()
                updateJournalPath()
            } catch (e : Exception) {
                e.printStackTrace()
            }
        }
        if(requestCode == EDITOR_CODE){
            if(resultCode == Activity.RESULT_OK) {
                Log.v("edited", "edited")
                updateJournalLocation()
                updateJournalPath()
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

        // Cluster Manager
        mClusterManager = ClusterManager<JournalLocation>(this, mMap)
        mClusterManager.renderer = JournalLocationRenderer()

        mMap.setOnCameraIdleListener(mClusterManager)
        mMap.setOnMarkerClickListener(mClusterManager)
        mMap.setOnInfoWindowClickListener(mClusterManager)

        mClusterManager.setOnClusterClickListener(this)
        mClusterManager.setOnClusterInfoWindowClickListener(this)
        mClusterManager.setOnClusterItemClickListener(this)
        mClusterManager.setOnClusterItemInfoWindowClickListener(this)

        mClusterManager.cluster()
        updateJournalLocation()
        updateJournalPath()
    }

    override fun onClusterClick(cluster: Cluster<JournalLocation>?): Boolean {
        val firstName = cluster!!.items.iterator().next().mName
        Toast.makeText(this, "${cluster.size} including $firstName)", Toast.LENGTH_SHORT).show()

        val builder = LatLngBounds.builder()
        cluster.items.forEach { builder.include(it.position) }

        val bounds = builder.build()

        try {
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return true
    }

    override fun onClusterInfoWindowClick(cluster: Cluster<JournalLocation>?) {
        return
    }

    override fun onClusterItemInfoWindowClick(p0: JournalLocation?) {
        return
    }

    override fun onClusterItemClick(p0: JournalLocation?): Boolean {
        return false
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
        if (checkNoDifference()) return
        val journalLocations = journalBox.get(mId).mJournalLocations
        if (!journalLocationList.isEmpty()) {
            journalLocationList.clear()
        }
        journalLocations?.forEach {
            journalLocationList.add(it)
        }
        addItems()
        mAdapter.notifyDataSetChanged()
        mClusterManager.cluster()
    }

    private fun updateJournalPath() {
        if (journalLocationList.size < 2) return
        if (checkNoDifference()) return
        val size = journalLocationList.size
        val start = LatLng(journalLocationList[0].mLat, journalLocationList[0].mLng)
        val locList = ArrayList<LatLng>()
        for (i in 1 until size-1) {
            locList.add(LatLng(journalLocationList[i].mLat, journalLocationList[i].mLng))
        }
        val last = LatLng(journalLocationList[size-1].mLat, journalLocationList[size-1].mLng)

        Log.v("path", "$start, $locList, $last")

        GoogleDirection.withServerKey("AIzaSyB042ESM7syHb52l9pFH-0RO8RIMd2E9eU")
                .from(start)
                .and(locList)
                .to(last)
                .transportMode(TransportMode.WALKING)
                .execute(object: DirectionCallback{
                    override fun onDirectionFailure(t: Throwable?) {
                        Toast.makeText(this@JournalActivity, "failed", Toast.LENGTH_SHORT)
                    }

                    override fun onDirectionSuccess(direction: Direction?, rawBody: String?) {
                        Toast.makeText(this@JournalActivity, "succeed", Toast.LENGTH_SHORT)
                    }
                })
    }

    private fun checkNoDifference(): Boolean {
        val journalLocations = journalBox.get(mId).mJournalLocations
        if (journalLocationList.size != journalLocations?.size) {
            return false
        }
        for (i in 0 until journalLocationList.size) {
            if (journalLocationList[i].id != journalLocations[i].id) {
                return false
            }
        }
        return true
    }

    fun moveMapCamera(latLng: LatLng) {
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
    }

    private fun moveToDefaultLocation(){
        moveMapCamera(LatLng(lat, lng))
    }

    private fun addItems() {
        mClusterManager.clearItems()
        journalLocationList.forEach {
            mClusterManager.addItem(it)
        }
    }

    private inner class JournalLocationRenderer(context: Context,
                                                map: GoogleMap,
                                                clusterManager: ClusterManager<JournalLocation>) :
            DefaultClusterRenderer<JournalLocation>(context, map, clusterManager) {

        private val mIconGenerator = IconGenerator(applicationContext)
        private val mClusterIconGenerator = IconGenerator(applicationContext)

        private lateinit var mImageView: ImageView
        private lateinit var mClusterImageView: ImageView
        private var mDimension = 0


        constructor() : this(applicationContext, mMap, mClusterManager) {
            val multiProfile = layoutInflater.inflate(R.layout.multi_profile, null)
            mClusterIconGenerator.setContentView(multiProfile)
            mClusterImageView = multiProfile.findViewById(R.id.multi_profile_image) as ImageView

            mImageView = ImageView(applicationContext)
            mDimension = resources.getDimension(R.dimen.custom_profile_image).toInt()
            mImageView.layoutParams = ViewGroup.LayoutParams(mDimension, mDimension)
            val padding = resources.getDimension(R.dimen.custom_profile_padding).toInt()

            mImageView.setPadding(padding, padding, padding, padding)
            mIconGenerator.setContentView(mImageView)
        }

        override fun onBeforeClusterItemRendered(item: JournalLocation?, markerOptions: MarkerOptions?) {
            mImageView.setImageDrawable(decodeFile(File(getPathFromUri(item!!.mImageUri))))
            val icon = mIconGenerator.makeIcon()
            markerOptions?.icon(BitmapDescriptorFactory.fromBitmap(icon))?.title(item.mName)
        }

        override fun onBeforeClusterRendered(cluster: Cluster<JournalLocation>?, markerOptions: MarkerOptions?) {
            val journalLocationPhotos = ArrayList<Drawable>(Math.min(4, cluster!!.size))
            val width = mDimension
            val height = mDimension

            cluster.items.forEach {
                if (journalLocationPhotos.size < 4) {
                    val drawable = decodeFile(File(getPathFromUri(it.mImageUri)))
                    drawable!!.setBounds(0, 0, width, height)
                    journalLocationPhotos.add(drawable)
                }
            }
            val multiDrawable = MultiDrawable(journalLocationPhotos)
            multiDrawable.setBounds(0, 0, width, height)

            mClusterImageView.setImageDrawable(multiDrawable)
            val icon = mClusterIconGenerator.makeIcon(cluster.size.toString())
            markerOptions?.icon(BitmapDescriptorFactory.fromBitmap(icon))
        }

        override fun shouldRenderAsCluster(cluster: Cluster<JournalLocation>?): Boolean {
            return cluster!!.size > 1
        }
    }

    private fun getPathFromUri(uri: String): String? {
        val contentUri = Uri.parse(uri)
        val cursor = contentResolver.query(contentUri, null, null, null, null)
        cursor.moveToNext()
        val path = cursor.getString(cursor.getColumnIndex("_data"))
        cursor.close()
        return path
    }

    private fun decodeFile(f: File): Drawable? {
        try {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeStream(FileInputStream(f), null, options)

            val REQUIRED_SIZE = 320
            var scale = 2

            while(options.outWidth / scale / 2 >= REQUIRED_SIZE &&
                    options.outHeight / scale /2 >= REQUIRED_SIZE) {
                scale *= 2
            }

            val options2 = BitmapFactory.Options()
            options2.inSampleSize = scale
            val bitmap = BitmapFactory.decodeStream(FileInputStream(f), null, options2)
            return BitmapDrawable(resources, bitmap)
        } catch(e : Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun exifLatLng(exif: ExifInterface): ArrayList<Double> {
        val array = ArrayList<Double>()
        try {
            val geoDegree = GeoDegree(exif)
            val exifLat = geoDegree.latitude.toDouble()
            val exifLng = geoDegree.longitude.toDouble()
            array.addAll(arrayOf(exifLat, exifLng))
        } catch (e: Exception) {
            array.addAll(arrayOf(lat, lng))
        }
        return array
    }

    fun openEditor(id : Long) {
        val editorIntent = Intent(this, EditorActivity::class.java)
        editorIntent.putExtra("id", id)
        startActivityForResult(editorIntent, EDITOR_CODE)
    }
}