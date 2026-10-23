package com.example.financetracker

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale
import java.util.UUID

data class Category(val id: String, val title: String, val icon: String, val color: Long, val income: Boolean = false)
val categories = listOf(
    Category("food", "Продукты", "basket", 0xFFE3A173),
    Category("cafe", "Кафе и рестораны", "coffee", 0xFFB99BCE),
    Category("transport", "Транспорт", "car", 0xFF83AEBB),
    Category("shopping", "Покупки", "bag", 0xFFD6AD65),
    Category("home", "Дом", "home", 0xFF87A791),
    Category("health", "Здоровье", "heart", 0xFFD68E92),
    Category("fun", "Развлечения", "spark", 0xFF9B9ACB),
    Category("other", "Другое", "grid", 0xFFA2AAA4),
    Category("salary", "Зарплата", "wallet", 0xFF73A58B, true),
    Category("gift", "Подарки", "gift", 0xFFD6AD65, true),
    Category("income", "Другой доход", "plus", 0xFF83AEBB, true)
)
fun category(id: String) = categories.firstOrNull { it.id == id } ?: categories[7]
data class Account(val id: String, val title: String, val opening: Long)
data class Entry(val id: String = UUID.randomUUID().toString(), val amount: Long, val category: String,
                 val account: String, val date: LocalDate, val note: String, val income: Boolean)
data class Budget(val category: String, val limit: Long)
data class FinanceState(val entries: List<Entry> = emptyList(), val accounts: List<Account> = emptyList(),
                        val budgets: List<Budget> = emptyList(), val ready: Boolean = false)

object Money {
    fun parse(text: String): Long? = try {
        val clean = text.replace(" ", "").replace("\u00a0", "").replace(',', '.')
        if (!clean.matches(Regex("[0-9]{1,10}(\\.[0-9]{1,2})?"))) null
        else BigDecimal(clean).movePointRight(2).longValueExact().takeIf { it > 0 }
    } catch (_: Exception) { null }
    fun input(value: Long) = BigDecimal.valueOf(value, 2).stripTrailingZeros().toPlainString()
    fun format(value: Long): String {
        val nf = NumberFormat.getNumberInstance(Locale.forLanguageTag("ru-RU"))
        nf.maximumFractionDigits = if (value % 100 != 0L) 2 else 0
        return nf.format(BigDecimal.valueOf(value, 2)) + " ₽"
    }
}
fun List<Entry>.expenses() = filter { !it.income }.sumOf { it.amount }
fun List<Entry>.incomes() = filter { it.income }.sumOf { it.amount }
fun List<Entry>.month(month: YearMonth) = filter { YearMonth.from(it.date) == month }
fun FinanceState.balance(accountId: String? = null): Long = accounts.filter { accountId == null || it.id == accountId }.sumOf { it.opening } +
    entries.filter { (accountId == null || it.account == accountId) && !it.date.isAfter(LocalDate.now()) }.sumOf { if (it.income) it.amount else -it.amount }

