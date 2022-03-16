package com.example.lori.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Product(
    var id: String = "",
    val image: String = "",
    val title: String = "",
    val price: Long = 0,
    val description: String = "",
    val stock_quantity: Int = 0,
    val uid: String = "",
    val username: String = "",
) : Parcelable
