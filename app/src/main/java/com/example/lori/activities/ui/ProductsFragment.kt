package com.example.lori.activities.ui

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.lori.R
import com.example.lori.activities.AddProductsActivity
import com.example.lori.activities.SettingsActivity

class ProductsFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // For using the option menu in fragment we need to add it
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_products, container, false)
        val textView: TextView = root.findViewById(R.id.text_home)
        textView.text = "2"
        return root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.products_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add_products -> {
                startActivity(Intent(activity, AddProductsActivity::class.java))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