/** SQLite is the source of truth. Monetary values are integer kopecks. */
class FinanceDatabase(context: Context) : SQLiteOpenHelper(context, "tiho.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE accounts(id TEXT PRIMARY KEY, title TEXT NOT NULL, opening INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE entries(id TEXT PRIMARY KEY, amount INTEGER NOT NULL CHECK(amount > 0), category TEXT NOT NULL, account TEXT NOT NULL REFERENCES accounts(id), date TEXT NOT NULL, note TEXT NOT NULL, income INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE budgets(category TEXT PRIMARY KEY, amount INTEGER NOT NULL CHECK(amount > 0))")
        db.execSQL("INSERT INTO accounts VALUES('card', 'Основная карта', 0), ('cash', 'Наличные', 0)")
    }
    override fun onConfigure(db: SQLiteDatabase) { db.setForeignKeyConstraintsEnabled(true) }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
    fun read(): FinanceState {
        val db = readableDatabase
        val accounts = mutableListOf<Account>()
        db.rawQuery("SELECT * FROM accounts ORDER BY rowid", null).use { c -> while(c.moveToNext()) accounts += Account(c.getString(0), c.getString(1), c.getLong(2)) }
        val entries = mutableListOf<Entry>()
        db.rawQuery("SELECT * FROM entries ORDER BY date DESC, rowid DESC", null).use { c -> while(c.moveToNext()) entries += Entry(c.getString(0), c.getLong(1), c.getString(2), c.getString(3), LocalDate.parse(c.getString(4)), c.getString(5), c.getInt(6) == 1) }
        val budgets = mutableListOf<Budget>()
        db.rawQuery("SELECT * FROM budgets ORDER BY rowid", null).use { c -> while(c.moveToNext()) budgets += Budget(c.getString(0), c.getLong(1)) }
        return FinanceState(entries, accounts, budgets, true)
    }
    fun save(entry: Entry) {
        require(entry.amount > 0 && categories.any { it.id == entry.category && it.income == entry.income })
        val values = ContentValues().apply {
            put("id", entry.id); put("amount", entry.amount); put("category", entry.category); put("account", entry.account)
            put("date", entry.date.toString()); put("note", entry.note); put("income", if(entry.income) 1 else 0)
        }
        check(writableDatabase.insertWithOnConflict("entries", null, values, SQLiteDatabase.CONFLICT_REPLACE) != -1L)
    }
    fun delete(id: String) { writableDatabase.delete("entries", "id=?", arrayOf(id)) }
    fun budget(categoryId: String, amount: Long) {
        require(amount > 0 && !category(categoryId).income)
        writableDatabase.execSQL("INSERT OR REPLACE INTO budgets VALUES(?, ?)", arrayOf<Any>(categoryId, amount))
    }
    fun deleteBudget(categoryId: String) { writableDatabase.delete("budgets", "category=?", arrayOf(categoryId)) }
    fun reset() {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete("entries", null, null); db.delete("budgets", null, null); db.delete("accounts", null, null)
            db.execSQL("INSERT INTO accounts VALUES('card', 'Основная карта', 0), ('cash', 'Наличные', 0)")
            db.setTransactionSuccessful()
        } finally { db.endTransaction() }
    }
    fun account(id: String, title: String, opening: Long) {
        require(title.isNotBlank())
        writableDatabase.update("accounts", ContentValues().apply { put("title", title); put("opening", opening) }, "id=?", arrayOf(id))
    }
    fun demo() {
        check(read().entries.isEmpty())
        val db = writableDatabase; db.beginTransaction()
        try {
            account("card", "Основная карта", 3250000); account("cash", "Наличные", 500000)
            val today = LocalDate.now(); val month = today.withDayOfMonth(1)
            save(Entry(amount = 12000000, category = "salary", account = "card", date = month, note = "Зарплата", income = true))
            val samples = listOf(Triple("home", 3200000L, "Аренда квартиры"), Triple("food", 268000L, "ВкусВилл"),
                Triple("transport", 59000L, "Яндекс Go"), Triple("cafe", 42000L, "Кофе и круассан"),
                Triple("shopping", 649000L, "Новые кроссовки"), Triple("food", 324000L, "Продукты на неделю"),
                Triple("fun", 159000L, "Билеты в кино"), Triple("health", 185000L, "Аптека"),
                Triple("cafe", 235000L, "Ужин с друзьями"), Triple("transport", 210000L, "Проездной"),
                Triple("food", 187500L, "Перекрёсток"), Triple("cafe", 38000L, "Кофейня у дома"))
            samples.forEachIndexed { i, (cat, amount, note) ->
                save(Entry(amount = amount, category = cat, account = if(i % 4 == 0) "cash" else "card",
                    date = today.minusDays((i / 2).toLong()).coerceAtLeast(month), note = note, income = false))
            }
            budget("food", 1800000); budget("cafe", 700000); budget("transport", 600000); budget("shopping", 1000000)
            db.setTransactionSuccessful()
        } finally { db.endTransaction() }
    }
    fun backup(): String {
        val state = read()
        return JSONObject().put("version", 1).put("accounts", JSONArray().apply { state.accounts.forEach { put(JSONObject().put("id", it.id).put("title", it.title).put("opening", it.opening)) } })
            .put("entries", JSONArray().apply { state.entries.forEach { put(JSONObject().put("id", it.id).put("amount", it.amount).put("category", it.category).put("account", it.account).put("date", it.date).put("note", it.note).put("income", it.income)) } })
            .put("budgets", JSONArray().apply { state.budgets.forEach { put(JSONObject().put("category", it.category).put("limit", it.limit)) } }).toString(2)
    }
    /** Validate the entire backup before replacing data atomically. */
    fun restore(raw: String) {
        val root = JSONObject(raw); require(root.getInt("version") == 1)
        val a = root.getJSONArray("accounts"); require(a.length() in 1..100)
        val accounts = (0 until a.length()).map { a.getJSONObject(it).let { o -> Account(o.getString("id"), o.getString("title"), o.getLong("opening")) } }
        require(accounts.all { it.id.isNotBlank() && it.title.isNotBlank() && it.title.length <= 100 && it.opening in -999999999999L..999999999999L })
        require(accounts.map { it.id }.distinct().size == accounts.size)
        val e = root.getJSONArray("entries"); require(e.length() <= 100000)
        val entries = (0 until e.length()).map { e.getJSONObject(it).let { o -> Entry(o.getString("id"), o.getLong("amount"), o.getString("category"), o.getString("account"), LocalDate.parse(o.getString("date")), o.getString("note"), o.getBoolean("income")) } }
        require(entries.all { entry -> entry.id.isNotBlank() && entry.amount in 1..999999999999L && entry.note.length <= 500 && accounts.any { it.id == entry.account } && categories.any { it.id == entry.category && it.income == entry.income } })
        require(entries.map { it.id }.distinct().size == entries.size)
        val b = root.getJSONArray("budgets")
        val budgets = (0 until b.length()).map { b.getJSONObject(it).let { o -> Budget(o.getString("category"), o.getLong("limit")) } }
        require(budgets.all { it.limit in 1..999999999999L && categories.any { c -> c.id == it.category && !c.income } })
        require(budgets.map { it.category }.distinct().size == budgets.size)
        val db = writableDatabase; db.beginTransaction()
        try {
            db.delete("entries", null, null); db.delete("accounts", null, null); db.delete("budgets", null, null)
            accounts.forEach { db.execSQL("INSERT INTO accounts VALUES(?,?,?)", arrayOf<Any>(it.id, it.title, it.opening)) }
            entries.forEach(::save); budgets.forEach { budget(it.category, it.limit) }
            db.setTransactionSuccessful()
        } finally { db.endTransaction() }
    }
}

