package com.example.dodo.journalmap

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.maps.model.LatLng
import com.squareup.picasso.Picasso
import android.support.v4.content.ContextCompat.startActivity
import android.view.MotionEvent


class JournalLocationAdapter(context: Context, resource: Int, objects: ArrayList<JournalLocation>) :
        ArrayAdapter<JournalLocation>(context, resource, objects) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view: View? = convertView

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.activity_journal_list_view, parent, false)
        }

        view?.setOnClickListener {
            (context as JournalActivity).moveMapCamera(LatLng(getItem(position).mLat, getItem(position).mLng))
            Log.v("latlng", "latlng ${getItem(position).mLat}, ${getItem(position).mLng}")

        }

        view?.setOnLongClickListener {
            (context as JournalActivity).openEditor()
            Log.v("openEditor", "Opened textEditor")
            true
        }



        val imageView = view?.findViewById<ImageView>(R.id.activity_journal_list_view_image)
        val textView = view?.findViewById<TextView>(R.id.activity_journal_list_view_name)

        Picasso.with(context).load(getItem(position).mImageUri).resize(360,240).centerInside().into(imageView)
        textView?.text = getItem(position).mName

        return view!!
    }
}