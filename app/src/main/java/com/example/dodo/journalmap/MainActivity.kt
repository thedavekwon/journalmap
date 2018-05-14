package com.example.dodo.journalmap

import android.content.Intent
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast

import io.objectbox.Box
import io.objectbox.kotlin.boxFor
import io.objectbox.query.Query
import java.io.File

class MainActivity : AppCompatActivity(), MainDialogFragment.updateCards {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

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
        updateJournal()

        // Set up Recycler View
        viewManager = LinearLayoutManager(this)
        viewAdapter = MainCardAdapter(mainCardList)
        recyclerView = findViewById<RecyclerView>(R.id.activity_main_recycler_view).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
        // Set up Floating Button
        val fab = findViewById<FloatingActionButton>(R.id.activity_main_floating_action_button)
        fab.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                try {
                    val mainDialogFragment = MainDialogFragment()
                    val fragmentManager = supportFragmentManager
                    mainDialogFragment.show(fragmentManager, "dialog")
                    viewAdapter.notifyDataSetChanged()
                } catch (e: Exception) {
                    Toast.makeText(this@MainActivity, "wait", Toast.LENGTH_LONG).show()
                }
            }
        })

        val fab_delete = findViewById<FloatingActionButton>(R.id.activity_main_floating_action_button_delete)
        fab_delete.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val journals = journalQeury.find()
                journals.forEach {
                    val deleted = File(it.imageUri).delete()
                    journalBox.remove(it.id)
                }
                updateJournal()
                viewAdapter.notifyDataSetChanged()
                //TODO(delete files too)
            }
        })
    }

    //Add Action Buttons(activity_main_menu)
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.activity_main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    //Control Action Buttons
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
                    imageUri = it.imageUri,
                    name = it.name,
                    title = it.title,
                    date = it.date,
                    lat = it.lat,
                    lng = it.lng))
        }
    }
}