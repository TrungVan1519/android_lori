package com.example.lori.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lori.R
import com.example.lori.activities.adapters.CartItemAdapter
import com.example.lori.models.*
import com.example.lori.utils.Constants
import com.example.lori.utils.FormatUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.android.synthetic.main.activity_checkout.*

class CheckoutActivity : BaseActivity(), View.OnClickListener {
    private var address: Address? = null
    private var subTotal: Double = 0.0
    private var totalAmount: Double = 0.0

    private lateinit var products: ArrayList<Product>
    private lateinit var cartItems: ArrayList<CartItem>
    private lateinit var adapter: CartItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        address = intent.getParcelableExtra(Constants.EXTRA_SELECTED_ADDRESS)!!
        if (address != null) {
            tvCheckoutAddressType.text = address?.type
            tvCheckoutFullName.text = address?.name
            tvCheckoutAddress.text = "${address!!.address}, ${address!!.zipCode}"
            tvCheckoutAdditionalNote.text = address?.additionalNote
            tvCheckoutOtherDetails.text = address?.otherDetails ?: ""
            tvCheckoutMobileNumber.text = address?.mobile.toString()
        }

        adapter = CartItemAdapter(this, arrayListOf(), R.layout.layout_cart_item)

        btPlaceOrder.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        getProducts()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btPlaceOrder -> {
                placeAnOrder()
            }
        }
    }

    private fun getProducts() {
        showProgressDialog(resources.getString(R.string.label_please_wait))

        FirebaseFirestore.getInstance()
            .collection(Constants.PRODUCTS)
            .orderBy(Constants.TITLE, Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { query ->
                products = ArrayList()
                query.documents.forEach { doc ->
                    val product = doc.toObject(Product::class.java)!!
                    product.id = doc.id
                    products.add(product)
                }

                getCartItems()
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
    private fun getCartItems() {
        FirebaseFirestore.getInstance()
            .collection(Constants.CART_ITEMS)
            .whereEqualTo(Constants.UID, FirebaseAuth.getInstance().currentUser!!.uid)
            .orderBy(Constants.TITLE, Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { query ->
                hideProgressDialog()

                cartItems = ArrayList()
                query.documents.forEach { doc ->
                    val cartItem = doc.toObject(CartItem::class.java)!!
                    cartItem.id = doc.id
                    cartItems.add(cartItem)
                }

                products.forEach { product ->
                    cartItems.forEach { cartItem ->
                        if (product.id == cartItem.pid) {
                            cartItem.stock_quantity = product.stock_quantity
                        }
                    }
                }

                // todo calculate product costs and shipping fee
                cartItems.forEach { cartItem ->
                    if (cartItem.stock_quantity > 0) {
                        subTotal += cartItem.price.toDouble() * cartItem.cart_quantity
                    }
                }

                tvCheckoutSubtotal.text = "${FormatUtils.format(num = subTotal)} VND"
                tvCheckoutShippingCharge.text =
                    "${FormatUtils.format(num = Constants.SHIPPING_CHARGE)} VND"
                tvCheckoutTotalAmount.text =
                    "${FormatUtils.format(num = subTotal + Constants.SHIPPING_CHARGE)} VND"

                rvCartItems.layoutManager = LinearLayoutManager(this)
                rvCartItems.setHasFixedSize(true)
                rvCartItems.adapter = adapter // todo force redraw RecyclerView items
                adapter.notifyItemChanged(cartItems)

                llCheckoutPlaceOrder.visibility = if (subTotal > 0) View.VISIBLE else View.GONE
            }
            .addOnFailureListener { e ->
                hideProgressDialog()
                Log.e(
                    javaClass.simpleName,
                    "Errors while getting cart items and calculating costs",
                    e
                )
            }
    }

    private fun placeAnOrder() {
        showProgressDialog(resources.getString(R.string.label_please_wait))

        totalAmount = subTotal + Constants.SHIPPING_CHARGE
        val order = Order(
            items = cartItems,
            address = address!!,
            title = "My order ${System.currentTimeMillis()}",
            image = cartItems[0].image,
            subTotalAmount = subTotal,
            shippingCharge = Constants.SHIPPING_CHARGE.toDouble(),
            totalAmount = totalAmount,
            order_datetime = System.currentTimeMillis(),
            uid = FirebaseAuth.getInstance().currentUser!!.uid,
        )

        FirebaseFirestore.getInstance()
            .collection(Constants.ORDERS)
            .document()
            .set(order, SetOptions.merge())
            .addOnSuccessListener {
                val writeBatch = FirebaseFirestore.getInstance().batch()

                cartItems.forEach { cartItem ->
                    // todo update stock quantity of products based on cart quantity
                    writeBatch.update(
                        FirebaseFirestore.getInstance()
                            .collection(Constants.PRODUCTS)
                            .document(cartItem.pid),
                        mapOf(Constants.STOCK_QUANTITY to cartItem.stock_quantity - cartItem.cart_quantity)
                    )

                    // todo delete cartItems
                    writeBatch.delete(
                        FirebaseFirestore.getInstance()
                            .collection(Constants.CART_ITEMS)
                            .document(cartItem.id)
                    )
                }

                writeBatch.commit()
                    .addOnSuccessListener {
                        hideProgressDialog()
                        showSnackBar(resources.getString(R.string.success_to_place_orders), false)

                        Handler(Looper.myLooper()!!).postDelayed({
                            val intent = Intent(this, DashboardActivity::class.java)
                            intent.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }, Constants.DELAYED_MILLIS)
                    }
                    .addOnFailureListener { e ->
                        hideProgressDialog()
                        showSnackBar(resources.getString(R.string.fail_to_place_orders), true)

                        Log.e(javaClass.simpleName, "Error while placing orders", e)
                    }
            }
            .addOnFailureListener { e ->
                hideProgressDialog()
                showSnackBar(resources.getString(R.string.fail_to_place_orders), true)

                Log.e(
                    javaClass.simpleName,
                    "Error while placing orders",
                    e
                )
            }
    }
}
