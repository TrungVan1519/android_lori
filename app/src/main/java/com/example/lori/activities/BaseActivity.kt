package com.example.lori.activities

import android.annotation.SuppressLint
import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.lori.R
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.layout_dialog_progress.*

open class BaseActivity : AppCompatActivity() {

    private lateinit var pbProgress: Dialog

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

    protected fun showProgressDialog(text: String) {
        pbProgress = Dialog(this)
        pbProgress.setContentView(R.layout.layout_dialog_progress)
        pbProgress.tvProgress.text = text
        pbProgress.setCancelable(false)
        pbProgress.setCanceledOnTouchOutside(false)
        pbProgress.show()
    }

    protected fun hideProgressDialog() {
        pbProgress.dismiss()
    }
}
