package com.example.lori.activities

import android.app.Dialog
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.lori.R
import com.example.lori.utils.Constants
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.layout_dialog_progress.*

open class BaseActivity : AppCompatActivity() {

    private lateinit var pbProgress: Dialog
    private var doubleBackToExitPressedOnce = false

    protected fun showSnackBar(message: String, isErrorMessage: Boolean) {
        val color = if (isErrorMessage) R.color.snack_bar_error else R.color.snack_bar_success
        val snackBar = Snackbar.make(
            findViewById(android.R.id.content),
            message,
            Snackbar.LENGTH_LONG
        )
        snackBar.view.setBackgroundColor(ContextCompat.getColor(this, color))
        snackBar.show()
    }

    protected fun showProgressDialog(text: String) {
        pbProgress = Dialog(this)
        pbProgress.setContentView(R.layout.layout_dialog_progress)
        pbProgress.tvProgress.text = text
        pbProgress.setCancelable(false)
        pbProgress.setCanceledOnTouchOutside(false)
        pbProgress.show()
    }

    fun hideProgressDialog() {
        pbProgress.dismiss()
    }

    fun doubleBackToExit() {
        if (doubleBackToExitPressedOnce) {
            return super.onBackPressed()
        }

        doubleBackToExitPressedOnce = true
        Handler().postDelayed({ doubleBackToExitPressedOnce = false }, Constants.DELAYED_MILLIS)
        Toast.makeText(
            this,
            resources.getString(R.string.label_please_click_back_again_to_exit),
            Toast.LENGTH_SHORT
        ).show()
    }
}
