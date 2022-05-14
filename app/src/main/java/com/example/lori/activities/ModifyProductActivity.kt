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
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.lori.R
import com.example.lori.models.Product
import com.example.lori.models.User
import com.example.lori.utils.Constants
import com.example.lori.utils.ImageUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_modify_product.*
import java.io.IOException

class ModifyProductActivity : BaseActivity(), View.OnClickListener {
    private var selectedImageFileUri: Uri? = null
    private var mProduct: Product? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modify_product)

        mProduct = intent.getParcelableExtra(Constants.EXTRA_PRODUCT_DETAILS)
        if (mProduct != null && mProduct!!.id.isNotEmpty()) {
            tvTitle.text = resources.getString(R.string.title_edit_product)
            btSubmit.text = resources.getString(R.string.label_btn_update)

            ImageUtils.loadProductImage(this, mProduct!!.image, ivProductImage)
            ivUploadProductImage.visibility = View.GONE

            etProductTitle.setText(mProduct?.title)
            etProductPrice.setText(mProduct?.price.toString())
            etProductDescription.setText(mProduct?.description)
            etProductQuantity.setText(mProduct?.stock_quantity.toString())

            FirebaseFirestore.getInstance()
                .collection(Constants.USERS)
                .document(FirebaseAuth.getInstance().currentUser?.uid ?: "")
                .get()
                .addOnSuccessListener { doc ->
                    val user = doc.toObject(User::class.java)!!
                    if (user.role == Constants.ROLE_ADMIN) {
                        cbProductAR.visibility = View.VISIBLE
                        cbProductAR.isChecked = mProduct!!.ar
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(
                        javaClass.simpleName,
                        "Errors while getting user",
                        e
                    )
                }
        }

        ivUploadProductImage.setOnClickListener(this)
        btSubmit.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivUploadProductImage -> {
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
                if (validateData()) {
                    createOrUpdateProduct()
                }
            }
        }
    }

    private fun createOrUpdateProduct() {
        showProgressDialog(resources.getString(R.string.label_please_wait))

        val product = Product(
            title = etProductTitle.text.toString().trim { it <= ' ' },
            price = etProductPrice.text.toString().trim { it <= ' ' }.toLong(),
            description = etProductDescription.text.toString().trim { it <= ' ' },
            stock_quantity = etProductQuantity.text.toString().trim { it <= ' ' }.toInt(),
            uid = FirebaseAuth.getInstance().currentUser!!.uid,
            username = getSharedPreferences(
                Constants.LORI_PREFERENCES,
                Context.MODE_PRIVATE
            ).getString(Constants.LOGGED_IN_USERNAME, "")!!,
            ar = cbProductAR.isChecked,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
        )

        // todo create new address
        if (mProduct == null || mProduct!!.id.isEmpty()) {
            // todo upload product image
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
                .addOnSuccessListener { task ->
                    // todo get the downloadable url from the task
                    task.metadata!!.reference!!.downloadUrl
                        .addOnSuccessListener { url ->
                            showSnackBar(
                                resources.getString(R.string.success_to_upload_image),
                                false
                            )

                            product.image = url.toString()
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

                                    Handler(Looper.myLooper()!!).postDelayed({
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
                                        "Errors while creating product",
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

                    Log.e(javaClass.simpleName, "Errors while uploading image", e)
                }
        }
        // todo update existing product by using tricks via SetOptions.merge(). Beside, we can use "update()" manually
        else {
            product.image = mProduct!!.image
            FirebaseFirestore.getInstance()
                .collection(Constants.PRODUCTS)
                .document(mProduct!!.id)
                .set(product, SetOptions.merge())
                .addOnSuccessListener {
                    hideProgressDialog()
                    showSnackBar(
                        resources.getString(R.string.success_to_update_products),
                        false
                    )

                    Handler(Looper.myLooper()!!).postDelayed({
                        finish()
                    }, Constants.DELAYED_MILLIS)
                }
                .addOnFailureListener { e ->
                    hideProgressDialog()
                    showSnackBar(
                        resources.getString(R.string.fail_to_update_products),
                        true
                    )

                    Log.e(
                        javaClass.simpleName,
                        "Errors while updating product",
                        e
                    )
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
                            ivProductImage
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
            selectedImageFileUri == null && (mProduct == null || mProduct!!.id.isEmpty()) -> {
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
            else -> true
        }
    }
}
