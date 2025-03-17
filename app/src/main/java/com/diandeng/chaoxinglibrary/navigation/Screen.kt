// navigation/Screen.kt
package com.diandeng.chaoxinglibrary.navigation

sealed class Screen(val route:String){
    data object AccountList: Screen("account_list")
    data object Login: Screen("login")
    data object Detail: Screen("detail/{accountId}"){
        fun createRoute(accountId: String)="detail/$accountId"
    }
}