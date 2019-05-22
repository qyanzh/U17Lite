package com.example.u17lite.dataBeans

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
class Chapter(
    @PrimaryKey val chapterId: Long,
    val name: String,
    val smallCoverURL: String,
    val publishTime: Long,
    var belongTo: Long = 0
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString(),
        parcel.readString(),
        parcel.readLong(),
        parcel.readLong()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(chapterId)
        parcel.writeString(name)
        parcel.writeString(smallCoverURL)
        parcel.writeLong(publishTime)
        parcel.writeLong(belongTo)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Chapter> {
        override fun createFromParcel(parcel: Parcel): Chapter {
            return Chapter(parcel)
        }

        override fun newArray(size: Int): Array<Chapter?> {
            return arrayOfNulls(size)
        }
    }
}