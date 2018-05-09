package com.example.dodo.journalmap

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ListView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class JournalActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_journal)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.activity_journal_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val listView = findViewById<ListView>(R.id.activity_journal_list_view)
        listView.emptyView = findViewById(android.R.id.empty)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val mapIntent = intent
        val lat = mapIntent.getDoubleExtra("latitude", 0.0)
        val lng = mapIntent.getDoubleExtra("longitude", 0.0)
        val name = mapIntent.getStringExtra("name")
        Log.v("lat, long", "$lat   $lng")
        // Add a marker in Sydney and move the camera
        val curLoc = LatLng(lat, lng)
        mMap.addMarker(MarkerOptions().position(curLoc).title("Marker in $name"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(curLoc))
    }
}
