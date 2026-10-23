@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.financetracker.ui

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financetracker.*
import com.example.financetracker.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.min

private val Ru = Locale.forLanguageTag("ru-RU")
private val CardShape = RoundedCornerShape(24.dp)
private val iconPaths = mapOf(
    "home" to "M3 10 L12 3 L21 10 M5 9 V21 H10 V15 H14 V21 H19 V9",
    "grid" to "M3 3 H9 V9 H3 Z M15 3 H21 V9 H15 Z M3 15 H9 V21 H3 Z M15 15 H21 V21 H15 Z",
    "history" to "M4 7 H20 M4 12 H16 M4 17 H20 M2 7 H2 M2 12 H2 M2 17 H2",
    "chart" to "M4 20 V13 M10 20 V5 M16 20 V9 M22 20 V2",
    "target" to "M12 3 A9 9 0 1 0 21 12 M12 7 A5 5 0 1 0 17 12 M12 12 L21 3 M16 3 H21 V8",
    "plus" to "M12 5 V19 M5 12 H19",
    "close" to "M6 6 L18 18 M18 6 L6 18",
    "left" to "M15 5 L8 12 L15 19",
    "right" to "M9 5 L16 12 L9 19",
    "down" to "M6 9 L12 15 L18 9",
    "arrowDown" to "M12 4 V20 M6 14 L12 20 L18 14",
    "arrowUp" to "M12 20 V4 M6 10 L12 4 L18 10",
    "wallet" to "M3 5 H19 V9 M3 5 V20 H21 V8 H3 Z M16 12 H21 V16 H16 Z",
    "basket" to "M3 9 H21 L19 21 H5 Z M7 9 L10 3 M17 9 L14 3 M9 13 V17 M15 13 V17",
    "coffee" to "M4 8 H17 V16 Q17 20 13 20 H8 Q4 20 4 16 Z M17 9 H19 Q23 12 19 15 H17 M7 2 V4 M12 2 V4",
    "car" to "M4 11 L6 4 H18 L20 11 M3 11 H21 V19 H3 Z M6 19 V21 M18 19 V21 M6 15 H7 M17 15 H18",
    "bag" to "M4 7 H20 L21 21 H3 Z M8 8 V6 A4 4 0 0 1 16 6 V8",
    "heart" to "M12 21 L3 12 A5 5 0 0 1 12 5 A5 5 0 0 1 21 12 Z",
    "spark" to "M12 2 L15 9 L22 12 L15 15 L12 22 L9 15 L2 12 L9 9 Z",
    "gift" to "M3 8 H21 V12 H3 Z M5 12 V21 H19 V12 M12 8 V21 M12 8 C0 8 6 -3 12 8 C24 -3 24 8 12 8",
    "search" to "M16 16 L22 22 M18 10 A8 8 0 1 1 2 10 A8 8 0 1 1 18 10",
    "settings" to "M12 3 A9 9 0 1 0 12 21 A9 9 0 1 0 12 3 M12 8 A4 4 0 1 0 12 16 A4 4 0 1 0 12 8 M12 1 V5 M12 19 V23 M1 12 H5 M19 12 H23",
    "calendar" to "M3 5 H21 V21 H3 Z M3 10 H21 M7 2 V7 M17 2 V7 M7 14 H10 M14 14 H17",
    "check" to "M4 12 L9 17 L20 6",
    "download" to "M12 2 V16 M6 10 L12 16 L18 10 M3 17 V22 H21 V17",
    "shield" to "M12 2 L21 6 V12 Q21 19 12 23 Q3 19 3 12 V6 Z M8 12 L11 15 L17 9",
    "trash" to "M3 5 H21 M9 5 V2 H15 V5 M5 5 L6 22 H18 L19 5 M10 10 V17 M14 10 V17",
    "edit" to "M4 16 L17 3 L21 7 L8 20 L3 21 Z M14 6 L18 10"
)
@Composable
fun Glyph(name: String, modifier: Modifier = Modifier, color: Color = Ink) {
    val path = remember(name) { PathParser().parsePathString(iconPaths[name] ?: iconPaths.getValue("grid")).toPath() }
    Canvas(modifier.size(22.dp)) { scale(size.width / 24f, size.height / 24f, pivot = Offset.Zero) {
        drawPath(path, color, style = Stroke(1.7f, cap = StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
    } }
}
@Composable private fun RoundButton(icon: String, label: String, onClick: () -> Unit) {
    IconButton(onClick, Modifier.background(Color.White, CircleShape).size(46.dp)) { Glyph(icon, Modifier.semantics { contentDescription = label }) }
}
@Composable private fun Label(text: String, modifier: Modifier = Modifier, color: Color = Muted) = Text(text, modifier, color, fontSize = 13.sp, lineHeight = 18.sp, letterSpacing = 0.sp)
@Composable private fun Heading(text: String, action: String? = null, onClick: () -> Unit = {}) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(text, fontSize = 19.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        if(action != null) TextButton(onClick) { Text(action, fontSize = 13.sp) }
    }
}
@Composable private fun Panel(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.fillMaxWidth().clip(CardShape).background(Color.White).padding(20.dp), content = content)
}
@Composable private fun CategoryBadge(cat: Category, size: Int = 44) {
    Box(Modifier.size(size.dp).background(Color(cat.color).copy(alpha = .18f), RoundedCornerShape(15.dp)), contentAlignment = Alignment.Center) {
        Glyph(cat.icon, Modifier.size((size * .48).dp), Color(cat.color).let { if(cat.id == "food") Color(0xFFB17448) else it.copy(alpha = 1f) })
    }
}
@Composable private fun Chip(text: String, active: Boolean, onClick: () -> Unit) {
    Surface(onClick, color = if(active) Forest else Color.White, shape = RoundedCornerShape(14.dp)) {
        Text(text, Modifier.padding(horizontal = 16.dp, vertical = 12.dp), color = if(active) Color.White else Muted, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun FinanceApp(model: FinanceViewModel) {
    val state = model.state
    var page by rememberSaveable { mutableIntStateOf(0) }
    var monthText by rememberSaveable { mutableStateOf(YearMonth.now().toString()) }
    val month = YearMonth.parse(monthText)
    var account by rememberSaveable { mutableStateOf<String?>(null) }
    var settings by rememberSaveable { mutableStateOf(false) }
    var editor by rememberSaveable { mutableStateOf(false) }
    var editId by rememberSaveable { mutableStateOf<String?>(null) }
    var budgetEditor by rememberSaveable { mutableStateOf(false) }
    var budgetId by rememberSaveable { mutableStateOf<String?>(null) }
    val snack = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val visible = state.entries.month(month).filter { account == null || it.account == account }
    LaunchedEffect(model.error) { model.error?.let { snack.showSnackbar(it); model.clearError() } }
    LaunchedEffect(state.accounts) { if(account != null && state.accounts.none { it.id == account }) account = null }
    BackHandler(settings || page != 0) { if(settings) settings = false else page = 0 }
    Scaffold(containerColor = Canvas, snackbarHost = { SnackbarHost(snack) },
        bottomBar = { if(!settings) BottomNav(page) { page = it } },
        floatingActionButton = { if(!settings && state.ready) FloatingActionButton(onClick = { editId = null; editor = true },
            containerColor = Forest, contentColor = Color.White, shape = RoundedCornerShape(22.dp)) { Glyph("plus", Modifier.semantics { contentDescription = "Добавить операцию" }, Color.White) } }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(36.dp).background(Forest, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) { Glyph("chart", Modifier.size(20.dp), Lime) }
                Text("тихо", Modifier.padding(start = 9.dp).weight(1f), fontSize = 27.sp, fontWeight = FontWeight.Bold, letterSpacing = (-1).sp)
                Label("ФИНАНСЫ В ПОРЯДКЕ", Modifier.padding(end = 12.dp))
                RoundButton(if(settings) "close" else "settings", if(settings) "Закрыть настройки" else "Настройки") { settings = !settings }
            }
            if(!state.ready) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            else if(settings) Settings(model, snack)
            else {
                Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(listOf("Обзор", "Операции", "Аналитика", "Бюджеты")[page], fontSize = 30.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-.8).sp, modifier = Modifier.weight(1f))
                    IconButton({ monthText = month.minusMonths(1).toString() }, Modifier.size(36.dp)) { Glyph("left", Modifier.size(17.dp).semantics { contentDescription = "Предыдущий месяц" }) }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(month.format(DateTimeFormatter.ofPattern("LLLL", Ru)).replaceFirstChar { it.titlecase(Ru) }, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text(month.year.toString(), fontSize = 10.sp, color = Muted, lineHeight = 12.sp)
                    }
                    IconButton({ monthText = month.plusMonths(1).toString() }, Modifier.size(36.dp)) { Glyph("right", Modifier.size(17.dp).semantics { contentDescription = "Следующий месяц" }) }
                }
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(start = 24.dp, end = 24.dp, top = 14.dp, bottom = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Chip("Все счета", account == null) { account = null }
                    state.accounts.forEach { a -> Chip(a.title, account == a.id) { account = a.id } }
                }
                AnimatedContent(page, label = "page") { current ->
                    when(current) {
                        0 -> Overview(state, visible, month, account, { editId = null; editor = true }, { page = 1 }, { page = 2 }, { page = 3 }, { model.demo() }, { editId = it.id; editor = true })
                        1 -> Transactions(visible, state.accounts) { editId = it.id; editor = true }
                        2 -> Analytics(visible, month, state.entries.filter { account == null || it.account == account })
                        else -> Budgets(state, visible, account != null, { budgetId = null; budgetEditor = true }) { budgetId = it; budgetEditor = true }
                    }
                }
            }
        }
    }
    if(editor) EntryEditor(state, editId?.let { id -> state.entries.find { it.id == id } }, account, model.busy,
        { editor = false }, { model.save(it); editor = false }, { entry ->
            model.delete(entry.id); editor = false
            scope.launch { val result = snack.showSnackbar("Операция удалена", "Отменить", duration = SnackbarDuration.Long); if(result == SnackbarResult.ActionPerformed) model.save(entry) }
        })
    if(budgetEditor) BudgetEditor(state.budgets.find { it.category == budgetId }, { budgetEditor = false }, { id, amount -> model.budget(id, amount); budgetEditor = false }, { id -> model.deleteBudget(id); budgetEditor = false })
}

