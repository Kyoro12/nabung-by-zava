package com.example

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.TransactionEntity
import com.example.ui.components.AppBackground
import com.example.ui.components.PinLockScreen
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TransactionFormBottomSheet
import com.example.ui.screens.TransactionScreen
import com.example.ui.screens.WishlistFormDialog
import com.example.ui.screens.WishlistScreen
import com.example.ui.theme.NabungZavaTheme
import com.example.ui.viewmodel.FinanceViewModel

enum class NavTab(val title: String) {
    HOME("Beranda"),
    TRANSACTIONS("Transaksi"),
    WISHLIST("Wishlist"),
    ANALYTICS("Grafik"),
    SETTINGS("Pengaturan")
}

class MainActivity : FragmentActivity() {

    private var viewModelInstance: FinanceViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val viewModel: FinanceViewModel = viewModel()
            viewModelInstance = viewModel

            val preferences by viewModel.preferences.collectAsStateWithLifecycle()
            val summary by viewModel.financialSummary.collectAsStateWithLifecycle()
            val transactions by viewModel.transactions.collectAsStateWithLifecycle()
            val categories by viewModel.categories.collectAsStateWithLifecycle()
            val wishlists by viewModel.wishlist.collectAsStateWithLifecycle()
            val isAppLocked by viewModel.isAppLocked.collectAsStateWithLifecycle()

            val isBiometricAvailable = remember { viewModel.securityManager.isBiometricAvailable() }

