package com.example.lori.models

import android.os.Parcelable
import com.example.lori.models.Address
import com.example.lori.models.CartItem
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Order(
    var id: String = "",
    val items: ArrayList<CartItem> = ArrayList(),
    val address: Address = Address(),
    val title: String = "",
    val image: String = "",
    val subTotalAmount: Double = 0.0,
    val shippingCharge: Double = 0.0,
    val totalAmount: Double = 0.0,
    val order_datetime: Long = 0,
    val uid: String = "",
    var createdAt: Long = 0,
    var updatedAt: Long = 0,
) : Parcelable
