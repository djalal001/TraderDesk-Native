package com.traderdesk.nativeapp

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.util.UUID
import kotlin.math.abs

class DeskRepository(context: Context) {
    private val prefs = context.getSharedPreferences("traderdesk_data", Context.MODE_PRIVATE)

    fun load(): DeskSnapshot {
        val raw = prefs.getString("data", null) ?: return DeskSnapshot()
        return runCatching {
            val root = JSONObject(raw)
            DeskSnapshot(
                accounts = root.optJSONArray("accounts")?.toList { Account.fromJson(it as JSONObject) } ?: emptyList(),
                trades = root.optJSONArray("trades")?.toList { Trade.fromJson(it as JSONObject) } ?: emptyList(),
                isDark = root.optBoolean("isDark", true),
                language = root.optString("languageCode", "ar"),
                selectedAccountId = root.optString("selectedAccountId").ifBlank { null }
            )
        }.getOrDefault(DeskSnapshot())
    }

    fun save(snapshot: DeskSnapshot) {
        val root = JSONObject().apply {
            put("formatVersion", 1)
            put("accounts", JSONArray().also { a -> snapshot.accounts.forEach { a.put(it.toJson()) } })
            put("trades", JSONArray().also { a -> snapshot.trades.forEach { a.put(it.toJson()) } })
            put("isDark", snapshot.isDark)
            put("languageCode", snapshot.language)
            put("selectedAccountId", snapshot.selectedAccountId ?: JSONObject.NULL)
        }
        prefs.edit().putString("data", root.toString()).apply()
    }

    private inline fun <T> JSONArray.toList(mapper: (Any) -> T): List<T> = buildList {
        for (i in 0 until length()) opt(i)?.let { add(mapper(it)) }
    }
}

data class DeskSnapshot(
    val accounts: List<Account> = emptyList(),
    val trades: List<Trade> = emptyList(),
    val isDark: Boolean = true,
    val language: String = "ar",
    val selectedAccountId: String? = null
)

enum class AccountType { DEMO, REAL }
data class Account(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val type: AccountType = AccountType.DEMO,
    val startingBalance: Double,
    val currency: String = "USD",
    val createdAt: String = java.time.Instant.now().toString(),
    val profitTarget: Double = 0.0
) {
    fun toJson() = JSONObject().apply {
        put("id", id); put("name", name); put("type", type.name.lowercase())
        put("startingBalance", startingBalance); put("currency", currency)
        put("createdAt", createdAt); put("profitTarget", profitTarget)
    }
    companion object {
        fun fromJson(o: JSONObject) = Account(
            id = o.optString("id", UUID.randomUUID().toString()), name = o.optString("name"),
            type = if (o.optString("type") == "real") AccountType.REAL else AccountType.DEMO,
            startingBalance = o.optDouble("startingBalance", 0.0), currency = o.optString("currency", "USD"),
            createdAt = o.optString("createdAt", Instant.now().toString()), profitTarget = o.optDouble("profitTarget", 0.0)
        )
    }
}

