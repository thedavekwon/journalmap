package com.example.dodo.journalmap

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.example.dodo.journalmap.R.id.*
import io.objectbox.Box
import io.objectbox.BoxStore
import io.objectbox.annotation.Id
import io.objectbox.kotlin.boxFor
import kotlinx.android.synthetic.main.activity_editor.*
import com.google.android.gms.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*

class EditorActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var journalLocationBox: Box<JournalLocation>
    private lateinit var journalLocation: JournalLocation
    private lateinit var mButton: Button
    private lateinit var mMap: GoogleMap
    private lateinit var curloc: LatLng

    private val PLACE_PICKER_REQUEST = 101
    private val EDITOR_CODE = 100
    private var mId = 0L
    var draggableMarker = MarkerOptions()
//
//    var mapFragment = fragmentManager?.findFragmentById(R.id.activity_editor_Map) as SupportMapFragment


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editor)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.activity_editor_Map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Set up for DB
        journalLocationBox = (application as App).boxStore.boxFor<JournalLocation>()
        mId = intent.getLongExtra("id", 0)

        // Get info from DB
        journalLocation = journalLocationBox.get(mId)

//        // Set up PlacePicker
//        val intent =  PlacePicker.IntentBuilder() as Intent

        // Set up edit button
        activity_editor_edit_button.setOnClickListener {
            journalLocation.mText = activity_editor_Context.editText?.text.toString()
            journalLocation.mName = activity_editor_Name.editText?.text.toString()
            journalLocation.mDate = activity_editor_Date.editText?.text.toString()

            Log.v("location", "${journalLocation.mName}")

            journalLocationBox.put(journalLocation)

            setResult()
        }


//        draggableMarker.icon.setOnMarkerDragListener{
//
//        }
//        mMap.setOnMarkerDragListener(listener){
//            draggableMarker.onMarkerDragStart()
//            onMarkerDrag()
//            onMarkerDragEnd()
//            markerDragListener
//        }
    }

    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap



        var defaultposition = LatLng(40.7128, 74.0060)
//        var default = CameraPosition.builder().target(defaultposition).build() as CameraPosition
        mMap.moveCamera(CameraUpdateFactory.newLatLng(defaultposition))
        mMap.addMarker(draggableMarker
                .position(defaultposition)
                .title("New Position")
                .draggable(true)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)))


    }

    fun onMarkerDragStart(draggableMarker: Marker) {
        curloc =  draggableMarker.position

        Log.v("onMarkerDragStart",curloc.toString())
    }

    fun onMarkerDrag(draggableMarker: Marker) {
        curloc = draggableMarker.position

        Log.v("onMarkerDrag", curloc.toString())
    }

    fun onMarkerDragEnd(draggableMarker: Marker) {
        curloc = draggableMarker.position

        Log.v("onMarkerDrag", curloc.toString())
    }

    fun setResult() {
        val intent = Intent(this, JournalActivity::class.java)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }


////    fun updateJournalInfo{
////        journalLocation = journalLocationBox.get(mId)
////        journalLocation.add(JournalLocation {
////
////            journalLocationList.add(JournalLocation(
////                    mText = it.mText,
////                    mName = it.mName,
////                    mImageUri = it.mImageUri,
////                    mLat = it.mLat,
////                    mLng = it.mLng
////            ))
////        }
////                mAdapter . notifyDataSetChanged ()
//    }
}