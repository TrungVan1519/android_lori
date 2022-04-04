package com.example.lori.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import com.example.lori.R
import com.example.lori.models.CartItem
import com.example.lori.models.FavProduct
import com.example.lori.models.Product
import com.example.lori.utils.Constants
import com.example.lori.utils.FormatUtils
import com.example.lori.utils.ImageUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.android.synthetic.main.activity_product_details.*

class ProductDetailsActivity : BaseActivity(), View.OnClickListener {

    private var product: Product? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_details)

        getProductDetails(intent.getStringExtra(Constants.EXTRA_PRODUCT_ID) ?: "")
        getFavProductDetails()

        ivAddToFav.setOnClickListener(this)
        btAddToCart.setOnClickListener(this)
        btGoToCart.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivAddToFav -> {
                addToFav()
            }
            R.id.btAddToCart -> {
                addToCart()
            }
            R.id.btGoToCart -> {
                startActivity(Intent(this, CartActivity::class.java))
            }
        }
    }

    private fun getProductDetails(id: String) {
        showProgressDialog(resources.getString(R.string.label_please_wait))

        FirebaseFirestore.getInstance()
            .collection(Constants.PRODUCTS)
            .document(id)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                product = documentSnapshot.toObject(Product::class.java)!!
                product!!.id = id

                ImageUtils.loadProductImage(this, product!!.image, ivProductDetailImage)
                tvProductOwnerName.text = "By owner: ${product!!.username}"
                tvProductDetailsTitle.text = product!!.title
                tvProductDetailsPrice.text = "${FormatUtils.format(num = product!!.price)} VND"
                tvProductDetailsDescription.text = product!!.description

                if (product!!.stock_quantity == 0) {
                    tvProductDetailsStockQuantity.text =
                        resources.getString(R.string.label_out_of_stock)
                    tvProductDetailsStockQuantity.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.snack_bar_error
                        )
                    )
                } else {
                    tvProductDetailsStockQuantity.text =
                        FormatUtils.format(num = product!!.stock_quantity)
                }

                if (FirebaseAuth.getInstance().currentUser!!.uid == product!!.uid || product!!.stock_quantity <= 0) {
                    hideProgressDialog()
                } else {
                    // Get products from cart via cart items
                    FirebaseFirestore.getInstance()
                        .collection(Constants.CART_ITEMS)
                        .whereEqualTo(
                            Constants.UID,
                            FirebaseAuth.getInstance().currentUser!!.uid
                        )
                        .whereEqualTo(Constants.PID, product!!.id)
                        .get()
                        .addOnSuccessListener { snapshot ->
                            hideProgressDialog()

                            // show btAddToCart if product not exist in cart
                            if (snapshot.documents.size <= 0) {
                                btAddToCart.visibility = View.VISIBLE
                            }
                        }
                        .addOnFailureListener { e ->
                            hideProgressDialog()
                            showSnackBar(
                                resources.getString(R.string.fail_to_check_existing_cart_items),
                                true
                            )

                            Log.e(
                                javaClass.simpleName,
                                "Error while checking existing cart items",
                                e
                            )
                        }
                }
            }
            .addOnFailureListener { e ->
                hideProgressDialog()
                showSnackBar(resources.getString(R.string.fail_to_get_product_details), true)

                Log.e(javaClass.simpleName, "Errors while getting product details", e)
            }
    }

    private fun getFavProductDetails() {
        FirebaseFirestore.getInstance()
            .collection(Constants.FAV_PRODUCTS)
            .whereEqualTo(Constants.UID, FirebaseAuth.getInstance().currentUser!!.uid)
            .whereEqualTo(Constants.PID, product!!.id)
            .get()
            .addOnSuccessListener {
                ivAddToFav.setImageResource(R.drawable.ic_favorite_24dp)
            }
            .addOnFailureListener {
                ivAddToFav.setImageResource(R.drawable.ic_favorite_border_24dp)
            }
    }

    private fun addToFav() {
        val favProduct = FavProduct(
            image = product!!.image,
            title = product!!.title,
            price = product!!.price,
            uid = FirebaseAuth.getInstance().currentUser!!.uid,
            pid = product!!.id,
            createdAt = System.currentTimeMillis()
        )

        FirebaseFirestore.getInstance()
            .collection(Constants.FAV_PRODUCTS)
            .document()
            .set(favProduct, SetOptions.merge())
            .addOnSuccessListener {
                ivAddToFav.setImageResource(R.drawable.ic_favorite_24dp)
            }
            .addOnFailureListener {
                ivAddToFav.setImageResource(R.drawable.ic_favorite_border_24dp)
            }
    }

    private fun addToCart() {
        showProgressDialog(resources.getString(R.string.label_please_wait))

        val cartItem = CartItem(
            title = product!!.title,
            price = product!!.price,
            image = product!!.image,
            cart_quantity = Constants.DEFAULT_CART_QUANTITY,
            uid = FirebaseAuth.getInstance().uid!!,
            pid = product!!.id
        )

        FirebaseFirestore.getInstance()
            .collection(Constants.CART_ITEMS)
            .document()
            .set(cartItem, SetOptions.merge())
            .addOnSuccessListener {
                hideProgressDialog()
                showSnackBar(resources.getString(R.string.success_to_add_products_to_cart), false)

                btAddToCart.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                hideProgressDialog()
                showSnackBar(resources.getString(R.string.fail_to_add_products_to_cart), true)

                Log.e(javaClass.simpleName, "Errors while saving cart items", e)
            }
    }
}
