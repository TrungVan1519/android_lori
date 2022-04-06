package com.example.lori.activities.adapters

import android.app.AlertDialog
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.lori.R
import com.example.lori.activities.AddEditProductsActivity
import com.example.lori.activities.ProductDetailsActivity
import com.example.lori.activities.ui.ProductsFragment
import com.example.lori.models.Product
import com.example.lori.utils.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.layout_my_products.view.*

class MyProductsAdapter(
    private val fragment: Fragment,
    private var products: ArrayList<Product>,
    private val layout: Int
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(layout, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val product = products[position]

        // todo prepare for multiple ViewHolder situations
        when (holder) {
            is ViewHolder -> {
                ImageUtils.loadProductImage(
                    fragment.requireActivity(),
                    product.image,
                    holder.itemView.ivItemImage
                )
                holder.itemView.tvItemTitle.text = product.title
                holder.itemView.tvItemPrice.text = "${FormatUtils.format(num = product.price)} VND"
                holder.itemView.ibDeleteItem.setOnClickListener { deleteProduct(product.id) }
                holder.itemView.setOnClickListener { getProduct(product) }
            }
        }
    }

    override fun getItemCount() = products.size

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        ItemTouchHelper(object : SwipeToEditCallback(fragment.requireActivity()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                updateProduct(viewHolder.adapterPosition)
            }
        }).attachToRecyclerView(recyclerView)

        ItemTouchHelper(object : SwipeToDeleteCallback(fragment.requireActivity()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                deleteProduct(products[viewHolder.adapterPosition].id)
            }
        }).attachToRecyclerView(recyclerView)
    }

    /**
     * Update RecyclerView UI by DiffUtil instead of notifyDataSetChanged()
     */
    fun notifyItemChanged(products: ArrayList<Product>) {
        val diffResult = DiffUtil.calculateDiff(DiffUtilCallBack(this.products, products))
        this.products = products
        diffResult.dispatchUpdatesTo(this)
    }

    private fun getProduct(product: Product) {
        val intent = Intent(fragment.requireActivity(), ProductDetailsActivity::class.java)
        intent.putExtra(Constants.EXTRA_PRODUCT_ID, product.id)
        intent.putExtra(Constants.EXTRA_PRODUCT_OWNER_ID, product.uid)
        fragment.startActivity(intent)
    }

    private fun updateProduct(position: Int) {
        when (fragment) {
            is ProductsFragment -> {
                val intent = Intent(fragment.requireActivity(), AddEditProductsActivity::class.java)
                intent.putExtra(Constants.EXTRA_PRODUCT_DETAILS, products[position])
                fragment.startActivity(intent)
            }
        }
    }

    private fun deleteProduct(id: String) {
        when (fragment) {
            is ProductsFragment -> {
                AlertDialog.Builder(fragment.requireActivity())
                    .setTitle(R.string.title_delete_dialog)
                    .setMessage(R.string.label_delete_dialog)
                    .setIcon(R.drawable.ic_delete_red_24dp)
                    .setPositiveButton(fragment.resources.getString(R.string.label_yes)) { dialogInterface, _ ->
                        fragment.showProgressDialog(fragment.resources.getString(R.string.label_please_wait))

                        FirebaseFirestore.getInstance()
                            .collection(Constants.PRODUCTS)
                            .document(id)
                            .delete()
                            .addOnSuccessListener {
                                fragment.hideProgressDialog()
                                fragment.showSnackBar(
                                    fragment.resources.getString(R.string.success_to_delete_products),
                                    false
                                )

                                fragment.getMyAllProducts()
                            }
                            .addOnFailureListener { e ->
                                fragment.hideProgressDialog()
                                fragment.showSnackBar(
                                    fragment.resources.getString(R.string.fail_to_delete_products),
                                    true
                                )

                                fragment.getMyAllProducts()
                                Log.e(
                                    fragment.requireActivity().javaClass.simpleName,
                                    "Error while deleting products",
                                    e
                                )
                            }
                        dialogInterface.dismiss()
                    }
                    .setNegativeButton(fragment.resources.getString(R.string.label_no)) { dialogInterface, _ ->
                        fragment.getMyAllProducts()
                        dialogInterface.dismiss()
                    }
                    .setCancelable(false)
                    .create()
                    .show()
            }
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    class DiffUtilCallBack(
        private val oldList: ArrayList<Product>,
        private val newList: ArrayList<Product>,
    ) : DiffUtil.Callback() {
        override fun getOldListSize() = oldList.size

        override fun getNewListSize() = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            oldList[oldItemPosition].id == newList[newItemPosition].id

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            oldList[oldItemPosition] == newList[newItemPosition]
    }
}
