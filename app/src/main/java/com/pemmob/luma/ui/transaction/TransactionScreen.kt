package com.pemmob.luma.ui.transaction

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pemmob.luma.data.local.entity.TransactionEntity
import com.pemmob.luma.ui.theme.SakuCanvasBackground
import com.pemmob.luma.ui.theme.SakuCardBackground
import com.pemmob.luma.ui.theme.SakuInputBackground
import com.pemmob.luma.ui.theme.SakuTextDark
import com.pemmob.luma.ui.theme.SakuTextMuted
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionScreen(
    viewModel: TransactionViewModel = hiltViewModel(),
    onNavigateBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentFilter by viewModel.currentFilter.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var transactionToEdit by remember { mutableStateOf<TransactionEntity?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedDateFilter by remember { mutableStateOf<Long?>(null) }
    
    val currentBalance = if (uiState is TransactionUiState.Success) {
        (uiState as TransactionUiState.Success).balance
    } else {
        0L
    }

    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val cal = Calendar.getInstance().apply {
                set(year, month, dayOfMonth, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            selectedDateFilter = cal.timeInMillis
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Riwayat Transaksi", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = SakuTextDark) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SakuCanvasBackground,
                    titleContentColor = SakuTextDark
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFF4338CA),
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Transaksi")
            }
        },
        containerColor = SakuCanvasBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Search Bar + Kalender
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari transaksi...", color = SakuTextMuted) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SakuTextMuted) },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SakuInputBackground,
                        unfocusedContainerColor = SakuInputBackground,
                        disabledContainerColor = SakuInputBackground,
                        focusedBorderColor = Color(0xFF4338CA),
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                )

                Surface(
                    onClick = { datePickerDialog.show() },
                    shape = RoundedCornerShape(16.dp),
                    color = SakuInputBackground,
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    modifier = Modifier.size(54.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = "Pilih Tanggal",
                            tint = Color(0xFF4338CA)
                        )
                    }
                }
            }

            if (selectedDateFilter != null) {
                val filterDateFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Filter Tanggal: ${filterDateFormat.format(Date(selectedDateFilter!!))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF4338CA),
                        fontWeight = FontWeight.SemiBold
                    )
                    TextButton(onClick = { selectedDateFilter = null }) {
                        Text("Reset Tanggal", fontSize = 12.sp, color = Color(0xFFF43F5E))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    FilterChipItem(
                        text = "Semua",
                        selected = currentFilter == TransactionFilter.ALL,
                        onClick = { viewModel.setFilter(TransactionFilter.ALL) }
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    FilterChipItem(
                        text = "Pemasukan",
                        selected = currentFilter == TransactionFilter.INCOME,
                        onClick = { viewModel.setFilter(TransactionFilter.INCOME) }
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    FilterChipItem(
                        text = "Pengeluaran",
                        selected = currentFilter == TransactionFilter.EXPENSE,
                        onClick = { viewModel.setFilter(TransactionFilter.EXPENSE) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Summary Cards
            if (uiState is TransactionUiState.Success) {
                val successState = uiState as TransactionUiState.Success
                FinancialSummaryCards(
                    totalIncome = successState.totalIncome,
                    totalExpense = successState.totalExpense
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Content State Handling & Grouped Date Transaction List
            when (val state = uiState) {
                is TransactionUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF4338CA))
                    }
                }
                is TransactionUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                is TransactionUiState.Success -> {
                    val displayedTransactions = state.transactions.filter { tx ->
                        val matchesSearch = searchQuery.isBlank() ||
                            tx.category.contains(searchQuery, ignoreCase = true) ||
                            tx.note.contains(searchQuery, ignoreCase = true) ||
                            tx.wallet.contains(searchQuery, ignoreCase = true)

                        val matchesDate = selectedDateFilter == null || {
                            val txCal = Calendar.getInstance().apply { timeInMillis = tx.date }
                            val filterCal = Calendar.getInstance().apply { timeInMillis = selectedDateFilter!! }
                            txCal.get(Calendar.YEAR) == filterCal.get(Calendar.YEAR) &&
                                txCal.get(Calendar.DAY_OF_YEAR) == filterCal.get(Calendar.DAY_OF_YEAR)
                        }()

                        matchesSearch && matchesDate
                    }

                    if (displayedTransactions.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Tidak ada transaksi yang ditemukan.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = SakuTextMuted
                            )
                        }
                    } else {
                        GroupedTransactionList(
                            transactions = displayedTransactions,
                            onEdit = { transactionToEdit = it }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog || transactionToEdit != null) {
        TransactionFormDialog(
            transactionToEdit = transactionToEdit,
            currentBalance = currentBalance,
            onDismiss = {
                showAddDialog = false
                transactionToEdit = null
            },
            onSave = { type, amount, category, wallet, note, date ->
                if (transactionToEdit == null) {
                    viewModel.addTransaction(type, amount, category, wallet, note, date)
                } else {
                    viewModel.updateTransaction(transactionToEdit!!, type, amount, category, wallet, note, date)
                }
                showAddDialog = false
                transactionToEdit = null
            },
            onDelete = if (transactionToEdit != null) {
                {
                    viewModel.deleteTransaction(transactionToEdit!!)
                    transactionToEdit = null
                }
            } else null
        )
    }
}

@Composable
fun FilterChipItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (selected) Color(0xFF4338CA) else SakuCardBackground
    val contentColor = if (selected) Color.White else SakuTextMuted

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = backgroundColor,
        shadowElevation = if (selected) 2.dp else 0.dp,
        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = contentColor,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
fun FinancialSummaryCards(
    totalIncome: Long,
    totalExpense: Long
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID")).apply {
        maximumFractionDigits = 0
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = SakuCardBackground),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Card Pengeluaran
            Card(
                colors = CardDefaults.cardColors(containerColor = SakuInputBackground),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFEE2E2)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = Color(0xFFF43F5E),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pengeluaran",
                            fontSize = 13.sp,
                            color = SakuTextMuted,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = currencyFormat.format(totalExpense),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF43F5E)
                    )
                }
            }

            // Card Pemasukan
            Card(
                colors = CardDefaults.cardColors(containerColor = SakuInputBackground),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFDCFCE7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pemasukan",
                            fontSize = 13.sp,
                            color = SakuTextMuted,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = currencyFormat.format(totalIncome),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                }
            }
        }
    }
}

