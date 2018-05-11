package com.example.dodo.journalmap

import android.content.Intent
import android.net.Uri
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import java.io.File

class MainCardAdapter(val mainCardList: ArrayList<MainCard>) :
        RecyclerView.Adapter<MainCardAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageView: ImageView = itemView.findViewById(R.id.activity_main_card_view_image)
        var textTitleView: TextView = itemView.findViewById(R.id.activity_main_card_view_text_title)
        var textDateView: TextView = itemView.findViewById(R.id.activity_main_card_view_text_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.activity_main_card_view, parent, false) as CardView
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.imageView.setImageURI(Uri.fromFile(File(mainCardList[position].mImageUri)))
        holder.textTitleView.setText(mainCardList[position].mTitle)
        holder.textDateView.setText(mainCardList[position].mDate)

        holder.imageView.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val journalIntent = Intent(v?.context, JournalActivity::class.java)
                journalIntent.putExtra("latitude", mainCardList[position].mLat)
                journalIntent.putExtra("longitude", mainCardList[position].mLng)
                journalIntent.putExtra("name", mainCardList[position].mName)
                v?.context?.startActivity(journalIntent)
            }
        })
    }

    override fun getItemCount(): Int {
        return mainCardList.size
    }
}