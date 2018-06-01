package com.example.dodo.journalmap

import android.app.DatePickerDialog
import android.app.DialogFragment
import android.media.Image
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import io.github.mthli.knife.KnifeText
import io.objectbox.Box
import io.objectbox.kotlin.boxFor
import java.util.*
import android.widget.Toast
import android.widget.ImageButton
import com.squareup.picasso.Picasso


class EditDialogFragment : DialogFragment() {

    private lateinit var journalLocationBox: Box<JournalLocation>
    private lateinit var knife: KnifeText
    private val BOLD = "<b>Write Your Journal Here</b><br><br>"
    private val ITALIT = "<i>blah blah blah</i><br><br>"
    private val EXAMPLE = BOLD + ITALIT

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val rootView = inflater.inflate(R.layout.fragment_edit_dialog, container, false)
        val nameText = rootView.findViewById<EditText>(R.id.fragment_edit_dialog_name)
        val dateText = rootView.findViewById<Button>(R.id.fragment_edit_dialog_date)
        val saveBtn = rootView.findViewById<Button>(R.id.fragment_edit_dialog_save)
        knife = rootView.findViewById<KnifeText>(R.id.fragment_edit_dialog_context)

        val mId = arguments.getLong("mId")

        journalLocationBox = (activity.application as App).boxStore.boxFor<JournalLocation>()

        val journalLocation = journalLocationBox.get(mId)

        if (journalLocation.mName == "") {
            nameText.hint = "tell me name"
        } else {
            nameText.text = SpannableStringBuilder(journalLocation.mName)
        }

        if (journalLocation.mText == "") {
            knife.fromHtml(EXAMPLE)
            knife.setSelection(knife.editableText.length)

        } else {
//            contextText.text = SpannableStringBuilder(journalLocation.mText)
            knife.fromHtml(journalLocation.mHtml)
            knife.setSelection(knife.editableText.length)
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
                    DatePickerDialog.OnDateSetListener { _, y, m, d ->
                        var mStr = ""
                        var dStr = ""
                        if (m + 1 < 10) {
                            mStr = "0${m + 1}"
                        } else {
                            mStr = "${m + 1}"
                        }
                        if (d < 10) {
                            dStr = "0$d"
                        } else {
                            dStr = "$d"
                        }
                        dateText.text = y.toString() + "-" + mStr + "-" + dStr
                    },
                    now.get(Calendar.YEAR),
                    now.get(Calendar.MONTH),
                    now.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

        saveBtn.setOnClickListener {
            Log.v("knife", knife.toHtml())
            journalLocation.mName = nameText.text.toString()
            journalLocation.mText = knife.text.toString()
            journalLocation.mHtml = knife.toHtml()
            if (dateText.text.toString() == "select date") {
                dateText.text = ""
            }
            journalLocation.mDate = dateText.text.toString()
            journalLocationBox.put(journalLocation)

            (activity as JournalActivity).updateJournalLocation()
            (activity as JournalActivity).updateCardPhoto(true)
            dismiss()
        }

        return rootView
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBold()
        setupItalic()
        setupUnderline()
        setupStrikethrough()
        setupBullet()
        setupQuote()
        setupClear()
        setupRedo()
        setupUndo()
    }

    private fun setupBold() {

        val bold = view.findViewById(R.id.bold) as ImageButton
        Picasso.with(context)
                .load(R.drawable.ic_format_bold)
                .fit()
                .centerCrop()
                .into(bold)
        bold.setOnClickListener { knife.bold(!knife.contains(KnifeText.FORMAT_BOLD)) }
    }

    private fun setupItalic() {
        val italic = view.findViewById(R.id.italic) as ImageButton
        Picasso.with(context)
                .load(R.drawable.ic_format_italic)
                .fit()
                .centerCrop()
                .into(italic)
        italic.setOnClickListener { knife.italic(!knife.contains(KnifeText.FORMAT_ITALIC)) }
    }

    private fun setupUnderline() {
        val underline = view.findViewById(R.id.underline) as ImageButton
        Picasso.with(context)
                .load(R.drawable.ic_format_underline)
                .fit()
                .centerCrop()
                .into(underline)
        underline.setOnClickListener { knife.underline(!knife.contains(KnifeText.FORMAT_UNDERLINED)) }

    }

    private fun setupStrikethrough() {
        val strikethrough = view.findViewById(R.id.strikethrough) as ImageButton
        Picasso.with(context)
                .load(R.drawable.ic_format_strikethrough)
                .fit()
                .centerCrop()
                .into(strikethrough)
        strikethrough.setOnClickListener { knife.strikethrough(!knife.contains(KnifeText.FORMAT_STRIKETHROUGH)) }
    }

    private fun setupBullet() {
        val bullet = view.findViewById(R.id.bullet) as ImageButton
        Picasso.with(context)
                .load(R.drawable.ic_format_bullet)
                .fit()
                .centerCrop()
                .into(bullet)
        bullet.setOnClickListener { knife.bullet(!knife.contains(KnifeText.FORMAT_BULLET)) }
    }

    private fun setupQuote() {
        val quote = view.findViewById(R.id.quote) as ImageButton
        Picasso.with(context)
                .load(R.drawable.ic_format_quote)
                .fit()
                .centerCrop()
                .into(quote)
        quote.setOnClickListener { knife.quote(!knife.contains(KnifeText.FORMAT_QUOTE)) }
    }

    private fun setupClear() {
        val clear = view.findViewById(R.id.clear) as ImageButton
        Picasso.with(context)
                .load(R.drawable.ic_format_clear)
                .fit()
                .centerCrop()
                .into(clear)
        clear.setOnClickListener { knife.clearFormats() }
    }

    private fun setupRedo() {
        val redo = view.findViewById(R.id.redo) as ImageButton
        Picasso.with(context)
                .load(R.drawable.ic_action_redo)
                .fit()
                .centerCrop()
                .into(redo)
        redo.setOnClickListener { knife.redo() }
    }

    private fun setupUndo() {
        val undo = view.findViewById(R.id.undo) as ImageButton
        Picasso.with(context)
                .load(R.drawable.ic_action_undo)
                .fit()
                .centerCrop()
                .into(undo)
        undo.setOnClickListener { knife.undo() }
    }
}