package com.enso.util.format

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatDrawDate(date: Date): String {
    val formatter = SimpleDateFormat("yyyy.MM.dd", Locale.KOREA)
    return formatter.format(date)
}
