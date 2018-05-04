package com.example.dodo.journalmap

import android.content.Intent
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set up Toolbar
        setSupportActionBar(findViewById(R.id.toolbar))

        // Set up Recycler View
        val mainCardList = ArrayList<MainCard>()
        for (i in 1..30) {
            if (i < 10) {
                mainCardList.add(MainCard(R.drawable.card_image_test1, "card_image_test1", "2018-04-0$i"))
                mainCardList.add(MainCard(R.drawable.card_image_test2, "card_image_test2", "2018-04-0$i"))
            } else {
                mainCardList.add(MainCard(R.drawable.card_image_test1, "card_image_test1", "2018-04-$i"))
                mainCardList.add(MainCard(R.drawable.card_image_test2, "card_image_test2", "2018-04-$i"))
            }
        }
        viewManager = LinearLayoutManager(this)
        viewAdapter = MainCardAdapter(mainCardList)
        recyclerView = findViewById<RecyclerView>(R.id.activity_main_recycler_view).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

        // Set up Floating Button
        val fab = findViewById<FloatingActionButton>(R.id.activity_main_floating_action_button)
        fab.setOnClickListener(object: View.OnClickListener{
            override fun onClick(v: View?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId){
        R.id.activity_main_menu_action_settings -> {
            val settingsIntent = Intent(this, SettingsActivity::class.java)
            startActivity(settingsIntent)
            true
        } else -> {
            super.onOptionsItemSelected(item)
        }
    }
}
