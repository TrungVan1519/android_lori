package com.example.lori.activities.ui

import android.app.Dialog
import androidx.fragment.app.Fragment
import com.example.lori.R
import kotlinx.android.synthetic.main.layout_dialog_progress.*

open class BaseFragment : Fragment() {

    private lateinit var pbProgress: Dialog
    
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
