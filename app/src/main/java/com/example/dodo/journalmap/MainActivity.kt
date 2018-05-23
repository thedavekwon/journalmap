package com.example.dodo.journalmap

import android.app.Activity
import android.content.Intent
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.widget.Toast

import kotlinx.android.synthetic.main.activity_main.*

import io.objectbox.Box
import io.objectbox.kotlin.boxFor
import io.objectbox.query.Query
import java.io.File

class MainActivity : AppCompatActivity(), MainDialogFragment.updateCards, OnStartDragListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var mItemTouchHelper: ItemTouchHelper

    private var journalList = ArrayList<Journal>()

    private lateinit var journalQuery: Query<Journal>
    private lateinit var journalBox: Box<Journal>
    private lateinit var journalLocationBox: Box<JournalLocation>


    private val JOURNAL_ACTIVITY = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set up Toolbar
        setSupportActionBar(findViewById(R.id.toolbar))

        // Set up Db
        journalBox = (application as App).boxStore.boxFor<Journal>()
        journalLocationBox = (application as App).boxStore.boxFor<JournalLocation>()
        journalQuery = journalBox.query().build()

        // Set up Recycler View
        viewManager = LinearLayoutManager(this)
        viewAdapter = MainCardAdapter(journalList)
        recyclerView = findViewById<RecyclerView>(R.id.activity_main_recycler_view).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
        // Set up Drag and Drop
        val callback = SimpleItemTouchHelperCallback(viewAdapter as MainCardAdapter, this)
        mItemTouchHelper = ItemTouchHelper(callback)
        mItemTouchHelper.attachToRecyclerView(recyclerView)
        updateJournal()


        // Set up Floating Button for Add
        activity_main_floating_action_button.setOnClickListener {
            try {
                val mainDialogFragment = MainDialogFragment()
                val fragmentManager = supportFragmentManager
                mainDialogFragment.show(fragmentManager, "dialog")
                updateJournal()
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "wait", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.v("onActivityResult", "$requestCode, $resultCode")
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == JOURNAL_ACTIVITY && resultCode == Activity.RESULT_OK) {
            Log.v("edited", "edited")
            updateJournal()
        }
        viewAdapter.notifyDataSetChanged()
    }

    // Add Action Buttons(activity_main_menu)
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.activity_main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // Control Action Buttons
    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.activity_main_menu_action_settings -> {
            val settingsIntent = Intent(this, SettingsActivity::class.java)
            startActivity(settingsIntent)
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    override fun updateCards() {
        updateJournal()
    }

    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
        mItemTouchHelper.startDrag(viewHolder)
    }

    private fun updateJournal() {
        val journals = journalQuery.find().also { it.sortBy { it.mLoc } }
        if (journalList.size != 0) { journalList.clear() }
        journalList.addAll(journals)
        viewAdapter.notifyDataSetChanged()
    }

    fun deleteJournal(journal: Journal) {
        journal.mJournalLocations?.forEach {
            journalLocationBox.remove(it.id)
        }
        File(journal.mImageUri).delete()
        journalBox.remove(journal.id)
    }
}