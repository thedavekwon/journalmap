package com.example.dodo.journalmap

import android.graphics.Bitmap
import android.net.Uri

class MainCard {
    var mImageUri: String
    var mName: String
    var mTitle: String
    var mDate: String
    var mId: Long
    var mLat: Double
    var mLng: Double

    constructor(id: Long, name: String, imageUri: String, title: String, date: String, lat: Double, lng: Double) {
        mId = id
        mName = name
        mImageUri = imageUri
        mTitle = title
        mDate = date
        mLat = lat
        mLng = lng
    }
}