package com.example.lori.activities.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.recyclerview.widget.GridLayoutManager
import com.example.lori.R
import com.example.lori.activities.SettingsActivity
import com.example.lori.activities.adapters.AllProductsAdapter
import com.example.lori.models.Product
import com.example.lori.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_dashboard.*
import kotlinx.android.synthetic.main.fragment_products.tvNoProductsFound

class DashboardFragment : BaseFragment() {

    private lateinit var rootView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // For using the option menu in fragment we need to add it
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootView = inflater.inflate(R.layout.fragment_dashboard, container, false)
        return rootView
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.dashboard_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(activity, SettingsActivity::class.java))
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
            .get()
            .addOnSuccessListener { querySnapshot ->
                hideProgressDialog()

                val products = ArrayList<Product>()
                querySnapshot.documents.forEach { documentSnapshot ->
                    val product = documentSnapshot.toObject(Product::class.java)!!
                    product.id = documentSnapshot.id
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
                Log.e(javaClass.simpleName, "Errors while getting products", e)
            }
    }
}
