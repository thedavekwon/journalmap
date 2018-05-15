package com.example.dodo.journalmap

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

    private var mainCardList = ArrayList<MainCard>()
    private lateinit var journalQeury: Query<Journal>
    private lateinit var journalBox: Box<Journal>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set up Toolbar
        setSupportActionBar(findViewById(R.id.toolbar))

        // Set up Db
        journalBox = (application as App).boxStore.boxFor<Journal>()
        journalQeury = journalBox.query().build()

        // Set up Recycler View
        viewManager = LinearLayoutManager(this)
        viewAdapter = MainCardAdapter(mainCardList, this)
        recyclerView = findViewById<RecyclerView>(R.id.activity_main_recycler_view).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
        // Set up Drag and Drop
        val callback = SimpleItemTouchHelperCallback(viewAdapter as MainCardAdapter)
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

        // Set up Floating Button for Delete
        activity_main_floating_action_button_delete.setOnClickListener{
            val journals = journalQeury.find()
            journals.forEach {
                deleteJournal(it)
            }
            updateJournal()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
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
        Log.v("check", "updateJournal Working")
        val journals = journalQeury.find()
        //var tmp = ""
        //journals.forEach { tmp += "$it " }
        //Log.v("journals", tmp)
        if (mainCardList.size != 0) {
            mainCardList.clear()
        }
        journals.forEach {
            mainCardList.add(MainCard(
                    id = it.id,
                    imageUri = it.mImageUri,
                    name = it.mName,
                    title = it.mTitle,
                    date = it.mDate,
                    lat = it.mLat,
                    lng = it.mLng))
        }
        viewAdapter.notifyDataSetChanged()
    }

    fun deleteJournal(card: MainCard) {
        journalBox.remove(card.mId)
        File(card.mImageUri).delete()
    }

    fun deleteJournal(journal: Journal) {
        File(journal.mImageUri).delete()
        journalBox.remove(journal.id)
    }
}