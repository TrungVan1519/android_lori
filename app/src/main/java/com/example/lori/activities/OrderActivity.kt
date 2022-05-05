package com.example.lori.activities

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lori.R
import com.example.lori.activities.adapters.CartItemAdapter
import com.example.lori.models.Order
import com.example.lori.utils.Constants
import com.example.lori.utils.FormatUtils
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_order.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class OrderActivity : BaseActivity(), View.OnClickListener {
    var order = Order()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order)

        order = intent.getParcelableExtra(Constants.EXTRA_MY_ORDER_DETAILS)!!
        tvOrderDetailsId.text = order.title

        val cal = Calendar.getInstance()
        cal.timeInMillis = order.order_datetime
        tvOrderDetailsDate.text =
            SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(cal.time)

        if (order.status) {
            btConfirmReceiveOrder.visibility = View.GONE
            tvOrderStatus.text =
                resources.getString(R.string.label_order_status_received)
            tvOrderStatus.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.order_status_delivered
                )
            )
        } else {
            val diffInHours =
                TimeUnit.MICROSECONDS.toHours(System.currentTimeMillis() - order.order_datetime)
            when {
                diffInHours < 1 -> {
                    tvOrderStatus.text = resources.getString(R.string.label_order_status_pending)
                    tvOrderStatus.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.colorAccent
                        )
                    )
                }
                diffInHours < 2 -> {
                    tvOrderStatus.text = resources.getString(R.string.label_order_status_in_process)
                    tvOrderStatus.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.order_status_in_process
                        )
                    )
                }
                else -> {
                    tvOrderStatus.text = resources.getString(R.string.label_order_status_delivered)
                    tvOrderStatus.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.order_status_delivered
                        )
                    )
                }
            }
        }

        rvAllOrders.layoutManager = LinearLayoutManager(this@OrderActivity)
        rvAllOrders.setHasFixedSize(true)
        rvAllOrders.adapter = CartItemAdapter(this, order.items, R.layout.layout_cart_item)

        tvMyOrderDetailsAddressType.text = order.address.type
        tvMyOrderDetailsFullName.text = order.address.name
        tvMyOrderDetailsAddress.text =
            "${order.address.address}, ${order.address.zipCode}"
        tvMyOrderDetailsAdditionalNote.text = order.address.additionalNote
        tvMyOrderDetailsOtherDetails.text = order.address.otherDetails
        tvMyOrderDetailsOtherDetails.visibility =
            if (order.address.otherDetails.isNotEmpty()) View.VISIBLE else View.GONE
        tvMyOrderDetailsMobileNumber.text = order.address.mobile.toString()

        tvOrderDetailsSubTotal.text = "${FormatUtils.format(num = order.subTotalAmount)} VND"
        tvOrderDetailsShippingCharge.text =
            "${FormatUtils.format(num = Constants.SHIPPING_CHARGE)} VND"
        tvOrderDetailsTotalAmount.text = "${FormatUtils.format(num = order.totalAmount)} VND"

        btConfirmReceiveOrder.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btConfirmReceiveOrder -> confirmReceiveOrders()
        }
    }

    private fun confirmReceiveOrders() {
        AlertDialog.Builder(this)
            .setTitle("Confirmation")
            .setMessage("Have you received your orders?")
            .setIcon(R.drawable.ic_products_24dp)
            .setPositiveButton("Yes, I have") { dialog, _ ->
                showProgressDialog(resources.getString(R.string.label_please_wait))

                FirebaseFirestore.getInstance()
                    .collection(Constants.ORDERS)
                    .document(order.id)
                    .update(mapOf(Constants.STATUS to true))
                    .addOnSuccessListener {
                        hideProgressDialog()
                        btConfirmReceiveOrder.visibility = View.GONE
                        tvOrderStatus.text =
                            resources.getString(R.string.label_order_status_received)
                        tvOrderStatus.setTextColor(
                            ContextCompat.getColor(
                                this,
                                R.color.order_status_delivered
                            )
                        )
                    }
                    .addOnFailureListener { e ->
                        hideProgressDialog()
                        showSnackBar(
                            resources.getString(R.string.fail_to_confirm_receive_orders),
                            true
                        )

                        Log.e(
                            javaClass.simpleName,
                            "Error while confirming receive orders",
                            e
                        )
                    }

                dialog.dismiss()
            }
            .setNegativeButton("No, I haven't") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }
}
