package com.example.dodo.journalmap

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.widget.ImageView
import android.widget.TextView
import android.util.Log
import android.view.*
import java.util.*
import com.squareup.picasso.Picasso
import io.objectbox.Box
import io.objectbox.kotlin.boxFor
import io.objectbox.query.Query
import java.io.File


interface ItemTouchHelperAdapter {
    fun onItemMove(fromPosition: Int, toPosition: Int): Boolean
    fun onItemDismiss(position: Int)
}

interface ItemTouchHelperViewHolder {
    fun onItemSelected()
    fun onItemClear()
}

interface OnStartDragListener {
    fun onStartDrag(viewHolder: RecyclerView.ViewHolder)
}

val TAG = "GestureListener"

class MainCardAdapter(val journalList: ArrayList<Journal>) :
        RecyclerView.Adapter<MainCardAdapter.ItemViewHolder>(), ItemTouchHelperAdapter {
    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), ItemTouchHelperViewHolder {
        var imageView: ImageView = itemView.findViewById(R.id.activity_main_card_view_image)
        var textTitleView: TextView = itemView.findViewById(R.id.activity_main_card_view_text_title)
        var textDateView: TextView = itemView.findViewById(R.id.activity_main_card_view_text_date)
        override fun onItemSelected() {
            itemView.setBackgroundColor(Color.LTGRAY)
        }

        override fun onItemClear() {
            itemView.setBackgroundColor(0)
        }
    }

    private lateinit var pcontext: Context
    private lateinit var journalQuery: Query<Journal>
    private lateinit var journalBox: Box<Journal>

    private val JOURNAL_ACTIVITY = 101

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.activity_main_card_view, parent, false) as CardView
        pcontext = parent.context
        journalBox = (pcontext.applicationContext as App).boxStore.boxFor<Journal>()
        journalQuery = journalBox.query().build()
        return ItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        Picasso.with(pcontext)
                .load(File(journalList[position].mImageUri))
                .fit()
                .centerCrop()
                .into(holder.imageView)
        holder.textTitleView.text = journalList[position].mTitle
        holder.textDateView.text = journalList[position].mDate
        holder.imageView.setOnClickListener {
            val journalIntent = Intent(it.context, JournalActivity::class.java)
            journalIntent.putExtra("latitude", journalList[position].mLat)
            journalIntent.putExtra("longitude", journalList[position].mLng)
            journalIntent.putExtra("name", journalList[position].mName)
            journalIntent.putExtra("id", journalList[position].id)
            (pcontext as MainActivity).startActivityForResult(journalIntent, JOURNAL_ACTIVITY)
        }
    }

    override fun getItemCount(): Int {
        return journalList.size
    }

    override fun onItemDismiss(position: Int) {
        val size = journalList.size
        (pcontext as MainActivity).deleteJournal(journalList[position])
        journalList.removeAt(position)
        notifyItemRangeRemoved(0, size)
    }

    //TODO( only moves one card)
    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        swapLoc(journalList[fromPosition], journalList[toPosition])
        notifyItemMoved(fromPosition, toPosition)
        return true
    }

    private fun swapLoc(journal1: Journal, journal2: Journal) {
        val tmp = journal1.mLoc
        Log.v("swapLoc", "${journal1.mLoc}, ${journal2.mLoc}")
        journal1.mLoc = journal2.mLoc
        journal2.mLoc = tmp
        Log.v("swapLoc", "${journal1.mLoc}, ${journal2.mLoc}")
        journalBox.put(journal1)
        journalBox.put(journal2)
        (pcontext as MainActivity).updateCards()
    }
}