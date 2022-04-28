package com.example.lori.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lori.R
import com.example.lori.activities.adapters.TermsAndConditionAdapter
import com.example.lori.models.TermsAndCondition
import kotlinx.android.synthetic.main.activity_terms_and_condition.*

class TermsAndConditionActivity : AppCompatActivity() {
    private lateinit var adapter: TermsAndConditionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms_and_condition)

        adapter = TermsAndConditionAdapter()
        adapter.submitList(TermsAndCondition.createTermsAndCondition())

        rvTermsAndCondition.adapter = adapter
        rvTermsAndCondition.setHasFixedSize(true)
        rvTermsAndCondition.layoutManager = LinearLayoutManager(this)
    }
}
