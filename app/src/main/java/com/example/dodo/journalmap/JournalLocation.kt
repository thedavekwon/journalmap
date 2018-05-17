package com.example.dodo.journalmap

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
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
        var mDate: String = "") : ClusterItem {
    lateinit var journal: ToOne<Journal>

    override fun getPosition(): LatLng {
        return LatLng(mLat, mLng)
    }

    override fun getTitle(): String? {
        return null
    }

    override fun getSnippet(): String? {
        return null
    }
}