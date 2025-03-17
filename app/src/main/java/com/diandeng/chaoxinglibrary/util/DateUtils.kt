package com.diandeng.chaoxinglibrary.util

import com.diandeng.chaoxinglibrary.data.model.ReservationDate
import java.text.SimpleDateFormat
import java.util.Locale

object DateUtils {
    fun getReservationDateString(reservationDate: ReservationDate): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return when (reservationDate) {
            ReservationDate.TODAY -> dateFormat.format(System.currentTimeMillis())
            ReservationDate.TOMORROW -> dateFormat.format(System.currentTimeMillis() + 24 * 60 * 60 * 1000)
        }
    }
}