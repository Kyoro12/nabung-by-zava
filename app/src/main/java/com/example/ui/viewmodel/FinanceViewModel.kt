package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.backup.BackupManager
import com.example.data.local.AppDatabase
import com.example.data.local.AppPreferences
import com.example.data.local.PreferencesManager
import com.example.data.model.CategoryEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.WishlistEntity
import com.example.data.repository.FinanceRepository
import com.example.data.security.SecurityManager
import com.example.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FinancialSummary(
    val currentBalance: Long = 0,
    val totalIncome: Long = 0,
    val totalExpense: Long = 0,
    val totalSavings: Long = 0,
    val todayIncome: Long = 0,
    val todayExpense: Long = 0,
    val thisMonthIncome: Long = 0,
    val thisMonthExpense: Long = 0
)

class FinanceViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = FinanceRepository(database)
    val preferencesManager = PreferencesManager(application)
    val securityManager = SecurityManager(application)

    val preferences: StateFlow<AppPreferences> = preferencesManager.preferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = AppPreferences()
        )

    val transactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val categories: StateFlow<List<CategoryEntity>> = repository.allCategories
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val wishlist: StateFlow<List<WishlistEntity>> = repository.allWishlist
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val financialSummary: StateFlow<FinancialSummary> = combine(transactions, wishlist) { txs, wList ->
        var totalInc = 0L
        var totalExp = 0L
        var todayInc = 0L
        var todayExp = 0L
        var monthInc = 0L
        var monthExp = 0L

        for (tx in txs) {
            val isInc = tx.type == "INCOME"
            val amt = tx.amount

            if (isInc) totalInc += amt else totalExp += amt

            if (DateUtils.isToday(tx.dateMillis)) {
                if (isInc) todayInc += amt else todayExp += amt
            }

            if (DateUtils.isThisMonth(tx.dateMillis)) {
                if (isInc) monthInc += amt else monthExp += amt
            }
        }

        val totalSav = wList.sumOf { it.currentAmount }
        val balance = totalInc - totalExp

        FinancialSummary(
            currentBalance = balance,
            totalIncome = totalInc,
            totalExpense = totalExp,
            totalSavings = totalSav,
            todayIncome = todayInc,
            todayExpense = todayExp,
            thisMonthIncome = monthInc,
            thisMonthExpense = monthExp
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FinancialSummary()
    )

    private val _isAppLocked = MutableStateFlow(false)
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()

    private var lastBackgroundTimeMillis: Long = 0

    init {
        viewModelScope.launch {
            val prefs = preferencesManager.preferencesFlow.first()
            if (prefs.isPinEnabled) {
                _isAppLocked.value = true
            }
        }
    }

    fun onAppForegrounded() {
        viewModelScope.launch {
            val prefs = preferencesManager.preferencesFlow.first()
            if (prefs.isPinEnabled && lastBackgroundTimeMillis > 0) {
                val elapsedSeconds = (System.currentTimeMillis() - lastBackgroundTimeMillis) / 1000
                if (elapsedSeconds >= prefs.autoLockTimeoutSeconds) {
                    _isAppLocked.value = true
                }
            }
        }
    }

    fun onAppBackgrounded() {
        lastBackgroundTimeMillis = System.currentTimeMillis()
    }

    fun unlockWithPin(enteredPin: String): Boolean {
        val prefs = preferences.value
        val isValid = securityManager.verifyPin(enteredPin, prefs.pinSalt, prefs.pinHash)
        if (isValid) {
            _isAppLocked.value = false
        }
        return isValid
    }

    fun unlockBiometricSuccess() {
        _isAppLocked.value = false
    }

    fun lockAppManually() {
        if (preferences.value.isPinEnabled) {
            _isAppLocked.value = true
        }
    }

    fun setupPin(newPin: String) {
        viewModelScope.launch {
            val salt = securityManager.generateSalt()
            val hash = securityManager.hashPin(newPin, salt)
            preferencesManager.setPin(salt, hash, enabled = true)
        }
    }

    fun disablePin() {
        viewModelScope.launch {
            preferencesManager.disablePin()
            _isAppLocked.value = false
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setBiometricEnabled(enabled)
        }
    }

    fun setAutoLockTimeout(seconds: Int) {
        viewModelScope.launch {
            preferencesManager.setAutoLockTimeoutSeconds(seconds)
        }
    }

    fun addTransaction(
        title: String,
        amount: Long,
        type: String,
        category: String,
        categoryIcon: String = "category",
        dateMillis: Long,
        note: String = "",
        paymentMethod: String = "Tunai",
        relatedWishlistId: Long? = null
    ) {
        viewModelScope.launch {
            repository.insertTransaction(
                TransactionEntity(
                    title = title.trim(),
                    amount = amount,
                    type = type,
                    category = category.trim(),
                    categoryIcon = categoryIcon,
                    dateMillis = dateMillis,
                    note = note.trim(),
                    paymentMethod = paymentMethod,
                    relatedWishlistId = relatedWishlistId
                )
            )
        }
    }

    fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun addCategory(name: String, type: String, icon: String, colorHex: String) {
        viewModelScope.launch {
            repository.insertCategory(
                CategoryEntity(
                    name = name.trim(),
                    type = type,
                    icon = icon,
                    colorHex = colorHex,
                    isDefault = false
                )
            )
        }
    }

    fun updateCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.updateCategory(category)
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }

    fun addWishlist(
        title: String,
        targetAmount: Long,
        currentAmount: Long = 0,
        targetDateMillis: Long? = null,
        note: String = "",
        imagePath: String? = null
    ) {
        viewModelScope.launch {
            val isAchieved = currentAmount >= targetAmount
            repository.insertWishlist(
                WishlistEntity(
                    title = title.trim(),
                    targetAmount = targetAmount,
                    currentAmount = currentAmount,
                    targetDateMillis = targetDateMillis,
                    note = note.trim(),
                    imagePath = imagePath,
                    isAchieved = isAchieved
                )
            )
        }
    }

    fun updateWishlist(wishlist: WishlistEntity) {
        viewModelScope.launch {
            val isAchieved = wishlist.currentAmount >= wishlist.targetAmount
            repository.updateWishlist(wishlist.copy(isAchieved = isAchieved))
        }
    }

    fun deleteWishlist(wishlist: WishlistEntity) {
        viewModelScope.launch {
            repository.deleteWishlist(wishlist)
        }
    }

    fun depositToWishlist(
        wishlist: WishlistEntity,
        depositAmount: Long,
        recordAsTransaction: Boolean,
        paymentMethod: String = "Tunai"
    ) {
        viewModelScope.launch {
            repository.depositToWishlist(wishlist, depositAmount, recordAsTransaction, paymentMethod)
        }
    }

    fun toggleAchieved(wishlist: WishlistEntity) {
        viewModelScope.launch {
            val updated = wishlist.copy(isAchieved = !wishlist.isAchieved)
            repository.updateWishlist(updated)
        }
    }

    fun updateProfile(name: String, bio: String, photoPath: String?) {
        viewModelScope.launch {
            preferencesManager.updateProfile(name, bio, photoPath)
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            preferencesManager.setThemeMode(mode)
        }
    }

    fun setAccentColor(color: String) {
        viewModelScope.launch {
            preferencesManager.setAccentColor(color)
        }
    }

    fun setBackgroundPreset(preset: String) {
        viewModelScope.launch {
            preferencesManager.setBackgroundPreset(preset)
        }
    }

    fun setCustomBackgroundImagePath(path: String?) {
        viewModelScope.launch {
            preferencesManager.setCustomBackgroundImagePath(path)
        }
    }

    fun resetDisplayPreferences() {
        viewModelScope.launch {
            preferencesManager.resetDisplayPreferences()
        }
    }

    fun exportBackupJson(): String {
        return BackupManager.exportToJson(
            transactions = transactions.value,
            categories = categories.value,
            wishlists = wishlist.value
        )
    }

    suspend fun restoreBackupJson(jsonString: String): Result<Unit> {
        return try {
            val data = BackupManager.parseFromJson(jsonString)
            repository.restoreBackup(data)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun exportTransactionsCsv(): String {
        return BackupManager.exportTransactionsToCsv(transactions.value)
    }
}
