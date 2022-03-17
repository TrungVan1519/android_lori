package com.example.lori.activities.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lori.R
import com.example.lori.activities.AddProductsActivity
import com.example.lori.activities.adapters.MyProductsAdapter
import com.example.lori.models.Product
import com.example.lori.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.fragment_products.*

class ProductsFragment : BaseFragment() {

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
        rootView = inflater.inflate(R.layout.fragment_products, container, false)
        return rootView
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.products_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add_products -> {
                startActivity(Intent(activity, AddProductsActivity::class.java))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        getMyProducts()
    }

    fun getMyProducts() {
        showProgressDialog(resources.getString(R.string.label_please_wait))

        FirebaseFirestore.getInstance()
            .collection(Constants.PRODUCTS)
            .whereEqualTo(Constants.UID, FirebaseAuth.getInstance().currentUser!!.uid)
            .orderBy(Constants.TITLE, Query.Direction.ASCENDING)
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
                    rvMyProducts.visibility = View.VISIBLE
                    tvNoProductsFound.visibility = View.GONE

                    rvMyProducts.layoutManager = LinearLayoutManager(activity)
                    rvMyProducts.setHasFixedSize(true)
                    rvMyProducts.adapter =
                        MyProductsAdapter(this, products, R.layout.layout_my_products)
                } else {
                    rvMyProducts.visibility = View.GONE
                    tvNoProductsFound.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener { e ->
                hideProgressDialog()
                Log.e(javaClass.simpleName, "Errors while getting products", e)
            }
    }
}
