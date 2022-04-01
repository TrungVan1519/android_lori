package com.example.lori.activities.adapters

import android.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.lori.R
import com.example.lori.activities.ui.OrdersFragment
import com.example.lori.models.Order
import com.example.lori.utils.Constants
import com.example.lori.utils.FormatUtils
import com.example.lori.utils.ImageUtils
import com.example.lori.utils.SwipeToDeleteCallback
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.layout_order_item.view.*

class MyOrderItemsAdapter(
    private val fragment: Fragment,
    private var orderItems: ArrayList<Order>,
    private var layout: Int
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(layout, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val orderItem = orderItems[position]

        // Prepare for multiple ViewHolder situations
        when (holder) {
            is ViewHolder -> {
                ImageUtils.loadProductImage(
                    fragment.requireActivity(),
                    orderItem.image,
                    holder.itemView.ivItemImage
                )
                holder.itemView.tvItemTitle.text = orderItem.title
                holder.itemView.tvItemPrice.text = "${FormatUtils.format(num = orderItem.totalAmount)} VND"
                holder.itemView.ibDeleteItem.setOnClickListener { deleteOrderItem(orderItem.id) }
                holder.itemView.setOnClickListener { getOrderItem(orderItem) }
            }
        }
    }

    override fun getItemCount() = orderItems.size

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        ItemTouchHelper(object : SwipeToDeleteCallback(fragment.requireActivity()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                deleteOrderItem(orderItems[viewHolder.adapterPosition].id)
            }
        }).attachToRecyclerView(recyclerView)
    }

    /**
     * Update RecyclerView UI by DiffUtil instead of notifyDataSetChanged()
     */
    fun notifyItemChanged(orderItems: ArrayList<Order>) {
        val diffResult = DiffUtil.calculateDiff(DiffUtilCallBack(this.orderItems, orderItems))
        this.orderItems = orderItems
        diffResult.dispatchUpdatesTo(this)
    }

    private fun getOrderItem(orderItem: Order) {

    }

    private fun deleteOrderItem(id: String) {
        when (fragment) {
            is OrdersFragment -> {
                AlertDialog.Builder(fragment.requireActivity())
                    .setTitle(R.string.title_delete_dialog)
                    .setMessage(R.string.label_delete_dialog)
                    .setIcon(R.drawable.ic_delete_red_24dp)
                    .setPositiveButton(fragment.resources.getString(R.string.label_yes)) { dialogInterface, _ ->
                        fragment.showProgressDialog(fragment.resources.getString(R.string.label_please_wait))

                        FirebaseFirestore.getInstance()
                            .collection(Constants.ORDERS)
                            .document(id)
                            .delete()
                            .addOnSuccessListener {
                                fragment.hideProgressDialog()
                                fragment.showSnackBar(
                                    fragment.resources.getString(R.string.success_to_delete_order_items),
                                    false
                                )

                                fragment.getMyOrders()
                            }
                            .addOnFailureListener { e ->
                                fragment.hideProgressDialog()
                                fragment.showSnackBar(
                                    fragment.resources.getString(R.string.fail_to_delete_order_items),
                                    true
                                )

                                fragment.getMyOrders()
                                Log.e(
                                    fragment.requireActivity().javaClass.simpleName,
                                    "Error while deleting the order item.",
                                    e
                                )
                            }
                        dialogInterface.dismiss()
                    }
                    .setNegativeButton(fragment.resources.getString(R.string.label_no)) { dialogInterface, _ ->
                        fragment.getMyOrders()
                        dialogInterface.dismiss()
                    }
                    .setCancelable(false)
                    .create()
                    .show()
            }
        }
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
