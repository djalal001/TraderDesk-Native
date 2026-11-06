package com.traderdesk.nativeapp

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import org.json.JSONObject

class DeskViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = DeskRepository(app)
    private var snapshot by mutableStateOf(repository.load())
    var tab by mutableStateOf(Tab.OVERVIEW)
    val accounts get() = snapshot.accounts
    val trades get() = snapshot.trades
    val isDark get() = snapshot.isDark
    val selectedAccount: Account? get() = accounts.firstOrNull { it.id == snapshot.selectedAccountId } ?: accounts.firstOrNull()
    val accountTrades get() = selectedAccount?.let { a -> trades.filter { it.accountId == a.id } } ?: emptyList()

    fun navigateTo(value: Tab) { tab = value }
    fun toggleTheme() { update { it.copy(isDark = !it.isDark) } }
    fun selectAccount(id: String) { update { it.copy(selectedAccountId=id) } }
    fun addAccount(account: Account) { update { it.copy(accounts=it.accounts+account, selectedAccountId=it.selectedAccountId ?: account.id) } }
    fun addTrade(trade: Trade) { update { it.copy(trades=it.trades+trade) } }
    fun updateTrade(trade: Trade) { update { it.copy(trades=it.trades.map { old -> if (old.id == trade.id) trade else old }) } }
    fun deleteTrade(id: String) { update { it.copy(trades=it.trades.filterNot { t -> t.id==id }) } }
    fun exportJson(): String = JSONObject().apply {
        put("formatVersion", 2)
        put("accounts", org.json.JSONArray().also { a -> accounts.forEach { a.put(it.toJson()) } })
        put("trades", org.json.JSONArray().also { a -> trades.forEach { a.put(it.toJson()) } })
        put("isDark", isDark); put("languageCode", "ar"); put("selectedAccountId", selectedAccount?.id ?: JSONObject.NULL)
    }.toString()
    fun restoreJson(raw: String): Boolean = runCatching {
        val root=JSONObject(raw)
        val restored=DeskSnapshot(
            accounts=root.optJSONArray("accounts")?.let { a -> (0 until a.length()).mapNotNull { a.optJSONObject(it)?.let(Account::fromJson) } } ?: emptyList(),
            trades=root.optJSONArray("trades")?.let { a -> (0 until a.length()).mapNotNull { a.optJSONObject(it)?.let(Trade::fromJson) } } ?: emptyList(),
            isDark=root.optBoolean("isDark",true), language=root.optString("languageCode","ar"), selectedAccountId=root.optString("selectedAccountId").ifBlank{null}
        )
        snapshot=restored; repository.save(restored); true
    }.getOrDefault(false)
    private fun update(block: (DeskSnapshot)->DeskSnapshot) { snapshot=block(snapshot); repository.save(snapshot) }
}
enum class Tab { OVERVIEW, JOURNAL, DAYS, ACCOUNTS, PERFORMANCE, SETTINGS }
