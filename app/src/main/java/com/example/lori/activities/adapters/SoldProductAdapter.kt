package com.example.lori.activities.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.lori.R
import com.example.lori.models.SoldProduct
import com.example.lori.utils.FormatUtils
import com.example.lori.utils.ImageUtils
import kotlinx.android.synthetic.main.layout_sold_product.view.*

class SoldProductAdapter : ListAdapter<SoldProduct, RecyclerView.ViewHolder>(DiffCallback()) {
    var listener: Listener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_sold_product, parent, false), listener
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).displayData(getItem(position))
    }

    fun submitData(soldProducts: ArrayList<SoldProduct>) {
        submitList(soldProducts)
    }

    class ViewHolder(
        itemView: View,
        private val listener: Listener?
    ) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener {
                listener?.onClick(adapterPosition)
            }
            itemView.setOnLongClickListener {
                listener?.onLongClick(adapterPosition)
                true
            }
        }

        fun displayData(soldProduct: SoldProduct) {
            ImageUtils.loadProductImage(
                itemView.context,
                soldProduct.image,
                itemView.ivItemImage
            )
            itemView.tvItemTitle.text = soldProduct.title
            itemView.tvItemPrice.text = "${FormatUtils.format(num = soldProduct.price)} VND"
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<SoldProduct>() {
        override fun areItemsTheSame(oldItem: SoldProduct, newItem: SoldProduct) =
            oldItem === newItem

        override fun areContentsTheSame(oldItem: SoldProduct, newItem: SoldProduct) =
            oldItem.id == newItem.id
    }

    interface Listener {
        fun onClick(position: Int)
        fun onLongClick(position: Int)
    }
}
