package com.example.lori.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.example.lori.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : BaseActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        if (beAlreadyRegistered()) {
            showSnackBar(resources.getString(R.string.success_login), false)
        }

        tvForgotPassword.setOnClickListener(this)
        btLogin.setOnClickListener(this)
        tvRegister.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvForgotPassword -> {
//                    startActivity(Intent(this, ForgotPasswordActivity::class.java))
            }
            R.id.btLogin -> {
                loginUser()
            }
            R.id.tvRegister -> {
                startActivity(Intent(this, RegisterActivity::class.java))
            }
        }
    }

    private fun beAlreadyRegistered(): Boolean {
        return FirebaseAuth.getInstance().currentUser != null
    }

    private fun loginUser() {
        if (validateLoginDetails()) {
            showProgressDialog(resources.getString(R.string.please_wait))

            FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(
                    etEmail.text.toString().trim { it <= ' ' },
                    etPassword.text.toString().trim { it <= ' ' })
                .addOnCompleteListener { task ->
                    hideProgressDialog()

                    if (task.isSuccessful) {
                        showSnackBar(resources.getString(R.string.success_login), false)
                    } else {
                        showSnackBar(task.exception!!.message.toString(), true)
                    }
                }
        }
    }

    private fun validateLoginDetails(): Boolean {
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
