package com.example.dodo.journalmap

import io.objectbox.Box
import io.objectbox.BoxStore
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class Journal(
        @Id var id: Long = 0,
        var title: String = "",
        var date: String = "",
        var lat: Double = 0.0,
        var lng: Double = 0.0,
        var imageUri: String = "",
        var name: String = ""
)