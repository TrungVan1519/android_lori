package com.example.lori.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SoldProduct(
    var id: String = "",
    val address: Address = Address(),
    val image: String = "",
    val title: String = "",
    val price: Long = 0,
    val sold_quantity: Int = 0,
    val subTotalAmount: Double = 0.0,
    val shippingCharge: Double = 0.0,
    val totalAmount: Double = 0.0,
    val order_datetime: Long = 0,
    var status: Boolean = false,
    val oid: String = "", // order id
    val uid: String = "", // user id == buyer id
    val pid: String = "", // product id
    val product_owner_id: String = "", // seller id
    var createdAt: Long = 0,
    var updatedAt: Long = 0,
) : Parcelable
