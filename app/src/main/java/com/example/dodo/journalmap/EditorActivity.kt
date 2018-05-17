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
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng

class EditorActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var journalLocationBox: Box<JournalLocation>
    private lateinit var journalLocation: JournalLocation
    private lateinit var mButton: Button
    private lateinit var mMap: GoogleMap

    private val EDITOR_CODE = 100


    private var mId = 0L

    private val PLACE_PICKER_REQUEST = 101


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editor)

        // Set up for DB
        journalLocationBox = (application as App).boxStore.boxFor<JournalLocation>()
        mId = intent.getLongExtra("id", 0)

        // Get info from DB
        journalLocation = journalLocationBox.get(mId)

        // Set up PlacePicker
//        val intent = PlacePicker.IntentBuilder() as Intent

        // Set up edit button
        activity_editor_edit_button.setOnClickListener {
            journalLocation.mText = activity_editor_Context.editText?.text.toString()
            journalLocation.mName = activity_editor_Name.editText?.text.toString()
            journalLocation.mDate = activity_editor_Date.editText?.text.toString()

            Log.v("location", "${journalLocation.mName}")

            journalLocationBox.put(journalLocation)

            setResult()


        }
    }

    override fun onMapReady(googleMap : GoogleMap){
        mMap = googleMap
        var defaultposition = LatLng(20.0,20.0)
        var default  = CameraPosition.builder().target(defaultposition).build() as CameraPosition
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(default))
    }

    fun setResult() {
        val intent = Intent(this, JournalActivity::class.java)
        setResult(Activity.RESULT_OK,intent)
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