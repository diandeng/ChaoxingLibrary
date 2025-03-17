// data/network/OfficeApiService.kt
package com.diandeng.chaoxinglibrary.data.network

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface OfficeApiService {
    // 获取房间信息
    @GET("data/apps/seat/room/info")
    suspend fun getRoomInfo(
        @Header("Cookie") cookies: String,
        @Query("id") id: String,
        @Query("toDay") toDay: String
    ): String

    // 获取座位号
    @GET("front/third/apps/seat/codemyselfuse")
    suspend fun getSeatCode(
        @Header("Cookie") cookies: String,
        @Query("seatNum") seatNum: String,
        @Query("id") roomId: String
    ): String

    // 提交预约
    @GET("data/apps/seat/submit")
    suspend fun submitReservation(
        @Header("Cookie") cookies: String,
        @Header("Host") host: String = "office.chaoxing.com", // 添加 Host 头
        @Header("Referer") referer: String, // 添加 Referer 头
        //@Query("deptIdEnc") deptIdEnc: String="",
        @Query("roomId") roomId: String,
        @Query("startTime") startTime: String,
        @Query("endTime") endTime: String,
        @Query("day") day: String,
        @Query("captcha") captcha: String = "",
        @Query("seatNum") seatNum: String,
        @Query("token") token: String,
        @Query("enc") enc: String
    ): String

    @GET("data/apps/seat/reserve/info")
    suspend fun getReserveInfo(
        @Header("Cookie") cookies: String,
        @Query("id") roomId: String,
        @Query("seatNum") seatNum: String
    ): String

    @GET("data/apps/seat/sign")
    suspend fun signIn(
        @Header("Cookie") cookies: String,
        @Query("id") reserveId: String
    ): String


    @GET("data/apps/seat/reservelist")
    suspend fun getReserveList(
        @Header("Cookie") cookies: String,
        @Query("indexId") indexId: Int = 0,
        @Query("pageSize") pageSize: Int = 10,
        @Query("type") type: Int = 0
    ): String
}