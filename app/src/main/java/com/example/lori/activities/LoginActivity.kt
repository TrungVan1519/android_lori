package com.example.lori.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import com.example.lori.R
import com.example.lori.models.User
import com.example.lori.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : BaseActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        if (FirebaseAuth.getInstance().currentUser != null) {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }

        tvForgotPassword.setOnClickListener(this)
        btLogin.setOnClickListener(this)
        tvRegister.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvForgotPassword -> {
                startActivity(Intent(this, ForgotPasswordActivity::class.java))
            }
            R.id.btLogin -> {
                loginUser()
            }
            R.id.tvRegister -> {
                startActivity(Intent(this, RegisterActivity::class.java))
            }
        }
    }

    private fun loginUser() {
        if (validateData()) {
            showProgressDialog(resources.getString(R.string.label_please_wait))

            FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(
                    etEmail.text.toString().trim { it <= ' ' },
                    etPassword.text.toString().trim { it <= ' ' })
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // todo get the logged user details from FireStore DB
                        FirebaseFirestore.getInstance()
                            .collection(Constants.USERS)
                            .document(FirebaseAuth.getInstance().currentUser?.uid ?: "")
                            .get()
                            .addOnSuccessListener { document ->
                                hideProgressDialog()

                                val user = document.toObject(User::class.java)!!
                                getSharedPreferences(
                                    Constants.LORI_PREFERENCES,
                                    Context.MODE_PRIVATE
                                )
                                    .edit()
                                    .putString(
                                        Constants.LOGGED_IN_USERNAME,
                                        "${user.firstName} ${user.lastName}"
                                    )
                                    .apply()

                                if (user.profileCompleted == 0) {
                                    val intent = Intent(this, UserProfileActivity::class.java)
                                    intent.putExtra(Constants.EXTRA_USER_DETAILS, user)
                                    startActivity(intent)
                                } else {
                                    startActivity(Intent(this, DashboardActivity::class.java))
                                }
                            }
                            .addOnFailureListener { e ->
                                hideProgressDialog()
                                showSnackBar(resources.getString(R.string.fail_to_login), true)

                                Log.e(
                                    javaClass.simpleName,
                                    "Errors while getting user",
                                    e
                                )
                            }
                    } else {
                        hideProgressDialog()
                        showSnackBar(task.exception!!.message.toString(), true)

                        Log.e(
                            javaClass.simpleName,
                            "Errors while logging in user",
                            task.exception
                        )
                    }
                }
        }
    }

    private fun validateData(): Boolean {
        return when {
            TextUtils.isEmpty(etEmail.text.toString().trim { it <= ' ' }) -> {
                showSnackBar(resources.getString(R.string.err_msg_enter_email), true)
                false
            }
            TextUtils.isEmpty(etPassword.text.toString().trim { it <= ' ' }) -> {
                showSnackBar(resources.getString(R.string.err_msg_enter_password), true)
                false
            }
            else -> {
                true
            }
        }
    }
}
