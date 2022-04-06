package com.example.lori.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lori.R
import com.example.lori.activities.adapters.AddressesAdapter
import com.example.lori.models.Address
import com.example.lori.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_addresses.*

class AddressesActivity : BaseActivity(), View.OnClickListener {
    private var selectedAddress = false
    private lateinit var adapter: AddressesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_addresses)

        selectedAddress = intent.getBooleanExtra(Constants.EXTRA_SELECT_ADDRESS, false)
        if (selectedAddress) {
            tvTitle.text = resources.getString(R.string.title_select_address)
        }

        adapter = AddressesAdapter(this, arrayListOf(), R.layout.layout_address_item, selectedAddress)

        btAddAddresses.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        getAllAddresses()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btAddAddresses -> {
                startActivity(Intent(this, AddEditAddressesActivity::class.java))
            }
        }
    }

    fun getAllAddresses() {
        showProgressDialog(resources.getString(R.string.label_please_wait))

        FirebaseFirestore.getInstance()
            .collection(Constants.ADDRESSES)
            .whereEqualTo(Constants.UID, FirebaseAuth.getInstance().currentUser!!.uid)
            .orderBy(Constants.ADDRESS, Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { query ->
                hideProgressDialog()

                val addresses = ArrayList<Address>()
                query.documents.forEach { doc ->
                    val address = doc.toObject(Address::class.java)!!
                    address.id = doc.id
                    addresses.add(address)
                }

                if (addresses.size > 0) {
                    rvAddresses.visibility = View.VISIBLE
                    tvNoAddressesFound.visibility = View.GONE

                    rvAddresses.layoutManager = LinearLayoutManager(this)
                    rvAddresses.setHasFixedSize(true)
                    rvAddresses.adapter = adapter // force redraw RecyclerView items
                    adapter.notifyItemChanged(addresses)
                } else {
                    rvAddresses.visibility = View.GONE
                    tvNoAddressesFound.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener { e ->
                hideProgressDialog()
                Log.e(javaClass.simpleName, "Errors while getting all addresses", e)
            }
    }
}