@Composable private fun BottomNav(page: Int, onSelect: (Int) -> Unit) {
    Surface(color = Color.White, shadowElevation = 10.dp) {
        Row(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 12.dp, vertical = 10.dp)) {
            listOf("grid" to "Обзор", "history" to "Операции", "chart" to "Аналитика", "target" to "Бюджеты").forEachIndexed { i, (icon, text) ->
                Column(Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).clickable { onSelect(i) }.padding(vertical = 5.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.width(52.dp).height(30.dp).background(if(page == i) Lime.copy(alpha = .55f) else Color.Transparent, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) { Glyph(icon, Modifier.size(20.dp), if(page == i) Forest else Muted) }
                    Text(text, Modifier.padding(top = 4.dp), color = if(page == i) Forest else Muted, fontSize = 11.sp, fontWeight = if(page == i) FontWeight.SemiBold else FontWeight.Normal)
                }
            }
        }
    }
}

@Composable private fun Overview(state: FinanceState, entries: List<Entry>, month: YearMonth, account: String?, add: () -> Unit,
    all: () -> Unit, analytics: () -> Unit, budgets: () -> Unit, demo: () -> Unit, edit: (Entry) -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 100.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        item {
            Column(Modifier.fillMaxWidth().clip(CardShape).background(Forest).padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) { Label("Общий баланс", Modifier.weight(1f), Color(0xFFC4D4C9)); Glyph("wallet", color = Lime) }
                Text(Money.format(state.balance(account)), Modifier.padding(top = 8.dp, bottom = 22.dp), color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Medium, letterSpacing = (-1).sp)
                HorizontalDivider(color = Color.White.copy(alpha = .13f))
                Row(Modifier.padding(top = 18.dp)) {
                    FlowTotal("Доходы", entries.incomes(), true, Modifier.weight(1f))
                    FlowTotal("Расходы", entries.expenses(), false, Modifier.weight(1f))
                }
            }
        }
        if(state.entries.isEmpty()) item {
            Panel {
                Box(Modifier.size(48.dp).background(Lime.copy(alpha = .5f), CircleShape), contentAlignment = Alignment.Center) { Glyph("spark") }
                Text("Спокойствие начинается\nс первой записи", Modifier.padding(top = 14.dp), fontSize = 23.sp, fontWeight = FontWeight.SemiBold)
                Label("Добавьте расход — и ваши финансы станут понятнее.", Modifier.padding(top = 10.dp))
                Button(add, Modifier.fillMaxWidth().padding(top = 16.dp), shape = RoundedCornerShape(14.dp)) { Text("Добавить первую операцию") }
                TextButton(demo, Modifier.align(Alignment.CenterHorizontally)) { Text("Посмотреть на примере") }
            }
        }
        if(entries.isNotEmpty()) item {
            Column {
                Heading("Расходы по категориям", "Все", analytics)
                Spacer(Modifier.height(10.dp))
                Panel {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Donut(entries, Modifier.size(130.dp))
                        Column(Modifier.padding(start = 22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            categoryTotals(entries).take(3).forEach { (cat, amount) ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(7.dp).background(Color(cat.color), CircleShape)); Spacer(Modifier.width(8.dp))
                                    Column { Label(cat.title); Text(Money.format(amount), fontSize = 14.sp, fontWeight = FontWeight.SemiBold) }
                                }
                            }
                        }
                    }
                }
            }
        }
        if(state.budgets.isNotEmpty()) item {
            val ids = state.budgets.map { it.category }
            val spent = entries.filter { !it.income && it.category in ids }.sumOf { it.amount }
            val limit = state.budgets.sumOf { it.limit }
            Column(Modifier.fillMaxWidth().clip(CardShape).background(Color(0xFFECF1E4)).clickable(onClick = budgets).padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) { Glyph("target", Modifier.size(22.dp)); Text("Бюджет под контролем", Modifier.weight(1f).padding(start = 10.dp), fontSize = 15.sp, fontWeight = FontWeight.Medium); Glyph("right", Modifier.size(16.dp)) }
                Progress(spent, limit, Forest, Modifier.padding(top = 14.dp, bottom = 8.dp))
                Label("${Money.format(spent)} из ${Money.format(limit)}${if(account != null) " · выбранный счёт" else ""}")
            }
        }
        item { Heading("Последние операции", "Все", all) }
        if(entries.isEmpty() && state.entries.isNotEmpty()) item { Empty("В этом месяце пока тихо", "Операции появятся здесь после добавления.") }
        items(entries.take(4), key = { it.id }) { EntryRow(it, state.accounts, edit) }
        item { Label(month.format(DateTimeFormatter.ofPattern("LLLL yyyy", Ru)).replaceFirstChar { it.titlecase(Ru) }, Modifier.fillMaxWidth(), Muted) }
    }
}
@Composable private fun FlowTotal(label: String, amount: Long, income: Boolean, modifier: Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(30.dp).background(Color.White.copy(alpha = .1f), CircleShape), contentAlignment = Alignment.Center) { Glyph(if(income) "arrowDown" else "arrowUp", Modifier.size(15.dp), if(income) Lime else Color(0xFFE8B99A)) }
        Column(Modifier.padding(start = 9.dp)) { Label(label, color = Color(0xFFC4D4C9)); Text(Money.format(amount), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium) }
    }
}
private fun categoryTotals(entries: List<Entry>) = entries.filter { !it.income }.groupBy { it.category }.map { (id, group) -> category(id) to group.sumOf { it.amount } }.sortedByDescending { it.second }
@Composable private fun Donut(entries: List<Entry>, modifier: Modifier = Modifier) {
    val total = entries.expenses()
    val parts = categoryTotals(entries)
    Box(modifier.semantics { contentDescription = "Расходы по категориям: ${Money.format(total)}" }, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize().padding(9.dp)) {
            val stroke = 15.dp.toPx()
            var start = -90f
            if(total == 0L) drawArc(Line, 0f, 360f, false, style = Stroke(stroke))
            parts.forEach { (cat, amount) -> val angle = amount.toFloat() / total.toFloat() * 360f
                drawArc(Color(cat.color), start + min(2f, angle/4), (angle - min(4f, angle/2)).coerceAtLeast(.1f), false, style = Stroke(stroke, cap = StrokeCap.Round)); start += angle
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) { Text("Всего", fontSize = 11.sp, color = Muted); Text("${parts.size}", fontSize = 24.sp, fontWeight = FontWeight.SemiBold); Text("категорий", fontSize = 10.sp, color = Muted) }
    }
}
@Composable private fun Progress(value: Long, total: Long, color: Color, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(6.dp).background(color.copy(alpha = .12f), CircleShape)) {
        Box(Modifier.fillMaxWidth(if(total > 0) (value.toFloat()/total).coerceIn(0f, 1f) else 0f).fillMaxHeight().background(color, CircleShape))
    }
}
@Composable private fun EntryRow(entry: Entry, accounts: List<Account>, onClick: (Entry) -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable { onClick(entry) }.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        CategoryBadge(category(entry.category))
        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(entry.note.ifBlank { category(entry.category).title }, fontSize = 15.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Label("${category(entry.category).title} · ${accounts.find { it.id == entry.account }?.title.orEmpty()}", Modifier.padding(top = 4.dp))
        }
        Column(horizontalAlignment = Alignment.End) {
            Text((if(entry.income) "+ " else "− ") + Money.format(entry.amount), color = if(entry.income) Forest else Ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Label(entry.date.format(DateTimeFormatter.ofPattern("d MMM", Ru)), Modifier.padding(top = 4.dp))
        }
    }
}
@Composable private fun Empty(title: String, body: String) {
    Column(Modifier.fillMaxWidth().padding(vertical = 36.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(68.dp).background(Lime.copy(alpha = .4f), CircleShape), contentAlignment = Alignment.Center) { Glyph("spark", Modifier.size(28.dp), Forest) }
        Text(title, Modifier.padding(top = 18.dp), fontSize = 19.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
        Label(body, Modifier.padding(top = 8.dp), Muted)
    }
}

@Composable private fun Transactions(entries: List<Entry>, accounts: List<Account>, edit: (Entry) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var type by rememberSaveable { mutableIntStateOf(0) }
    var catId by rememberSaveable { mutableStateOf<String?>(null) }
    val filtered = entries.filter { (type == 0 || it.income == (type == 2)) && (catId == null || it.category == catId) && (it.note.contains(query, true) || category(it.category).title.contains(query, true) || Money.format(it.amount).contains(query)) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 100.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth(), placeholder = { Text("Найти операцию", fontSize = 14.sp) }, leadingIcon = { Glyph("search", color = Muted) }, singleLine = true, shape = RoundedCornerShape(18.dp), colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Line, unfocusedContainerColor = Color.White)) }
        item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf("Все", "Расходы", "Доходы").forEachIndexed { i, t -> Chip(t, i == type) { type = i; catId = null } } } }
        item { Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            FilterChip(catId == null, { catId = null }, { Text("Все категории", fontSize = 12.sp) })
            categories.filter { cat -> entries.any { it.category == cat.id } }.forEach { cat -> FilterChip(catId == cat.id, { catId = cat.id }, { Text(cat.title, fontSize = 12.sp) }) }
        } }
        item { Panel { Label("${filtered.size} операций"); Text(Money.format(if(type == 2) filtered.incomes() else filtered.expenses()), fontSize = 28.sp, fontWeight = FontWeight.SemiBold); Label(if(type == 2) "Доходы по выбранным фильтрам" else "Расходы по выбранным фильтрам", Modifier.padding(top = 4.dp)) } }
        if(filtered.isEmpty()) item { Empty("Ничего не найдено", "Измените фильтры или добавьте операцию.") }
        filtered.groupBy { it.date }.forEach { (date, group) ->
            item(key = "day$date") { Label(if(date == LocalDate.now()) "СЕГОДНЯ" else date.format(DateTimeFormatter.ofPattern("d MMMM, EEEE", Ru)).uppercase(Ru), Modifier.padding(top = 6.dp)) }
            items(group, key = { it.id }) { EntryRow(it, accounts, edit) }
        }
    }
}

