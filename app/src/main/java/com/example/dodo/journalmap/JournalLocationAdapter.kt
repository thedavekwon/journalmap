package com.example.dodo.journalmap

import android.content.Context
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

class JournalLocationAdapter(context: Context, resource: Int, objects: ArrayList<JournalLocation>) :
        ArrayAdapter<JournalLocation>(context, resource, objects) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View? = convertView

        if (view == null) {
            super.getView(position, convertView, parent)
        }

        val imageView = view!!.findViewById<ImageView>(R.id.activity_journal_list_view_image)
        val textView = view.findViewById<TextView>(R.id.activity_journal_list_view_name)

        val currentLocation = getItem(position)

        imageView.setImageResource(currentLocation.mImageId)
        textView.setText(currentLocation.mName)
        view.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val intent = Intent()
            }
        })

        return view
    }
}