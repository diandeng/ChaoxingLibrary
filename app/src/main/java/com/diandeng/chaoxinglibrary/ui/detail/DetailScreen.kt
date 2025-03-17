// ui/detail/DetailScreen.kt
package com.diandeng.chaoxinglibrary.ui.detail

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.diandeng.chaoxinglibrary.data.model.ReservationDate
import com.diandeng.chaoxinglibrary.ui.account.AccountViewModel
import com.diandeng.chaoxinglibrary.util.ClipboardUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    viewModel: AccountViewModel,
    navController: NavController,
    accountId: String
) {
    val accounts by viewModel.accounts.collectAsState()
    val roomInfo by viewModel.roomInfo.collectAsState()
    Log.d("DetailScreen", "accountId: $accountId, accounts size: ${accounts.size}")

    val account = accounts.find { it.id == accountId } ?: return
    val context = LocalContext.current

    var room by remember { mutableStateOf(account.reservedRoom ?: "") }
    var seat by remember { mutableStateOf(account.reservedSeat ?: "") }

    var reservationDate by remember {
        mutableStateOf(
            account.reservationDate ?: ReservationDate.TODAY
        )
    }

    LaunchedEffect(room, reservationDate) {
        if (room.isNotEmpty()) {
            viewModel.fetchRoomInfo(room, reservationDate, account.cookies)
        } else {
            viewModel.clearRoomInfoPreview() // 清空预览
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("账号详情 - ${account.username}") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.updateAccount(accountId, room, seat, reservationDate)
                    navController.popBackStack()
                }
            )
            {
                Icon(Icons.Default.Save, contentDescription = "保存")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = room,
                onValueChange = { room = it },
                label = { Text("预约房间") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            roomInfo?.let {
                Text(
                    text = "房间名称: ${it.getFullName()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            } ?: account.roomInfo?.let {
                Text(
                    text = "房间名称: ${it.getFullName()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            OutlinedTextField(
                value = seat,
                onValueChange = { seat = it },
                label = { Text("预约座位") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Text("预约日期", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = reservationDate == ReservationDate.TODAY,
                        onClick = { reservationDate = ReservationDate.TODAY }
                    )
                    Text(ReservationDate.TODAY.toString())
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = reservationDate == ReservationDate.TOMORROW,
                        onClick = { reservationDate = ReservationDate.TOMORROW }
                    )
                    Text(ReservationDate.TOMORROW.toString())
                }
            }
            // 添加 Cookies 文本框和复制按钮
            Text("Cookies", style = MaterialTheme.typography.labelLarge)
            OutlinedTextField(
                value = account.cookies.entries.joinToString("; ") { "${it.key}=${it.value}" },
                onValueChange = {}, // 不可编辑
                label = { Text("Cookies") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp), // 设置高度支持多行
                readOnly = true, // 只读
                textStyle = MaterialTheme.typography.bodySmall // 小字体适应多行
            )
            Button(
                onClick = {
                    ClipboardUtils.copyToClipboard(
                        context,
                        account.cookies.entries.joinToString("; ") { "${it.key}=${it.value}" },
                        "Account Cookies"
                    )
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "复制 Cookies",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("复制到剪切板")
            }
        }
    }
}