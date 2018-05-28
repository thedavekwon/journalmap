package com.example.dodo.journalmap

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.DialogFragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.dodo.journalmap.Journal_.*
import com.example.dodo.journalmap.R.layout.fragment_main_dialog
import com.github.ybq.android.spinkit.style.DoubleBounce
import com.github.ybq.android.spinkit.style.Wave
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog
import io.objectbox.Box
import io.objectbox.BoxStore
import io.objectbox.kotlin.boxFor
import kotlinx.android.synthetic.main.activity_maps.*
import permissions.dispatcher.*
import java.text.SimpleDateFormat
import java.util.*

const val googleMapApiUrl = "http://maps.googleapis.com/maps/api/geocode/json?address="

@RuntimePermissions
class MainDialogFragment : DialogFragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var journalBox: Box<Journal>
    private lateinit var mProgressBar: SweetAlertDialog
    private lateinit var mMarker: Marker

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

        // Set up Progress Dialog
        mProgressBar = SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE)
        mProgressBar.progressHelper.barColor = Color.parseColor("#A5DC86")
        mProgressBar.titleText = "Loading"
        mProgressBar.setCancelable(true)


        // Set up DB
        journalBox = (activity?.application as App).boxStore.boxFor<Journal>()

        mapFragment.getMapAsync(this)

        searchBtn.setOnClickListener {
            Log.v("name", nameText.text.toString())
            try {
                queue.add(getLocationFromGoogle(nameText.text.toString()))
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(activity?.applicationContext, "Not Found Try Again", Toast.LENGTH_LONG).show()
            }
        }


        saveBtn.setOnClickListener {
            try {
                mMarker.remove()
            } catch (e: Exception) {
                SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Oops...")
                        .setContentText("Select Specific Location to Progress")
                        .show()
                return@setOnClickListener
            }
            // Snapshot the map
            val callback = GoogleMap.SnapshotReadyCallback {
                title = titleText.text.toString()
                saveJournal(it)
            }
            mMap.snapshot(callback)
        }
        return rootView

    }


    @SuppressLint("SimpleDateFormat")
    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    fun saveJournal(it: Bitmap) {
        try {
            imageUri = FileUtils.storeImage(context, it)
            val journal = Journal(
                    mImageUri = imageUri,
                    mTitle = title,
                    mDate = SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().time),
                    mLat = lat,
                    mLng = lng,
                    mLoc = SimpleDateFormat("yyyyMMddmmss").format(Calendar.getInstance().time),
                    mName = name
            )
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

    override fun onDestroyView() {
        super.onDestroyView()
        val f = fragmentManager?.findFragmentById(R.id.fragment_main_dialog_city_map)
        if (f != null) {
            fragmentManager?.beginTransaction()?.remove(f)?.commit()
        }
    }

    private fun getLocationFromGoogle(loc: String): JsonObjectRequest {

        val preprocessedName = loc.replace(" ", "%20")
        val url = googleMapApiUrl + preprocessedName
        return JsonObjectRequest(Request.Method.GET, url, null,
                Response.Listener { response ->
                    try {
                        mProgressBar.show()
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
                        mMarker = mMap.addMarker(MarkerOptions().position(curLoc).title("Marker in $name"))
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(curLoc, 9.0f))
                        Log.v("map", "$lat, $lng")
                        mProgressBar.dismissWithAnimation()
                        mProgressBar.cancel()
                    } catch (e: Exception) {
                        Toast.makeText(activity?.applicationContext, "1", Toast.LENGTH_LONG).show()
                        mProgressBar.dismissWithAnimation()
                        mProgressBar.cancel()
                    }
                },
                Response.ErrorListener {
                    Toast.makeText(activity?.applicationContext, "2", Toast.LENGTH_LONG).show()
                    mProgressBar.dismissWithAnimation()
                    mProgressBar.cancel()

                }
        )
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

