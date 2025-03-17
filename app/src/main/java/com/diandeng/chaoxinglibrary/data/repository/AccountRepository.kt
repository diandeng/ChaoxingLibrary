package com.diandeng.chaoxinglibrary.data.repository

import android.util.Log
import com.diandeng.chaoxinglibrary.data.model.Account
import com.diandeng.chaoxinglibrary.data.network.Passport2ApiService
import com.diandeng.chaoxinglibrary.di.Passport2Retrofit
import com.diandeng.chaoxinglibrary.util.PrefsUtil
import org.jsoup.Jsoup
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepository @Inject constructor(
    private val prefsUtil: PrefsUtil,
    @Passport2Retrofit private val passport2ApiService: Passport2ApiService
) {
    private val accounts = mutableListOf<Account>()
    private val accountsKey = "accounts"

    init {
        loadAccounts()
    }

    suspend fun saveAccount(uid: String, cookies: Map<String, String>): Account {
        val html = passport2ApiService.getAccountManagePage(cookies.entries.joinToString("; ") { "${it.key}=${it.value}" })
        val doc = Jsoup.parse(html)
        val messageName = doc.select("#messageName").first()?.text() ?: "unknown"
        val account = Account(uid = uid, username = messageName, cookies = cookies)
        accounts.add(account)
        saveAccountsToPrefs()
        Log.d(
            "AccountRepository",
            "Saved account: $account, Total accounts: ${accounts.size}"
        )
        return account
    }

    fun saveAccount(account: Account) {
        accounts.add(account)
        saveAccountsToPrefs()
        Log.d("AccountRepository", "Saved existing account: $account, Total accounts: ${accounts.size}")
    }

    fun getAccounts(): List<Account> {
        Log.d("AccountRepository", "Returning accounts: ${accounts.size}")
        return accounts.toList()
    }

    fun deleteAccount(account: Account) {
        accounts.remove(account)
        saveAccountsToPrefs()
    }

    private fun loadAccounts() {
        val loadedAccounts = prefsUtil.load(accountsKey, emptyList<Account>())
        accounts.clear()
        accounts.addAll(loadedAccounts)
    }

    private fun saveAccountsToPrefs() {
        prefsUtil.save(accountsKey, accounts)
    }
}