package com.example.dodo.journalmap

import android.app.Application
import io.objectbox.BoxStore

class App : Application() {

    companion object Constants {
        const val TAG = "JournalBox"
    }

    lateinit var boxStore: BoxStore
        private set

    override fun onCreate() {
        super.onCreate()
        boxStore = MyObjectBox.builder().androidContext(this).build()
    }
}