package com.example.dodo.journalmap

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import io.objectbox.Box
import io.objectbox.BoxStore
import io.objectbox.annotation.Id
import io.objectbox.kotlin.boxFor
import kotlinx.android.synthetic.main.activity_editor.*

class EditorActivity : AppCompatActivity() {

    private lateinit var journalLocationBox: Box<JournalLocation>
    private lateinit var journalLocation: JournalLocation
    private lateinit var mButton: Button

    private var mId = 0L


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editor)

        // Set up for DB
        journalLocationBox = (application as App).boxStore.boxFor<JournalLocation>()
        mId = intent.getLongExtra("id", 0)

        // Get info from DB
        journalLocation = journalLocationBox.get(mId)

        // Set up edit button
        activity_editor_edit_button.setOnClickListener {
            journalLocation.mText = activity_editor_Context.toString()
            journalLocation.mName = activity_editor_Location.toString()
            journalLocation.mDate = activity_editor_Date.toString()

            journalLocationBox.put(journalLocation)

            val intent = Intent(this, JournalActivity::class.java)
            startActivity(intent)
            finish()
        }
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