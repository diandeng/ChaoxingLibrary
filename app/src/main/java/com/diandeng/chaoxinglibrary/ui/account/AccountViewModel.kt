// ui/account/AccountViewModel.kt
package com.diandeng.chaoxinglibrary.ui.account

import android.content.Context
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.diandeng.chaoxinglibrary.data.model.Account
import com.diandeng.chaoxinglibrary.data.model.ReservationDate
import com.diandeng.chaoxinglibrary.data.model.RoomInfo
import com.diandeng.chaoxinglibrary.data.model.RoomResponse
import com.diandeng.chaoxinglibrary.data.network.OfficeApiService
import com.diandeng.chaoxinglibrary.data.repository.AccountRepository
import com.diandeng.chaoxinglibrary.di.OfficeRetrofit
import com.diandeng.chaoxinglibrary.navigation.Screen
import com.diandeng.chaoxinglibrary.util.DateUtils
import com.diandeng.chaoxinglibrary.util.MD5Utils
import com.diandeng.chaoxinglibrary.util.NotificationUtils
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val accountRepository: AccountRepository,
    @OfficeRetrofit private val officeApiService: OfficeApiService
) : ViewModel() {
    private val _accounts = MutableStateFlow<List<Account>>(emptyList())
    val accounts: StateFlow<List<Account>> = _accounts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isLoginSuccess = MutableStateFlow(false)
    val isLoginSuccess: StateFlow<Boolean> = _isLoginSuccess

    private val _roomInfo = MutableStateFlow<RoomInfo?>(null)
    val roomInfo: StateFlow<RoomInfo?> = _roomInfo.asStateFlow()

    init {
        fetchAccountsAndUpdateRoomInfo()
    }

    fun navigateToLogin(navController: NavController) {
        _isLoginSuccess.value = false // 重置登录状态
        navController.navigate(Screen.Login.route)
    }

    fun navigateToDetail(navController: NavController, accountId: String) {
        navController.navigate(Screen.Detail.createRoute(accountId))
        Log.d("AccountViewModel", "Navigate to detail screen, accountId: $accountId")
    }

    fun saveLoginResult(uid: String, cookies: Map<String, String>) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val account = accountRepository.saveAccount(uid, cookies)
                Log.d("AccountViewModel", "Account saved: $account")
                val updatedAccounts = accountRepository.getAccounts()
                _accounts.value = updatedAccounts // 更新状态
                Log.d("AccountViewModel", "Updated accounts: ${updatedAccounts.size}")
                _isLoginSuccess.value = true
            } catch (e: Exception) {
                Log.e("AccountViewModel", "Error saving account: ${e.message}")
                _isLoginSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateAccount(
        accountId: String,
        room: String?,
        seat: String?,
        reservationDate: ReservationDate?
    ) {
        viewModelScope.launch {
            val account = _accounts.value.find { it.id == accountId }
            if (account != null) {
                val updatedAccount = account.copy(
                    reservedRoom = room,
                    reservedSeat = seat,
                    reservationDate = reservationDate,
                    roomInfo = _roomInfo.value
                )
                accountRepository.deleteAccount(account)
                accountRepository.saveAccount(updatedAccount)
                _accounts.value = accountRepository.getAccounts()
                Log.d("AccountViewModel", "Account updated: $updatedAccount")
            }
        }
    }

    fun fetchRoomInfo(
        roomId: String,
        reservationDate: ReservationDate,
        cookies: Map<String, String>
    ) {
        viewModelScope.launch {
            try {
                val dateString = DateUtils.getReservationDateString(reservationDate)
                val cookieString = cookies.entries.joinToString("; ") { "${it.key}=${it.value}" }
                val response =
                    officeApiService.getRoomInfo(cookieString, roomId, dateString)
                val roomResponse = Gson().fromJson(response, RoomResponse::class.java)
                if (roomResponse.success) {
                    _roomInfo.value = roomResponse.data.seatRoom
                    Log.d(
                        "AccountViewModel",
                        "Room info fetched: ${_roomInfo.value?.getFullName()}"
                    )
                } else {
                    _roomInfo.value = null
                    Log.e("AccountViewModel", "Failed to fetch room info: response unsuccessful")
                }
            } catch (e: Exception) {
                _roomInfo.value = null
                Log.e("AccountViewModel", "Error fetching room info: ${e.message}")
            }
        }
    }

    fun clearRoomInfoPreview() {
        _roomInfo.value = null
        Log.d("AccountViewModel", "Room info preview cleared")
    }

    fun deleteAccount(account: Account) {
        viewModelScope.launch {
            accountRepository.deleteAccount(account)
            _accounts.value = accountRepository.getAccounts()
            Log.d("AccountViewModel", "Account deleted, updated accounts: ${_accounts.value.size}")
        }
    }

    private suspend fun fetchRoomInfoForAccount(account: Account) {
        if (account.reservedRoom != null && account.reservationDate != null) {
            try {
                val dateString = DateUtils.getReservationDateString(account.reservationDate)
                val cookieString =
                    account.cookies.entries.joinToString("; ") { "${it.key}=${it.value}" }
                val response = officeApiService.getRoomInfo(
                    cookieString,
                    account.reservedRoom,
                    dateString
                )
                val roomResponse = Gson().fromJson(response, RoomResponse::class.java)
                if (roomResponse.success) {
                    val updatedAccount = account.copy(roomInfo = roomResponse.data.seatRoom)
                    accountRepository.deleteAccount(account)
                    accountRepository.saveAccount(updatedAccount)
                    Log.d(
                        "AccountViewModel",
                        "Room info updated for account: ${account.id}, ${updatedAccount.roomInfo?.getFullName()}"
                    )
                }
            } catch (e: Exception) {
                Log.e(
                    "AccountViewModel",
                    "Error fetching room info for ${account.id}: ${e.message}"
                )
            }
        }
    }

    private fun fetchAccountsAndUpdateRoomInfo() {
        viewModelScope.launch {
            val initialAccounts = accountRepository.getAccounts()
            _accounts.value = initialAccounts
            Log.d("AccountViewModel", "Fetched initial accounts: ${initialAccounts.size}")
            // 更新每个 account 的 roomInfo
            initialAccounts.forEach { account ->
                fetchRoomInfoForAccount(account)
            }
            _accounts.value = accountRepository.getAccounts() // 刷新状态
        }
    }

    fun resetLoginSuccess() {
        _isLoginSuccess.value = false
    }

    fun startReservation(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val accountsList = _accounts.value
                if (accountsList.isEmpty()) {
                    Log.e("AccountViewModel", "No accounts available for reservation")
                    return@launch
                }
                accountsList.forEach { account ->
                    if (account.reservedRoom == null || account.reservedSeat == null || account.reservationDate == null) {
                        Log.e(
                            "AccountViewModel",
                            "Account ${account.id} is not ready for reservation"
                        )
                        return@forEach
                    }
                    val cookieString =
                        account.cookies.entries.joinToString("; ") { "${it.key}=${it.value}" }
                    val roomId = account.reservedRoom
                    val seatNum = account.reservedSeat
                    val day = DateUtils.getReservationDateString(account.reservationDate)

                    // Step 2: 从 getRoomInfo 获取时间配置
                    val roomInfoResponse = officeApiService.getRoomInfo(cookieString, roomId, day)
                    val roomResponse = Gson().fromJson(roomInfoResponse, RoomResponse::class.java)
                    val timeConfig = roomResponse.data.seatConfig.commonTimeConfig
                    val calendar = Calendar.getInstance()
                    if (account.reservationDate == ReservationDate.TOMORROW) {
                        calendar.add(Calendar.DAY_OF_YEAR, 1)
                    }
                    val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
                        .let { if (it == Calendar.SUNDAY) 7 else it - 1 }
                    val (startTimeKey, endTimeKey) = when (dayOfWeek) {
                        Calendar.MONDAY -> "monStartTime" to "monEndTime"
                        Calendar.TUESDAY -> "tuesStartTime" to "tuesEndTime"
                        Calendar.WEDNESDAY -> "wedStartTime" to "wedEndTime"
                        Calendar.THURSDAY -> "thurStartTime" to "thurEndTime"
                        Calendar.FRIDAY -> "friStartTime" to "friEndTime"
                        Calendar.SATURDAY -> "satStartTime" to "satEndTime"
                        Calendar.SUNDAY -> "sunStartTime" to "sunEndTime"
                        else -> "monStartTime" to "monEndTime"
                    }
                    var startTime = timeConfig[startTimeKey] as String
                    val endTime = timeConfig[endTimeKey] as String

                    // Step 3: 生成时间段
                    val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                    if (account.reservationDate == ReservationDate.TODAY) {
                        val currentTime = Calendar.getInstance()
                        val startDate = dateTimeFormat.parse("$day $startTime")
                        if (currentTime.time.after(startDate)) {
                            Log.d("AccountViewModel", "${currentTime.time}, $startDate")
                            val minutes = currentTime.get(Calendar.MINUTE)
                            val adjustedMinutes = (minutes / 15) * 15 // 取 15 分钟整数倍，向下取整
                            currentTime.set(Calendar.MINUTE, adjustedMinutes)
                            currentTime.set(Calendar.SECOND, 0)
                            currentTime.set(Calendar.MILLISECOND, 0)
                            startTime = timeFormat.format(currentTime.time)
                            Log.d(
                                "AccountViewModel",
                                "Adjusted startTime to $startTime for ${account.username}"
                            )
                        }
                    }
                    val endDate = try {
                        timeFormat.parse(endTime)
                            ?: throw IllegalArgumentException("Invalid endTime format: $endTime")
                    } catch (e: Exception) {
                        Log.e("AccountViewModel", "Failed to parse endTime: $e")
                        throw e
                    }
                    val startDate = try {
                        timeFormat.parse(startTime)
                            ?: throw IllegalArgumentException("Invalid startTime format: $startTime")
                    } catch (e: Exception) {
                        Log.e("AccountViewModel", "Failed to parse startTime: $e")
                        throw e
                    }
                    val timeSlots = mutableListOf<Pair<String, String>>()
                    var currentEnd = endDate
                    while (currentEnd.time > startDate.time) {
                        val currentStart = Calendar.getInstance().apply {
                            time = currentEnd
                            add(Calendar.HOUR_OF_DAY, -4)
                        }.time
                        if (currentStart.time < startDate.time) {
                            // 不足 4 小时，取剩余时间
                            timeSlots.add(
                                timeFormat.format(startDate) to timeFormat.format(
                                    currentEnd
                                )
                            )
                            break
                        } else {
                            timeSlots.add(
                                timeFormat.format(currentStart) to timeFormat.format(
                                    currentEnd
                                )
                            )
                        }
                        currentEnd = currentStart
                    }

                    // 尝试预约
                    for ((start, end) in timeSlots) {
                        // 获取Token
                        val codeResponse = officeApiService.getSeatCode(
                            cookies = cookieString,
                            seatNum = seatNum,
                            roomId = roomId
                        )
                        val doc = Jsoup.parse(codeResponse)
                        val token = doc.select("script:containsData(submitSeatReserve)")
                            .first()
                            ?.data()
                            ?.let { script ->
                                Regex("token:\\s*'([a-f0-9]{32})'").find(script)?.groupValues?.get(1)
                            } ?: throw Exception("Token not found in response")
                        Log.d("AccountViewModel", "Token: $token")
                        val referer = "https://office.chaoxing.com/front/third/apps/seat/codemyselfuse?id=$roomId&seatNum=$seatNum&bindSno=false&bindSnoUrl=&token=$token"
                        val seq =
                            "[captcha=][day=$day][endTime=$end][roomId=$roomId][seatNum=$seatNum][startTime=$start][token=$token][%sd`~7^/>N4!Q#){'']"
                        val enc = MD5Utils.toHexMD5(seq)
                        Log.d("AccountViewModel", "Seq: $seq")
                        val submitResponse = officeApiService.submitReservation(
                            cookies = cookieString,
                            referer = referer,
                            roomId = roomId,
                            startTime = start,
                            endTime = end,
                            day = day,
                            captcha = "",
                            seatNum = seatNum,
                            token = token,
                            enc = enc
                        )
                        val submitResult = Gson().fromJson(submitResponse, Map::class.java)
                        Log.d(
                            "AccountViewModel",
                            "Trying to reserve for ${account.username}: $start - $end"
                        )
                        Log.d("AccountViewModel", "Raw submit response: $submitResponse")
                        if (submitResult["success"] == true) {
                            Log.d(
                                "AccountViewModel",
                                "Reservation successful for ${account.username}: $start - $end"
                            )
                            NotificationUtils.sendNotification(
                                context,
                                "预约成功",
                                "${account.username} 的座位已预约: $start - $end",
                                account.id.hashCode() // 使用账号 ID 作为唯一通知 ID
                            )
                        } else {
                            Log.w(
                                "AccountViewModel",
                                "Reservation failed for ${account.username}: ${submitResult["msg"]}"
                            )
                            NotificationUtils.sendNotification(
                                context,
                                "预约失败",
                                "${account.username} 的座位预约失败: ${submitResult["msg"]}: $start - $end",
                                account.id.hashCode()
                            )
                        }
                        delay(5000)
                    }
                }
            } catch (e: Exception) {
                Log.e("AccountViewModel", "Error during reservation: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun startCheckIn(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val accountsList = _accounts.value
                if (accountsList.isEmpty()) {
                    Log.e("AccountViewModel", "No accounts available for check-in")
                    return@launch
                }
                accountsList.forEach { account ->
                    if (account.reservedRoom == null || account.reservedSeat == null) {
                        Log.e("AccountViewModel", "Account ${account.id} is not ready for check-in")
                        return@forEach
                    }
                    val cookieString = account.cookies.entries.joinToString("; ") { "${it.key}=${it.value}" }
                    val roomId = account.reservedRoom
                    val seatNum = account.reservedSeat

                    // Step 1: 获取 seatReserve.id
                    val reserveResponse = officeApiService.getReserveInfo(cookieString, roomId, seatNum)
                    Log.d("AccountViewModel", "Raw reserve response for ${account.username}: $reserveResponse")
                    val reserveResult = Gson().fromJson(reserveResponse, RoomResponse::class.java)
                    if (!reserveResult.success) {
                        Log.e("AccountViewModel", "Failed to get reserve info for ${account.username}: API returned unsuccessful or empty data")
                        return@forEach
                    }
                    val reserveId = reserveResult.data.seatReserve?.id
                    if (reserveId == null) {
                        Log.e("AccountViewModel", "No active reservation found for ${account.username}: seatReserve is null")
                        return@forEach
                    }
                    Log.d("AccountViewModel", "Reserve ID for ${account.username}: $reserveId")
                    // Step 2: 发送签到请求
                    val signResponse = officeApiService.signIn(cookieString, reserveId.toString()) // 转换为 String，因为 API 期望字符串参数
                    val signResult = Gson().fromJson(signResponse, Map::class.java)
                    if (signResult["success"] == true) {
                        Log.d("AccountViewModel", "Check-in successful for ${account.username}")
                        NotificationUtils.sendNotification(
                            context,
                            "签到成功",
                            "${account.username} 已完成签到",
                            account.id.hashCode() + 1 // 使用偏移 ID 避免与预约通知冲突
                        )
                    } else {
                        Log.w("AccountViewModel", "Check-in failed for ${account.username}: ${signResult["msg"]}")
                    }
                }
            } catch (e: Exception) {
                Log.e("AccountViewModel", "Error during check-in: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}