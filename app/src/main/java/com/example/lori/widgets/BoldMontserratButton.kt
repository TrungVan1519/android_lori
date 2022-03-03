package com.example.lori.widgets

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton

class BoldMontserratButton(context: Context, attrs: AttributeSet) :
    AppCompatButton(context, attrs) {

    init {
        applyFont()
    }

    private fun applyFont() {
        typeface = Typeface.createFromAsset(context.assets, "Montserrat-Bold.ttf")
    }
}
