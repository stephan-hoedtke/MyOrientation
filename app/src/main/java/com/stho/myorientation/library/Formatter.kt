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
    val df4: DecimalFormat = decimalFormat().apply {
        minimumFractionDigits = 4
        maximumFractionDigits = 4
        minimumIntegerDigits = 1
    }
    val df11: DecimalFormat = decimalFormat().apply {
        minimumFractionDigits = 15
        maximumFractionDigits = 15
        minimumIntegerDigits = 1
    }

    private const val UNICODE_THIN_SPACE = '\u2009'

    private fun decimalFormat(): DecimalFormat =
        DecimalFormat.getNumberInstance(Locale.ENGLISH) as DecimalFormat
}

fun Double.f0(): String = Formatter.df0.format(this)
fun Double.f2(): String = Formatter.df2.format(this)
fun Double.f4(): String = Formatter.df4.format(this)
fun Double.f11(): String = Formatter.df11.format(this)

