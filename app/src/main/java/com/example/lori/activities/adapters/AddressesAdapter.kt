package com.example.lori.activities.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.lori.R
import com.example.lori.activities.AddEditAddressesActivity
import com.example.lori.activities.AddressesActivity
import com.example.lori.models.Address
import com.example.lori.utils.Constants
import com.example.lori.utils.SwipeToDeleteCallback
import com.example.lori.utils.SwipeToEditCallback
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.layout_address_item.view.*

class AddressesAdapter(
    private val context: Context,
    private var addresses: ArrayList<Address>,
    private val layout: Int,
    private val selectedAddress: Boolean = false
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(layout, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val address = addresses[position]

        // Prepare for multiple ViewHolder situations
        when (holder) {
            is ViewHolder -> {
                holder.itemView.tvAddressFullname.text = address.name
                holder.itemView.tvAddressType.text = address.type
                holder.itemView.tvAddressDetails.text = "${address.address} - ${address.zipCode}"
                holder.itemView.tvAddressMobileNumber.text = address.mobile.toString()
                holder.itemView.setOnClickListener {
                    if (!selectedAddress) {
                        Toast.makeText(
                            context,
                            "Swipe right to update the address \nSwipe left to delete the address",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            context,
                            "Selected address : ${address.address}, ${address.zipCode}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    override fun getItemCount() = addresses.size

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        if (!selectedAddress) {
            ItemTouchHelper(object : SwipeToEditCallback(context) {
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    updateAddress(viewHolder.adapterPosition)
                }
            }).attachToRecyclerView(recyclerView)

            ItemTouchHelper(object : SwipeToDeleteCallback(context) {
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    deleteAddress(viewHolder.adapterPosition)
                }
            }).attachToRecyclerView(recyclerView)
        }
    }

    /**
     * Update RecyclerView UI by DiffUtil instead of notifyDataSetChanged()
     */
    fun notifyItemChanged(addresses: ArrayList<Address>) {
        val diffResult = DiffUtil.calculateDiff(DiffUtilCallBack(this.addresses, addresses))
        this.addresses = addresses
        diffResult.dispatchUpdatesTo(this)
    }

    private fun updateAddress(position: Int) {
        when (context) {
            is AddressesActivity -> {
                val intent = Intent(context, AddEditAddressesActivity::class.java)
                intent.putExtra(Constants.EXTRA_ADDRESS_DETAILS, addresses[position])
                context.startActivity(intent)
            }
        }
    }

    private fun deleteAddress(position: Int) {
        when (context) {
            is AddressesActivity -> {
                AlertDialog.Builder(context)
                    .setTitle(R.string.title_delete_dialog)
                    .setMessage(R.string.label_delete_addresses_dialog)
                    .setIcon(R.drawable.ic_delete_red_24dp)
                    .setPositiveButton(context.resources.getString(R.string.label_yes)) { dialogInterface, _ ->
                        context.showProgressDialog(context.resources.getString(R.string.label_please_wait))

                        FirebaseFirestore.getInstance()
                            .collection(Constants.ADDRESSES)
                            .document(addresses[position].id)
                            .delete()
                            .addOnSuccessListener {
                                context.hideProgressDialog()
                                context.showSnackBar(
                                    context.resources.getString(R.string.success_to_delete_address),
                                    false
                                )

                                context.getAddresses()
                            }
                            .addOnFailureListener { e ->
                                context.hideProgressDialog()
                                context.showSnackBar(
                                    context.resources.getString(R.string.fail_to_delete_addresses),
                                    true
                                )

                                context.getAddresses()
                                Log.e(javaClass.simpleName, "Errors while deleting addresses", e)
                            }
                        dialogInterface.dismiss()
                    }
                    .setNegativeButton(context.resources.getString(R.string.label_no)) { dialogInterface, _ ->
                        context.getAddresses()
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
        private val oldList: ArrayList<Address>,
        private val newList: ArrayList<Address>,
    ) : DiffUtil.Callback() {

        override fun getOldListSize() = oldList.size

        override fun getNewListSize() = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            oldList[oldItemPosition].id == newList[newItemPosition].id

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            oldList[oldItemPosition] == newList[newItemPosition]
    }
}
