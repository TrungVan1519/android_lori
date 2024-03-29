package com.example.lori.activities

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lori.R
import com.example.lori.activities.adapters.FavProductAdapter
import com.example.lori.models.FavProduct
import com.example.lori.utils.Constants
import com.example.lori.utils.SwipeToDeleteCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_fav_product.*

class FavProductActivity : BaseActivity() {
    val adapter = FavProductAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fav_product)

        getAllFavProducts()

        adapter.listener = object : FavProductAdapter.Listener {
            override fun onClick(position: Int) {
                val intent = Intent(this@FavProductActivity, ProductActivity::class.java)
                intent.putExtra(Constants.EXTRA_PRODUCT_ID, adapter.currentList[position].pid)
                intent.putExtra(Constants.EXTRA_PRODUCT_OWNER_ID, adapter.currentList[position].product_owner_id)
                startActivity(intent)
            }

            override fun onLongClick(position: Int) {
            }
        }

        ItemTouchHelper(object : SwipeToDeleteCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // todo delete fav products from Firebase
                AlertDialog.Builder(this@FavProductActivity)
                    .setTitle(R.string.title_delete_dialog)
                    .setMessage(R.string.label_delete_dialog)
                    .setIcon(R.drawable.ic_delete_red_24dp)
                    .setPositiveButton(resources.getString(R.string.label_yes)) { dialogInterface, _ ->
                        showProgressDialog(resources.getString(R.string.label_please_wait))

                        FirebaseFirestore.getInstance()
                            .collection(Constants.FAV_PRODUCTS)
                            .document(adapter.currentList[viewHolder.adapterPosition].id)
                            .delete()
                            .addOnSuccessListener {
                                hideProgressDialog()

                                // todo delete fav products from RecyclerView
                                val list = adapter.currentList.toMutableList()
                                list.removeAt(viewHolder.adapterPosition)
                                adapter.submitList(list)
                            }
                            .addOnFailureListener { e ->
                                hideProgressDialog()
                                showSnackBar(
                                    resources.getString(R.string.fail_to_delete_cart_items),
                                    true
                                )
                                Log.e(javaClass.simpleName, "Errors while deleting fav products", e)
                            }
                        dialogInterface.dismiss()
                    }
                    .setNegativeButton(resources.getString(R.string.label_no)) { dialogInterface, _ ->
                        rvFavProducts.adapter = adapter
                        dialogInterface.dismiss()
                    }
                    .setCancelable(false)
                    .create()
                    .show()
            }
        }).attachToRecyclerView(rvFavProducts)

        rvFavProducts.adapter = adapter
        rvFavProducts.setHasFixedSize(true)
        rvFavProducts.layoutManager = LinearLayoutManager(this)
    }

    private fun getAllFavProducts() {
        showProgressDialog(resources.getString(R.string.label_please_wait))

        FirebaseFirestore.getInstance()
            .collection(Constants.FAV_PRODUCTS)
            .whereEqualTo(Constants.UID, FirebaseAuth.getInstance().currentUser!!.uid)
            .orderBy(Constants.TITLE, Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { query ->
                hideProgressDialog()

                val favProducts = ArrayList<FavProduct>()
                query.documents.forEach { doc ->
                    val favProduct = doc.toObject(FavProduct::class.java)!!
                    favProduct.id = doc.id
                    favProducts.add(favProduct)
                }

                if (favProducts.size > 0) {
                    rvFavProducts.visibility = View.VISIBLE
                    tvNoFavProducts.visibility = View.GONE
                    adapter.submitList(favProducts)
                } else {
                    rvFavProducts.visibility = View.GONE
                    tvNoFavProducts.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener { e ->
                hideProgressDialog()
                Log.e(javaClass.simpleName, "Errors while getting all fav products", e)
            }
    }
}
