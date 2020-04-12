package com.zen.alchan.helper

import android.app.Activity
import android.view.WindowManager
import com.google.gson.reflect.TypeToken
import com.zen.alchan.data.response.FuzzyDate
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs


fun Activity.changeStatusBarColor(color: Int) {
    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    window.statusBarColor = color
}

fun Double.removeTrailingZero(): String {
    val format = DecimalFormat("#.#")
    return format.format(this)
}

fun Int.secondsToDateTime(): String {
    val dateFormat = SimpleDateFormat(Constant.DATE_TIME_FORMAT, Locale.US)
    val date = Date(this * 1000L)
    return dateFormat.format(date)
}

fun FuzzyDate?.toMillis(): Long? {
    if (this?.year == null || month == null || day == null) {
        return null
    }

    val calendar = Calendar.getInstance()
    calendar.set(Calendar.YEAR, year!!)
    calendar.set(Calendar.MONTH, month!! - 1)
    calendar.set(Calendar.DAY_OF_MONTH, day!!)

    return calendar.timeInMillis
}

fun String?.replaceUnderscore(): String {
    return this?.replace("_", " ") ?: ""
}

inline fun <reified T> genericType() = object: TypeToken<T>() {}.type

fun FuzzyDate?.toStringDateFormat(): String {
    if (this?.year == null || month == null || day == null) {
        return "-"
    }

    val dateFormat = SimpleDateFormat(Constant.DEFAULT_DATE_FORMAT, Locale.US)
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = this.toMillis()!!

    return dateFormat.format(calendar.time)
}

fun Int.toHex(): String {
    return String.format("#%06X", 0xFFFFFF and this)
}

fun Long.toAlphaHex(): String {
    return String.format("#%08X", 0xFFFFFFFF and this)
}

fun String?.setRegularPlural(count: Int?): String {
    if (this == null) return ""
    if (count == null || abs(count) <= 1) return this

    // TODO: add more rules

    return if (endsWith("y")) {
        replace(Regex("y$"), "ies")
    } else if (endsWith("s") || endsWith("x") || endsWith("z") || endsWith("ch") || endsWith("sh")) {
        this + "es"
    } else {
        this + "s"
    }
}