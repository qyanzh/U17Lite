package com.example.u17lite

import android.os.Parcel
import android.os.Parcelable
import android.util.Log


class Comic(
    val title: String,
    val comicId: Int,
    val author: String,
    val description: String,
    val coverURL: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    init {
        Log.d("TAG", title + comicId)
    }

    class Chapter(val id: Int, val name: String)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeInt(comicId)
        parcel.writeString(author)
        parcel.writeString(description)
        parcel.writeString(coverURL)
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