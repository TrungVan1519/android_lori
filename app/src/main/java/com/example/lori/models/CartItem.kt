package com.example.lori.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CartItem(
    var id: String = "",
    val title: String = "",
    val price: Long = 0,
    val image: String = "",
    var cart_quantity: Int = 0,
    var stock_quantity: Int = 0,
    val uid: String = "", // user id
    val pid: String = "", // product id
    var createdAt: Long = 0,
    var updatedAt: Long = 0,
) : Parcelable
