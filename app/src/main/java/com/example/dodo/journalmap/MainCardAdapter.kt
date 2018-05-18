package com.example.dodo.journalmap

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.widget.ImageView
import android.widget.TextView
import java.io.File
import android.util.Log
import android.view.*
import java.util.*
import android.view.MotionEvent
import io.objectbox.Box
import io.objectbox.kotlin.boxFor
import io.objectbox.query.Query
import java.util.Collections.swap

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

class MainCardAdapter(val journalList: ArrayList<Journal>, val dragStartListener: OnStartDragListener) :
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

    private lateinit var gestureDetector: GestureDetector
    private lateinit var context: Context

    private lateinit var journalQuery: Query<Journal>
    private lateinit var journalBox: Box<Journal>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.activity_main_card_view, parent, false) as CardView
        context = parent.context
        journalBox = (context.applicationContext as App).boxStore.boxFor<Journal>()
        journalQuery = journalBox.query().build()
        return ItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.imageView.setImageURI(Uri.fromFile(File(journalList[position].mImageUri)))
        holder.textTitleView.text = journalList[position].mTitle
        holder.textDateView.text = journalList[position].mDate

        //TODO( it does not work because of custom adapter)
        gestureDetector = GestureDetector(holder.imageView.context, object: GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent?): Boolean {
                Log.i(TAG, "Single Tap Up $e")
                val journalIntent = Intent(holder.imageView.context, JournalActivity::class.java)
                journalIntent.putExtra("latitude", journalList[position].mLat)
                journalIntent.putExtra("longitude", journalList[position].mLng)
                journalIntent.putExtra("name", journalList[position].mName)
                journalIntent.putExtra("id", journalList[position].id)
                holder.imageView.context?.startActivity(journalIntent)
                return false
            }

            override fun onLongPress(e: MotionEvent) {
                // Touch has been long enough to indicate a long press.
                // Does not indicate motion is complete yet (no up event necessarily)
                Log.i(TAG, "Long Press $e")
            }

            override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float,
                                  distanceY: Float): Boolean {
                // User attempted to scroll
                Log.i(TAG, "Scroll $e1")
                return false
            }

            override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float,
                                 velocityY: Float): Boolean {
                // Fling event occurred.  Notification of this one happens after an "up" event.
                Log.i(TAG, "Fling $e1")
                return false
            }

            override fun onShowPress(e: MotionEvent) {
                // User performed a down event, and hasn't moved yet.
                Log.i(TAG, "Show Press $e")
            }

            override fun onDown(e: MotionEvent): Boolean {
                // "Down" event - User touched the screen.
                Log.i(TAG, "Down $e")
                return false
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                // User tapped the screen twice.
                Log.i(TAG, "Double tap $e")
                return false
            }

            override fun onDoubleTapEvent(e: MotionEvent): Boolean {
                // Since double-tap is actually several events which are considered one aggregate
                // gesture, there's a separate callback for an individual event within the doubletap
                // occurring.  This occurs for down, up, and move.
                Log.i(TAG, "Event within double tap $e")
                return false
            }

            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                // A confirmed single-tap event has occurred.  Only called when the detector has
                // determined that the first tap stands alone, and is not part of a double tap.
                Log.i(TAG, "Single tap confirmed $e")
                return false
            }
        })


        holder.imageView.setOnClickListener{
            val journalIntent = Intent(it.context, JournalActivity::class.java)
            journalIntent.putExtra("latitude", journalList[position].mLat)
            journalIntent.putExtra("longitude", journalList[position].mLng)
            journalIntent.putExtra("name", journalList[position].mName)
            journalIntent.putExtra("id", journalList[position].id)
            it.context?.startActivity(journalIntent)
        }
        /*
        holder.imageView.setOnTouchListener(object : View.OnTouchListener{
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if (gestureDetector.onTouchEvent(event)) {
                    return true
                } else if(event?.action == MotionEvent.ACTION_DOWN) {
                    Log.v("Action Down", "Action Down $event")
                    dragStartListener.onStartDrag(holder)
                }
                return false
            }
        })
        */
    }

    override fun getItemCount(): Int {
        return journalList.size
    }

    override fun onItemDismiss(position: Int) {
        val size = journalList.size
        (context as MainActivity).deleteJournal(journalList[position])
        journalList.removeAt(position)
        Log.v("journalList", "journalList size: ${journalList.size}")
        notifyItemRangeRemoved(0, size)
    }

    //TODO( only moves one card)
    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        //Collections.swap(journalList, fromPosition, toPosition)
        /*
        Log.v("onItemMove", "$fromPosition -> $toPosition")
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                swapLoc(journalList[i], journalList[i+1])
                notifyItemMoved(i, i+1)
                //Collections.swap(journalList, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                swapLoc(journalList[i], journalList[i-1])
                notifyItemMoved(i, i-1)
                //Collections.swap(journalList, i, i - 1)
            }
        }
        */
        Log.v("onItemMove", "$fromPosition -> $toPosition")
        swapLoc(journalList[fromPosition], journalList[toPosition])
        notifyItemMoved(fromPosition, toPosition)
        //notifyItemMoved(fromPosition, toPosition)
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
        (context as MainActivity).updateCards()
    }
}