class FinanceViewModel(application: Application) : AndroidViewModel(application) {
    private val db = FinanceDatabase(application)
    var state by mutableStateOf(FinanceState()); private set
    var error by mutableStateOf<String?>(null); private set
    var busy by mutableStateOf(false); private set
    init { mutate {} }
    private fun mutate(action: () -> Unit) {
        if (busy) return
        busy = true
        viewModelScope.launch {
            try { state = withContext(Dispatchers.IO) { action(); db.read() } }
            catch (_: Exception) { error = "Не удалось сохранить данные. Попробуйте ещё раз." }
            finally { busy = false }
        }
    }
    fun save(e: Entry) = mutate { db.save(e) }
    fun delete(id: String) = mutate { db.delete(id) }
    fun budget(id: String, amount: Long) = mutate { db.budget(id, amount) }
    fun deleteBudget(id: String) = mutate { db.deleteBudget(id) }
    fun account(id: String, title: String, opening: Long) = mutate { db.account(id, title, opening) }
    fun demo() = mutate { db.demo() }
    fun reset() = mutate { db.reset() }
    fun clearError() { error = null }
    override fun onCleared() { db.close(); super.onCleared() }
    suspend fun backup(): String = withContext(Dispatchers.IO) { db.backup() }
    suspend fun restore(raw: String): Boolean {
        if(busy) return false
        busy = true
        return try { withContext(Dispatchers.IO) { db.restore(raw) }; state = withContext(Dispatchers.IO) { db.read() }; true }
        catch (_: Exception) { false }
        finally { busy = false }
    }
}
