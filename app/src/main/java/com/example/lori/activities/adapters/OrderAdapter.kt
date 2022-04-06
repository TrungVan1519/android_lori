package com.example.lori.activities.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.lori.activities.OrderActivity
import com.example.lori.models.Order
import com.example.lori.utils.Constants
import com.example.lori.utils.FormatUtils
import com.example.lori.utils.ImageUtils
import kotlinx.android.synthetic.main.layout_order.view.*

class OrderAdapter(
    private val fragment: Fragment,
    private var orders: ArrayList<Order>,
    private var layout: Int
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(layout, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val order = orders[position]

        // todo prepare for multiple ViewHolder situations
        when (holder) {
            is ViewHolder -> {
                ImageUtils.loadProductImage(
                    fragment.requireActivity(),
                    order.image,
                    holder.itemView.ivItemImage
                )
                holder.itemView.tvItemTitle.text = order.title
                holder.itemView.tvItemPrice.text = "${FormatUtils.format(num = order.totalAmount)} VND"
                holder.itemView.setOnClickListener { getOrder(order) }
            }
        }
    }

    override fun getItemCount() = orders.size

    /**
     * Update RecyclerView UI by DiffUtil instead of notifyDataSetChanged()
     */
    fun notifyItemChanged(orders: ArrayList<Order>) {
        val diffResult = DiffUtil.calculateDiff(DiffUtilCallBack(this.orders, orders))
        this.orders = orders
        diffResult.dispatchUpdatesTo(this)
    }

    private fun getOrder(order: Order) {
        val intent = Intent(fragment.requireActivity(), OrderActivity::class.java)
        intent.putExtra(Constants.EXTRA_MY_ORDER_DETAILS, order)
        fragment.requireActivity().startActivity(intent)
    }

    private class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    class DiffUtilCallBack(
        private val oldList: ArrayList<Order>,
        private val newList: ArrayList<Order>,
    ) : DiffUtil.Callback() {
        override fun getOldListSize() = oldList.size

        override fun getNewListSize() = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            oldList[oldItemPosition].id == newList[newItemPosition].id

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            oldList[oldItemPosition] == newList[newItemPosition]
    }
}
