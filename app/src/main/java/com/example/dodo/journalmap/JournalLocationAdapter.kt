package com.example.dodo.journalmap

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso


class JournalLocationAdapter(context: Context, resource: Int, objects: ArrayList<JournalLocation>) :
        ArrayAdapter<JournalLocation>(context, resource, objects) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view: View? = convertView

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.activity_journal_list_view, parent, false)
        }

        val imageView = view?.findViewById<ImageView>(R.id.activity_journal_list_view_image)

        val nameView = view?.findViewById<TextView>(R.id.activity_journal_list_view_Name)
        val contextView = view?.findViewById<TextView>(R.id.activity_journal_list_view_Context)
        val dateView = view?.findViewById<TextView>(R.id.activity_journal_list_view_Date)

        Picasso.with(context)
                .load(getItem(position).mImageUri)
                .fit()
                .centerCrop()
                .into(imageView)
        nameView?.text = getItem(position).mName
        contextView?.text = getItem(position).mText
        dateView?.text = getItem(position).mDate

        Log.v("adapter", "${getItem(position).mName} ${getItem(position).mText} ${getItem(position).mDate}")

        return view!!
    }
}