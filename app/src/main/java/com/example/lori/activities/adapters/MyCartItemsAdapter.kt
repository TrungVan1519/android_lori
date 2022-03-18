package com.example.lori.activities.adapters

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.lori.R
import com.example.lori.activities.CartActivity
import com.example.lori.activities.ui.ProductsFragment
import com.example.lori.models.CartItem
import com.example.lori.utils.Constants
import com.example.lori.utils.ImageUtils
import com.example.lori.utils.SwipeToDeleteCallback
import com.example.lori.utils.SwipeToEditCallback
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.layout_cart_item.view.*

open class MyCartItemsAdapter(
    private val context: Context,
    private var cartItems: ArrayList<CartItem>,
    private var layout: Int
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(layout, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val cartItem = cartItems[position]

        // Prepare for multiple ViewHolder situations
        when (holder) {
            is ViewHolder -> {
                ImageUtils.loadProductImage(context, cartItem.image, holder.itemView.ivItemImage)
                holder.itemView.tvItemTitle.text = cartItem.title
                holder.itemView.tvItemPrice.text = "${cartItem.price} VND"
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
                    holder.itemView.tvItemQuantity.text = cartItem.cart_quantity.toString()
                    holder.itemView.tvItemQuantity.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.primary_text
                        )
                    )
                    holder.itemView.ibDecreaseItemQuantity.visibility = View.VISIBLE
                    holder.itemView.ibIncreaseItemQuantity.visibility = View.VISIBLE
                }

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
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        ItemTouchHelper(object : SwipeToDeleteCallback(context) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                deleteCartItem(cartItems[viewHolder.adapterPosition].id)
            }
        }).attachToRecyclerView(recyclerView)
    }

    override fun getItemCount() = cartItems.size

    private fun decreaseCartItemQuantity(cartItem: CartItem) {
        if (cartItem.cart_quantity == 1) {
            deleteCartItem(cartItem.id)
        } else {
            updateCartItem(
                cartItem.id,
                hashMapOf(Constants.CART_QUANTITY to cartItem.cart_quantity - 1)
            )
        }
    }

    @SuppressLint("StringFormatMatches")
    private fun increaseCartItemQuantity(cartItem: CartItem) {
        if (cartItem.cart_quantity < cartItem.stock_quantity) {
            updateCartItem(
                cartItem.id,
                hashMapOf(Constants.CART_QUANTITY to cartItem.cart_quantity + 1)
            )
        } else {
            if (context is CartActivity) {
                context.showSnackBar(
                    context.resources.getString(
                        R.string.msg_for_available_stock,
                        cartItem.stock_quantity
                    ),
                    true
                )
            }
        }
    }

    private fun updateCartItem(id: String, itemHashMap: HashMap<String, Any>) {
        when (context) {
            is CartActivity -> {
                context.showProgressDialog(context.resources.getString(R.string.label_please_wait))

                FirebaseFirestore.getInstance()
                    .collection(Constants.CART_ITEMS)
                    .document(id)
                    .update(itemHashMap)
                    .addOnSuccessListener {
                        context.hideProgressDialog()
                        context.getMyCartItems()
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
            is CartActivity -> {
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
                                context.getMyCartItems()
                            }
                            .addOnFailureListener { e ->
                                context.hideProgressDialog()
                                context.showSnackBar(
                                    context.resources.getString(R.string.fail_to_delete_cart_items),
                                    true
                                )

                                Log.e(javaClass.simpleName, "Errors while deleting cart items", e)
                            }
                        dialogInterface.dismiss()
                    }
                    .setNegativeButton(context.resources.getString(R.string.label_no)) { dialogInterface, _ ->
                        context.getMyCartItems()
                        dialogInterface.dismiss()
                    }
                    .setCancelable(false)
                    .create()
                    .show()
            }
        }
    }

    private class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
}
