package com.example.lori.activities.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lori.R
import com.example.lori.activities.ModifyProductActivity
import com.example.lori.activities.FavProductActivity
import com.example.lori.activities.adapters.MyProductAdapter
import com.example.lori.models.Product
import com.example.lori.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.fragment_products.*

class ProductsFragment : BaseFragment() {
    private lateinit var adapter: MyProductAdapter

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
        adapter = MyProductAdapter(this, arrayListOf(), R.layout.layout_my_product)
        return inflater.inflate(R.layout.fragment_products, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.products_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_list_fav_products -> {
                startActivity(Intent(activity, FavProductActivity::class.java))
                return true
            }
            R.id.action_add_products -> {
                startActivity(Intent(activity, ModifyProductActivity::class.java))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        getMyAllProducts()
    }

    fun getMyAllProducts() {
        showProgressDialog(resources.getString(R.string.label_please_wait))

        FirebaseFirestore.getInstance()
            .collection(Constants.PRODUCTS)
            .whereEqualTo(Constants.UID, FirebaseAuth.getInstance().currentUser!!.uid)
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
                    rvMyAllProducts.visibility = View.VISIBLE
                    tvNoProductsFound.visibility = View.GONE

                    rvMyAllProducts.layoutManager = LinearLayoutManager(activity)
                    rvMyAllProducts.setHasFixedSize(true)
                    rvMyAllProducts.adapter = adapter
                    adapter.notifyItemChanged(products)
                } else {
                    rvMyAllProducts.visibility = View.GONE
                    tvNoProductsFound.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener { e ->
                hideProgressDialog()
                Log.e(javaClass.simpleName, "Errors while getting my all products", e)
            }
    }
}
