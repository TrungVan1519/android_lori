package com.example.lori.activities.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.recyclerview.widget.GridLayoutManager
import com.example.lori.R
import com.example.lori.activities.CartItemActivity
import com.example.lori.activities.SettingActivity
import com.example.lori.activities.adapters.AllProductsAdapter
import com.example.lori.models.Product
import com.example.lori.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.fragment_dashboard.*

class DashboardFragment : BaseFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // todo use the option menu in fragment
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.dashboard_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(activity, SettingActivity::class.java))
                return true
            }
            R.id.action_cart -> {
                startActivity(Intent(activity, CartItemActivity::class.java))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        getAllProducts()
    }

    private fun getAllProducts() {
        showProgressDialog(resources.getString(R.string.label_please_wait))

        FirebaseFirestore.getInstance()
            .collection(Constants.PRODUCTS)
            .orderBy(Constants.TITLE, Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { query ->
                hideProgressDialog()

                val products = ArrayList<Product>()
                query.documents.forEach { doc ->
                    val product = doc.toObject(Product::class.java)!!
                    product.id = doc.id
                    products.add(product)
                }

                if (products.size > 0) {
                    rvAllProducts.visibility = View.VISIBLE
                    tvNoProductsFound.visibility = View.GONE

                    rvAllProducts.layoutManager = GridLayoutManager(activity, 2)
                    rvAllProducts.setHasFixedSize(true)
                    rvAllProducts.adapter =
                        AllProductsAdapter(requireActivity(), products, R.layout.layout_all_products)
                } else {
                    rvAllProducts.visibility = View.GONE
                    tvNoProductsFound.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener { e ->
                hideProgressDialog()
                Log.e(javaClass.simpleName, "Errors while getting all products", e)
            }
    }
}
