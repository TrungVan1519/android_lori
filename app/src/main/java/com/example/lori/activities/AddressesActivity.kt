package com.example.lori.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lori.R
import com.example.lori.activities.adapters.AddressesAdapter
import com.example.lori.models.Address
import com.example.lori.utils.Constants
import com.example.lori.utils.SwipeToDeleteCallback
import com.example.lori.utils.SwipeToEditCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_addresses.*

class AddressesActivity : BaseActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_addresses)

        btAddAddresses.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        getAddresses()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btAddAddresses -> startActivity(
                Intent(
                    this,
                    AddEditAddressesActivity::class.java
                )
            )
        }
    }

    fun getAddresses() {
        showProgressDialog(resources.getString(R.string.label_please_wait))

        FirebaseFirestore.getInstance()
            .collection(Constants.ADDRESSES)
            .whereEqualTo(Constants.UID, FirebaseAuth.getInstance().currentUser!!.uid)
            .orderBy(Constants.ADDRESS, Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                hideProgressDialog()

                val addresses = ArrayList<Address>()
                querySnapshot.documents.forEach { documentSnapshot ->
                    val address = documentSnapshot.toObject(Address::class.java)
                    address!!.id = documentSnapshot.id
                    addresses.add(address)
                }

                if (addresses.size > 0) {
                    rvAddresses.visibility = View.VISIBLE
                    tvNoAddressesFound.visibility = View.GONE

                    rvAddresses.layoutManager = LinearLayoutManager(this)
                    rvAddresses.setHasFixedSize(true)
                    rvAddresses.adapter =
                        AddressesAdapter(this, addresses, R.layout.layout_address_item)
                } else {
                    rvAddresses.visibility = View.GONE
                    tvNoAddressesFound.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener { e ->
                hideProgressDialog()
                Log.e(javaClass.simpleName, "Errors while getting addresses", e)
            }
    }
}
