package com.example.dodo.journalmap

class JournalLocation {
    var mLng: Double = 0.0
    var mLat: Double = 0.0
    var mImageId: Int = 0
    var mName: String = ""

    constructor(lng: Double, lat: Double, imageId: Int, name: String) {
        mLng = lng
        mLat = lat
        mImageId = imageId
        mName = name
    }
}