// data/network/Passport2ApiService.kt
package com.diandeng.chaoxinglibrary.data.network

import retrofit2.http.GET
import retrofit2.http.Header

interface Passport2ApiService {
    @GET("mooc/accountManage")
    suspend fun getAccountManagePage(
        @Header("Cookie") cookies:String
    ):String
}