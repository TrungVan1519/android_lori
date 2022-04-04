package com.example.lori.activities.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.lori.R
import com.example.lori.models.FavProduct
import kotlinx.android.synthetic.main.layout_my_fav_products.view.*

class FavProductAdapter : ListAdapter<FavProduct, FavProductAdapter.ViewHolder>(DiffCallback()) {
    var listener: Listener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_my_fav_products, parent, false),
            listener
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.displayData(getItem(position))
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

        fun displayData(favProduct: FavProduct) {
            Glide.with(itemView.context).load(favProduct.image).centerCrop().into(itemView.ivItemImage)
            itemView.tvItemTitle.text = favProduct.title
            itemView.tvItemPrice.text = favProduct.price.toString()
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<FavProduct>() {
        override fun areItemsTheSame(oldItem: FavProduct, newItem: FavProduct): Boolean =
            oldItem === newItem

        override fun areContentsTheSame(oldItem: FavProduct, newItem: FavProduct): Boolean =
            oldItem.id == newItem.id
    }

    interface Listener {
        fun onClick(position: Int)
        fun onLongClick(position: Int)
    }
}
