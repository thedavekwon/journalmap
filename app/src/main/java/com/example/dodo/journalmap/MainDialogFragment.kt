package com.example.dodo.journalmap

import android.location.Location
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.TimeoutError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import io.objectbox.Box
import io.objectbox.kotlin.boxFor
import java.text.SimpleDateFormat
import java.util.*

const val googleMapApiUrl = "http://maps.googleapis.com/maps/api/geocode/json?address="

class MainDialogFragment : DialogFragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var journalBox: Box<Journal>
    private var name = ""
    private var lat = 0.0
    private var lng = 0.0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_main_dialog, container, false)
        val nameText = rootView.findViewById<EditText>(R.id.fragment_main_dialog_city_name)
        val searchBtn = rootView.findViewById<Button>(R.id.fragment_main_dialog_city_search)
        val mapFragment = fragmentManager?.findFragmentById(R.id.fragment_main_dialog_city_map) as SupportMapFragment
        val queue = Volley.newRequestQueue(activity?.applicationContext)
        val titleText = rootView.findViewById<EditText>(R.id.fragment_main_dialog_city_title)
        val saveBtn = rootView.findViewById<Button>(R.id.fragment_main_dialog_city_save)

        val formatter = SimpleDateFormat("yyyy-mm-dd")
        // Set up DB
        journalBox = (activity?.application as App).boxStore.boxFor<Journal>()

        mapFragment.getMapAsync(this)

        searchBtn.setOnClickListener(object: View.OnClickListener {
            override fun onClick(v: View?) {
                Log.v("name", nameText.text.toString())
                queue.add(getLocationFromGoogle(nameText.text.toString()))
            }
        })

        saveBtn.setOnClickListener(object: View.OnClickListener{
            override fun onClick(v: View?) {
                val journal = Journal(title=name,
                        date=formatter.format(Calendar.getInstance().time),
                        lat = lat,
                        lng = lng)
                journalBox.put(journal)
                dismiss()
            }
        })

        return rootView
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(37.566535, 126.9779692), 9.0f))
    }

    fun getLocationFromGoogle(loc: String): JsonObjectRequest {
        val preprocessedName = loc.replace(" ", "%20")
        val url = googleMapApiUrl + preprocessedName
        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
                Response.Listener { response ->
                    val jsonArray = response.getJSONArray("results")
                    var jsonObject = jsonArray.getJSONObject(0)
                    name = jsonObject.getJSONArray("address_components")
                            .getJSONObject(0)
                            .getString("short_name")
                    jsonObject = jsonObject.getJSONObject("geometry")
                            .getJSONObject("location")
                    lat = jsonObject.getDouble("lat")
                    lng = jsonObject.getDouble("lng")
                    val curLoc = LatLng(lat, lng)
                    mMap.addMarker(MarkerOptions().position(curLoc).title("Marker in $name"))
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(curLoc, 9.0f))
                    Log.v("map", "$lat, $lng")
                },
                Response.ErrorListener {
                    Toast.makeText(activity?.applicationContext, "Not Found Try Again", Toast.LENGTH_LONG).show()
                }
        )
        return jsonObjectRequest
    }
}