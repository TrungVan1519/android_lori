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
import com.example.lori.utils.FormatUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_cart.*

class CartActivity : BaseActivity() {

    private lateinit var products: ArrayList<Product>
    private lateinit var adapter: MyCartItemsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        adapter = MyCartItemsAdapter(this, arrayListOf(), R.layout.layout_cart_item)
    }

    override fun onResume() {
        super.onResume()
        getProducts()
    }

    private fun getProducts() {
        showProgressDialog(resources.getString(R.string.label_please_wait))

        FirebaseFirestore.getInstance()
            .collection(Constants.PRODUCTS)
            .orderBy(Constants.TITLE, Query.Direction.DESCENDING)
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

    /**
     * Be careful of using hideProgressDialog() because we don't use showProgressDialog(),
     * so we must call this fun inside of another fun using showProgressDialog() instead
     * such as getProducts(), adapter.updateCartItem(), adapter.deleteCartItem(), etc
     */
    fun getMyCartItems() {
        FirebaseFirestore.getInstance()
            .collection(Constants.CART_ITEMS)
            .whereEqualTo(Constants.UID, FirebaseAuth.getInstance().currentUser!!.uid)
            .orderBy(Constants.TITLE, Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                hideProgressDialog()

                val cartItems = ArrayList<CartItem>()
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
                    // Calculate product costs and shopping fee
                    var subTotal = 0.0
                    cartItems.forEach { cartItem ->
                        if (cartItem.stock_quantity > 0) {
                            subTotal += cartItem.price.toDouble() * cartItem.cart_quantity
                        }
                    }

                    tvSubtotal.text = "${FormatUtils.format(num = subTotal)} VND"
                    tvShippingCharge.text =
                        "${FormatUtils.format(num = Constants.SHIPPING_CHARGE)} VND"
                    tvTotalAmount.text =
                        "${FormatUtils.format(num = subTotal + Constants.SHIPPING_CHARGE)} VND"

                    rvCartItems.layoutManager = LinearLayoutManager(this)
                    rvCartItems.setHasFixedSize(true)
                    rvCartItems.adapter = adapter // force redraw RecyclerView items
                    adapter.notifyItemChanged(cartItems)

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
