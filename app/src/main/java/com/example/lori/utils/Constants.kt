package com.example.lori.utils

object Constants {

    // Delay times
    const val DELAYED_MILLIS = 1000L

    // Firebase DB collection names
    const val USERS = "users"

    // SharedPreferences data
    const val LORI_PREFERENCES = "Lori"
    const val LOGGED_IN_USERNAME = "logged_in_username"

    // Intent extra constants.
    const val EXTRA_USER_DETAILS = "extra_user_details"

    // A unique code for asking the Read Storage Permission using this we will be check and identify in the method onRequestPermissionsResult in the Base Activity.
    const val READ_STORAGE_PERMISSION_CODE = 2

    // A unique code of image selection from Phone Storage.
    const val PICK_IMAGE_REQUEST_CODE = 1

    // Constant variables for Gender
    const val MALE = "Male"
    const val FEMALE = "Female"

    // Firebase DB field names
    const val MOBILE = "mobile"
    const val GENDER = "gender"
    const val PROFILE_COMPLETED = "profileCompleted"
}
