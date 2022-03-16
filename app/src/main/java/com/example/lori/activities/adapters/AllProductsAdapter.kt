package com.example.lori.activities.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.lori.models.Product
import com.example.lori.utils.ImageUtils
import com.example.lori.utils.SwipeToDeleteCallback
import com.example.lori.utils.SwipeToEditCallback
import kotlinx.android.synthetic.main.layout_all_products.view.*

class AllProductsAdapter(
    private val context: Context,
    private val products: ArrayList<Product>,
    private val layout: Int
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(layout, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val product = products[position]

        // Prepare for multiple ViewHolder situations
        when (holder) {
            is ViewHolder -> {
                ImageUtils.loadProductImage(context, product.image, holder.itemView.ivItemImage)
                holder.itemView.tvItemTitle.text = product.title
                holder.itemView.tvItemPrice.text = "${product.price} VND"
                holder.itemView.setOnClickListener { getProduct(position) }
            }
        }
    }

    override fun getItemCount() = products.size


    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        ItemTouchHelper(object : SwipeToEditCallback(context) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                updateProduct(viewHolder.adapterPosition)
            }
        }).attachToRecyclerView(recyclerView)

        ItemTouchHelper(object : SwipeToDeleteCallback(context) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                deleteProduct(viewHolder.adapterPosition)
            }
        }).attachToRecyclerView(recyclerView)
    }

    private fun getProduct(position: Int) {

    }

    private fun updateProduct(position: Int) {

    }

    private fun deleteProduct(position: Int) {

    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
}
