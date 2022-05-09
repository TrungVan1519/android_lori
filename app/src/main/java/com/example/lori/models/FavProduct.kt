package com.example.lori.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class FavProduct(
    var id: String = "",
    var image: String = "",
    val title: String = "",
    val price: Long = 0,
    var uid: String = "", // user id == buyer id
    var pid: String = "", // product id
    val product_owner_id: String = "", // seller id
    var createdAt: Long = 0
) : Parcelable
