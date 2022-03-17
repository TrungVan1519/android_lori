package com.example.lori.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.lori.R
import com.example.lori.models.Product
import com.example.lori.utils.Constants
import com.example.lori.utils.ImageUtils
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_product_details.*

class ProductDetailsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_details)

        getProductDetails(intent.getStringExtra(Constants.EXTRA_PRODUCT_ID) ?: "")
    }

    private fun getProductDetails(id: String) {
        showProgressDialog(resources.getString(R.string.label_please_wait))

        FirebaseFirestore.getInstance()
            .collection(Constants.PRODUCTS)
            .document(id)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                hideProgressDialog()

                val product = documentSnapshot.toObject(Product::class.java)!!
                product.id = id

                ImageUtils.loadProductImage(this, product.image, ivProductDetailImage)
                tvProductDetailsTitle.text = product.title
                tvProductDetailsPrice.text = "${product.price} VND"
                tvProductDetailsDescription.text = product.description
                tvProductDetailsStockQuantity.text = product.stock_quantity.toString()
            }
            .addOnFailureListener { e ->
                hideProgressDialog()
                showSnackBar(resources.getString(R.string.fail_to_get_product_details), true)

                Log.e(javaClass.simpleName, "Errors while getting product details", e)
            }
    }
}
