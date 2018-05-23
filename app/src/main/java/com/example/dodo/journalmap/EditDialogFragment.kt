package com.example.dodo.journalmap

import android.app.DatePickerDialog
import android.app.DialogFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import io.objectbox.Box
import io.objectbox.kotlin.boxFor
import java.util.*

class EditDialogFragment: DialogFragment() {

    private lateinit var journalLocationBox: Box<JournalLocation>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val rootView = inflater.inflate(R.layout.fragment_edit_dialog, container, false)
        val nameText = rootView.findViewById<EditText>(R.id.fragment_edit_dialog_name)
        val contextText = rootView.findViewById<EditText>(R.id.fragment_edit_dialog_context)
        val dateText = rootView.findViewById<Button>(R.id.fragment_edit_dialog_date)
        val saveBtn = rootView.findViewById<Button>(R.id.fragment_edit_dialog_save)

        val mId = arguments.getLong("mId")

        journalLocationBox = (activity.application as App).boxStore.boxFor<JournalLocation>()


        val journalLocation = journalLocationBox.get(mId)

        if (journalLocation.mName == "") {
            nameText.hint = "tell me name"
        } else {
            nameText.hint = journalLocation.mName
        }

        if (journalLocation.mText == "") {
            contextText.hint = "tell me context"
        } else {
            contextText.hint = journalLocation.mText
        }

        if (journalLocation.mDate == "") {
            dateText.text = "select date"
        } else {
            dateText.text = journalLocation.mDate
        }

        dateText.setOnClickListener {
            val now = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(
                    activity,
                    DatePickerDialog.OnDateSetListener { v, y, m, d ->
                        dateText.text = y.toString() + "-"+ m.toString() + "-" + d.toString()
                    },
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH),
                    now.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

        saveBtn.setOnClickListener {
            journalLocation.mName = nameText.text.toString()
            journalLocation.mText = contextText.text.toString()
            journalLocation.mDate = dateText.text.toString()
            journalLocationBox.put(journalLocation)

            (activity as JournalActivity).updateJournalLocation()
            dismiss()
        }

        return rootView
    }
}