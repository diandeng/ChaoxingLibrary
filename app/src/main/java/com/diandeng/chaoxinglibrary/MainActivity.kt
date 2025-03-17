// MainActivity.kt
package com.diandeng.chaoxinglibrary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.diandeng.chaoxinglibrary.navigation.Screen
import com.diandeng.chaoxinglibrary.ui.account.AccountListScreen
import com.diandeng.chaoxinglibrary.ui.account.AccountViewModel
import com.diandeng.chaoxinglibrary.ui.detail.DetailScreen
import com.diandeng.chaoxinglibrary.ui.login.LoginScreen
import com.diandeng.chaoxinglibrary.ui.theme.ChaoxingLibraryTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChaoxingLibraryTheme {
                val navController = rememberNavController()
                val viewModel: AccountViewModel = hiltViewModel()
                NavHost(
                    navController = navController,
                    startDestination = Screen.AccountList.route
                ) {
                    composable(Screen.AccountList.route) {
                        AccountListScreen(
                            viewModel = viewModel,
                            navController = navController
                        )
                    }
                    composable(Screen.Login.route) {
                        LoginScreen(
                            viewModel = viewModel,
                            navController = navController
                        )
                    }
                    composable(Screen.Detail.route) { backStackEntry ->
                        val accountId = backStackEntry.arguments?.getString("accountId") ?: ""
                        DetailScreen(
                            viewModel = viewModel,
                            navController = navController,
                            accountId = accountId
                        )
                    }
                }
            }
        }
    }
}
