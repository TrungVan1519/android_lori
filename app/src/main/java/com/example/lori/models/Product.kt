package com.example.lori.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Product(
    var id: String = "",
    var image: String = "",
    val title: String = "",
    val price: Long = 0,
    val description: String = "",
    val stock_quantity: Int = 0,
    val uid: String = "", // user id
    val username: String = "",
    var ar: Boolean = false,
    var createdAt: Long = 0,
    var updatedAt: Long = 0,
) : Parcelable
