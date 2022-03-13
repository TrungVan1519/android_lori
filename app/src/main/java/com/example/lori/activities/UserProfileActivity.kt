package com.example.lori.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.lori.R
import com.example.lori.models.User
import com.example.lori.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_user_profile.*
import java.io.IOException

class UserProfileActivity : BaseActivity(), View.OnClickListener {

    private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        if (intent.hasExtra(Constants.EXTRA_USER_DETAILS)) {
            user = intent.getParcelableExtra(Constants.EXTRA_USER_DETAILS)!!
            etFirstName.setText(user.firstName)
            etLastName.setText(user.lastName)
            etEmail.setText(user.email)
            etMobileNumber.setText(user.mobile.toString())
        }

        ivUserPhoto.setOnClickListener(this)
        btSave.setOnClickListener(this)
        btCancel.setOnClickListener(this)
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

                showImageChooser()
            }
            R.id.btSave -> {
                if (validateUserProfileDetails()) {
                    val userHashMap = HashMap<String, Any>()
                    userHashMap[Constants.GENDER] =
                        if (rbMale.isChecked) Constants.MALE else Constants.FEMALE
                    userHashMap[Constants.MOBILE] =
                        etMobileNumber.text.toString().trim { it <= ' ' }.toLong()
                    userHashMap[Constants.PROFILE_COMPLETED] = 1 // not completed = 0

                    showProgressDialog(resources.getString(R.string.please_wait))

                    // Update user details.
                    FirebaseFirestore.getInstance()
                        .collection(Constants.USERS)
                        .document(FirebaseAuth.getInstance().currentUser?.uid ?: "")
                        .update(userHashMap)
                        .addOnSuccessListener {
                            hideProgressDialog()
                            showSnackBar(
                                resources.getString(R.string.msg_profile_update_success),
                                false
                            )

                            Handler().postDelayed({
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            }, Constants.DELAYED_MILLIS)
                        }
                        .addOnFailureListener { e ->
                            hideProgressDialog()
                            showSnackBar(
                                resources.getString(R.string.fail_update_profile),
                                false
                            )

                            Log.e(
                                javaClass.simpleName,
                                "Error while updating the user details.",
                                e
                            )
                        }
                }
            }
            R.id.btCancel -> {
                FirebaseAuth.getInstance().signOut()
                finish()
            }
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
                showImageChooser()
            } else {
                showSnackBar(
                    resources.getString(R.string.read_storage_permission_denied),
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
                        // Load the user image in the ImageView.
                        Glide
                            .with(this)
                            .load(Uri.parse(data.data!!.toString())) // URI of the image
                            .centerCrop() // Scale type of the image.
                            .placeholder(R.drawable.ic_user_image_placeholder) // A default place holder if image is failed to load.
                            .into(ivUserPhoto) // the view in which the image will be loaded.
                    } catch (e: IOException) {
                        showSnackBar(resources.getString(R.string.image_selection_failed), true)
                        Log.e(javaClass.simpleName, "Errors while upload user photo", e)
                    }
                }
            }
            Activity.RESULT_CANCELED -> {
                showSnackBar(resources.getString(R.string.image_selection_failed), true)
                Log.e("Request Cancelled", "Image selection cancelled")
            }
        }
    }

    private fun validateUserProfileDetails(): Boolean {
        return when {
            TextUtils.isEmpty(etMobileNumber.text.toString().trim { it <= ' ' }) -> {
                showSnackBar(resources.getString(R.string.err_msg_enter_mobile_number), true)
                false
            }
            else -> {
                true
            }
        }
    }

    private fun showImageChooser() {
        // Launch the image selection of phone storage using the constant code.
        startActivityForResult(
            Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            ), Constants.PICK_IMAGE_REQUEST_CODE
        )
    }
}
