package com.example.dodo.journalmap

import io.objectbox.Box
import io.objectbox.BoxStore
import io.objectbox.annotation.Backlink
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.ToMany
import io.objectbox.relation.ToOne



@Entity
class Journal(
    @Id var id: Long = 0,
    var mTitle: String = "",
    var mDate: String = "",
    var mLat: Double = 0.0,
    var mLng: Double = 0.0,
    var mImageUri: String = "",
    var mName: String = "",
    var mLoc: String ="") {

    @Backlink
    var mJournalLocations: ToMany<JournalLocation>? = null
}
