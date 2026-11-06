package com.traderdesk.nativeapp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelsTest {
    private fun trade(result: Double, date: String) = Trade(
        accountId = "a", market = "NQ", reason = "breakout", result = result,
        riskPercent = 1.0, side = TradeSide.BUY, confirmation = "volume",
        sequence = "1", mood = "disciplined", tradedAt = date
    )

    @Test fun analyticsMatchesFlutterRules() {
        val a = analytics(listOf(trade(100.0, "2026-01-01"), trade(-50.0, "2026-01-02"), trade(0.0, "2026-01-03")), 1000.0)
        assertEquals(3, a.count)
        assertEquals(1, a.wins)
        assertEquals(1, a.losses)
        assertEquals(1, a.breakEven)
        assertEquals(50.0, a.net, 0.001)
        assertEquals(33.333, a.winRate, 0.01)
        assertEquals(50.0, a.maxDrawdown, 0.001)
        assertEquals(4.545, a.maxDrawdownPercent, 0.01)
    }

    @Test fun riskCalculatorMatchesFlutterRules() {
        val r = calculateRisk(25_000.0, 1.0, 100.0, 98.0, 104.0)
        assertEquals(250.0, r.riskCash, 0.001)
        assertEquals(2.0, r.stopDistance, 0.001)
        assertEquals(125.0, r.units, 0.001)
        assertEquals(500.0, r.targetCash!!, 0.001)
        assertEquals(2.0, r.rewardRisk!!, 0.001)
    }

    @Test fun tradeJsonRoundTripKeepsFlutterFieldNames() {
        val original = trade(42.5, "2026-02-03T10:00:00Z").copy(
            notes = "review", entryTimeframe = "15m", expectedLoss = 25.0,
            entrySetups = listOf(EntrySetup("breakout", "15m")),
            confirmationSetups = listOf(ConfirmationSetup("volume", "5m")),
            executionJournal = listOf("enteredEarly"), beforeImagePath = "before.jpg"
        )
        val restored = Trade.fromJson(original.toJson())
        assertEquals(original.accountId, restored.accountId)
        assertEquals(original.market, restored.market)
        assertEquals(original.result, restored.result, 0.001)
        assertEquals(original.entryTimeframe, restored.entryTimeframe)
        assertEquals(original.expectedLoss, restored.expectedLoss)
        assertEquals(original.entrySetups.first().label, restored.entrySetups.first().label)
        assertEquals(original.confirmationSetups.first().label, restored.confirmationSetups.first().label)
        assertEquals(original.executionJournal, restored.executionJournal)
        assertEquals(original.beforeImagePath, restored.beforeImagePath)
        assertEquals(original.notes, restored.notes)
        assertTrue(original.toJson().has("riskPercent"))
        assertTrue(original.toJson().has("tradedAt"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun riskRejectsEqualEntryAndStop() {
        calculateRisk(10_000.0, 1.0, 100.0, 100.0, null)
    }
}