@Composable private fun Analytics(entries: List<Entry>, month: YearMonth, all: List<Entry>) {
    val expense = entries.expenses(); val previous = all.month(month.minusMonths(1)).expenses()
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 100.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        item { Panel {
            Label("Расходы за месяц")
            Text(Money.format(expense), Modifier.padding(top = 6.dp), fontSize = 34.sp, fontWeight = FontWeight.SemiBold)
            if(previous > 0) Label("${if(expense <= previous) "↓" else "↑"} ${kotlin.math.abs((expense - previous) * 100 / previous)}% к прошлому месяцу", Modifier.padding(top = 8.dp), if(expense <= previous) Forest else Color(0xFFB66F4A))
            else Label("Каждая запись делает картину яснее", Modifier.padding(top = 8.dp))
            Spacer(Modifier.height(24.dp))
            val weekTotals = (0 until (month.lengthOfMonth() + 6) / 7).map { week -> entries.filter { !it.income && (it.date.dayOfMonth - 1) / 7 == week }.sumOf { it.amount } }
            val max = weekTotals.maxOrNull()?.coerceAtLeast(1) ?: 1L
            Row(Modifier.fillMaxWidth().height(130.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.Bottom) {
                weekTotals.forEachIndexed { i, amount -> Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.fillMaxWidth().height((100f * amount / max).coerceAtLeast(5f).dp).background(if(amount == max && expense > 0) Forest else Lime.copy(alpha = .7f), RoundedCornerShape(topStart = 9.dp, topEnd = 9.dp)))
                    Text("${i*7+1}–${min((i+1)*7, month.lengthOfMonth())}", Modifier.padding(top = 10.dp), color = Muted, fontSize = 10.sp, lineHeight = 14.sp, letterSpacing = 0.sp)
                } }
            }
        } }
        item { Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Panel(Modifier.weight(1f).heightIn(min = 120.dp)) { Label("В среднем в день", Modifier.heightIn(min = 36.dp)); Text(Money.format(expense / (if(month == YearMonth.now()) LocalDate.now().dayOfMonth else month.lengthOfMonth())), Modifier.padding(top = 8.dp), fontSize = 20.sp, fontWeight = FontWeight.SemiBold) }
            Panel(Modifier.weight(1f).heightIn(min = 120.dp)) { Label("Осталось от доходов", Modifier.heightIn(min = 36.dp)); Text(Money.format(entries.incomes() - expense), Modifier.padding(top = 8.dp), fontSize = 20.sp, fontWeight = FontWeight.SemiBold) }
        } }
        item { Heading("Куда уходят деньги") }
        if(expense == 0L) item { Empty("Здесь будет ваша аналитика", "Добавьте расходы за выбранный месяц.") }
        else item { Panel { Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { Donut(entries, Modifier.size(190.dp)) }
            Spacer(Modifier.height(20.dp))
            categoryTotals(entries).forEach { (cat, amount) ->
                Row(Modifier.padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    CategoryBadge(cat, 36)
                    Column(Modifier.weight(1f).padding(horizontal = 10.dp)) { Text(cat.title, fontSize = 14.sp); Progress(amount, expense, Color(cat.color), Modifier.padding(top = 8.dp)) }
                    Column(horizontalAlignment = Alignment.End) { Text(Money.format(amount), fontSize = 14.sp, fontWeight = FontWeight.Medium); Label("${amount * 100 / expense}%") }
                }
            }
        } }
    }
}

