// data/model/ReservationDate.kt
package com.diandeng.chaoxinglibrary.data.model

enum class ReservationDate {
    TODAY {
        override fun toString() = "当天"
    },
    TOMORROW {
        override fun toString() = "明天"
    },
}