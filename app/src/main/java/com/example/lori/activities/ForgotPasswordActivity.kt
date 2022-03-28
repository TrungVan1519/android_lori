package com.example.lori.activities

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.example.lori.R
import com.example.lori.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_forgot_password.*

class ForgotPasswordActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        btSubmit.setOnClickListener {
            val email = etEmail.text.toString().trim { it <= ' ' }

            if (email.isEmpty()) {
                showSnackBar(resources.getString(R.string.err_msg_enter_email), true)
            } else {
                showProgressDialog(resources.getString(R.string.label_please_wait))

                FirebaseAuth.getInstance()
                    .sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        hideProgressDialog()

                        if (task.isSuccessful) {
                            showSnackBar(
                                resources.getString(R.string.success_to_send_email),
                                false
                            )

                            Handler(Looper.myLooper()!!).postDelayed({
                                // todo sign out logged in user
                                if (FirebaseAuth.getInstance().currentUser != null) {
                                    FirebaseAuth.getInstance().signOut()
                                }

                                finish()
                            }, Constants.DELAYED_MILLIS)
                        } else {
                            showSnackBar(task.exception!!.message.toString(), true)
                        }
                    }
            }
        }
    }
}
