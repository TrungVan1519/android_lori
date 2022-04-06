package com.example.lori.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.lori.R
import com.example.lori.models.User
import com.example.lori.utils.Constants
import com.example.lori.utils.ImageUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_profile.*
import java.io.IOException

class ProfileActivity : BaseActivity(), View.OnClickListener {
    private lateinit var user: User
    private var selectedImageFileUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        user = intent.getParcelableExtra(Constants.EXTRA_USER_DETAILS)!!
        etFirstName.setText(user.firstName)
        etLastName.setText(user.lastName)
        etEmail.setText(user.email)
        etMobileNumber.setText(if (user.mobile != 0L) user.mobile.toString() else "")
        if (user.gender == Constants.FEMALE) {
            rbFemale.isChecked = true
        } else {
            rbMale.isChecked = true
        }

        if (user.profileCompleted == 0) {
            etFirstName.isEnabled = false
            etLastName.isEnabled = false
            etEmail.isEnabled = false
        } else {
            etEmail.isEnabled = false
            ImageUtils.loadUserImage(this, user.image, ivUserPhoto)
        }

        ivUserPhoto.setOnClickListener(this)
        btSave.setOnClickListener(this)
        btLogout.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivUserPhoto -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                    && ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                    == PackageManager.PERMISSION_DENIED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        Constants.READ_STORAGE_PERMISSION_CODE
                    )
                    return
                }

                ImageUtils.showImageChooser(this)
            }
            R.id.btSave -> {
                if (validateData()) {
                    val userHashMap = HashMap<String, Any>()
                    userHashMap[Constants.FIRST_NAME] =
                        etFirstName.text.toString().trim { it <= ' ' }
                    userHashMap[Constants.LAST_NAME] =
                        etLastName.text.toString().trim { it <= ' ' }
                    userHashMap[Constants.GENDER] =
                        if (rbMale.isChecked) Constants.MALE else Constants.FEMALE
                    userHashMap[Constants.MOBILE] =
                        etMobileNumber.text.toString().trim { it <= ' ' }.toLong()
                    userHashMap[Constants.PROFILE_COMPLETED] = 1 // 0: incomplete - 1: completed
                    userHashMap[Constants.CREATED_AT] = System.currentTimeMillis()
                    userHashMap[Constants.UPDATED_AT] = System.currentTimeMillis()

                    showProgressDialog(resources.getString(R.string.label_please_wait))

                    // todo upload user image
                    if (selectedImageFileUri != null) {
                        FirebaseStorage.getInstance().reference
                            .child(
                                "${Constants.USER_PROFILE_IMAGE}${System.currentTimeMillis()}.${
                                    ImageUtils.getFileExtension(
                                        this,
                                        selectedImageFileUri!!
                                    )
                                }"
                            )
                            .putFile(selectedImageFileUri!!)
                            .addOnSuccessListener { task ->
                                // todo get the downloadable url from the task snapshot
                                task.metadata!!.reference!!.downloadUrl
                                    .addOnSuccessListener { url ->
                                        showSnackBar(
                                            resources.getString(R.string.success_to_upload_image),
                                            false
                                        )

                                        userHashMap[Constants.IMAGE] = url.toString()
                                        updateUser(userHashMap)
                                    }
                            }
                            .addOnFailureListener { e ->
                                hideProgressDialog()
                                showSnackBar(
                                    resources.getString(R.string.fail_to_upload_image),
                                    true
                                )

                                Log.e(
                                    javaClass.simpleName,
                                    "Errors while uploading image",
                                    e
                                )
                            }
                    } else {
                        updateUser(userHashMap)
                    }
                }
            }
            R.id.btLogout -> {
                FirebaseAuth.getInstance().signOut()

                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            }
        }
    }

    private fun updateUser(userHashMap: HashMap<String, Any>) {
        FirebaseFirestore.getInstance()
            .collection(Constants.USERS)
            .document(FirebaseAuth.getInstance().currentUser?.uid ?: "")
            .update(userHashMap)
            .addOnSuccessListener {
                hideProgressDialog()
                showSnackBar(
                    resources.getString(R.string.success_to_update_profile),
                    false
                )

                Handler(Looper.myLooper()!!).postDelayed({
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                }, Constants.DELAYED_MILLIS)
            }
            .addOnFailureListener { e ->
                hideProgressDialog()
                showSnackBar(
                    resources.getString(R.string.fail_to_update_profile),
                    true
                )

                Log.e(
                    javaClass.simpleName,
                    "Errors while updating user",
                    e
                )
            }
    }

    /**
     * Identify the result of runtime permission after the user allows or denies permission based on the unique code.
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == Constants.READ_STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ImageUtils.showImageChooser(this)
            } else {
                showSnackBar(
                    resources.getString(R.string.err_msg_read_storage_permission_denied),
                    true
                )
            }
        }
    }

    /**
     * Receive the result from a previous call to
     * {@link #startActivityForResult(Intent, int)}.  This follows the
     * related Activity API as described there in
     * {@link Activity#onActivityResult(int, int, Intent)}.
     *
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode The integer result code returned by the child activity
     *                   through its setResult().
     * @param data An Intent, which can return result data to the caller
     *               (various data can be attached to Intent "extras").
     */
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (resultCode) {
            Activity.RESULT_OK -> {
                if (requestCode == Constants.PICK_IMAGE_REQUEST_CODE && data != null) {
                    try {
                        selectedImageFileUri = data.data
                        ImageUtils.loadUserImage(
                            this,
                            selectedImageFileUri!!,
                            ivUserPhoto
                        )
                    } catch (e: IOException) {
                        showSnackBar(resources.getString(R.string.fail_to_select_image), true)
                        Log.e(javaClass.simpleName, "Errors while uploading image", e)
                    }
                }
            }
            Activity.RESULT_CANCELED -> {
                showSnackBar(resources.getString(R.string.fail_to_select_image), true)
                Log.e("Request Cancelled", "Image selection cancelled")
            }
        }
    }

    private fun validateData(): Boolean {
        return when {
            TextUtils.isEmpty(etFirstName.text.toString().trim { it <= ' ' }) -> {
                showSnackBar(resources.getString(R.string.err_msg_enter_first_name), true)
                false
            }
            TextUtils.isEmpty(etLastName.text.toString().trim { it <= ' ' }) -> {
                showSnackBar(resources.getString(R.string.err_msg_enter_last_name), true)
                false
            }
            TextUtils.isEmpty(etMobileNumber.text.toString().trim { it <= ' ' }) -> {
                showSnackBar(resources.getString(R.string.err_msg_enter_mobile_number), true)
                false
            }
            else -> {
                true
            }
        }
    }
}
