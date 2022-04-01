package com.example.lori.activities.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lori.R
import com.example.lori.activities.adapters.MyOrderItemsAdapter
import com.example.lori.models.Order
import com.example.lori.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_orders.*

class OrdersFragment : BaseFragment() {

    private lateinit var adapter: MyOrderItemsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        adapter = MyOrderItemsAdapter(this, arrayListOf(), R.layout.layout_order_item)
        return inflater.inflate(R.layout.fragment_orders, container, false)
    }

    override fun onResume() {
        super.onResume()
        getMyOrders()
    }

     fun getMyOrders() {
        showProgressDialog(resources.getString(R.string.label_please_wait))

        FirebaseFirestore.getInstance()
            .collection(Constants.ORDERS)
            .whereEqualTo(Constants.UID, FirebaseAuth.getInstance().currentUser!!.uid)
            .get()
            .addOnSuccessListener { querySnapshot ->
                hideProgressDialog()

                val orderItems = ArrayList<Order>()
                querySnapshot.documents.forEach { documentSnapshot ->
                    val orderItem = documentSnapshot.toObject(Order::class.java)!!
                    orderItem.id = documentSnapshot.id
                    orderItems.add(orderItem)
                }

                if (orderItems.size > 0) {
                    rvMyOrderItems.visibility = View.VISIBLE
                    tvNoOrdersFound.visibility = View.GONE

                    rvMyOrderItems.layoutManager = LinearLayoutManager(activity)
                    rvMyOrderItems.setHasFixedSize(true)
                    rvMyOrderItems.adapter = adapter
                    adapter.notifyItemChanged(orderItems)
                }else {
                    rvMyOrderItems.visibility = View.GONE
                    tvNoOrdersFound.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener { e ->
                hideProgressDialog()
                Log.e(javaClass.simpleName, "Error while getting the orders list.", e)
            }
    }
}
