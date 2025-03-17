// data/model/RoomResponse.kt
package com.diandeng.chaoxinglibrary.data.model

import com.google.gson.annotations.SerializedName

data class RoomResponse(
    val success: Boolean,
    val data: RoomData
)

data class RoomData(
    @SerializedName("seatRoom") val seatRoom: RoomInfo,
    @SerializedName("seatConfig") val seatConfig: SeatConfig,
    @SerializedName("seatReserve") val seatReserve: SeatReserve?=null
)

data class RoomInfo (
    @SerializedName("firstLevelName") val firstLevelName: String,
    @SerializedName("secondLevelName") val secondLevelName: String,
    @SerializedName("thirdLevelName") val thirdLevelName: String
) {
    fun getFullName(): String = "$firstLevelName-$secondLevelName-$thirdLevelName"
}

data class SeatConfig(
    @SerializedName("commonTimeConfig") val commonTimeConfig: CommonTimeConfig
)

data class CommonTimeConfig(
    val monStartTime: String,
    val monEndTime: String,
    val tuesStartTime: String,
    val tuesEndTime: String,
    val wedStartTime: String,
    val wedEndTime: String,
    val thurStartTime: String,
    val thurEndTime: String,
    val friStartTime: String,
    val friEndTime: String,
    val satStartTime: String,
    val satEndTime: String,
    val sunStartTime: String,
    val sunEndTime: String
) {
    operator fun get(key: String): String? {
        return when (key) {
            "monStartTime" -> monStartTime
            "monEndTime" -> monEndTime
            "tuesStartTime" -> tuesStartTime
            "tuesEndTime" -> tuesEndTime
            "wedStartTime" -> wedStartTime
            "wedEndTime" -> wedEndTime
            "thurStartTime" -> thurStartTime
            "thurEndTime" -> thurEndTime
            "friStartTime" -> friStartTime
            "friEndTime" -> friEndTime
            "satStartTime" -> satStartTime
            "satEndTime" -> satEndTime
            "sunStartTime" -> sunStartTime
            "sunEndTime" -> sunEndTime
            else -> null // 无效键返回 null
        }
    }
}

data class SeatReserve(
    @SerializedName("id") val id: Long
)