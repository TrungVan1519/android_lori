package com.example.lori.activities.adapters

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.lori.R
import com.example.lori.activities.CartItemActivity
import com.example.lori.activities.CheckoutActivity
import com.example.lori.models.CartItem
import com.example.lori.utils.Constants
import com.example.lori.utils.FormatUtils
import com.example.lori.utils.ImageUtils
import com.example.lori.utils.SwipeToDeleteCallback
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.layout_cart_item.view.*

open class CartItemAdapter(
    private val context: Context,
    private var cartItems: ArrayList<CartItem>,
    private var layout: Int
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(layout, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val cartItem = cartItems[position]

        // todo prepare for multiple ViewHolder situations
        when (holder) {
            is ViewHolder -> {
                ImageUtils.loadProductImage(context, cartItem.image, holder.itemView.ivItemImage)
                holder.itemView.tvItemTitle.text = cartItem.title
                holder.itemView.tvItemPrice.text = "${FormatUtils.format(num = cartItem.price)} VND"

                if (cartItem.cart_quantity == 0) {
                    holder.itemView.tvItemQuantity.text =
                        context.resources.getString(R.string.label_out_of_stock)
                    holder.itemView.tvItemQuantity.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.snack_bar_error
                        )
                    )
                    holder.itemView.ibDecreaseItemQuantity.visibility = View.GONE
                    holder.itemView.ibIncreaseItemQuantity.visibility = View.GONE
                } else {
                    holder.itemView.tvItemQuantity.text =
                        FormatUtils.format(num = cartItem.cart_quantity)
                    holder.itemView.tvItemQuantity.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.primary_text
                        )
                    )
                    holder.itemView.ibDecreaseItemQuantity.visibility = View.VISIBLE
                    holder.itemView.ibIncreaseItemQuantity.visibility = View.VISIBLE
                }

                when (context) {
                    is CartItemActivity -> {
                        holder.itemView.ibDecreaseItemQuantity.setOnClickListener {
                            decreaseCartItemQuantity(cartItem)
                        }
                        holder.itemView.ibIncreaseItemQuantity.setOnClickListener {
                            increaseCartItemQuantity(cartItem)
                        }
                        holder.itemView.ibDeleteItem.setOnClickListener {
                            deleteCartItem(cartItem.id)
                        }
                    }
                    is CheckoutActivity -> {
                        holder.itemView.ibDeleteItem.visibility = View.GONE
                        holder.itemView.ibIncreaseItemQuantity.visibility = View.GONE
                        holder.itemView.ibDecreaseItemQuantity.visibility = View.GONE
                    }
                }
            }
        }
    }

    override fun getItemCount() = cartItems.size

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        when (context) {
            is CartItemActivity -> {
                ItemTouchHelper(object : SwipeToDeleteCallback(context) {
                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                        deleteCartItem(cartItems[viewHolder.adapterPosition].id)
                    }
                }).attachToRecyclerView(recyclerView)
            }
        }
    }

    /**
     * Update RecyclerView UI by DiffUtil instead of notifyDataSetChanged()
     */
    fun notifyItemChanged(cartItems: ArrayList<CartItem>) {
        val diffResult = DiffUtil.calculateDiff(DiffUtilCallBack(this.cartItems, cartItems))
        this.cartItems = cartItems
        diffResult.dispatchUpdatesTo(this)
    }

    private fun decreaseCartItemQuantity(cartItem: CartItem) {
        when (context) {
            is CartItemActivity -> {
                if (cartItem.cart_quantity == 1) {
                    deleteCartItem(cartItem.id)
                } else {
                    updateCartItem(
                        cartItem.id,
                        hashMapOf(Constants.CART_QUANTITY to cartItem.cart_quantity - 1)
                    )
                }
            }
        }
    }

    @SuppressLint("StringFormatMatches")
    private fun increaseCartItemQuantity(cartItem: CartItem) {
        when (context) {
            is CartItemActivity -> {
                if (cartItem.cart_quantity < cartItem.stock_quantity) {
                    updateCartItem(
                        cartItem.id,
                        hashMapOf(Constants.CART_QUANTITY to cartItem.cart_quantity + 1)
                    )
                } else {
                    context.showSnackBar(
                        context.resources.getString(
                            R.string.err_msg_available_stock,
                            cartItem.stock_quantity
                        ),
                        true
                    )
                }
            }
        }
    }

    private fun updateCartItem(id: String, itemHashMap: HashMap<String, Any>) {
        when (context) {
            is CartItemActivity -> {
                context.showProgressDialog(context.resources.getString(R.string.label_please_wait))

                FirebaseFirestore.getInstance()
                    .collection(Constants.CART_ITEMS)
                    .document(id)
                    .update(itemHashMap)
                    .addOnSuccessListener {
                        context.hideProgressDialog()
                        context.getCartItems()
                    }
                    .addOnFailureListener { e ->
                        context.hideProgressDialog()
                        context.showSnackBar(
                            context.resources.getString(R.string.fail_to_delete_cart_items),
                            true
                        )

                        Log.e(
                            context.javaClass.simpleName,
                            "Error while updating cart items",
                            e
                        )
                    }
            }
        }
    }

    private fun deleteCartItem(id: String) {
        when (context) {
            is CartItemActivity -> {
                AlertDialog.Builder(context)
                    .setTitle(R.string.title_delete_dialog)
                    .setMessage(R.string.label_delete_dialog)
                    .setIcon(R.drawable.ic_delete_red_24dp)
                    .setPositiveButton(context.resources.getString(R.string.label_yes)) { dialogInterface, _ ->
                        context.showProgressDialog(context.resources.getString(R.string.label_please_wait))

                        FirebaseFirestore.getInstance()
                            .collection(Constants.CART_ITEMS)
                            .document(id)
                            .delete()
                            .addOnSuccessListener {
                                context.hideProgressDialog()
                                context.showSnackBar(
                                    context.resources.getString(R.string.success_to_delete_cart_items),
                                    false
                                )

                                context.getCartItems()
                            }
                            .addOnFailureListener { e ->
                                context.hideProgressDialog()
                                context.showSnackBar(
                                    context.resources.getString(R.string.fail_to_delete_cart_items),
                                    true
                                )

                                context.getCartItems()
                                Log.e(javaClass.simpleName, "Errors while deleting cart items", e)
                            }
                        dialogInterface.dismiss()
                    }
                    .setNegativeButton(context.resources.getString(R.string.label_no)) { dialogInterface, _ ->
                        context.getCartItems()
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
        private val oldList: ArrayList<CartItem>,
        private val newList: ArrayList<CartItem>,
    ) : DiffUtil.Callback() {

        override fun getOldListSize() = oldList.size

        override fun getNewListSize() = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            oldList[oldItemPosition].id == newList[newItemPosition].id

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            oldList[oldItemPosition] == newList[newItemPosition]
    }
}