@Composable private fun Budgets(state: FinanceState, entries: List<Entry>, filtered: Boolean, add: () -> Unit, edit: (String) -> Unit) {
    val total = state.budgets.sumOf { it.limit }
    val spent = entries.filter { !it.income && it.category in state.budgets.map { b -> b.category } }.sumOf { it.amount }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 100.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Column(Modifier.fillMaxWidth().background(Color(0xFFECF1E4), CardShape).padding(24.dp)) {
            Label("${if(filtered) "Расходы выбранного счёта" else "Запланировано на месяц"}")
            Text(Money.format(total), Modifier.padding(top = 6.dp, bottom = 16.dp), fontSize = 34.sp, fontWeight = FontWeight.SemiBold)
            Progress(spent, total, Forest)
            Label(if(total >= spent) "Ещё ${Money.format(total - spent)} в рамках бюджета" else "Превышение на ${Money.format(spent-total)}", Modifier.padding(top = 12.dp))
        } }
        item { Heading("Лимиты по категориям", "+ Создать", add) }
        if(state.budgets.isEmpty()) item { Empty("Тратьте в своём ритме", "Создайте первый месячный лимит.") }
        items(state.budgets, key = { it.category }) { b ->
            val cat = category(b.category); val amount = entries.filter { !it.income && it.category == b.category }.sumOf { it.amount }
            val over = amount > b.limit
            Panel(Modifier.clickable { edit(b.category) }) {
                Row(verticalAlignment = Alignment.CenterVertically) { CategoryBadge(cat); Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(cat.title, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Label(if(over) "Лимит превышен" else "${(amount*100/b.limit)}% использовано", Modifier.padding(top = 4.dp), if(over) Color(0xFFB66F4A) else Muted)
                }; Glyph("edit", Modifier.size(17.dp), Muted) }
                Progress(amount, b.limit, if(over) Color(0xFFB66F4A) else Color(cat.color), Modifier.padding(top = 20.dp, bottom = 12.dp))
                Row { Text(Money.format(amount), fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f)); Label("из ${Money.format(b.limit)}") }
            }
        }
        item { Label("Лимиты повторяются каждый месяц. Нажмите на категорию, чтобы изменить сумму.", Modifier.padding(top = 4.dp)) }
    }
}

