package com.example.dodo.journalmap

class MainCard {
    var mImageId: Int = 0
    var mTextTitle: String = ""
    var mTextDate: String = ""

    constructor(imageId: Int, textTitle: String, textDate: String) {
        mImageId = imageId
        mTextTitle = textTitle
        mTextDate = textDate
    }
}