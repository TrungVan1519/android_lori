package com.example.lori.activities

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.lori.R
import com.google.android.material.snackbar.Snackbar

open class BaseActivity : AppCompatActivity() {

    @SuppressLint("ShowToast")
    protected fun showSnackBar(message: String, isErrorMessage: Boolean) {
        val color = if (isErrorMessage) R.color.colorSnackBarError else R.color.colorSnackBarSuccess
        val snackBar = Snackbar.make(
            findViewById(android.R.id.content),
            message,
            Snackbar.LENGTH_LONG
        )
        snackBar.view.setBackgroundColor(ContextCompat.getColor(this, color))
        snackBar.show()
    }
}