@Composable private fun EntryEditor(state: FinanceState, entry: Entry?, defaultAccount: String?, busy: Boolean, dismiss: () -> Unit, save: (Entry) -> Unit, delete: (Entry) -> Unit) {
    var income by rememberSaveable { mutableStateOf(entry?.income ?: false) }
    var amount by rememberSaveable { mutableStateOf(entry?.let { Money.input(it.amount) } ?: "") }
    var cat by rememberSaveable { mutableStateOf(entry?.category ?: "food") }
    var account by rememberSaveable { mutableStateOf(entry?.account ?: defaultAccount ?: state.accounts.first().id) }
    var date by rememberSaveable { mutableStateOf((entry?.date ?: LocalDate.now()).toString()) }
    var note by rememberSaveable { mutableStateOf(entry?.note ?: "") }
    var attempted by rememberSaveable { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    val context = LocalContext.current
    ModalBottomSheet(dismiss, containerColor = Canvas, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.fillMaxWidth().imePadding().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp).padding(bottom = 30.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) { Text(if(entry == null) "Новая операция" else "Изменить операцию", fontSize = 25.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f)); IconButton(dismiss) { Glyph("close", Modifier.semantics { contentDescription = "Закрыть" }) } }
            Row(Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(16.dp)).padding(5.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(false to "Расход", true to "Доход").forEach { (value, title) ->
                    Surface({ income = value; cat = if(value) "salary" else "food" }, Modifier.weight(1f), shape = RoundedCornerShape(12.dp), color = if(income == value) Forest else Color.White) {
                        Text(title, Modifier.padding(12.dp), textAlign = TextAlign.Center, color = if(income == value) Color.White else Muted, fontWeight = FontWeight.Medium)
                    }
                }
            }
            OutlinedTextField(amount, { if(it.length <= 14) amount = it }, Modifier.fillMaxWidth(), label = { Text("Сумма, ₽") }, placeholder = { Text("0") }, textStyle = LocalTextStyle.current.copy(fontSize = 36.sp, fontWeight = FontWeight.Medium), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, shape = RoundedCornerShape(18.dp), isError = attempted && Money.parse(amount) == null, supportingText = { if(attempted && Money.parse(amount) == null) Text("Введите сумму больше нуля, до 2 знаков после запятой") })
            Label("КАТЕГОРИЯ")
            val available = categories.filter { it.income == income }
            available.chunked(4).forEach { row -> Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEach { c -> Column(Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).background(if(cat == c.id) Color.White else Color.Transparent).border(if(cat == c.id) 1.dp else 0.dp, if(cat == c.id) Forest else Color.Transparent, RoundedCornerShape(16.dp)).clickable { cat = c.id }.padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    CategoryBadge(c, 38); Text(c.title, Modifier.padding(top = 6.dp), fontSize = 10.sp, textAlign = TextAlign.Center, maxLines = 2, lineHeight = 13.sp)
                } }; repeat(4-row.size) { Spacer(Modifier.weight(1f)) }
            } }
            Label("СЧЁТ")
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) { state.accounts.forEach { a -> Chip(a.title, account == a.id) { account = a.id } } }
            Surface(onClick = { val d = LocalDate.parse(date); DatePickerDialog(context, { _, y, m, day -> date = LocalDate.of(y,m+1,day).toString() }, d.year, d.monthValue-1, d.dayOfMonth).apply { datePicker.maxDate = System.currentTimeMillis() }.show() }, shape = RoundedCornerShape(16.dp)) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Glyph("calendar"); Text(LocalDate.parse(date).format(DateTimeFormatter.ofPattern("d MMMM yyyy", Ru)), Modifier.weight(1f).padding(start = 12.dp)); Glyph("right", Modifier.size(16.dp)) }
            }
            OutlinedTextField(note, { if(it.length <= 500) note = it }, Modifier.fillMaxWidth(), label = { Text("Заметка (необязательно)") }, shape = RoundedCornerShape(16.dp), maxLines = 3)
            Button({ attempted = true; Money.parse(amount)?.let { save(Entry(entry?.id ?: java.util.UUID.randomUUID().toString(), it, cat, account, LocalDate.parse(date), note.trim(), income)) } }, Modifier.fillMaxWidth().height(56.dp), enabled = !busy, shape = RoundedCornerShape(18.dp)) { Text("Сохранить операцию", fontSize = 16.sp) }
            if(entry != null) TextButton({ confirmDelete = true }, Modifier.align(Alignment.CenterHorizontally)) { Text("Удалить операцию", color = Color(0xFFAD5C4C)) }
        }
    }
    if(confirmDelete && entry != null) AlertDialog({ confirmDelete = false }, title = { Text("Удалить операцию?") }, text = { Text("${entry.note.ifBlank { category(entry.category).title }} · ${Money.format(entry.amount)}") }, confirmButton = { TextButton({ delete(entry); confirmDelete = false }) { Text("Удалить") } }, dismissButton = { TextButton({ confirmDelete = false }) { Text("Отмена") } })
}

