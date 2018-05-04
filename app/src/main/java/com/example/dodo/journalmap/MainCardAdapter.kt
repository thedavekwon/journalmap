package com.example.dodo.journalmap

import android.content.Intent
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

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
        itemView.setOnClickListener(object: View.OnClickListener{
            override fun onClick(v: View?) {
                val journalIntent = Intent(v?.context, JournalActivity::class.java)
                journalIntent.putExtra("latitude", 40.7282924)
                journalIntent.putExtra("longitude", -74.0003308)
                journalIntent.putExtra("name", "New York")
                v?.context?.startActivity(journalIntent)
            }
        })
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.imageView.setImageResource(mainCardList[position].mImageId)
        holder.textTitleView.setText(mainCardList[position].mTextTitle)
        holder.textDateView.setText(mainCardList[position].mTextDate)
    }

    override fun getItemCount(): Int {
        return mainCardList.size
    }
}