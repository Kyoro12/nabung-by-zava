package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.local.AppPreferences
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.util.ImageUtils
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@Composable
fun SettingsScreen(
    preferences: AppPreferences,
    isBiometricHardwareAvailable: Boolean,
    onSetThemeMode: (String) -> Unit,
    onSetAccentColor: (String) -> Unit,
    onSetBackgroundPreset: (String) -> Unit,
    onSetCustomBackgroundImage: (String?) -> Unit,
    onResetDisplay: () -> Unit,
    onUpdateProfile: (name: String, bio: String, photoPath: String?) -> Unit,
    onSetupPin: (String) -> Unit,
    onDisablePin: () -> Unit,
    onSetBiometric: (Boolean) -> Unit,
    onSetAutoLockTimeout: (Int) -> Unit,
    onExportBackupJson: () -> String,
    onRestoreBackupJson: suspend (String) -> Result<Unit>
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showSetupPinDialog by remember { mutableStateOf(false) }
    var showDisablePinDialog by remember { mutableStateOf(false) }
    var showAutoLockDialog by remember { mutableStateOf(false) }
    var showRestoreBackupDialog by remember { mutableStateOf(false) }

    val bgPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val saved = ImageUtils.saveImageToInternalStorage(context, uri, "bg")
            if (saved != null) {
                onSetCustomBackgroundImage(saved)
                onSetBackgroundPreset("CUSTOM_IMAGE")
                Toast.makeText(context, "Wallpaper kustom berhasil diterapkan!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val profilePhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val saved = ImageUtils.saveImageToInternalStorage(context, uri, "profile")
            if (saved != null) {
                onUpdateProfile(preferences.profileName, preferences.profileBio, saved)
                Toast.makeText(context, "Foto profil diperbarui!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun shareBackupJson() {
        val json = onExportBackupJson()
        try {
            val file = File(context.cacheDir, "cadangan_nabung_zava.json")
            FileOutputStream(file).use { it.write(json.toByteArray(Charsets.UTF_8)) }

            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_SUBJECT, "Cadangan Data Nabung By. Zava")
                putExtra(Intent.EXTRA_TEXT, json)
            }
            context.startActivity(Intent.createChooser(sendIntent, "Simpan / Bagikan File Cadangan"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Gagal mengekspor file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Pengaturan & Profil",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Kustomisasi, keamanan, dan cadangan lokal",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                .clickable {
                                    profilePhotoPicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (!preferences.profilePhotoPath.isNullOrBlank() && File(preferences.profilePhotoPath).exists()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(File(preferences.profilePhotoPath))
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Foto Profil",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.app_logo_zava),
                                    contentDescription = "Logo Zava",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = preferences.profileName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "🤍", fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = preferences.profileBio,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(onClick = { showEditProfileDialog = true }) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Profil", tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    TextButton(
                        onClick = {
                            profilePhotoPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Ganti Foto Profil dari Galeri", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
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
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Kustomisasi Tampilan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(text = "Mode Tema", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("SYSTEM" to "Sistem", "LIGHT" to "Terang", "DARK" to "Gelap").forEach { (mode, label) ->
                                FilterChip(
                                    selected = preferences.themeMode == mode,
                                    onClick = { onSetThemeMode(mode) },
                                    label = { Text(label) }
                                )
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(text = "Warna Aksen", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val colorMap = listOf(
                                "PURPLE" to Color(0xFF6750A4),
                                "PINK" to Color(0xFFB32656),
                                "BLUE" to Color(0xFF1A56A6),
                                "GREEN" to Color(0xFF256B3A),
                                "BLACK" to Color(0xFF263238)
                            )
                            colorMap.forEach { (colorKey, colorVal) ->
                                val isSelected = preferences.accentColor.equals(colorKey, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(colorVal)
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { onSetAccentColor(colorKey) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(text = "Latar Belakang / Wallpaper", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilterChip(
                                selected = preferences.backgroundPreset == "ROMANTIC_BW",
                                onClick = { onSetBackgroundPreset("ROMANTIC_BW") },
                                label = { Text("Zava Monokrom") }
                            )
                            FilterChip(
                                selected = preferences.backgroundPreset == "NONE",
                                onClick = { onSetBackgroundPreset("NONE") },
                                label = { Text("Polos") }
                            )
                            FilterChip(
                                selected = preferences.backgroundPreset == "SOFT_GRADIENT",
                                onClick = { onSetBackgroundPreset("SOFT_GRADIENT") },
                                label = { Text("Gradien") }
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                bgPhotoPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Wallpaper, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Pilih Foto Sendiri dari Galeri", fontSize = 12.sp)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = onResetDisplay,
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset Tampilan Bawaan", fontSize = 12.sp)
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
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Keamanan & PIN",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Kunci Aplikasi dengan PIN", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = if (preferences.isPinEnabled) "PIN aktif untuk melindungi data" else "Aplikasi terbuka langsung tanpa PIN",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = preferences.isPinEnabled,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    showSetupPinDialog = true
                                } else {
                                    showDisablePinDialog = true
                                }
                            }
                        )
                    }

                    if (preferences.isPinEnabled) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { showSetupPinDialog = true }) {
                                Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Ubah PIN Keamanan", fontSize = 13.sp)
                            }

                            TextButton(onClick = { showAutoLockDialog = true }) {
                                val label = when (preferences.autoLockTimeoutSeconds) {
                                    60 -> "1 Menit"
                                    300 -> "5 Menit"
                                    else -> "Segera"
                                }
                                Text("Kunci Otomatis: $label", fontSize = 13.sp)
                            }
                        }

                        if (isBiometricHardwareAvailable) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "Buka dengan Sidik Jari / Biometrik", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        text = "Gunakan sensor biometrik perangkat untuk membuka lebih cepat",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = preferences.isBiometricEnabled,
                                    onCheckedChange = { onSetBiometric(it) }
                                )
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "PIN dilindungi secara kriptografis menggunakan Salt acak dan Hash SHA-256 serta Android Keystore. Tidak disimpan dalam teks biasa.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
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
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Backup, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cadangan Data (100% Offline)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Data disimpan di memori internal HP Anda. Simpan salinan cadangan berkala agar data tetap aman saat berganti HP.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { shareBackupJson() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ekspor Cadangan", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { showRestoreBackupDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pulihkan Data", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Nabung By. Zava v1.0",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Aplikasi Keuangan Pribadi & Tabungan Bersama",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Dibuat khusus untuk Ivan & Zahra 🤍\n100% Offline • Tanpa Iklan • Privasi Terjamin",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }

    if (showEditProfileDialog) {
        var tempName by remember { mutableStateOf(preferences.profileName) }
        var tempBio by remember { mutableStateOf(preferences.profileBio) }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Edit Profil Pengguna") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        label = { Text("Nama Tampilan") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = tempBio,
                        onValueChange = { tempBio = it },
                        label = { Text("Catatan / Komitmen") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateProfile(tempName.trim().ifBlank { "Ivan & Zahra" }, tempBio.trim(), preferences.profilePhotoPath)
                        showEditProfileDialog = false
                    }
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) { Text("Batal") }
            }
        )
    }

    if (showSetupPinDialog) {
        var pinInput by remember { mutableStateOf("") }
        var pinConfirm by remember { mutableStateOf("") }
        var pinError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showSetupPinDialog = false },
            title = { Text("Atur PIN Keamanan") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Masukkan 4 sampai 6 digit angka:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) pinInput = it; pinError = null },
                        label = { Text("PIN Baru") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = pinConfirm,
                        onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) pinConfirm = it; pinError = null },
                        label = { Text("Konfirmasi PIN") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (pinError != null) {
                        Text(text = pinError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pinInput.length < 4) {
                            pinError = "PIN minimal 4 digit angka."
                            return@Button
                        }
                        if (pinInput != pinConfirm) {
                            pinError = "Konfirmasi PIN tidak cocok."
                            return@Button
                        }
                        onSetupPin(pinInput)
                        showSetupPinDialog = false
                        Toast.makeText(context, "PIN keamanan berhasil diaktifkan!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Aktifkan PIN")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSetupPinDialog = false }) { Text("Batal") }
            }
        )
    }

    if (showDisablePinDialog) {
        AlertDialog(
            onDismissRequest = { showDisablePinDialog = false },
            title = { Text("Nonaktifkan PIN Keamanan?") },
            text = {
                Text("Jika dinonaktifkan, aplikasi dapat dibuka siapa saja tanpa verifikasi PIN.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDisablePin()
                        showDisablePinDialog = false
                        Toast.makeText(context, "Kunci PIN dinonaktifkan.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Nonaktifkan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDisablePinDialog = false }) { Text("Batal") }
            }
        )
    }

    if (showAutoLockDialog) {
        AlertDialog(
            onDismissRequest = { showAutoLockDialog = false },
            title = { Text("Pilih Waktu Kunci Otomatis") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(0 to "Segera Saat Keluar", 60 to "Setelah 1 Menit", 300 to "Setelah 5 Menit").forEach { (sec, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onSetAutoLockTimeout(sec)
                                    showAutoLockDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(label, fontWeight = if (preferences.autoLockTimeoutSeconds == sec) FontWeight.Bold else FontWeight.Normal)
                            if (preferences.autoLockTimeoutSeconds == sec) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAutoLockDialog = false }) { Text("Tutup") }
            }
        )
    }

    if (showRestoreBackupDialog) {
        var restoreText by remember { mutableStateOf("") }
        var restoreError by remember { mutableStateOf<String?>(null) }
        var isRestoring by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { if (!isRestoring) showRestoreBackupDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = ExpenseRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pulihkan Cadangan")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "PERINGATAN: Memulihkan cadangan akan menimpa seluruh transaksi, kategori, dan wishlist yang ada saat ini. Pastikan data JSON cadangan Anda valid.",
                        color = ExpenseRed,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    OutlinedTextField(
                        value = restoreText,
                        onValueChange = { restoreText = it; restoreError = null },
                        label = { Text("Tempel Teks Cadangan JSON") },
                        placeholder = { Text("{\"appName\": \"Nabung By. Zava\", ...}") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        maxLines = 8
                    )

                    if (restoreError != null) {
                        Text(text = restoreError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (restoreText.isBlank()) {
                            restoreError = "Teks cadangan tidak boleh kosong."
                            return@Button
                        }
                        isRestoring = true
                        scope.launch {
                            val res = onRestoreBackupJson(restoreText)
                            isRestoring = false
                            if (res.isSuccess) {
                                showRestoreBackupDialog = false
                                Toast.makeText(context, "Data berhasil dipulihkan dari cadangan!", Toast.LENGTH_LONG).show()
                            } else {
                                restoreError = "Format cadangan salah atau rusak: ${res.exceptionOrNull()?.localizedMessage}"
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                    enabled = !isRestoring
                ) {
                    Text(if (isRestoring) "Memulihkan..." else "Pulihkan & Timpa Data")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreBackupDialog = false }, enabled = !isRestoring) {
                    Text("Batal")
                }
            }
        )
    }
}
