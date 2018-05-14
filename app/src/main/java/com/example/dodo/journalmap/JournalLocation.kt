package com.example.dodo.journalmap

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class JournalLocation(
        @Id var id: Long = 0,
        var mLng: Double = 0.0,
        var mLat: Double = 0.0,
        var mImageUri: String = "",
        var mName: String = ""
)