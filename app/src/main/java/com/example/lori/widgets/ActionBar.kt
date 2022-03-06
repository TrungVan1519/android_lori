package com.example.lori.widgets

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.lori.R

class ActionBar(context: Context, attrs: AttributeSet) : Toolbar(context, attrs) {

    init {
        applyStyles()
    }

    private fun applyStyles() {
        val activity = context as AppCompatActivity

        activity.setSupportActionBar(this)
        activity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        activity.supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        setNavigationOnClickListener { activity.onBackPressed() }
    }
}
