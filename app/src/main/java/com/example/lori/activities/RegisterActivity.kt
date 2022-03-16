package com.example.lori.activities

import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.view.View
import com.example.lori.R
import com.example.lori.models.User
import com.example.lori.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : BaseActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        if (FirebaseAuth.getInstance().currentUser != null) {
            finish()
        }

        btRegister.setOnClickListener(this)
        tvLogin.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btRegister -> {
                registerUser()
            }
            R.id.tvLogin -> {
                onBackPressed()
            }
        }
    }

    private fun registerUser() {
        if (validateRegisterDetails()) {
            showProgressDialog(resources.getString(R.string.label_please_wait))

            FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(
                    etEmail.text.toString().trim { it <= ' ' },
                    etPassword.text.toString().trim { it <= ' ' })
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = User(
                            task.result!!.user!!.uid,
                            etFirstName.text.toString().trim { it <= ' ' },
                            etLastName.text.toString().trim { it <= ' ' },
                            etEmail.text.toString().trim { it <= ' ' }
                        )

                        // Save to "users" table in FireStore DB
                        FirebaseFirestore.getInstance()
                            .collection(Constants.USERS)
                            .document(user.id)
                            .set(user, SetOptions.merge())
                            .addOnSuccessListener {
                                hideProgressDialog()
                                showSnackBar(
                                    resources.getString(R.string.success_to_register),
                                    false
                                )

                                Handler().postDelayed({
                                    // todo sign out new user because this user is logged in automatically after registering
                                    FirebaseAuth.getInstance().signOut()
                                    finish()
                                }, Constants.DELAYED_MILLIS)
                            }
                            .addOnFailureListener { e ->
                                hideProgressDialog()
                                showSnackBar(resources.getString(R.string.fail_to_register), true)

                                Log.e(
                                    javaClass.simpleName,
                                    "Errors while saving user.",
                                    e
                                )
                            }
                    } else {
                        hideProgressDialog()
                        showSnackBar(task.exception!!.message.toString(), true)

                        Log.e(
                            javaClass.simpleName,
                            "Errors while registering user.",
                            task.exception
                        )
                    }
                }
        }
    }

    private fun validateRegisterDetails(): Boolean {
        return when {
            TextUtils.isEmpty(etFirstName.text.toString().trim { it <= ' ' }) -> {
                showSnackBar(resources.getString(R.string.err_msg_enter_first_name), true)
                false
            }
            TextUtils.isEmpty(etLastName.text.toString().trim { it <= ' ' }) -> {
                showSnackBar(resources.getString(R.string.err_msg_enter_last_name), true)
                false
            }
            TextUtils.isEmpty(etEmail.text.toString().trim { it <= ' ' }) -> {
                showSnackBar(resources.getString(R.string.err_msg_enter_email), true)
                false
            }
            TextUtils.isEmpty(etPassword.text.toString().trim { it <= ' ' }) -> {
                showSnackBar(resources.getString(R.string.err_msg_enter_password), true)
                false
            }
            TextUtils.isEmpty(etConfirmPassword.text.toString().trim { it <= ' ' }) -> {
                showSnackBar(resources.getString(R.string.err_msg_enter_confirm_password), true)
                false
            }
            etPassword.text.toString().trim { it <= ' ' } != etConfirmPassword.text.toString()
                .trim { it <= ' ' } -> {
                showSnackBar(
                    resources.getString(R.string.err_msg_password_and_confirm_password_mismatch),
                    true
                )
                false
            }
            !cbTermsAndCondition.isChecked -> {
                showSnackBar(resources.getString(R.string.err_msg_agree_terms_and_condition), true)
                false
            }
            else -> {
                true
            }
        }
    }
}