@Composable
fun GroupedTransactionList(
    transactions: List<TransactionEntity>,
    onEdit: (TransactionEntity) -> Unit
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID")).apply {
        maximumFractionDigits = 0
    }
    val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
    val timeFormat = SimpleDateFormat("HH:mm", Locale("id", "ID"))
    val todayStr = dateFormat.format(Date())

    val calendarYesterday = Calendar.getInstance().apply { add(Calendar.DATE, -1) }
    val yesterdayStr = dateFormat.format(calendarYesterday.time)

    val grouped = transactions.groupBy { tx ->
        val txDateStr = dateFormat.format(Date(tx.date))
        when (txDateStr) {
            todayStr -> "Hari Ini, $txDateStr"
            yesterdayStr -> "Kemarin, $txDateStr"
            else -> txDateStr
        }
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        grouped.forEach { (dateHeader, txList) ->
            val dayIncome = txList.filter { it.type == "INCOME" }.sumOf { it.amount }
            val dayExpense = txList.filter { it.type == "EXPENSE" }.sumOf { it.amount }
            val dayNet = dayIncome - dayExpense
            val dayNetStr = "${if (dayNet >= 0) "+" else ""}${currencyFormat.format(dayNet)}"

            item(key = dateHeader) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = dateHeader,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = SakuTextDark
                        )
                        // Net total next to date header made muted gray (SakuTextMuted) as requested
                        Text(
                            text = dayNetStr,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = SakuTextMuted
                        )
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SakuCardBackground),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            txList.forEachIndexed { index, transaction ->
                                TransactionGroupItem(
                                    transaction = transaction,
                                    timeStr = timeFormat.format(Date(transaction.date)),
                                    onClick = { onEdit(transaction) }
                                )
                                if (index < txList.size - 1) {
                                    HorizontalDivider(
                                        color = Color(0xFFF1F5F9),
                                        thickness = 1.dp,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun getCategoryIcon(category: String, isIncome: Boolean): ImageVector {
    if (isIncome) return Icons.Default.ArrowDownward
    return when (category.lowercase()) {
        "makanan" -> Icons.Default.Restaurant
        "transportasi", "transport" -> Icons.Default.DirectionsCar
        "pendidikan" -> Icons.Default.School
        "tagihan" -> Icons.Default.Receipt
        "belanja" -> Icons.Default.ShoppingBag
        "hiburan" -> Icons.Default.Movie
        "kesehatan" -> Icons.Default.LocalHospital
        else -> Icons.Default.Category
    }
}

@Composable
fun TransactionGroupItem(
    transaction: TransactionEntity,
    timeStr: String,
    onClick: () -> Unit
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID")).apply {
        maximumFractionDigits = 0
    }
    val isIncome = transaction.type == "INCOME"
    val categoryIcon = getCategoryIcon(transaction.category, isIncome)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0E7FF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryIcon,
                    contentDescription = null,
                    tint = Color(0xFF4338CA),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = if (transaction.note.isNotBlank()) transaction.note else transaction.category,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = SakuTextDark
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFE0E7FF)
                    ) {
                        Text(
                            text = transaction.category,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF4338CA),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = timeStr,
                        fontSize = 11.sp,
                        color = SakuTextMuted
                    )
                }
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${if (isIncome) "+" else "-"}${currencyFormat.format(transaction.amount)}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = if (isIncome) Color(0xFF10B981) else Color(0xFFF43F5E)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionFormDialog(
    transactionToEdit: TransactionEntity?,
    currentBalance: Long,
    onDismiss: () -> Unit,
    onSave: (type: String, amount: Long, category: String, wallet: String, note: String, date: Long) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var type by remember { mutableStateOf(transactionToEdit?.type ?: "EXPENSE") }
    var amountText by remember { mutableStateOf(transactionToEdit?.amount?.toString() ?: "") }
    
    val categories = if (type == "INCOME") TransactionCategories.incomeCategories else TransactionCategories.expenseCategories
    var category by remember { mutableStateOf(transactionToEdit?.category ?: categories.first()) }
    
    var wallet by remember { mutableStateOf(transactionToEdit?.wallet ?: TransactionWallets.walletOptions.first()) }
    var note by remember { mutableStateOf(transactionToEdit?.note ?: "") }
    var date by remember { mutableStateOf(transactionToEdit?.date ?: System.currentTimeMillis()) }

    var expandedCategory by remember { mutableStateOf(false) }
    var expandedWallet by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID")).apply {
        maximumFractionDigits = 0
    }
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID"))

    val calendar = Calendar.getInstance().apply { timeInMillis = date }
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val newCalendar = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }
            date = newCalendar.timeInMillis
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (transactionToEdit == null) "Tambah Transaksi" else "Edit Transaksi", fontWeight = FontWeight.Bold, color = SakuTextDark) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Type selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            type = "EXPENSE"
                            category = TransactionCategories.expenseCategories.first()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (type == "EXPENSE") Color(0xFFF43F5E) else SakuInputBackground,
                            contentColor = if (type == "EXPENSE") Color.White else SakuTextMuted
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Pengeluaran")
                    }
                    Button(
                        onClick = {
                            type = "INCOME"
                            category = TransactionCategories.incomeCategories.first()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (type == "INCOME") Color(0xFF10B981) else SakuInputBackground,
                            contentColor = if (type == "INCOME") Color.White else SakuTextMuted
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Pemasukan")
                    }
                }

                // Amount
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Jumlah Nominal (Rp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SakuInputBackground,
                        unfocusedContainerColor = SakuInputBackground,
                        focusedBorderColor = Color(0xFF4338CA),
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    )
                )
                Text(
                    text = "Sisa saldo saat ini: ${currencyFormat.format(currentBalance)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = SakuTextMuted
                )

                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedCategory,
                    onExpandedChange = { expandedCategory = !expandedCategory }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Kategori") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SakuInputBackground,
                            unfocusedContainerColor = SakuInputBackground,
                            focusedBorderColor = Color(0xFF4338CA),
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCategory,
                        onDismissRequest = { expandedCategory = false }
                    ) {
                        val activeCategories = if (type == "INCOME") TransactionCategories.incomeCategories else TransactionCategories.expenseCategories
                        activeCategories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    expandedCategory = false
                                }
                            )
                        }
                    }
                }

                // Wallet Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedWallet,
                    onExpandedChange = { expandedWallet = !expandedWallet }
                ) {
                    OutlinedTextField(
                        value = wallet,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Dompet / Akun") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SakuInputBackground,
                            unfocusedContainerColor = SakuInputBackground,
                            focusedBorderColor = Color(0xFF4338CA),
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expandedWallet,
                        onDismissRequest = { expandedWallet = false }
                    ) {
                        TransactionWallets.walletOptions.forEach { w ->
                            DropdownMenuItem(
                                text = { Text(w) },
                                onClick = {
                                    wallet = w
                                    expandedWallet = false
                                }
                            )
                        }
                    }
                }

                // Date Picker Button
                OutlinedButton(
                    onClick = { datePickerDialog.show() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = SakuInputBackground),
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color(0xFF4338CA))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tanggal: ${dateFormat.format(Date(date))}", color = SakuTextDark)
                }

                // Note
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Catatan (Opsional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SakuInputBackground,
                        unfocusedContainerColor = SakuInputBackground,
                        focusedBorderColor = Color(0xFF4338CA),
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    )
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                // Delete button if editing
                if (transactionToEdit != null && onDelete != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedButton(
                        onClick = onDelete,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF43F5E)),
                        border = BorderStroke(1.dp, Color(0xFFF43F5E))
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Hapus Transaksi")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toLongOrNull()
                    if (amount == null || amount <= 0) {
                        errorMessage = "Masukkan nominal angka yang valid!"
                        return@Button
                    }
                    onSave(type, amount, category, wallet, note, date)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4338CA)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Simpan Transaksi")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = SakuTextMuted)
            }
        },
        containerColor = SakuCardBackground,
        shape = RoundedCornerShape(24.dp)
    )
}
