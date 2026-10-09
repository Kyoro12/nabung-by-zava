package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryEntity
import com.example.data.model.TransactionEntity
import com.example.ui.components.CategoryIconHelper
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpenseRedLight
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.IncomeGreenLight
import com.example.util.CurrencyUtils
import com.example.util.DateUtils
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionScreen(
    transactions: List<TransactionEntity>,
    categories: List<CategoryEntity>,
    onAddTransaction: (title: String, amount: Long, type: String, category: String, categoryIcon: String, dateMillis: Long, note: String, paymentMethod: String) -> Unit,
    onUpdateTransaction: (TransactionEntity) -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit,
    onAddCategory: (name: String, type: String, icon: String, colorHex: String) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf("ALL") }
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }
    var selectedPeriodFilter by remember { mutableStateOf("ALL") }

    // Dialog & BottomSheet state
    var showFormSheet by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    var transactionToDelete by remember { mutableStateOf<TransactionEntity?>(null) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }

    val filteredTransactions by remember(transactions, searchQuery, selectedTypeFilter, selectedCategoryFilter, selectedPeriodFilter) {
        derivedStateOf {
            transactions.filter { tx ->
                val matchesType = when (selectedTypeFilter) {
                    "INCOME" -> tx.type == "INCOME"
                    "EXPENSE" -> tx.type == "EXPENSE"
                    else -> true
                }
                val matchesCategory = (selectedCategoryFilter == "ALL" || tx.category == selectedCategoryFilter)
                val matchesSearch = searchQuery.isBlank() ||
                        tx.title.contains(searchQuery, ignoreCase = true) ||
                        tx.category.contains(searchQuery, ignoreCase = true) ||
                        tx.note.contains(searchQuery, ignoreCase = true)

                val matchesPeriod = when (selectedPeriodFilter) {
                    "TODAY" -> DateUtils.isToday(tx.dateMillis)
                    "THIS_MONTH" -> DateUtils.isThisMonth(tx.dateMillis)
                    "LAST_MONTH" -> tx.dateMillis in DateUtils.getStartOfLastMonth()..DateUtils.getEndOfLastMonth()
                    else -> true
                }

                matchesType && matchesCategory && matchesSearch && matchesPeriod
            }
        }
    }

    val totalFilteredIncome = remember(filteredTransactions) {
        filteredTransactions.filter { it.type == "INCOME" }.sumOf { it.amount }
    }
    val totalFilteredExpense = remember(filteredTransactions) {
        filteredTransactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingTransaction = null
                    showFormSheet = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_transaction_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah Transaksi")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Riwayat Transaksi",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_transaction_input"),
                    placeholder = { Text("Cari catatan, kategori, nominal...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Hapus Pencarian")
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedTypeFilter == "ALL",
                        onClick = { selectedTypeFilter = "ALL" },
                        label = { Text("Semua") }
                    )
                    FilterChip(
                        selected = selectedTypeFilter == "INCOME",
                        onClick = { selectedTypeFilter = "INCOME" },
                        label = { Text("Pemasukan") }
                    )
                    FilterChip(
                        selected = selectedTypeFilter == "EXPENSE",
                        onClick = { selectedTypeFilter = "EXPENSE" },
                        label = { Text("Pengeluaran") }
                    )
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(end = 16.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedPeriodFilter == "ALL",
                            onClick = { selectedPeriodFilter = "ALL" },
                            label = { Text("Semua Waktu") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedPeriodFilter == "TODAY",
                            onClick = { selectedPeriodFilter = "TODAY" },
                            label = { Text("Hari Ini") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedPeriodFilter == "THIS_MONTH",
                            onClick = { selectedPeriodFilter = "THIS_MONTH" },
                            label = { Text("Bulan Ini") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedPeriodFilter == "LAST_MONTH",
                            onClick = { selectedPeriodFilter = "LAST_MONTH" },
                            label = { Text("Bulan Lalu") }
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${filteredTransactions.size} Transaksi Ditemukan",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "+${CurrencyUtils.formatRupiah(totalFilteredIncome)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = IncomeGreen
                            )
                            Text(
                                text = "-${CurrencyUtils.formatRupiah(totalFilteredExpense)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = ExpenseRed
                            )
                        }
                    }

                    Text(
                        text = "Net: ${CurrencyUtils.formatRupiah(totalFilteredIncome - totalFilteredExpense)}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (filteredTransactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Tidak Ada Transaksi Sesuai Filter",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Coba ubah kata kunci pencarian atau sesuaikan opsi filter di atas.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredTransactions, key = { it.id }) { tx ->
                        TransactionRowItem(
                            transaction = tx,
                            onEdit = {
                                editingTransaction = tx
                                showFormSheet = true
                            },
                            onDelete = {
                                transactionToDelete = tx
                            }
                        )
                    }
                }
            }
        }
    }

    if (transactionToDelete != null) {
        val tx = transactionToDelete!!
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = { Text("Hapus Transaksi?") },
            text = {
                Text(
                    "Apakah Anda yakin ingin menghapus transaksi \"${tx.title}\" sebesar ${CurrencyUtils.formatRupiah(tx.amount)}? Tindakan ini akan memperbarui saldo Anda secara otomatis."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteTransaction(tx)
                        transactionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }

    if (showFormSheet) {
        TransactionFormBottomSheet(
            editingTransaction = editingTransaction,
            categories = categories,
            onDismiss = { showFormSheet = false },
            onSave = { title, amount, type, category, categoryIcon, dateMillis, note, paymentMethod ->
                if (editingTransaction != null) {
                    onUpdateTransaction(
                        editingTransaction!!.copy(
                            title = title,
                            amount = amount,
                            type = type,
                            category = category,
                            categoryIcon = categoryIcon,
                            dateMillis = dateMillis,
                            note = note,
                            paymentMethod = paymentMethod
                        )
                    )
                } else {
                    onAddTransaction(title, amount, type, category, categoryIcon, dateMillis, note, paymentMethod)
                }
                showFormSheet = false
            },
            onRequestAddCategory = { showAddCategoryDialog = true }
        )
    }

    if (showAddCategoryDialog) {
        AddCategoryDialog(
            onDismiss = { showAddCategoryDialog = false },
            onSave = { name, type, icon, colorHex ->
                onAddCategory(name, type, icon, colorHex)
                showAddCategoryDialog = false
            }
        )
    }
}

@Composable
fun TransactionRowItem(
    transaction: TransactionEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isIncome = transaction.type == "INCOME"
    val iconVector = CategoryIconHelper.getIconForName(transaction.categoryIcon)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("tx_row_${transaction.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isIncome) IncomeGreenLight else ExpenseRedLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = transaction.category,
                        tint = if (isIncome) IncomeGreen else ExpenseRed,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = transaction.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = transaction.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = " • ",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = DateUtils.formatDateShort(transaction.dateMillis),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = " (${transaction.paymentMethod})",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Text(
                    text = "${if (isIncome) "+" else "-"}${CurrencyUtils.formatRupiah(transaction.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isIncome) IncomeGreen else ExpenseRed
                )
            }

            if (transaction.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = transaction.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onEdit,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit", fontSize = 12.sp)
                }

                TextButton(
                    onClick = onDelete,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Hapus", fontSize = 12.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionFormBottomSheet(
    editingTransaction: TransactionEntity?,
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onSave: (title: String, amount: Long, type: String, category: String, categoryIcon: String, dateMillis: Long, note: String, paymentMethod: String) -> Unit,
    onRequestAddCategory: () -> Unit
) {
    val context = LocalContext.current
    var type by remember { mutableStateOf(editingTransaction?.type ?: "EXPENSE") }
    var rawAmount by remember { mutableStateOf(editingTransaction?.amount?.toString() ?: "") }
    var title by remember { mutableStateOf(editingTransaction?.title ?: "") }
    var note by remember { mutableStateOf(editingTransaction?.note ?: "") }
    var paymentMethod by remember { mutableStateOf(editingTransaction?.paymentMethod ?: "Tunai") }
    var dateMillis by remember { mutableLongStateOf(editingTransaction?.dateMillis ?: System.currentTimeMillis()) }

    val relevantCategories = categories.filter { it.type == type }
    var selectedCategory by remember {
        mutableStateOf(
            relevantCategories.find { it.name == editingTransaction?.category }
                ?: relevantCategories.firstOrNull()
        )
    }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun showDatePicker() {
        val cal = Calendar.getInstance().apply { timeInMillis = dateMillis }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
                dateMillis = newCal.timeInMillis
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = if (editingTransaction != null) "Edit Transaksi" else "Tambah Transaksi Baru",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (type == "EXPENSE") ExpenseRed else Color.Transparent)
                        .clickable {
                            type = "EXPENSE"
                            selectedCategory = categories.firstOrNull { it.type == "EXPENSE" }
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Pengeluaran",
                        fontWeight = FontWeight.Bold,
                        color = if (type == "EXPENSE") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (type == "INCOME") IncomeGreen else Color.Transparent)
                        .clickable {
                            type = "INCOME"
                            selectedCategory = categories.firstOrNull { it.type == "INCOME" }
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Pemasukan",
                        fontWeight = FontWeight.Bold,
                        color = if (type == "INCOME") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            OutlinedTextField(
                value = rawAmount,
                onValueChange = { input ->
                    val clean = input.filter { it.isDigit() }
                    rawAmount = clean
                    errorMessage = null
                },
                label = { Text("Nominal Transaksi (Rp)") },
                prefix = { Text("Rp ") },
                placeholder = { Text("50.000") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("amount_input_field"),
                singleLine = true,
                isError = errorMessage != null
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it; errorMessage = null },
                label = { Text("Judul / Keterangan Singkat") },
                placeholder = { Text("misal: Makan Siang, Gaji Bulanan") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("title_input_field"),
                singleLine = true
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pilih Kategori",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = onRequestAddCategory) {
                        Text("+ Kategori Baru", fontSize = 12.sp)
                    }
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(relevantCategories) { cat ->
                        FilterChip(
                            selected = selectedCategory?.id == cat.id,
                            onClick = { selectedCategory = cat },
                            leadingIcon = {
                                Icon(
                                    imageVector = CategoryIconHelper.getIconForName(cat.icon),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            label = { Text(cat.name) }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { showDatePicker() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(DateUtils.formatDateShort(dateMillis), fontSize = 13.sp)
                }

                var methodMenuOpen by remember { mutableStateOf(false) }
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { methodMenuOpen = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(paymentMethod, fontSize = 13.sp)
                    }

                    DropdownMenu(
                        expanded = methodMenuOpen,
                        onDismissRequest = { methodMenuOpen = false }
                    ) {
                        listOf("Tunai", "Transfer Bank", "E-Wallet", "Kartu Debit/Kredit").forEach { m ->
                            DropdownMenuItem(
                                text = { Text(m) },
                                onClick = {
                                    paymentMethod = m
                                    methodMenuOpen = false
                                }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Catatan Tambahan (Opsional)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Batal")
                }

                Button(
                    onClick = {
                        val amount = rawAmount.toLongOrNull()
                        if (amount == null || amount <= 0) {
                            errorMessage = "Nominal harus berupa angka valid dan lebih dari 0."
                            return@Button
                        }
                        if (title.isBlank()) {
                            errorMessage = "Judul transaksi tidak boleh kosong."
                            return@Button
                        }
                        val cat = selectedCategory ?: relevantCategories.firstOrNull()
                        if (cat == null) {
                            errorMessage = "Pilih kategori transaksi."
                            return@Button
                        }

                        onSave(
                            title,
                            amount,
                            type,
                            cat.name,
                            cat.icon,
                            dateMillis,
                            note,
                            paymentMethod
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("save_transaction_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Simpan")
                }
            }
        }
    }
}

@Composable
fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, type: String, icon: String, colorHex: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("EXPENSE") }
    var selectedIcon by remember { mutableStateOf("category") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Kategori Baru") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FilterChip(
                        selected = type == "EXPENSE",
                        onClick = { type = "EXPENSE" },
                        label = { Text("Pengeluaran") }
                    )
                    FilterChip(
                        selected = type == "INCOME",
                        onClick = { type = "INCOME" },
                        label = { Text("Pemasukan") }
                    )
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; error = null },
                    label = { Text("Nama Kategori") },
                    placeholder = { Text("misal: Skincare, Kopi, Saham") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Pilih Ikon:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(CategoryIconHelper.availableIcons) { (iconKey, _) ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (selectedIcon == iconKey) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { selectedIcon = iconKey },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = CategoryIconHelper.getIconForName(iconKey),
                                contentDescription = null,
                                tint = if (selectedIcon == iconKey) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                if (error != null) {
                    Text(text = error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        error = "Nama kategori tidak boleh kosong"
                        return@Button
                    }
                    onSave(name, type, selectedIcon, "#6750A4")
                }
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
