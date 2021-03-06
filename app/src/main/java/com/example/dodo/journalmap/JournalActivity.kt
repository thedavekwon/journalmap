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
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.support.media.ExifInterface
import android.net.Uri
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.akexorcist.googledirection.DirectionCallback
import com.akexorcist.googledirection.GoogleDirection
import com.akexorcist.googledirection.constant.TransportMode
import com.akexorcist.googledirection.model.Direction
import com.akexorcist.googledirection.util.DirectionConverter
import com.baoyz.swipemenulistview.SwipeMenuCreator
import com.baoyz.swipemenulistview.SwipeMenuItem
import com.baoyz.swipemenulistview.SwipeMenuListView
import com.google.android.gms.maps.model.*
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ui.IconGenerator
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import com.zhihu.matisse.engine.impl.PicassoEngine
import io.objectbox.Box
import io.objectbox.kotlin.boxFor
import io.objectbox.query.Query
import kotlin.collections.ArrayList
import kotlinx.android.synthetic.main.activity_journal_panel.*
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
    private val JOURNAL_ACTIVITY = 101

    private var lat = 0.0
    private var lng = 0.0
    private var name = ""
    private var mId: Long = 0

    private lateinit var mMap: GoogleMap
    private lateinit var mMarkerDragListener: GoogleMap.OnMarkerDragListener
    private lateinit var mAdapter: JournalLocationAdapter
    private lateinit var mapFragment: SupportMapFragment

    private lateinit var journalQuery: Query<Journal>
    private lateinit var journalLocationQuery: Query<JournalLocation>
    private lateinit var journalBox: Box<Journal>
    private lateinit var journalLocationBox: Box<JournalLocation>

    private lateinit var mClusterManager: ClusterManager<JournalLocation>

    private lateinit var mLayout: SlidingUpPanelLayout
    private lateinit var creator: SwipeMenuCreator

    private var polylineOptionList: ArrayList<Polyline> = ArrayList()
    private var journalLocationList: ArrayList<JournalLocation> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Set up for View
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_journal_panel)

        lat = intent.getDoubleExtra("latitude", 0.0)
        lng = intent.getDoubleExtra("longitude", 0.0)
        name = intent.getStringExtra("name")
        mId = intent.getLongExtra("id", 0)

        // Set up for DB
        journalBox = (application as App).boxStore.boxFor<Journal>()
        journalLocationBox = (application as App).boxStore.boxFor<JournalLocation>()
        journalQuery = journalBox.query().build()
        journalLocationQuery = journalLocationBox.query().build()

        // Set up for Marker Drag
        mMarkerDragListener = object : GoogleMap.OnMarkerDragListener {
            private lateinit var journalLocation: JournalLocation
            private lateinit var changed: LatLng

            override fun onMarkerDrag(p0: Marker?) {
                return
            }

            override fun onMarkerDragEnd(p0: Marker?) {
                changed = p0!!.position
                journalLocation = journalLocationBox.get(p0.title.toLong())
                journalLocation.mLat = changed.latitude
                journalLocation.mLng = changed.longitude
                journalLocationBox.put(journalLocation)
                updateJournalLocation()
                updateJournalPath()
                updateCardPhoto(false)
            }

            override fun onMarkerDragStart(p0: Marker?) {
                return
            }
        }

        // Set up for Map
        mapFragment = supportFragmentManager.findFragmentById(R.id.activity_journal_map_panel) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Set up for List View
        mAdapter = JournalLocationAdapter(this@JournalActivity, R.layout.activity_journal_list_view, journalLocationList)
        activity_journal_list_view_panel.adapter = mAdapter

        activity_journal_list_view_panel.setOnItemClickListener { _, _, position, _ ->
            if (mLayout.panelState == SlidingUpPanelLayout.PanelState.EXPANDED ||
                    mLayout.panelState == SlidingUpPanelLayout.PanelState.ANCHORED) {
                mLayout.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
            }
            moveMapCamera(LatLng(journalLocationList[position].mLat, journalLocationList[position].mLng))
        }

        activity_journal_list_view_panel.setOnItemLongClickListener { _, _, position, _ ->
            Log.v("long click", "openEditDialog")
            openEditDialog(journalLocationList[position].id)
            true
        }

        mLayout = findViewById<SlidingUpPanelLayout>(R.id.activity_journal_sliding_layout)

        mLayout.addPanelSlideListener(object: SlidingUpPanelLayout.PanelSlideListener {
            override fun onPanelSlide(panel: View?, slideOffset: Float) {
                return
            }

            override fun onPanelStateChanged(panel: View?, previousState: SlidingUpPanelLayout.PanelState?, newState: SlidingUpPanelLayout.PanelState?) {
                return
            }
        })

        mLayout.setFadeOnClickListener {
            mLayout.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
        }
        mLayout.anchorPoint = 0.6f

        // Set up for Photo Picker
        activity_journal_fab.setOnClickListener {
            photoPickerWithPermissionCheck()
        }

        // Set up for Swipe to Delete
        creator = SwipeMenuCreator{
            val deleteItem = SwipeMenuItem(applicationContext)
            deleteItem.width = dp2px(90f)
            deleteItem.icon = BitmapDrawable(
                    resources,
                    Bitmap.createScaledBitmap(
                            (resources.getDrawable(R.drawable.ic_delete, applicationContext.theme) as BitmapDrawable).bitmap,
                            120,
                            120,
                            false
                    )
            )
            it.addMenuItem(deleteItem)
            return@SwipeMenuCreator
        }
        activity_journal_list_view_panel.setMenuCreator(creator)

        activity_journal_list_view_panel.setOnMenuItemClickListener { position, _, index ->
            if(index == 0) {
                deleteJournalLocation(position)
            }
            false
        }
        activity_journal_list_view_panel.setSwipeDirection(SwipeMenuListView.DIRECTION_LEFT)
    }

    private fun dp2px(dp: Float): Int {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                resources.displayMetrics
        ).toInt()
    }

    private fun deleteJournalLocation(position: Int) {
        val journalLocation = mAdapter.getItem(position)
        mAdapter.remove(journalLocation)
        journalLocationBox.remove(journalLocation.id)
        forceUpdateJournalLocation()
        updateJournalPath()
    }

    fun updateCardPhoto(date: Boolean) {
        val builder = LatLngBounds.builder()
        var last = journalLocationList[0].mDate
        var first = journalLocationList[0].mDate

        var str = ""
        journalLocationList.forEach {
            boundCreate(LatLng(it.mLat, it.mLng)).forEach {
                builder.include(it)
            }
            if (date) {
                if (last < it.mDate) {
                    last = it.mDate
                }
                if (first > it.mDate) {
                    first = it.mDate
                }
                str = str + " " + it.mDate
            }
        }
//        Log.v("updateCardPhoto", str)
//        Log.v("updateCardPhoto", "$first, $last")

        val bounds = builder.build()
        val journal = journalBox.get(mId).also {
            Log.v("updateCardPhoto", "${it.mLat}, ${it.mLng}")
            it.mLat = bounds.center.latitude
            it.mLng = bounds.center.longitude
        }
        Log.v("updateCardPhoto", "${journal.mLat}, ${journal.mLng}")
        if (date) {
            if (first == last) {
                journal.mDate = first
            } else {
                journal.mDate = "$first~$last"
            }
        }

        journalBox.put(journal)

//        try {
//            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//        val callback = GoogleMap.SnapshotReadyCallback {
//            FileUtils.changeImage(File(journalBox.get(mId).mImageUri), it)
//            Toast.makeText(this, "photo update", Toast.LENGTH_LONG)
//        }
//        mMap.snapshot(callback)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.v("onActivityResult", "$requestCode, $resultCode")
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == Activity.RESULT_OK) {
            try {
                Matisse.obtainResult(data).forEach {
                    val filepath = getPathFromUri(it.toString())
                    val exif = ExifInterface(filepath!!)
                    val latLng = exifLatLng(exif)
                    val journalLocation = JournalLocation(
                            mLat = latLng[0],
                            mLng = latLng[1],
                            mImageUri = it.toString(),
                            mName = "",
                            mText = "",
                            mDate = "")
                    journalLocationBox.put(journalLocation)
                    val journal = journalBox.get(mId)
                    journal.mJournalLocations?.add(journalLocation)
                    journalBox.put(journal)
                    Log.v("size", "size ${journalLocationQuery.find().size}")
                }
                updateJournalLocation()
                updateCardPhoto(true)
                updateJournalPath()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (requestCode == EDITOR_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Log.v("edited", "edited")
                updateJournalLocation()
            }
        }
        mAdapter.notifyDataSetChanged()
    }

    override fun onBackPressed() {
        if (mLayout.panelState == SlidingUpPanelLayout.PanelState.EXPANDED ||
                mLayout.panelState == SlidingUpPanelLayout.PanelState.ANCHORED) {
            mLayout.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
        } else {
            super.onBackPressed()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val curLoc = LatLng(lat, lng)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(curLoc,11f))

        // Cluster Manager
        mClusterManager = ClusterManager<JournalLocation>(this, mMap)
        mClusterManager.renderer = JournalLocationRenderer()

        mMap.setOnCameraIdleListener(mClusterManager)
        mMap.setOnMarkerClickListener(mClusterManager)
        mMap.setOnInfoWindowClickListener(mClusterManager)
        mMap.setOnMarkerDragListener(mMarkerDragListener)

        mClusterManager.setOnClusterClickListener(this)
        mClusterManager.setOnClusterInfoWindowClickListener(this)
        mClusterManager.setOnClusterItemClickListener(this)
        mClusterManager.setOnClusterItemInfoWindowClickListener(this)

        mClusterManager.cluster()
        updateJournalLocation()
        updateJournalPath()
    }

    override fun onClusterClick(cluster: Cluster<JournalLocation>?): Boolean {
//        val firstName = cluster!!.items.iterator().next().mName

        val builder = LatLngBounds.builder()
        cluster!!.items.forEach {
            boundCreate(it.position).forEach {
                builder.include(it)
            }
        }

        val bounds = builder.build()

        try {
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return true
    }

    private fun boundCreate(latLng: LatLng):Array<LatLng> {
        return arrayOf(
                LatLng(latLng.latitude-0.005, latLng.longitude-0.005),
                LatLng(latLng.latitude-0.005, latLng.longitude+0.005),
                LatLng(latLng.latitude+0.005, latLng.longitude-0.005),
                LatLng(latLng.latitude+0.005, latLng.longitude+0.005)
        )
    }

    override fun onClusterInfoWindowClick(cluster: Cluster<JournalLocation>?) {
        return
    }

    override fun onClusterItemInfoWindowClick(p0: JournalLocation?) {
        return
    }

    override fun onClusterItemClick(p0: JournalLocation?): Boolean {
        return true
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
                .setNegativeButton("deny", { _, _ -> request.cancel() })
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

    private fun forceUpdateJournalLocation() {
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

    fun updateJournalLocation() {
        if (checkNoDifference()) return
        forceUpdateJournalLocation()
    }

    private fun updateJournalPath() {
        Log.v("updateJournalPath", "executed")
        if (journalLocationList.size < 2) return
        polylineOptionList.forEach { it.remove() }

        //if (checkNoDifference()) return
        val size = journalLocationList.size
        val start = LatLng(journalLocationList[0].mLat, journalLocationList[0].mLng)
        val locList = ArrayList<LatLng>()
        for (i in 1 until size - 1) {
            locList.add(LatLng(journalLocationList[i].mLat, journalLocationList[i].mLng))
        }
        val last = LatLng(journalLocationList[size - 1].mLat, journalLocationList[size - 1].mLng)

        Log.v("updateJournalPath", "$start, $locList, $last")

        if (locList.isEmpty()) {
            GoogleDirection.withServerKey("key")
                    .from(start)
                    .to(last)
                    .transportMode(TransportMode.WALKING)
                    .execute(object : DirectionCallback {
                        override fun onDirectionFailure(t: Throwable?) {
                            Toast.makeText(this@JournalActivity, "failed", Toast.LENGTH_SHORT)
                            Log.v("updateJournalPath", "failed")
                        }

                        override fun onDirectionSuccess(direction: Direction?, rawBody: String?) {
                            Log.v("updateJournalPath", "succeed")
                            try {
                                Log.v("updateJournalPath", "$direction")
                                val stepList = direction!!.routeList[0].legList[0].stepList
                                val curPolyline = DirectionConverter.createTransitPolyline(
                                        applicationContext,
                                        stepList,
                                        5,
                                        Color.BLUE,
                                        3,
                                        Color.BLUE)
                                curPolyline.forEach {
                                    val poly = mMap.addPolyline(it)
                                    polylineOptionList.add(poly)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    })
        } else {
            GoogleDirection.withServerKey("key")
                    .from(start)
                    .and(locList)
                    .to(last)
                    .transportMode(TransportMode.WALKING)
                    .execute(object : DirectionCallback {
                        override fun onDirectionFailure(t: Throwable?) {
                            Toast.makeText(this@JournalActivity, "failed", Toast.LENGTH_SHORT)
                            Log.v("updateJournalPath", "failed")
                        }

                        override fun onDirectionSuccess(direction: Direction?, rawBody: String?) {
                            Log.v("updateJournalPath", "succeed")
                            try {
                                Log.v("updateJournalPath", "$direction")
                                val stepLists = direction!!.routeList[0].legList
                                stepLists.forEach {
                                    val curPolyline = DirectionConverter.createTransitPolyline(
                                            applicationContext,
                                            it.stepList,
                                            3,
                                            Color.BLUE,
                                            3,
                                            Color.BLUE)
                                    curPolyline.forEach {
                                        val poly = mMap.addPolyline(it)
                                        polylineOptionList.add(poly)
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    })
        }
    }

    private fun checkNoDifference(): Boolean {
        val journalLocations = journalBox.get(mId).mJournalLocations
        if (journalLocationList.size != journalLocations?.size) {
            return false
        }
        for (i in 0 until journalLocationList.size) {
            if (journalLocationList[i].id != journalLocations[i].id ||
                    journalLocationList[i].mName != journalLocations[i].mName ||
                    journalLocationList[i].mText != journalLocations[i].mText ||
                    journalLocationList[i].mDate != journalLocations[i].mDate ||
                    journalLocationList[i].mLat != journalLocations[i].mLat ||
                    journalLocationList[i].mLng != journalLocations[i].mLng    ) {
                return false
            }
        }
        return true
    }

    fun moveMapCamera(latLng: LatLng) {
        Log.v("marker to move", "${latLng.latitude}, ${latLng.longitude}")
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
    }

    fun openEditDialog(id: Long) {
        try {
            val bundle = Bundle()
            bundle.putLong("mId", id)
            val editDialogFragment = EditDialogFragment()
            editDialogFragment.arguments = bundle
            val fragmentManager = fragmentManager
            editDialogFragment.show(fragmentManager, "editdialog")
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
            markerOptions?.icon(BitmapDescriptorFactory.fromBitmap(icon))?.title(item.id.toString())
            markerOptions?.draggable(true)
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

            val size = 320
            var scale = 2

            while (options.outWidth / scale / 2 >= size &&
                    options.outHeight / scale / 2 >= size) {
                scale *= 2
            }

            val options2 = BitmapFactory.Options()
            options2.inSampleSize = scale
            val bitmap = BitmapFactory.decodeStream(FileInputStream(f), null, options2)
            return BitmapDrawable(resources, bitmap)
        } catch (e: Exception) {
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
}
