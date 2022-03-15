package com.example.lori.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.lori.R
import java.io.IOException

object ImageUtils {

    /**
     * Select an image from phone storage.
     */
    fun showImageChooser(activity: Activity) {
        // Launch the image selection of phone storage using the constant code.
        activity.startActivityForResult(
            Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            ), Constants.PICK_IMAGE_REQUEST_CODE
        )
    }

    /**
     * Load image from Uri or URL for the user profile picture.
     */
    fun loadUserPicture(context: Context, image: Any, imageView: ImageView) {
        Glide
            .with(context)
            .load(image) // Uri or URL of the image
            .centerCrop() // Scale type of the image.
            .placeholder(R.drawable.im_user_placeholder) // A default place holder if image is failed to load.
            .into(imageView) // the view in which the image will be loaded.
    }

    /**
     * Get the image file extension of the selected image.
     *
     * @param activity Activity reference.
     * @param uri Image file uri.
     */
    fun getFileExtension(activity: Activity, uri: Uri): String? {
        /*
         * MimeTypeMap: Two-way map that maps MIME-types to file extensions and vice versa.
         * getSingleton(): Get the singleton instance of MimeTypeMap.
         * getExtensionFromMimeType: Return the registered extension for the given MIME type.
         * contentResolver.getType: Return the MIME type of the given content URL.
         */
        return MimeTypeMap
            .getSingleton()
            .getExtensionFromMimeType(activity.contentResolver.getType(uri))
    }
}
