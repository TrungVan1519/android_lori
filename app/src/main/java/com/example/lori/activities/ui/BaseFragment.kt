package com.example.lori.activities.ui

import android.app.Dialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.lori.R
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.layout_dialog_progress.*

open class BaseFragment : Fragment() {

    private lateinit var pbProgress: Dialog

    fun showSnackBar(message: String, isErrorMessage: Boolean) {
        val snackBar = Snackbar.make(
            requireActivity().findViewById(android.R.id.content),
            message,
            Snackbar.LENGTH_LONG
        )
        snackBar.view.setBackgroundColor(
            ContextCompat.getColor(
                requireActivity(),
                if (isErrorMessage) R.color.snack_bar_error else R.color.snack_bar_success
            )
        )
        snackBar.show()
    }

    fun showProgressDialog(text: String) {
        pbProgress = Dialog(requireActivity())
        pbProgress.setContentView(R.layout.layout_dialog_progress)
        pbProgress.tvProgress.text = text
        pbProgress.setCancelable(false)
        pbProgress.setCanceledOnTouchOutside(false)
        pbProgress.show()
    }

    fun hideProgressDialog() {
        pbProgress.dismiss()
    }
}
