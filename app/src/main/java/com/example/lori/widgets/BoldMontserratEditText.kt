package com.example.lori.widgets

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText

class BoldMontserratEditText(context: Context, attrs: AttributeSet) :
    AppCompatEditText(context, attrs) {

    init {
        applyFont()
    }

    private fun applyFont() {
        typeface = Typeface.createFromAsset(context.assets, "Montserrat-Bold.ttf")
    }
}
