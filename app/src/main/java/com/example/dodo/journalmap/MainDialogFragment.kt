package com.example.dodo.journalmap

import android.Manifest
import android.app.AlertDialog
import android.graphics.Bitmap
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
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import io.objectbox.Box
import io.objectbox.BoxStore
import io.objectbox.kotlin.boxFor
import permissions.dispatcher.*
import java.text.SimpleDateFormat
import java.util.*

const val googleMapApiUrl = "http://maps.googleapis.com/maps/api/geocode/json?address="

@RuntimePermissions
class MainDialogFragment : DialogFragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var journalBox: Box<Journal>
    private var name = ""
    private var lat = 0.0
    private var lng = 0.0
    private var imageUriId = 0
    private var imageUri = ""
    private var title = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_main_dialog, container, false)
        val nameText = rootView.findViewById<EditText>(R.id.fragment_main_dialog_city_name)
        val searchBtn = rootView.findViewById<Button>(R.id.fragment_main_dialog_city_search)
        val mapFragment = fragmentManager?.findFragmentById(R.id.fragment_main_dialog_city_map) as SupportMapFragment
        val queue = Volley.newRequestQueue(activity?.applicationContext)
        val titleText = rootView.findViewById<EditText>(R.id.fragment_main_dialog_city_title)
        val saveBtn = rootView.findViewById<Button>(R.id.fragment_main_dialog_city_save)
        // Set up DB
        journalBox = (activity?.application as App).boxStore.boxFor<Journal>()

        mapFragment.getMapAsync(this)

        searchBtn.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                Log.v("name", nameText.text.toString())
                try {
                    queue.add(getLocationFromGoogle(nameText.text.toString()))
                } catch (e: Exception) {
                    Toast.makeText(activity?.applicationContext, "Not Found Try Again", Toast.LENGTH_LONG).show()
                }
            }
        })

        saveBtn.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                // Snapshot the map
                val callback = GoogleMap.SnapshotReadyCallback {
                    title = titleText.text.toString()
                    //Log.v("title", title)
                    saveJournal(it)
                }
                mMap.snapshot(callback)
            }
        })
        return rootView
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun saveJournal(it: Bitmap) {
        try {
            imageUri = FileUtils.storeImage(context, it)
            val journal = Journal(
                    imageUri = imageUri,
                    title = title,
                    date = SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().time),
                    lat = lat,
                    lng = lng)
            journalBox.put(journal)
            //Update Card
            (activity as updateCards).updateCards()
            dismiss()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(37.566535, 126.9779692), 9.0f))
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val f = fragmentManager?.findFragmentById(R.id.fragment_main_dialog_city_map)
        if (f != null) {
            fragmentManager?.beginTransaction()?.remove(f)?.commit()
        }
    }

    fun getLocationFromGoogle(loc: String): JsonObjectRequest {
        val preprocessedName = loc.replace(" ", "%20")
        val url = googleMapApiUrl + preprocessedName
        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
                Response.Listener { response ->
                    try {
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
                    } catch (e: Exception) {
                        Toast.makeText(activity?.applicationContext, "Not Found Try Again", Toast.LENGTH_LONG).show()
                    }
                },
                Response.ErrorListener {
                    Toast.makeText(activity?.applicationContext, "Not Found Try Again", Toast.LENGTH_LONG).show()
                }
        )
        return jsonObjectRequest
    }

    @OnShowRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun showRationaleForStorage(request: PermissionRequest) {
        AlertDialog.Builder(activity)
                .setMessage("permission required")
                .setPositiveButton("allow", { _, _ -> request.proceed() })
                .setNegativeButton("deny", { _, _ -> request.proceed() })
                .show()
    }

    @OnPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun showDeniedForStorage() {
        Toast.makeText(activity, "Permission Denied", Toast.LENGTH_SHORT).show()
    }

    @OnNeverAskAgain(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun showNeverAskForCamera() {
        Toast.makeText(activity, "Never Ask Permission", Toast.LENGTH_SHORT).show()
    }

    interface updateCards {
        fun updateCards()
    }
}