package com.example.lori.activities.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.lori.activities.ProductDetailsActivity
import com.example.lori.models.Product
import com.example.lori.utils.Constants
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
                holder.itemView.setOnClickListener { getProduct(product) }
            }
        }
    }

    override fun getItemCount() = products.size

    private fun getProduct(product: Product) {
        val intent = Intent(context, ProductDetailsActivity::class.java)
        intent.putExtra(Constants.EXTRA_PRODUCT_ID, product.id)
        intent.putExtra(Constants.EXTRA_PRODUCT_OWNER_ID, product.uid)
        context.startActivity(intent)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
}
