package com.example.lori.activities.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lori.R
import com.example.lori.activities.adapters.OrdersAdapter
import com.example.lori.models.Order
import com.example.lori.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_orders.*

class OrdersFragment : BaseFragment() {
    private lateinit var adapter: OrdersAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        adapter = OrdersAdapter(this, arrayListOf(), R.layout.layout_orders)
        return inflater.inflate(R.layout.fragment_orders, container, false)
    }

    override fun onResume() {
        super.onResume()
        getAllOrders()
    }

     private fun getAllOrders() {
        showProgressDialog(resources.getString(R.string.label_please_wait))

        FirebaseFirestore.getInstance()
            .collection(Constants.ORDERS)
            .whereEqualTo(Constants.UID, FirebaseAuth.getInstance().currentUser!!.uid)
            .get()
            .addOnSuccessListener { query ->
                hideProgressDialog()

                val orders = ArrayList<Order>()
                query.documents.forEach { doc ->
                    val order = doc.toObject(Order::class.java)!!
                    order.id = doc.id
                    orders.add(order)
                }

                if (orders.size > 0) {
                    rvAllOrders.visibility = View.VISIBLE
                    tvNoOrdersFound.visibility = View.GONE

                    rvAllOrders.layoutManager = LinearLayoutManager(activity)
                    rvAllOrders.setHasFixedSize(true)
                    rvAllOrders.adapter = adapter
                    adapter.notifyItemChanged(orders)
                }else {
                    rvAllOrders.visibility = View.GONE
                    tvNoOrdersFound.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener { e ->
                hideProgressDialog()
                Log.e(javaClass.simpleName, "Errors while getting all orders", e)
            }
    }
}