enum class TradeSide { BUY, SELL }
enum class Adherence { FOLLOWED, NOT_FOLLOWED }
data class EntrySetup(val reason: String, val timeframe: String) {
    val label get() = if (timeframe.isBlank()) reason else "$reason · $timeframe"
    fun toJson() = JSONObject().apply { put("reason", reason); put("timeframe", timeframe) }
    companion object { fun fromJson(o: JSONObject) = EntrySetup(o.optString("reason"), o.optString("timeframe")) }
}
data class ConfirmationSetup(val type: String, val timeframe: String) {
    val label get() = if (timeframe.isBlank()) type else "$type · $timeframe"
    fun toJson() = JSONObject().apply { put("type", type); put("timeframe", timeframe) }
    companion object { fun fromJson(o: JSONObject) = ConfirmationSetup(o.optString("type"), o.optString("timeframe")) }
}
data class Trade(
    val id: String = UUID.randomUUID().toString(), val accountId: String, val market: String,
    val reason: String, val result: Double, val riskPercent: Double, val side: TradeSide,
    val confirmation: String, val sequence: String, val mood: String, val tradedAt: String = Instant.now().toString(),
    val entryTimeframe: String? = null, val entrySetups: List<EntrySetup> = emptyList(),
    val confirmationSetups: List<ConfirmationSetup> = emptyList(), val expectedLoss: Double? = null,
    val beforeImagePath: String? = null, val afterImagePath: String? = null,
    val confirmationImagePath: String? = null, val notes: String = "", val strategyAdherence: Adherence? = null,
    val executionJournal: List<String> = emptyList(), val executionReviewKeys: List<String> = emptyList(), val noEntryReasons: List<String> = emptyList()
) {
    fun toJson() = JSONObject().apply {
        put("id", id); put("accountId", accountId); put("market", market); put("reason", reason)
        put("result", result); put("riskPercent", riskPercent); put("side", side.name.lowercase())
        put("confirmation", confirmation); put("sequence", sequence); put("mood", mood); put("tradedAt", tradedAt)
        put("entryTimeframe", entryTimeframe ?: JSONObject.NULL)
        put("entrySetups", JSONArray().also { a -> entrySetups.forEach { a.put(it.toJson()) } })
        put("confirmationSetups", JSONArray().also { a -> confirmationSetups.forEach { a.put(it.toJson()) } })
        put("expectedLoss", expectedLoss ?: JSONObject.NULL); put("beforeImagePath", beforeImagePath ?: JSONObject.NULL)
        put("afterImagePath", afterImagePath ?: JSONObject.NULL); put("confirmationImagePath", confirmationImagePath ?: JSONObject.NULL)
        put("notes", notes)
        put("executionJournal", JSONArray(executionJournal))
        put("strategyAdherence", strategyAdherence?.name?.lowercase() ?: JSONObject.NULL)
        put("executionReviewKeys", JSONArray(executionReviewKeys)); put("noEntryReasons", JSONArray(noEntryReasons))
    }
    companion object {
        fun fromJson(o: JSONObject) = Trade(
            id=o.optString("id", UUID.randomUUID().toString()), accountId=o.optString("accountId"), market=o.optString("market"),
            reason=o.optString("reason"), result=o.optDouble("result"), riskPercent=o.optDouble("riskPercent"),
            side=if (o.optString("side") == "sell") TradeSide.SELL else TradeSide.BUY, confirmation=o.optString("confirmation"),
            sequence=o.optString("sequence"), mood=o.optString("mood"), tradedAt=o.optString("tradedAt", Instant.now().toString()),
            entryTimeframe=o.optString("entryTimeframe").ifBlank { null },
            entrySetups=o.optJSONArray("entrySetups")?.objects { EntrySetup.fromJson(it) } ?: emptyList(),
            confirmationSetups=o.optJSONArray("confirmationSetups")?.objects { ConfirmationSetup.fromJson(it) } ?: emptyList(),
            expectedLoss=o.optDouble("expectedLoss").takeIf { !it.isNaN() },
            beforeImagePath=o.optString("beforeImagePath").ifBlank { null }, afterImagePath=o.optString("afterImagePath").ifBlank { null },
            confirmationImagePath=o.optString("confirmationImagePath").ifBlank { null }, notes=o.optString("notes"),
            executionJournal=o.optJSONArray("executionJournal")?.strings() ?: emptyList(),
            strategyAdherence=when(o.optString("strategyAdherence")){ "followed" -> Adherence.FOLLOWED; "not_followed" -> Adherence.NOT_FOLLOWED; else -> null },
            executionReviewKeys=o.optJSONArray("executionReviewKeys")?.strings() ?: emptyList(), noEntryReasons=o.optJSONArray("noEntryReasons")?.strings() ?: emptyList()
        )
    }
}

private fun JSONArray.strings() = buildList { for (i in 0 until length()) optString(i).takeIf { it.isNotBlank() }?.let(::add) }
private inline fun <T> JSONArray.objects(mapper: (JSONObject) -> T) = buildList { for (i in 0 until length()) optJSONObject(i)?.let { add(mapper(it)) } }

data class Analytics(val count:Int, val wins:Int, val losses:Int, val breakEven:Int, val net:Double, val averageWin:Double, val averageLoss:Double, val winRate:Double, val maxDrawdown:Double, val maxDrawdownPercent:Double)
fun analytics(input: List<Trade>, startingBalance: Double = 0.0): Analytics {
    var wins=0; var losses=0; var even=0; var winTotal=0.0; var lossTotal=0.0; var net=0.0; var equity=startingBalance; var peak=startingBalance; var dd=0.0; var ddPct=0.0
    input.sortedBy { it.tradedAt }.forEach { t -> val r=t.result; net+=r; equity+=r; if(r>0){wins++;winTotal+=r}else if(r<0){losses++;lossTotal+=abs(r)}else even++; if(equity>peak)peak=equity; dd=maxOf(dd,peak-equity); ddPct=maxOf(ddPct,if(peak>0)(peak-equity)/peak*100 else 0.0) }
    return Analytics(input.size,wins,losses,even,net,if(wins>0)winTotal/wins else 0.0,if(losses>0)lossTotal/losses else 0.0,if(input.isNotEmpty())wins*100.0/input.size else 0.0,dd,ddPct)
}
data class RiskResult(val riskCash:Double,val stopDistance:Double,val units:Double,val targetCash:Double?,val rewardRisk:Double?)
fun calculateRisk(balance:Double,riskPercent:Double,entry:Double,stop:Double,target:Double?): RiskResult {
    require(balance>0 && riskPercent>0 && riskPercent<=100 && entry>0 && stop>0 && entry!=stop)
    val cash=balance*riskPercent/100; val distance=abs(entry-stop); val units=cash/distance; val reward=target?.let{abs(it-entry)}
    return RiskResult(cash,distance,units,reward?.times(units),reward?.div(distance))
}
