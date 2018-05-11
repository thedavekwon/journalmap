package com.example.dodo.journalmap

import android.app.Application
import io.objectbox.BoxStore

class App : Application() {

    companion object Constants {
        const val TAG = "JournalBox"
        const val EXTERNAL_DIR = false
    }

    lateinit var boxStore: BoxStore
        private set

    override fun onCreate() {
        super.onCreate()
        //EXTERNAL_DIR added here if we need one TODO()
        boxStore = MyObjectBox.builder().androidContext(this).build()
    }
}