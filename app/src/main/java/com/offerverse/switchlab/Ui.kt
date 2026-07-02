package com.offerverse.switchlab

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

fun money(v: Double): String = NumberFormat.getCurrencyInstance(Locale.US).apply { maximumFractionDigits = 0 }.format(v)
fun money0(v: Double): String = String.format(Locale.US, "USD %,.0f", v)
fun signed(v: Double): String = if (v >= 0) "+" + money(v) else "-" + money(abs(v))
