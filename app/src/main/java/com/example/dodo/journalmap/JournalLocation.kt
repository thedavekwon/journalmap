package com.example.dodo.journalmap

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.ToOne

@Entity
class JournalLocation(
    @Id var id: Long = 0,
    var mLng: Double = 0.0,
    var mLat: Double = 0.0,
    var mImageUri: String = "",
    var mName: String = "",
    var mText: String = "",
    var mDate: String = "") {
    lateinit var journal: ToOne<Journal>
}