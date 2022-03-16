package com.example.lori.activities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.lori.R
import com.example.lori.models.Product
import com.example.lori.utils.Constants
import com.example.lori.utils.ImageUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_add_products.*
import java.io.IOException

class AddProductsActivity : BaseActivity(), View.OnClickListener {

    private var selectedImageFileUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_products)

        ivAddUpdateProducts.setOnClickListener(this)
        btSubmit.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivAddUpdateProducts -> {
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
            R.id.btSubmit -> {
                if (validateProductDetails()) {
                    createProduct()
                }
            }
        }
    }

    private fun createProduct() {
        showProgressDialog(resources.getString(R.string.label_please_wait))

        // Upload product image.
        FirebaseStorage.getInstance().reference
            .child(
                "${Constants.PRODUCT_IMAGE}${System.currentTimeMillis()}.${
                    ImageUtils.getFileExtension(
                        this,
                        selectedImageFileUri!!
                    )
                }"
            )
            .putFile(selectedImageFileUri!!)
            .addOnSuccessListener { taskSnapshot ->
                // Get the downloadable url from the task snapshot
                taskSnapshot.metadata!!.reference!!.downloadUrl
                    .addOnSuccessListener { url ->
                        showSnackBar(
                            resources.getString(R.string.success_to_upload_image),
                            false
                        )

                        val product = Product(
                            image = url.toString(),
                            title = etProductTitle.text.toString().trim { it <= ' ' },
                            price = etProductPrice.text.toString().trim { it <= ' ' }.toLong(),
                            description = etProductDescription.text.toString().trim { it <= ' ' },
                            stock_quantity = etProductQuantity.text.toString().trim { it <= ' ' }
                                .toInt(),
                            uid = FirebaseAuth.getInstance().currentUser!!.uid,
                            username = getSharedPreferences(
                                Constants.LORI_PREFERENCES,
                                Context.MODE_PRIVATE
                            ).getString(Constants.LOGGED_IN_USERNAME, "")!!,
                        )

                        // Save to "products" table in FireStore DB
                        FirebaseFirestore.getInstance()
                            .collection(Constants.PRODUCTS)
                            .document()
                            .set(product, SetOptions.merge())
                            .addOnSuccessListener {
                                hideProgressDialog()
                                showSnackBar(
                                    resources.getString(R.string.success_to_add_products),
                                    false
                                )

                                Handler().postDelayed({
                                    finish()
                                }, Constants.DELAYED_MILLIS)
                            }
                            .addOnFailureListener { e ->
                                hideProgressDialog()
                                showSnackBar(
                                    resources.getString(R.string.fail_to_add_products),
                                    true
                                )

                                Log.e(
                                    javaClass.simpleName,
                                    "Errors while saving product.",
                                    e
                                )
                            }
                    }
            }
            .addOnFailureListener { e ->
                hideProgressDialog()
                showSnackBar(
                    resources.getString(R.string.fail_to_upload_image),
                    true
                )

                Log.e(javaClass.simpleName, "Errors while uploading image.", e)
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
                        // Load the user image in the ImageView.
                        selectedImageFileUri = data.data
                        ImageUtils.loadUserImage(
                            this,
                            selectedImageFileUri!!,
                            ivProductImage
                        )
                    } catch (e: IOException) {
                        showSnackBar(resources.getString(R.string.fail_to_select_image), true)
                        Log.e(javaClass.simpleName, "Errors while uploading image.", e)
                    }
                }
            }
            Activity.RESULT_CANCELED -> {
                showSnackBar(resources.getString(R.string.fail_to_select_image), true)
                Log.e("Request Cancelled", "Image selection cancelled")
            }
        }
    }

    private fun validateProductDetails(): Boolean {
        return when {
            selectedImageFileUri == null -> {
                showSnackBar(resources.getString(R.string.err_msg_select_product_image), true)
                false
            }
            TextUtils.isEmpty(etProductTitle.text.toString().trim { it <= ' ' }) -> {
                showSnackBar(resources.getString(R.string.err_msg_enter_product_title), true)
                false
            }
            TextUtils.isEmpty(etProductPrice.text.toString().trim { it <= ' ' }) -> {
                showSnackBar(resources.getString(R.string.err_msg_enter_product_price), true)
                false
            }
            TextUtils.isEmpty(etProductDescription.text.toString().trim { it <= ' ' }) -> {
                showSnackBar(
                    resources.getString(R.string.err_msg_enter_product_description),
                    true
                )
                false
            }
            TextUtils.isEmpty(etProductQuantity.text.toString().trim { it <= ' ' }) -> {
                showSnackBar(
                    resources.getString(R.string.err_msg_enter_product_quantity),
                    true
                )
                false
            }
            else -> {
                true
            }
        }
    }
}
