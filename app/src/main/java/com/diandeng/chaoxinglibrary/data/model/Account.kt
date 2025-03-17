// data/model/Account.kt
package com.diandeng.chaoxinglibrary.data.model

import java.util.UUID

data class Account(
    val id: String = UUID.randomUUID().toString(),
    val uid: String,
    val username: String,
    val cookies: Map<String, String>,
    val reservedRoom: String? = null,
    val reservedSeat: String? = null,
    val reservationDate: ReservationDate? = ReservationDate.TODAY,
    val roomInfo: RoomInfo? = null
)