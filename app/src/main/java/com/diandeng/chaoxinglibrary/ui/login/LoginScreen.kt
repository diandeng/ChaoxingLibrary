// ui/login/LoginScreen.kt
package com.diandeng.chaoxinglibrary.ui.login

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.diandeng.chaoxinglibrary.ui.account.AccountViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: AccountViewModel,
    navController: NavController
) {
    val isLoginSuccess by viewModel.isLoginSuccess.collectAsState()

    // 登录成功时返回上一界面
    LaunchedEffect(isLoginSuccess) {
        if (isLoginSuccess) {
            navController.popBackStack()
            viewModel.resetLoginSuccess()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("登录") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        WebViewLogin(
            viewModel = viewModel,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewLogin(
    viewModel: AccountViewModel,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = {
            WebView(it).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                // 清除之前的Cookies
                CookieManager.getInstance().removeAllCookies(null)
                CookieManager.getInstance().flush()

                // 设置WebView
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.userAgentString = settings.userAgentString

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String) {
                        super.onPageFinished(view, url)

                        // 检查是否登录成功（根据Cookies判断）
                        val cookies = CookieManager.getInstance().getCookie(url)
                        if (cookies != null && cookies.contains("UID")) {
                            val cookieMap = parseCookies(cookies)
                            val uid = cookieMap["UID"] ?: "unknown"
                            viewModel.saveLoginResult(uid, cookieMap)
                        }
                    }
                }
            }
        }, update = {
            it.loadUrl("https://passport2.chaoxing.com/login")
        },
        modifier = modifier
    )
}

private fun parseCookies(cookies: String?): Map<String, String> {
    if (cookies.isNullOrEmpty()) return emptyMap()
    return cookies.split(";").associate {
        val (key, value) = it.split("=", limit = 2).map { it.trim() }
        key to value
    }
}