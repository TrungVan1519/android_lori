package com.example.lori.utils

object Constants {

    // Delay times
    const val DELAYED_MILLIS = 1000L

    // SharedPreferences data
    const val LORI_PREFERENCES = "Lori"
    const val LOGGED_IN_USERNAME = "logged_in_username"

    // Intent extras
    const val EXTRA_USER_DETAILS = "extra_user_details"
    const val EXTRA_PRODUCT_ID = "extra_product_id"
    const val EXTRA_PRODUCT_DETAILS = "ProductDetails"
    const val EXTRA_PRODUCT_OWNER_ID = "extra_product_owner_id"
    const val EXTRA_ADDRESS_DETAILS = "AddressDetails"
    const val EXTRA_SELECT_ADDRESS = "extra_select_address"
    const val EXTRA_SELECTED_ADDRESS = "extra_selected_address"
    const val EXTRA_MY_ORDER_DETAILS = "extra_my_order_details"
    const val EXTRA_SOLD_PRODUCT_DETAILS = "extra_sold_product_details"

    // Permission codes
    const val PICK_IMAGE_REQUEST_CODE = 1
    const val READ_STORAGE_PERMISSION_CODE = 2

    // Firebase DB collection names
    const val USERS = "users"
    const val PRODUCTS = "products"
    const val FAV_PRODUCTS = "fav_products"
    const val SOLD_PRODUCTS = "sold_products"
    const val COMMENTS = "comments"
    const val CART_ITEMS = "cart_items"
    const val ADDRESSES = "addresses"
    const val ORDERS = "orders"

    // Firebase DB field names
    const val FIRST_NAME = "firstName"
    const val LAST_NAME = "lastName"
    const val IMAGE = "image"
    const val MOBILE = "mobile"
    const val GENDER = "gender"
    const val PROFILE_COMPLETED = "profileCompleted"
    const val ROLE = "role"
    const val USER_PROFILE_IMAGE = "User_Profile_Image"

    const val TITLE = "title"
    const val UID = "uid"
    const val AR = "ar"
    const val PRODUCT_IMAGE = "Product_Image"

    const val PID = "pid"
    const val CART_QUANTITY = "cart_quantity"
    const val STOCK_QUANTITY = "stock_quantity"

    const val ADDRESS = "address"

    const val STATUS = "status"
    const val PRODUCT_OWNER_ID = "product_owner_id"

    const val CREATED_AT = "createdAt"
    const val UPDATED_AT = "updatedAt"

    // For "users" collection
    const val MALE = "Male"
    const val FEMALE = "Female"
    const val ROLE_ADMIN = "Admin"
    const val ROLE_USER = "User"

    // For "cart_items" collection
    const val DEFAULT_CART_QUANTITY = 1
    const val SHIPPING_CHARGE = 10_000 // VND

    // For "addresses" collection
    const val HOME = "Home"
    const val OFFICE = "Office"
    const val OTHER = "Other"
}
