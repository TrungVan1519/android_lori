package com.example.lori.activities

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.view.View
import com.example.lori.R
import com.example.lori.models.Address
import com.example.lori.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.android.synthetic.main.activity_add_edit_address.*

class AddEditAddressesActivity : BaseActivity(), View.OnClickListener {
    private var mAddress: Address? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_address)

        mAddress = intent.getParcelableExtra(Constants.EXTRA_ADDRESS_DETAILS)
        if (mAddress != null && mAddress!!.id.isNotEmpty()) {
            tvTitle.text = resources.getString(R.string.title_edit_address)
            btSubmit.text = resources.getString(R.string.label_btn_update)

            etFullName.setText(mAddress?.name)
            etPhoneNumber.setText(mAddress?.mobile.toString())
            etAddress.setText(mAddress?.address)
            etZipCode.setText(mAddress?.zipCode)
            etAdditionalNote.setText(mAddress?.additionalNote)

            when (mAddress?.type) {
                Constants.HOME -> rbHome.isChecked = true
                Constants.OFFICE -> rbOffice.isChecked = true
                else -> {
                    rbOther.isChecked = true
                    tilOtherDetails.visibility = View.VISIBLE
                    etOtherDetails.setText(mAddress?.otherDetails)
                }
            }
        }

        rgType.setOnCheckedChangeListener { _, checkedId ->
            tilOtherDetails.visibility = if (checkedId == R.id.rbOther) View.VISIBLE else View.GONE
        }

        btSubmit.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btSubmit -> {
                if (validateData()) {
                    createOrUpdateAddress()
                }
            }
        }
    }

    private fun createOrUpdateAddress() {
        showProgressDialog(resources.getString(R.string.label_please_wait))

        val address = Address(
            name = etFullName.text.toString().trim { it <= ' ' },
            mobile = etPhoneNumber.text.toString().trim { it <= ' ' }.toLong(),
            address = etAddress.text.toString().trim { it <= ' ' },
            zipCode = etZipCode.text.toString().trim { it <= ' ' },
            additionalNote = etAdditionalNote.text.toString().trim { it <= ' ' },
            type = when {
                rbHome.isChecked -> Constants.HOME
                rbOffice.isChecked -> Constants.OFFICE
                else -> Constants.OTHER
            },
            otherDetails = etOtherDetails.text.toString().trim { it <= ' ' },
            uid = FirebaseAuth.getInstance().currentUser!!.uid
        )

        // todo create new address
        if (mAddress == null || mAddress!!.id.isEmpty()) {
            FirebaseFirestore.getInstance()
                .collection(Constants.ADDRESSES)
                .document()
                .set(address, SetOptions.merge())
                .addOnSuccessListener {
                    hideProgressDialog()
                    showSnackBar(resources.getString(R.string.success_to_add_address), false)

                    Handler(Looper.myLooper()!!).postDelayed({
                        finish()
                    }, Constants.DELAYED_MILLIS)
                }
                .addOnFailureListener { e ->
                    hideProgressDialog()
                    showSnackBar(resources.getString(R.string.fail_to_add_addresses), true)

                    Log.e(javaClass.simpleName, "Errors while creating addresses", e)
                }
        }
        // todo update existing address by using tricks via SetOptions.merge(). Beside, we can use "update()" manually
        else {
            FirebaseFirestore.getInstance()
                .collection(Constants.ADDRESSES)
                .document(mAddress!!.id)
                .set(address, SetOptions.merge())
                .addOnSuccessListener {
                    hideProgressDialog()
                    showSnackBar(resources.getString(R.string.success_to_update_address), false)

                    Handler(Looper.myLooper()!!).postDelayed({
                        finish()
                    }, Constants.DELAYED_MILLIS)
                }
                .addOnFailureListener { e ->
                    hideProgressDialog()
                    showSnackBar(resources.getString(R.string.fail_to_update_addresses), true)

                    Log.e(javaClass.simpleName, "Errors while updating addresses", e)
                }
        }
    }

    private fun validateData(): Boolean {
        return when {
            TextUtils.isEmpty(etFullName.text.toString().trim { it <= ' ' }) -> {
                showSnackBar(
                    resources.getString(R.string.err_msg_please_enter_full_name),
                    true
                )
                false
            }
            TextUtils.isEmpty(etPhoneNumber.text.toString().trim { it <= ' ' }) -> {
                showSnackBar(
                    resources.getString(R.string.err_msg_please_enter_phone_number),
                    true
                )
                false
            }
            TextUtils.isEmpty(etAddress.text.toString().trim { it <= ' ' }) -> {
                showSnackBar(resources.getString(R.string.err_msg_please_enter_address), true)
                false
            }
            TextUtils.isEmpty(etZipCode.text.toString().trim { it <= ' ' }) -> {
                showSnackBar(resources.getString(R.string.err_msg_please_enter_zip_code), true)
                false
            }
            rbOther.isChecked && TextUtils.isEmpty(
                etZipCode.text.toString().trim { it <= ' ' }) -> {
                showSnackBar(resources.getString(R.string.err_msg_please_enter_zip_code), true)
                false
            }
            else -> true
        }
    }
}
