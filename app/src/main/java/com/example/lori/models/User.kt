package com.example.lori.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
    val id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val image: String = "",
    val mobile: String = "",
    val gender: String = "",
    val profileCompleted: Int = 0,
    val role: String = "",
    var createdAt: Long = 0,
    var updatedAt: Long = 0,
) : Parcelable