@Composable private fun BudgetEditor(budget: Budget?, dismiss: () -> Unit, save: (String, Long) -> Unit, delete: (String) -> Unit) {
    var cat by rememberSaveable { mutableStateOf(budget?.category ?: "food") }
    var amount by rememberSaveable { mutableStateOf(budget?.let { Money.input(it.limit) } ?: "") }
    var attempted by rememberSaveable { mutableStateOf(false) }
    ModalBottomSheet(dismiss, containerColor = Canvas, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.imePadding().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Text(if(budget == null) "Новый бюджет" else "Настроить бюджет", fontSize = 26.sp, fontWeight = FontWeight.SemiBold)
            Label("Месячный лимит на выбранную категорию")
            categories.filter { !it.income }.chunked(2).forEach { row -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { row.forEach { c -> FilterChip(cat == c.id, { if(budget == null) cat = c.id }, { Text(c.title) }, Modifier.weight(1f), enabled = budget == null || cat == c.id) } } }
            OutlinedTextField(amount, { if(it.length <= 14) amount = it }, Modifier.fillMaxWidth(), label = { Text("Лимит, ₽") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, shape = RoundedCornerShape(16.dp), isError = attempted && Money.parse(amount) == null)
            Button({ attempted = true; Money.parse(amount)?.let { save(cat, it) } }, Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(16.dp)) { Text("Сохранить бюджет") }
            if(budget != null) TextButton({ delete(budget.category) }, Modifier.align(Alignment.CenterHorizontally)) { Text("Удалить лимит", color = Color(0xFFAD5C4C)) }
        }
    }
}

