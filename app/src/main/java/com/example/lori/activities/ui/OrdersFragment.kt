package com.example.lori.activities.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lori.R
import com.example.lori.activities.adapters.MyOrdersAdapter
import com.example.lori.models.Order
import com.example.lori.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_orders.*

class OrdersFragment : BaseFragment() {

    private lateinit var adapter: MyOrdersAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        adapter = MyOrdersAdapter(this, arrayListOf(), R.layout.layout_order)
        return inflater.inflate(R.layout.fragment_orders, container, false)
    }

    override fun onResume() {
        super.onResume()
        getMyOrders()
    }

     private fun getMyOrders() {
        showProgressDialog(resources.getString(R.string.label_please_wait))

        FirebaseFirestore.getInstance()
            .collection(Constants.ORDERS)
            .whereEqualTo(Constants.UID, FirebaseAuth.getInstance().currentUser!!.uid)
            .get()
            .addOnSuccessListener { querySnapshot ->
                hideProgressDialog()

                val orders = ArrayList<Order>()
                querySnapshot.documents.forEach { documentSnapshot ->
                    val order = documentSnapshot.toObject(Order::class.java)!!
                    order.id = documentSnapshot.id
                    orders.add(order)
                }

                if (orders.size > 0) {
                    rvMyOrders.visibility = View.VISIBLE
                    tvNoOrdersFound.visibility = View.GONE

                    rvMyOrders.layoutManager = LinearLayoutManager(activity)
                    rvMyOrders.setHasFixedSize(true)
                    rvMyOrders.adapter = adapter
                    adapter.notifyItemChanged(orders)
                }else {
                    rvMyOrders.visibility = View.GONE
                    tvNoOrdersFound.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener { e ->
                hideProgressDialog()
                Log.e(javaClass.simpleName, "Error while getting the orders list.", e)
            }
    }
}
