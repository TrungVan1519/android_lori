package com.example.lori.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lori.R
import com.example.lori.activities.adapters.MyCartItemsAdapter
import com.example.lori.models.CartItem
import com.example.lori.models.Product
import com.example.lori.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_cart.*

class CartActivity : BaseActivity() {

    private lateinit var products: ArrayList<Product>
    private lateinit var cartItems: ArrayList<CartItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)
    }

    override fun onResume() {
        super.onResume()
        getProducts()
    }

    private fun getProducts() {
        showProgressDialog(resources.getString(R.string.label_please_wait))

        FirebaseFirestore.getInstance()
            .collection(Constants.PRODUCTS)
            .orderBy(Constants.TITLE, Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                products = ArrayList()
                querySnapshot.documents.forEach { documentSnapshot ->
                    val product = documentSnapshot.toObject(Product::class.java)!!
                    product.id = documentSnapshot.id
                    products.add(product)
                }

                getMyCartItems()
            }
            .addOnFailureListener { e ->
                hideProgressDialog()
                Log.e(javaClass.simpleName, "Errors while getting products", e)
            }
    }

    fun getMyCartItems() {
        FirebaseFirestore.getInstance()
            .collection(Constants.CART_ITEMS)
            .whereEqualTo(Constants.UID, FirebaseAuth.getInstance().currentUser!!.uid)
            .orderBy(Constants.TITLE, Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                hideProgressDialog()

                cartItems = ArrayList()
                querySnapshot.documents.forEach { documentSnapshot ->
                    val cartItem = documentSnapshot.toObject(CartItem::class.java)!!
                    cartItem.id = documentSnapshot.id
                    cartItems.add(cartItem)
                }

                products.forEach { product ->
                    cartItems.forEach { cartItem ->
                        if (product.id == cartItem.pid) {
                            cartItem.stock_quantity = product.stock_quantity

                            if (product.stock_quantity == 0) {
                                cartItem.cart_quantity = product.stock_quantity
                            }
                        }
                    }
                }

                if (cartItems.size > 0) {
                    var subTotal = 0.0
                    cartItems.forEach { cartItem ->
                        if (cartItem.stock_quantity > 0) {
                            subTotal += (cartItem.price.toDouble() * cartItem.cart_quantity)
                        }
                    }

                    tvSubtotal.text = "$subTotal VND"
                    tvShippingCharge.text = "${Constants.SHIPPING_CHARGE} VND"
                    tvTotalAmount.text = "${subTotal + Constants.SHIPPING_CHARGE} VND"

                    rvCartItems.layoutManager = LinearLayoutManager(this)
                    rvCartItems.setHasFixedSize(true)
                    rvCartItems.adapter =
                        MyCartItemsAdapter(this, cartItems, R.layout.layout_cart_item)

                    rvCartItems.visibility = View.VISIBLE
                    llCheckout.visibility = if (subTotal > 0) View.VISIBLE else View.GONE
                    tvNoCartItemsFound.visibility = View.GONE
                } else {
                    rvCartItems.visibility = View.GONE
                    llCheckout.visibility = View.GONE
                    tvNoCartItemsFound.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener { e ->
                hideProgressDialog()
                Log.e(
                    javaClass.simpleName,
                    "Errors while getting cart items and calculating cart item costs",
                    e
                )
            }
    }
}