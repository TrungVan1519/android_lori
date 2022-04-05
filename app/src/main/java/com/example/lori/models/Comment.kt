package com.example.lori.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Comment(
    var id: String = "",
    var content: String = "",
    var start: Int = 5,
    var userEmail: String = "",
    var userImage: String = "",
    var uid: String = "",
    var pid: String = "",
    var createdAt: Long = 0,
    var updatedAt: Long = 0,
) : Parcelable
