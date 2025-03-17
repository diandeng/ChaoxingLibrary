package com.diandeng.chaoxinglibrary.ui.account

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.diandeng.chaoxinglibrary.MyApplication
import com.diandeng.chaoxinglibrary.util.NotificationUtils
import kotlinx.coroutines.launch

class TaskReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val application = context.applicationContext as MyApplication
        val viewModel = application.accountViewModel
        Log.d("TaskReceiver", "Received intent: ${intent.action}")

        // 确保通知渠道已创建
        NotificationUtils.createNotificationChannel(context)
        when (intent.action) {
            "com.diandeng.chaoxinglibrary.RESERVATION" -> {
                Log.d("TaskReceiver", "Starting reservation")
                viewModel.viewModelScope.launch { viewModel.startReservation(context) }
            }
            "com.diandeng.chaoxinglibrary.CHECK_IN" -> {
                Log.d("TaskReceiver", "Starting check-in")
                viewModel.viewModelScope.launch { viewModel.startCheckIn(context) }
            }
        }
    }
}