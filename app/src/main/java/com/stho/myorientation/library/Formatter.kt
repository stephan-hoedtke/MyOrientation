package com.stho.myorientation.library

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

object Formatter {
    val df0: DecimalFormat = decimalFormat().apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
        minimumIntegerDigits = 1
        decimalFormatSymbols.groupingSeparator = UNICODE_THIN_SPACE
    }
    val df2: DecimalFormat = decimalFormat().apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
        minimumIntegerDigits = 1
    }

    private const val UNICODE_THIN_SPACE = '\u2009'

    private fun decimalFormat(): DecimalFormat =
        DecimalFormat.getNumberInstance(Locale.ENGLISH) as DecimalFormat
}

