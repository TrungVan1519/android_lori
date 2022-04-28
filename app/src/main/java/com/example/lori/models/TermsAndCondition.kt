package com.example.lori.models

import android.os.Parcelable
import com.example.lori.R
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TermsAndCondition(
    var id: Long? = null,
    var content: String? = null,
    var drawableStart: Int? = null,
) : Parcelable {
    companion object {
        fun createTermsAndCondition(): MutableList<TermsAndCondition> {
            return mutableListOf(
                TermsAndCondition(
                    0,
                    "These terms and condition can be updated in the future.",
                    R.drawable.ic_check_circle_24dp
                ),
                TermsAndCondition(
                    1,
                    "By using this app, you certify that you have read and reviewed this Agreement and that you agree to comply with its terms. If you do not want to be bound by the terms of this Agreement, you are advised to leave the app accordingly. Only grants to use and access this app, its products and its services to who have accepted its terms.",
                    R.drawable.ic_check_circle_24dp
                ),
                TermsAndCondition(
                    2,
                    "As a user of this app, you may be asked to register with us and provide some private information. You are responsible for ensuring the accuracy of this information, and you are responsible for maintaining the safety and security of your identifying information. You are also responsible for all activities that occur under your account or password.",
                    R.drawable.ic_check_circle_24dp
                ),
                TermsAndCondition(
                    3,
                    "By using this app, you agree that laws of your position, without regard to principles of conflicts laws, will govern these terms and conditions, or any dispute of any sort that might com between us and you, or its business partners and associates.",
                    R.drawable.ic_check_circle_24dp
                ),
                TermsAndCondition(
                    4,
                    "We recommend you should be 18 year old or higher. Otherwise, age is unlimited.",
                    R.drawable.ic_check_circle_24dp
                ),
                TermsAndCondition(
                    5,
                    "Be respect to other sellers and buyers like you while writing your comments about product.",
                    R.drawable.ic_check_circle_24dp
                ),
                TermsAndCondition(
                    6,
                    "Only submit confirmation if you receive your orders or you can feedback to us about services and products to us by using the telephone number 0987654321.",
                    R.drawable.ic_check_circle_24dp
                ),
                TermsAndCondition(
                    7,
                    "Do not scam other buyers or hide product's information about manufactures, cost, additional fees, material, etc.",
                    R.drawable.ic_cancel_circle_24dp
                ),
                TermsAndCondition(
                    8,
                    "You must not be a convicted sex offender.",
                    R.drawable.ic_cancel_circle_24dp
                ),
            )
        }
    }
}
