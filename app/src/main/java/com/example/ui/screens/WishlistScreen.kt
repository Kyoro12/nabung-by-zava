package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.WishlistEntity
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.IncomeGreenLight
import com.example.ui.theme.TargetGold
import com.example.util.CurrencyUtils
import com.example.util.DateUtils
import com.example.util.ImageUtils
import java.io.File
import java.util.Calendar

@Composable
fun WishlistScreen(
    wishlists: List<WishlistEntity>,
    onAddWishlist: (title: String, targetAmount: Long, currentAmount: Long, targetDateMillis: Long?, note: String, imagePath: String?) -> Unit,
    onUpdateWishlist: (WishlistEntity) -> Unit,
    onDeleteWishlist: (WishlistEntity) -> Unit,
    onDeposit: (wishlist: WishlistEntity, depositAmount: Long, recordAsTransaction: Boolean, paymentMethod: String) -> Unit,
    onToggleAchieved: (WishlistEntity) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingWishlist by remember { mutableStateOf<WishlistEntity?>(null) }
    var wishlistToDelete by remember { mutableStateOf<WishlistEntity?>(null) }
    var depositingWishlist by remember { mutableStateOf<WishlistEntity?>(null) }

    val totalSavings = wishlists.sumOf { it.currentAmount }
    val totalTarget = wishlists.sumOf { it.targetAmount }
    val achievedCount = wishlists.count { it.isAchieved }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingWishlist = null
                    showAddDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_wishlist_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah Target Impian")
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
                    text = "Wishlist & Target Tabungan",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Kumpulkan dana secara bertahap untuk impianmu 🎯",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
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
                        Column {
                            Text(
                                text = "TOTAL TERKUMPUL",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            Text(
                                text = CurrencyUtils.formatRupiah(totalSavings),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                        ) {
                            Text(
                                text = "$achievedCount / ${wishlists.size} Tercapai 🏆",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val overallProgress = if (totalTarget > 0) (totalSavings.toFloat() / totalTarget).coerceIn(0f, 1f) else 0f
                    LinearProgressIndicator(
                        progress = { overallProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f),
                        strokeCap = StrokeCap.Round
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Total Sasaran Semua Impian: ${CurrencyUtils.formatRupiah(totalTarget)} (${(overallProgress * 100).toInt()}%)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                    )
                }
            }

            if (wishlists.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Savings,
                            contentDescription = null,
                            modifier = Modifier.size(54.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Belum Ada Wishlist",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Ingin membeli barang idaman atau merencanakan tabungan masa depan? Tambahkan sekarang dan pasang fotonya!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(wishlists, key = { it.id }) { item ->
                        WishlistCardItem(
                            item = item,
                            onDeposit = { depositingWishlist = item },
                            onEdit = {
                                editingWishlist = item
                                showAddDialog = true
                            },
                            onDelete = { wishlistToDelete = item },
                            onToggleAchieved = { onToggleAchieved(item) }
                        )
                    }
                }
            }
        }
    }

    if (wishlistToDelete != null) {
        val item = wishlistToDelete!!
        AlertDialog(
            onDismissRequest = { wishlistToDelete = null },
            title = { Text("Hapus Wishlist?") },
            text = {
                Text(
                    "Apakah Anda yakin ingin menghapus target impian \"${item.title}\"? Data tabungan terkumpul pada item ini akan dihapus."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteWishlist(item)
                        wishlistToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = { wishlistToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }

    if (showAddDialog) {
        WishlistFormDialog(
            editingWishlist = editingWishlist,
            onDismiss = { showAddDialog = false },
            onSave = { title, targetAmount, currentAmount, targetDateMillis, note, imagePath ->
                if (editingWishlist != null) {
                    onUpdateWishlist(
                        editingWishlist!!.copy(
                            title = title,
                            targetAmount = targetAmount,
                            currentAmount = currentAmount,
                            targetDateMillis = targetDateMillis,
                            note = note,
                            imagePath = imagePath
                        )
                    )
                } else {
                    onAddWishlist(title, targetAmount, currentAmount, targetDateMillis, note, imagePath)
                }
                showAddDialog = false
            }
        )
    }

    if (depositingWishlist != null) {
        DepositDialog(
            wishlist = depositingWishlist!!,
            onDismiss = { depositingWishlist = null },
            onConfirm = { amount, recordAsTx, method ->
                onDeposit(depositingWishlist!!, amount, recordAsTx, method)
                depositingWishlist = null
            }
        )
    }
}

@Composable
fun WishlistCardItem(
    item: WishlistEntity,
    onDeposit: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleAchieved: () -> Unit
) {
    val context = LocalContext.current
    val progress = if (item.targetAmount > 0) {
        (item.currentAmount.toFloat() / item.targetAmount).coerceIn(0f, 1f)
    } else 0f
    val remaining = (item.targetAmount - item.currentAmount).coerceAtLeast(0L)
    val percentage = (progress * 100).toInt()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("wishlist_card_${item.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    if (!item.imagePath.isNullOrBlank() && File(item.imagePath).exists()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(File(item.imagePath))
                                .crossfade(true)
                                .build(),
                            contentDescription = item.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Savings,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        if (item.isAchieved) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = IncomeGreenLight
                            ) {
                                Text(
                                    text = "Tercapai 🏆",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = IncomeGreen
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    if (item.targetDateMillis != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Tenggat: ${DateUtils.formatDateShort(item.targetDateMillis)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    if (item.note.isNotBlank()) {
                        Text(
                            text = item.note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Terkumpul: ${CurrencyUtils.formatRupiah(item.currentAmount)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "$percentage%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (item.isAchieved) IncomeGreen else MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = if (item.isAchieved) IncomeGreen else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    strokeCap = StrokeCap.Round
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Target: ${CurrencyUtils.formatRupiah(item.targetAmount)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (remaining > 0) "Kurang: ${CurrencyUtils.formatRupiah(remaining)}" else "Sudah Tercapai! 🎉",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (remaining > 0) TargetGold else IncomeGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onDeposit,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (item.isAchieved) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
                        contentColor = if (item.isAchieved) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(imageVector = Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Setor Tabungan", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleAchieved,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (item.isAchieved) Icons.Default.CheckCircle else Icons.Default.Check,
                            contentDescription = "Tandai Tercapai",
                            tint = if (item.isAchieved) IncomeGreen else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WishlistFormDialog(
    editingWishlist: WishlistEntity?,
    onDismiss: () -> Unit,
    onSave: (title: String, targetAmount: Long, currentAmount: Long, targetDateMillis: Long?, note: String, imagePath: String?) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(editingWishlist?.title ?: "") }
    var rawTarget by remember { mutableStateOf(editingWishlist?.targetAmount?.toString() ?: "") }
    var rawCurrent by remember { mutableStateOf(editingWishlist?.currentAmount?.toString() ?: "0") }
    var note by remember { mutableStateOf(editingWishlist?.note ?: "") }
    var selectedImagePath by remember { mutableStateOf(editingWishlist?.imagePath) }
    var targetDateMillis by remember { mutableStateOf(editingWishlist?.targetDateMillis) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val savedPath = ImageUtils.saveImageToInternalStorage(context, uri, "wishlist")
            if (savedPath != null) {
                selectedImagePath = savedPath
            }
        }
    }

    fun showDatePicker() {
        val cal = Calendar.getInstance()
        if (targetDateMillis != null) {
            cal.timeInMillis = targetDateMillis!!
        }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
                targetDateMillis = newCal.timeInMillis
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (editingWishlist != null) "Edit Target Impian" else "Tambah Wishlist Impian") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (!selectedImagePath.isNullOrBlank() && File(selectedImagePath!!).exists()) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(File(selectedImagePath!!))
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Foto Barang",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text("Pilih Foto", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Foto Barang Impian",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Ambil dari galeri HP menggunakan Photo Picker",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (!selectedImagePath.isNullOrBlank()) {
                            TextButton(
                                onClick = { selectedImagePath = null },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Hapus Foto", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; errorMessage = null },
                    label = { Text("Nama Barang / Sasaran") },
                    placeholder = { Text("misal: Kamera Mirrorless, Trip Liburan") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = rawTarget,
                    onValueChange = { rawTarget = it.filter { c -> c.isDigit() }; errorMessage = null },
                    label = { Text("Harga Target (Rp)") },
                    prefix = { Text("Rp ") },
                    placeholder = { Text("5.000.000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = rawCurrent,
                    onValueChange = { rawCurrent = it.filter { c -> c.isDigit() }; errorMessage = null },
                    label = { Text("Nominal Terkumpul Saat Ini (Rp)") },
                    prefix = { Text("Rp ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = { showDatePicker() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (targetDateMillis != null) DateUtils.formatDateShort(targetDateMillis!!) else "Pasang Tenggat",
                            fontSize = 12.sp
                        )
                    }

                    if (targetDateMillis != null) {
                        IconButton(onClick = { targetDateMillis = null }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Hapus Tenggat", tint = MaterialTheme.colorScheme.outline)
                        }
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Catatan / Alasan Menabung") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )

                if (errorMessage != null) {
                    Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val target = rawTarget.toLongOrNull()
                    val current = rawCurrent.toLongOrNull() ?: 0L
                    if (title.isBlank()) {
                        errorMessage = "Nama barang tidak boleh kosong."
                        return@Button
                    }
                    if (target == null || target <= 0) {
                        errorMessage = "Harga target harus lebih dari 0."
                        return@Button
                    }
                    onSave(title, target, current, targetDateMillis, note, selectedImagePath)
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

@Composable
fun DepositDialog(
    wishlist: WishlistEntity,
    onDismiss: () -> Unit,
    onConfirm: (amount: Long, recordAsTransaction: Boolean, paymentMethod: String) -> Unit
) {
    var rawAmount by remember { mutableStateOf("") }
    var recordAsTransaction by remember { mutableStateOf(true) }
    var paymentMethod by remember { mutableStateOf("Tunai") }
    var methodMenuOpen by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val remaining = (wishlist.targetAmount - wishlist.currentAmount).coerceAtLeast(0L)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Setor Tabungan: ${wishlist.title}") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Sisa dana yang dibutuhkan: ${CurrencyUtils.formatRupiah(remaining)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                OutlinedTextField(
                    value = rawAmount,
                    onValueChange = { rawAmount = it.filter { c -> c.isDigit() }; error = null },
                    label = { Text("Nominal Setoran (Rp)") },
                    prefix = { Text("Rp ") },
                    placeholder = { Text("100.000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(50_000L, 100_000L, 250_000L).forEach { quickVal ->
                        OutlinedButton(
                            onClick = { rawAmount = quickVal.toString() },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text("+${quickVal / 1000}rb", fontSize = 11.sp)
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { methodMenuOpen = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Metode: $paymentMethod", fontSize = 13.sp)
                    }

                    DropdownMenu(
                        expanded = methodMenuOpen,
                        onDismissRequest = { methodMenuOpen = false }
                    ) {
                        listOf("Tunai", "Transfer Bank", "E-Wallet", "Kartu").forEach { m ->
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = recordAsTransaction,
                        onCheckedChange = { recordAsTransaction = it }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "Catat di Transaksi Harian",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Dicatat sebagai pengeluaran/tabungan agar saldo tetap sinkron dan tidak dihitung ganda.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
                    val amount = rawAmount.toLongOrNull()
                    if (amount == null || amount <= 0) {
                        error = "Nominal harus berupa angka valid dan lebih dari 0."
                        return@Button
                    }
                    onConfirm(amount, recordAsTransaction, paymentMethod)
                }
            ) {
                Text("Setor Sekarang")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