            NabungZavaTheme(
                themeMode = preferences.themeMode,
                accentColor = preferences.accentColor
            ) {
                if (isAppLocked) {
                    PinLockScreen(
                        profileName = preferences.profileName,
                        profilePhotoPath = preferences.profilePhotoPath,
                        isBiometricEnabled = preferences.isBiometricEnabled,
                        onVerifyPin = { pin -> viewModel.unlockWithPin(pin) },
                        onBiometricSuccess = { viewModel.unlockBiometricSuccess() }
                    )
                } else {
                    AppContent(
                        viewModel = viewModel,
                        preferences = preferences,
                        summary = summary,
                        transactions = transactions,
                        categories = categories,
                        wishlists = wishlists,
                        isBiometricAvailable = isBiometricAvailable
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModelInstance?.onAppForegrounded()
    }

    override fun onPause() {
        super.onPause()
        viewModelInstance?.onAppBackgrounded()
    }
}

@Composable
fun AppContent(
    viewModel: FinanceViewModel,
    preferences: com.example.data.local.AppPreferences,
    summary: com.example.ui.viewmodel.FinancialSummary,
    transactions: List<TransactionEntity>,
    categories: List<com.example.data.model.CategoryEntity>,
    wishlists: List<com.example.data.model.WishlistEntity>,
    isBiometricAvailable: Boolean
) {
    var currentTab by remember { mutableStateOf(NavTab.HOME) }

    var quickAddTransactionIncome by remember { mutableStateOf<Boolean?>(null) }
    var showQuickAddWishlist by remember { mutableStateOf(false) }
    var selectedTxDetail by remember { mutableStateOf<TransactionEntity?>(null) }

    if (currentTab != NavTab.HOME) {
        BackHandler {
            currentTab = NavTab.HOME
        }
    }

    AppBackground(
        preset = preferences.backgroundPreset,
        customImagePath = preferences.customBackgroundImagePath,
        dim = preferences.backgroundDim
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    NavigationBarItem(
                        selected = currentTab == NavTab.HOME,
                        onClick = { currentTab = NavTab.HOME },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Beranda") },
                        label = { Text(NavTab.HOME.title, fontWeight = if (currentTab == NavTab.HOME) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )

                    NavigationBarItem(
                        selected = currentTab == NavTab.TRANSACTIONS,
                        onClick = { currentTab = NavTab.TRANSACTIONS },
                        icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Transaksi") },
                        label = { Text(NavTab.TRANSACTIONS.title, fontWeight = if (currentTab == NavTab.TRANSACTIONS) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )

                    NavigationBarItem(
                        selected = currentTab == NavTab.WISHLIST,
                        onClick = { currentTab = NavTab.WISHLIST },
                        icon = { Icon(Icons.Default.Savings, contentDescription = "Wishlist") },
                        label = { Text(NavTab.WISHLIST.title, fontWeight = if (currentTab == NavTab.WISHLIST) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )

                    NavigationBarItem(
                        selected = currentTab == NavTab.ANALYTICS,
                        onClick = { currentTab = NavTab.ANALYTICS },
                        icon = { Icon(Icons.Default.BarChart, contentDescription = "Grafik") },
                        label = { Text(NavTab.ANALYTICS.title, fontWeight = if (currentTab == NavTab.ANALYTICS) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )

                    NavigationBarItem(
                        selected = currentTab == NavTab.SETTINGS,
                        onClick = { currentTab = NavTab.SETTINGS },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Pengaturan") },
                        label = { Text(NavTab.SETTINGS.title, fontWeight = if (currentTab == NavTab.SETTINGS) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Crossfade(targetState = currentTab, label = "tab_transition") { tab ->
                    when (tab) {
                        NavTab.HOME -> {
                            val recentTxs = transactions.take(5)
                            HomeScreen(
                                summary = summary,
                                recentTransactions = recentTxs,
                                preferences = preferences,
                                onNavigateToTransactions = { currentTab = NavTab.TRANSACTIONS },
                                onNavigateToWishlist = { currentTab = NavTab.WISHLIST },
                                onOpenAddTransaction = { isIncome ->
                                    quickAddTransactionIncome = isIncome
                                },
                                onOpenAddWishlist = { showQuickAddWishlist = true },
                                onTransactionClick = { tx ->
                                    selectedTxDetail = tx
                                }
                            )
                        }

                        NavTab.TRANSACTIONS -> {
                            TransactionScreen(
                                transactions = transactions,
                                categories = categories,
                                onAddTransaction = { title, amount, type, category, categoryIcon, dateMillis, note, paymentMethod ->
                                    viewModel.addTransaction(title, amount, type, category, categoryIcon, dateMillis, note, paymentMethod)
                                },
                                onUpdateTransaction = { viewModel.updateTransaction(it) },
                                onDeleteTransaction = { viewModel.deleteTransaction(it) },
                                onAddCategory = { name, type, icon, colorHex ->
                                    viewModel.addCategory(name, type, icon, colorHex)
                                }
                            )
                        }

                        NavTab.WISHLIST -> {
                            WishlistScreen(
                                wishlists = wishlists,
                                onAddWishlist = { title, targetAmount, currentAmount, targetDateMillis, note, imagePath ->
                                    viewModel.addWishlist(title, targetAmount, currentAmount, targetDateMillis, note, imagePath)
                                },
                                onUpdateWishlist = { viewModel.updateWishlist(it) },
                                onDeleteWishlist = { viewModel.deleteWishlist(it) },
                                onDeposit = { wishlist, amount, recordAsTx, method ->
                                    viewModel.depositToWishlist(wishlist, amount, recordAsTx, method)
                                },
                                onToggleAchieved = { viewModel.toggleAchieved(it) }
                            )
                        }

                        NavTab.ANALYTICS -> {
                            AnalyticsScreen(
                                transactions = transactions,
                                wishlists = wishlists,
                                onExportCsv = { viewModel.exportTransactionsCsv() }
                            )
                        }

                        NavTab.SETTINGS -> {
                            SettingsScreen(
                                preferences = preferences,
                                isBiometricHardwareAvailable = isBiometricAvailable,
                                onSetThemeMode = { viewModel.setThemeMode(it) },
                                onSetAccentColor = { viewModel.setAccentColor(it) },
                                onSetBackgroundPreset = { viewModel.setBackgroundPreset(it) },
                                onSetCustomBackgroundImage = { viewModel.setCustomBackgroundImagePath(it) },
                                onResetDisplay = { viewModel.resetDisplayPreferences() },
                                onUpdateProfile = { name, bio, photoPath ->
                                    viewModel.updateProfile(name, bio, photoPath)
                                },
                                onSetupPin = { viewModel.setupPin(it) },
                                onDisablePin = { viewModel.disablePin() },
                                onSetBiometric = { viewModel.setBiometricEnabled(it) },
                                onSetAutoLockTimeout = { viewModel.setAutoLockTimeout(it) },
                                onExportBackupJson = { viewModel.exportBackupJson() },
                                onRestoreBackupJson = { viewModel.restoreBackupJson(it) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (quickAddTransactionIncome != null) {
        val initialType = if (quickAddTransactionIncome == true) "INCOME" else "EXPENSE"
        val dummyInitial = TransactionEntity(
            title = "",
            amount = 0,
            type = initialType,
            category = "",
            dateMillis = System.currentTimeMillis()
        )
        TransactionFormBottomSheet(
            editingTransaction = dummyInitial,
            categories = categories,
            onDismiss = { quickAddTransactionIncome = null },
            onSave = { title, amount, type, category, categoryIcon, dateMillis, note, paymentMethod ->
                viewModel.addTransaction(title, amount, type, category, categoryIcon, dateMillis, note, paymentMethod)
                quickAddTransactionIncome = null
            },
            onRequestAddCategory = {
                currentTab = NavTab.TRANSACTIONS
                quickAddTransactionIncome = null
            }
        )
    }

    if (showQuickAddWishlist) {
        WishlistFormDialog(
            editingWishlist = null,
            onDismiss = { showQuickAddWishlist = false },
            onSave = { title, targetAmount, currentAmount, targetDateMillis, note, imagePath ->
                viewModel.addWishlist(title, targetAmount, currentAmount, targetDateMillis, note, imagePath)
                showQuickAddWishlist = false
            }
        )
    }

    if (selectedTxDetail != null) {
        TransactionFormBottomSheet(
            editingTransaction = selectedTxDetail,
            categories = categories,
            onDismiss = { selectedTxDetail = null },
            onSave = { title, amount, type, category, categoryIcon, dateMillis, note, paymentMethod ->
                viewModel.updateTransaction(
                    selectedTxDetail!!.copy(
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
                selectedTxDetail = null
            },
            onRequestAddCategory = {
                currentTab = NavTab.TRANSACTIONS
                selectedTxDetail = null
            }
        )
    }
}
