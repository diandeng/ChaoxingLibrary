// .MyApplication.kt
package com.diandeng.chaoxinglibrary

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import com.diandeng.chaoxinglibrary.data.network.OfficeApiService
import com.diandeng.chaoxinglibrary.data.repository.AccountRepository
import com.diandeng.chaoxinglibrary.di.OfficeRetrofit
import com.diandeng.chaoxinglibrary.ui.account.AccountViewModel
import com.diandeng.chaoxinglibrary.util.NotificationUtils
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MyApplication: Application() {
    @Inject
    lateinit var accountRepository: AccountRepository
    @Inject
    @OfficeRetrofit
    lateinit var officeApiService: OfficeApiService // 添加 @OfficeRetrofit

    val accountViewModel: AccountViewModel by lazy {
        AccountViewModel(SavedStateHandle(), accountRepository, officeApiService)
    }
    override fun onCreate() {
        super.onCreate()
        NotificationUtils.createNotificationChannel(this) // 初始化通知渠道
    }
}