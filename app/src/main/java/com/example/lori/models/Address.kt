package com.example.lori.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Address(
    var id: String = "",
    val name: String = "",
    val mobile: Long = 0,
    val address: String = "",
    val zipCode: String = "",
    val additionalNote: String = "",
    val type: String = "",
    val otherDetails: String = "",
    val uid: String = "",
    var createdAt: Long = 0,
    var updatedAt: Long = 0,
) : Parcelable