@Composable private fun Settings(model: FinanceViewModel, snack: SnackbarHostState) {
    val state = model.state; val context = LocalContext.current; val scope = rememberCoroutineScope()
    var accountId by rememberSaveable { mutableStateOf<String?>(null) }
    var importRaw by remember { mutableStateOf<String?>(null) }
    var fileBusy by remember { mutableStateOf(false) }
    var resetConfirm by remember { mutableStateOf(false) }
    fun message(text: String) { scope.launch { snack.showSnackbar(text) } }
    val csv = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri -> if(uri != null) scope.launch {
        try { withContext(Dispatchers.IO) {
            fun cell(s: String) = "\"${s.replace("\"", "\"\"").let { if(it.startsWith("=") || it.startsWith("+") || it.startsWith("-") || it.startsWith("@")) "'" + it else it }}\""
            val content = "\uFEFFДата;Тип;Сумма RUB;Категория;Счёт;Заметка\r\n" + state.entries.joinToString("\r\n") { e -> listOf(e.date.toString(), if(e.income) "Доход" else "Расход", Money.input(e.amount), category(e.category).title, state.accounts.find { it.id == e.account }?.title.orEmpty(), e.note).joinToString(";", transform = ::cell) }
            checkNotNull(context.contentResolver.openOutputStream(uri)).bufferedWriter(Charsets.UTF_8).use { it.write(content) }
        }; message("CSV сохранён") } catch (_: Exception) { message("Не удалось сохранить CSV") }
    } }
    val backup = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri -> if(uri != null) scope.launch {
        fileBusy = true
        try { val raw = model.backup(); withContext(Dispatchers.IO) { checkNotNull(context.contentResolver.openOutputStream(uri)).bufferedWriter().use { it.write(raw) } }; message("Резервная копия сохранена") } catch (_: Exception) { message("Не удалось создать копию") }; fileBusy = false
    } }
    val restore = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> if(uri != null) scope.launch {
        try { importRaw = withContext(Dispatchers.IO) {
            checkNotNull(context.contentResolver.openInputStream(uri)).use { stream ->
                val output = java.io.ByteArrayOutputStream()
                val buffer = ByteArray(8192)
                var count = stream.read(buffer)
                while(count != -1) { require(output.size() + count <= 10_000_000); output.write(buffer, 0, count); count = stream.read(buffer) }
                output.toString("UTF-8")
            }
        } }
        catch (_: Exception) { message("Не удалось прочитать файл") }
    } }
    LazyColumn(contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        item { Text("Настройки", fontSize = 30.sp, fontWeight = FontWeight.SemiBold) }
        item { Heading("Мои счета") }
        items(state.accounts, key = { it.id }) { a -> Panel(Modifier.clickable { accountId = a.id }) { Row(verticalAlignment = Alignment.CenterVertically) { Glyph("wallet"); Column(Modifier.weight(1f).padding(start = 14.dp)) { Text(a.title, fontWeight = FontWeight.Medium); Label("Начальный остаток: ${Money.format(a.opening)}", Modifier.padding(top = 6.dp)) }; Glyph("edit", Modifier.size(18.dp)) }; Text(Money.format(state.balance(a.id)), Modifier.padding(top = 16.dp), fontSize = 25.sp, fontWeight = FontWeight.SemiBold) } }
        item { Heading("Ваши данные") }
        item { Panel {
            SettingsAction("download", "Экспорт операций", "Таблица CSV для Excel", !fileBusy) { csv.launch("Тихо-операции-${LocalDate.now()}.csv") }
            HorizontalDivider(Modifier.padding(vertical = 12.dp), color = Line)
            SettingsAction("shield", "Создать резервную копию", "Все счета, операции и бюджеты", !fileBusy) { backup.launch("Тихо-копия-${LocalDate.now()}.json") }
            HorizontalDivider(Modifier.padding(vertical = 12.dp), color = Line)
            SettingsAction("history", "Восстановить данные", "Из резервной копии Тихо", !fileBusy && !model.busy) { restore.launch(arrayOf("application/json", "text/plain")) }
        } }
        item { Panel { Glyph("shield", color = Forest); Text("Ваши финансы — только ваши", Modifier.padding(top = 12.dp), fontWeight = FontWeight.SemiBold); Label("Данные хранятся на этом устройстве. Приложение работает без регистрации и доступа к интернету. Для переноса используйте резервную копию.", Modifier.padding(top = 8.dp)) } }
        item { TextButton({ resetConfirm = true }, enabled = !fileBusy && !model.busy) { Text("Начать с чистого листа", color = Color(0xFFAD5C4C)) } }
        item { Label("Тихо 1.0 · Сделано для спокойствия", Modifier.fillMaxWidth()) }
    }
    accountId?.let { id -> state.accounts.find { it.id == id }?.let { a -> AccountEditor(a, { accountId = null }) { title, opening -> model.account(a.id, title, opening); accountId = null } } }
    if(importRaw != null) AlertDialog({ importRaw = null }, title = { Text("Восстановить из копии?") }, text = { Text("Текущие счета, операции и бюджеты будут заменены данными из файла. Рекомендуем сначала сохранить текущую копию.") }, confirmButton = { TextButton({ val raw = importRaw!!; importRaw = null; fileBusy = true; scope.launch { val ok = model.restore(raw); fileBusy = false; message(if(ok) "Данные восстановлены" else "Некорректная копия. Данные не изменены.") } }) { Text("Восстановить") } }, dismissButton = { TextButton({ importRaw = null }) { Text("Отмена") } })
    if(resetConfirm) AlertDialog({ resetConfirm = false }, title = { Text("Начать с чистого листа?") }, text = { Text("Все операции и бюджеты будут удалены, остатки счетов обнулены. Сначала сохраните резервную копию, если данные понадобятся.") }, confirmButton = { TextButton({ model.reset(); resetConfirm = false }) { Text("Очистить данные") } }, dismissButton = { TextButton({ resetConfirm = false }) { Text("Отмена") } })
}
@Composable private fun SettingsAction(icon: String, title: String, subtitle: String, enabled: Boolean, action: () -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).clickable(enabled = enabled, onClick = action).padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
        Glyph(icon, color = Forest); Column(Modifier.weight(1f).padding(horizontal = 12.dp)) { Text(title, fontSize = 15.sp, fontWeight = FontWeight.Medium); Label(subtitle, Modifier.padding(top = 4.dp)) }; Glyph("right", Modifier.size(16.dp), Muted)
    }
}
@Composable private fun AccountEditor(account: Account, dismiss: () -> Unit, save: (String, Long) -> Unit) {
    var title by rememberSaveable { mutableStateOf(account.title) }; var amount by rememberSaveable { mutableStateOf(Money.input(account.opening)) }; var attempted by remember { mutableStateOf(false) }
    val parsed = if(amount.trim() == "0") 0L else Money.parse(amount.removePrefix("-"))?.let { if(amount.startsWith("-")) -it else it }
    AlertDialog(dismiss, title = { Text("Настроить счёт") }, text = { Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(title, { if(it.length <= 40) title = it }, label = { Text("Название") }, singleLine = true, isError = attempted && title.isBlank())
        OutlinedTextField(amount, { if(it.length <= 15) amount = it }, label = { Text("Начальный остаток, ₽") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), isError = attempted && parsed == null, singleLine = true)
        Label("Баланс = начальный остаток + доходы − расходы.")
    } }, confirmButton = { TextButton({ attempted = true; if(title.isNotBlank() && parsed != null) save(title.trim(), parsed) }) { Text("Сохранить") } }, dismissButton = { TextButton(dismiss) { Text("Отмена") } })
}
