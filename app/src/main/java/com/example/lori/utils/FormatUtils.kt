package com.example.lori.utils

import java.math.RoundingMode
import java.text.DecimalFormat

object FormatUtils {

    fun format(fmt: String = "###,###.###", num: Any): String {
        val df = DecimalFormat(fmt)
        df.roundingMode = RoundingMode.CEILING

        return df.format(num)
    }
}
