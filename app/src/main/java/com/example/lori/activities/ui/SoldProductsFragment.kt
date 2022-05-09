package com.example.lori.activities.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lori.R
import com.example.lori.activities.SoldProductActivity
import com.example.lori.activities.adapters.SoldProductAdapter
import com.example.lori.models.SoldProduct
import com.example.lori.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_sold_products.*

class SoldProductsFragment : BaseFragment() {
    private lateinit var adapter: SoldProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        adapter = SoldProductAdapter()
        adapter.listener = object : SoldProductAdapter.Listener {
            override fun onClick(position: Int) {
                val intent = Intent(requireActivity(), SoldProductActivity::class.java)
                intent.putExtra(Constants.EXTRA_SOLD_PRODUCT_DETAILS, adapter.currentList[position])
                startActivity(intent)
            }

            override fun onLongClick(position: Int) {
            }
        }
        return inflater.inflate(R.layout.fragment_sold_products, container, false)
    }

    override fun onResume() {
        super.onResume()
        getAllSoldProducts()
    }

    private fun getAllSoldProducts() {
        showProgressDialog(resources.getString(R.string.label_please_wait))

        FirebaseFirestore.getInstance()
            .collection(Constants.SOLD_PRODUCTS)
            .whereEqualTo(Constants.PRODUCT_OWNER_ID, FirebaseAuth.getInstance().currentUser!!.uid)
            .get()
            .addOnSuccessListener { query ->
                hideProgressDialog()

                val soldProducts = ArrayList<SoldProduct>()
                query.documents.forEach { doc ->
                    val soldProduct = doc.toObject(SoldProduct::class.java)!!
                    soldProduct.id = doc.id
                    soldProducts.add(soldProduct)
                }

                if (soldProducts.size > 0) {
                    rvSoldProducts.visibility = View.VISIBLE
                    tvNoSoldProductsFound.visibility = View.GONE

                    rvSoldProducts.layoutManager = LinearLayoutManager(activity)
                    rvSoldProducts.setHasFixedSize(true)
                    rvSoldProducts.adapter = adapter
                    adapter.submitData(soldProducts)
                } else {
                    rvSoldProducts.visibility = View.GONE
                    tvNoSoldProductsFound.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener { e ->
                hideProgressDialog()
                Log.e(javaClass.simpleName, "Errors while get all sold products", e)
            }
    }
}
