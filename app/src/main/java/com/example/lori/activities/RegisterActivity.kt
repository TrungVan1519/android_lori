package com.example.lori.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import com.example.lori.R
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // todo set up ActionBar
        setSupportActionBar(tbActionBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        tbActionBar.setOnClickListener { onBackPressed() }

        tvLogin.setOnClickListener { startActivity(Intent(this, LoginActivity::class.java)) }
        btRegister.setOnClickListener { validateRegisterDetails() }
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
                showSnackBar(resources.getString(R.string.success_register), false)
                true
            }
        }
    }
}
