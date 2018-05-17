package com.example.dodo.journalmap

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import io.objectbox.Box
import io.objectbox.BoxStore
import io.objectbox.annotation.Id
import io.objectbox.kotlin.boxFor

class EditorActivity : AppCompatActivity() {

    private lateinit var journalLocationBox: Box<JournalLocation>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editor)

        // Set up for DB
        journalLocationBox = (application as App).boxStore.boxFor<JournalLocation>()
        mId = intent.getLongExtra("id",0)




    }
}
