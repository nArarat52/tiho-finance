package com.example.financetracker

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class FinanceLogicTest {
    @Test fun moneyKeepsKopecksWithoutFloatingPointRounding() {
        assertEquals(123456L, Money.parse("1 234,56"))
        assertEquals(1L, Money.parse("0.01"))
        assertEquals(999999999999L, Money.parse("9999999999.99"))
        assertEquals("1234.56", Money.input(123456))
        listOf("", "0", "-1", "NaN", "1e3", "1.999", "10000000000", "1,2.3").forEach { assertNull(it, Money.parse(it)) }
    }
    @Test fun monthTotalsExcludeOtherMonthsAndPreserveDirection() {
        val entries = listOf(
            Entry(amount = 50000, category = "salary", account = "card", date = LocalDate.of(2026,10,1), note = "", income = true),
            Entry(amount = 1234, category = "food", account = "card", date = LocalDate.of(2026,10,31), note = "", income = false),
            Entry(amount = 9000, category = "food", account = "cash", date = LocalDate.of(2026,9,30), note = "", income = false))
        val october = entries.month(YearMonth.of(2026,10))
        assertEquals(2, october.size)
        assertEquals(1234L, october.expenses())
        assertEquals(50000L, october.incomes())
    }
    @Test fun balanceCombinesOpeningAndTransactionsPerAccountAndExcludesFuture() {
        val today = LocalDate.now()
        val entries = listOf(
            Entry(amount = 10000, category = "salary", account = "card", date = today, note = "", income = true),
            Entry(amount = 1234, category = "food", account = "card", date = today, note = "", income = false),
            Entry(amount = 100, category = "food", account = "cash", date = today, note = "", income = false),
            Entry(amount = 99999, category = "salary", account = "card", date = today.plusDays(1), note = "", income = true))
        val state = FinanceState(entries, listOf(Account("card", "Карта", 2000), Account("cash", "Наличные", 500)))
        assertEquals(10766L, state.balance("card"))
        assertEquals(400L, state.balance("cash"))
        assertEquals(11166L, state.balance())
    }
}
