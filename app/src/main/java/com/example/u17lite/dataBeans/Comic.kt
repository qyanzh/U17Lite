package com.example.u17lite.dataBeans

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Comic(
    @PrimaryKey val comicId: Long,
    var title: String,
    val author: String,
    val description: String,
    val coverURL: String,
    var lastUpdateTime: Long = 0,
    var isSubscribed: Boolean = false,
    var isDownloaded: Boolean = false
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readLong(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte()
    )

    override fun toString(): String {
        return "title:$title"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(comicId)
        parcel.writeString(title)
        parcel.writeString(author)
        parcel.writeString(description)
        parcel.writeString(coverURL)
        parcel.writeLong(lastUpdateTime)
        parcel.writeByte(if (isSubscribed) 1 else 0)
        parcel.writeByte(if (isDownloaded) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Comic> {
        override fun createFromParcel(parcel: Parcel): Comic {
            return Comic(parcel)
        }

        override fun newArray(size: Int): Array<Comic?> {
            return arrayOfNulls(size)
        }
    }


}
