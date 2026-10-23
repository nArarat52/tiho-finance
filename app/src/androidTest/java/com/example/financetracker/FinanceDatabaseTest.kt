package com.example.financetracker

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Before
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class FinanceDatabaseTest {
    private lateinit var db: FinanceDatabase
    @Before fun prepare() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.deleteDatabase("tiho.db")
        db = FinanceDatabase(context)
    }
    @After fun close() { db.close() }
    @Test fun saveEditDeleteAndReopen() {
        val entry = Entry(amount = 123456, category = "food", account = "card", date = LocalDate.now(), note = "Кофе; \"дома\"\n", income = false)
        db.save(entry); db.save(entry.copy(amount = 42))
        assertEquals(1, db.read().entries.size)
        db.close(); db = FinanceDatabase(ApplicationProvider.getApplicationContext())
        assertEquals(42L, db.read().entries.single().amount)
        db.delete(entry.id); assertTrue(db.read().entries.isEmpty())
    }
    @Test fun backupRoundTripAndInvalidRestoreNeverDestroysData() {
        db.demo()
        val before = db.read(); val backup = db.backup()
        db.reset(); assertTrue(db.read().entries.isEmpty())
        db.restore(backup)
        assertEquals(before.entries.toSet(), db.read().entries.toSet())
        assertEquals(before.accounts, db.read().accounts)
        assertEquals(before.budgets, db.read().budgets)
        val corrupt = JSONObject(backup).apply { getJSONArray("entries").getJSONObject(0).put("account", "missing") }.toString()
        try { db.restore(corrupt); fail("Should reject invalid account") } catch (_: IllegalArgumentException) { }
        assertEquals(before.entries.toSet(), db.read().entries.toSet())
        try { db.restore("{}"); fail("Should reject invalid format") } catch (_: Exception) { }
        assertEquals(before.entries.size, db.read().entries.size)
    }
}
