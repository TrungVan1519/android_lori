package com.example.lori.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.lori.R
import com.example.lori.models.User
import com.example.lori.utils.Constants
import com.example.lori.utils.ImageUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : BaseActivity(), View.OnClickListener {

    private var user: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        getUserDetails()

        tvEdit.setOnClickListener(this)
        btLogout.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvEdit -> {
                val intent = Intent(this, UserProfileActivity::class.java)
                intent.putExtra(Constants.EXTRA_USER_DETAILS, user)
                startActivity(intent)
            }
            R.id.btLogout -> {
                if (FirebaseAuth.getInstance().currentUser != null) {
                    FirebaseAuth.getInstance().signOut()

                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                }
            }
        }
    }

    private fun getUserDetails() {
        showProgressDialog(resources.getString(R.string.label_please_wait))

        // Get the logged user details from FireStore DB
        FirebaseFirestore.getInstance()
            .collection(Constants.USERS)
            .document(FirebaseAuth.getInstance().currentUser?.uid ?: "")
            .get()
            .addOnSuccessListener { document ->
                hideProgressDialog()

                // Convert to User Data model object
                user = document.toObject(User::class.java)!!

                ImageUtils.loadUserPicture(this, user!!.image, ivUserPhoto)
                tvName.text = "${user!!.firstName} ${user!!.lastName}"
                tvEmail.text = user!!.email
                tvMobileNumber.text = user!!.mobile.toString()
                tvGender.text = user!!.gender
            }
            .addOnFailureListener { e ->
                hideProgressDialog()
                showSnackBar(resources.getString(R.string.fail_to_get_user_details), true)

                Log.e(
                    javaClass.simpleName,
                    "Errors while getting user details.",
                    e
                )
            }
    }
}
