package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TransactionEntity
import com.example.data.model.WishlistEntity
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.SavingsBlue
import com.example.util.CurrencyUtils
import com.example.util.DateUtils
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar

@Composable
fun AnalyticsScreen(
    transactions: List<TransactionEntity>,
    wishlists: List<WishlistEntity>,
    onExportCsv: () -> String
) {
    val context = LocalContext.current
    var selectedPeriod by remember { mutableStateOf("THIS_MONTH") }

    val filteredTransactions by remember(transactions, selectedPeriod) {
        derivedStateOf {
            transactions.filter { tx ->
                when (selectedPeriod) {
                    "THIS_MONTH" -> DateUtils.isThisMonth(tx.dateMillis)
                    "LAST_MONTH" -> tx.dateMillis in DateUtils.getStartOfLastMonth()..DateUtils.getEndOfLastMonth()
                    "LAST_3_MONTHS" -> {
                        val threeMonthsAgo = Calendar.getInstance().apply {
                            add(Calendar.MONTH, -3)
                        }.timeInMillis
                        tx.dateMillis >= threeMonthsAgo
                    }
                    "THIS_YEAR" -> tx.dateMillis >= DateUtils.getStartOfYear()
                    else -> true
                }
            }
        }
    }

    val periodIncome = remember(filteredTransactions) {
        filteredTransactions.filter { it.type == "INCOME" }.sumOf { it.amount }
    }
    val periodExpense = remember(filteredTransactions) {
        filteredTransactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    }
    val periodNet = periodIncome - periodExpense
    val totalSavings = wishlists.sumOf { it.currentAmount }

    val expenseCategoryMap by remember(filteredTransactions) {
        derivedStateOf {
            val map = mutableMapOf<String, Long>()
            for (tx in filteredTransactions) {
                if (tx.type == "EXPENSE") {
                    map[tx.category] = (map[tx.category] ?: 0L) + tx.amount
                }
            }
            map.toList().sortedByDescending { it.second }
        }
    }

    fun shareCsv() {
        val csvContent = onExportCsv()
        try {
            val file = File(context.cacheDir, "laporan_nabung_zava.csv")
            FileOutputStream(file).use { it.write(csvContent.toByteArray(Charsets.UTF_8)) }

            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_SUBJECT, "Laporan Keuangan Nabung By. Zava")
                putExtra(Intent.EXTRA_TEXT, "Berikut terlampir laporan transaksi Nabung By. Zava:\n\n$csvContent")
            }
            context.startActivity(Intent.createChooser(sendIntent, "Bagikan / Ekspor Laporan CSV"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("analytics_screen"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Grafik & Laporan",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Analisis arus kas & pengeluaranmu",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { shareCsv() },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ekspor CSV", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                listOf(
                    "THIS_MONTH" to "Bulan Ini",
                    "LAST_MONTH" to "Bulan Lalu",
                    "LAST_3_MONTHS" to "3 Bulan",
                    "THIS_YEAR" to "Tahun Ini",
                    "ALL" to "Semua"
                ).forEach { (key, label) ->
                    item {
                        FilterChip(
                            selected = selectedPeriod == key,
                            onClick = { selectedPeriod = key },
                            label = { Text(label) }
                        )
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "RINGKASAN PERIODE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Selisih Bersih: ${CurrencyUtils.formatRupiah(periodNet)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (periodNet >= 0) IncomeGreen else ExpenseRed
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Pemasukan", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("+${CurrencyUtils.formatRupiah(periodIncome)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = IncomeGreen)
                        }
                        Column {
                            Text("Total Pengeluaran", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("-${CurrencyUtils.formatRupiah(periodExpense)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = ExpenseRed)
                        }
                        Column {
                            Text("Dana Tabungan", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(CurrencyUtils.formatRupiah(totalSavings), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = SavingsBlue)
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "Grafik Pemasukan vs Pengeluaran",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val maxVal = maxOf(periodIncome, periodExpense, 100_000L).toFloat()
                    val incRatio = (periodIncome / maxVal).coerceIn(0.05f, 1f)
                    val expRatio = (periodExpense / maxVal).coerceIn(0.05f, 1f)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val canvasWidth = size.width
                            val canvasHeight = size.height

                            drawLine(
                                color = Color.Gray.copy(alpha = 0.3f),
                                start = Offset(0f, canvasHeight - 20f),
                                end = Offset(canvasWidth, canvasHeight - 20f),
                                strokeWidth = 2f
                            )

                            val barWidth = 60.dp.toPx()
                            val spacing = 40.dp.toPx()
                            val totalBarsWidth = (barWidth * 2) + spacing
                            val startX = (canvasWidth - totalBarsWidth) / 2

                            val incBarHeight = (canvasHeight - 40f) * incRatio
                            drawRoundRect(
                                color = IncomeGreen,
                                topLeft = Offset(startX, canvasHeight - 20f - incBarHeight),
                                size = Size(barWidth, incBarHeight),
                                cornerRadius = CornerRadius(16f, 16f)
                            )

                            val expBarHeight = (canvasHeight - 40f) * expRatio
                            drawRoundRect(
                                color = ExpenseRed,
                                topLeft = Offset(startX + barWidth + spacing, canvasHeight - 20f - expBarHeight),
                                size = Size(barWidth, expBarHeight),
                                cornerRadius = CornerRadius(16f, 16f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(IncomeGreen))
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("Pemasukan", style = MaterialTheme.typography.labelSmall)
                                Text(CurrencyUtils.formatRupiah(periodIncome), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = IncomeGreen)
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(ExpenseRed))
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("Pengeluaran", style = MaterialTheme.typography.labelSmall)
                                Text(CurrencyUtils.formatRupiah(periodExpense), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = ExpenseRed)
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Pengeluaran Berdasarkan Kategori",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.PieChart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (expenseCategoryMap.isEmpty()) {
                        Text(
                            text = "Tidak ada pengeluaran pada periode ini.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        val totalExp = periodExpense.coerceAtLeast(1L)
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            expenseCategoryMap.forEach { (catName, amount) ->
                                val pct = (amount.toFloat() / totalExp).coerceIn(0f, 1f)
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = catName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "${CurrencyUtils.formatRupiah(amount)} (${(pct * 100).toInt()}%)",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    LinearProgressIndicator(
                                        progress = { pct },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                        strokeCap = StrokeCap.Round
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}
