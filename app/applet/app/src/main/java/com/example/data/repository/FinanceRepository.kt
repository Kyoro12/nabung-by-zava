package com.example.data.repository

import com.example.data.backup.BackupData
import com.example.data.local.AppDatabase
import com.example.data.local.CategoryDao
import com.example.data.local.TransactionDao
import com.example.data.local.WishlistDao
import com.example.data.model.CategoryEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.WishlistEntity
import kotlinx.coroutines.flow.Flow

class FinanceRepository(private val database: AppDatabase) {

    private val transactionDao: TransactionDao = database.transactionDao()
    private val categoryDao: CategoryDao = database.categoryDao()
    private val wishlistDao: WishlistDao = database.wishlistDao()

    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()
    val allWishlist: Flow<List<WishlistEntity>> = wishlistDao.getAllWishlist()

    fun getRecentTransactions(limit: Int = 5): Flow<List<TransactionEntity>> {
        return transactionDao.getRecentTransactions(limit)
    }

    suspend fun insertTransaction(transaction: TransactionEntity): Long {
        return transactionDao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: TransactionEntity) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun insertCategory(category: CategoryEntity): Long {
        return categoryDao.insertCategory(category)
    }

    suspend fun updateCategory(category: CategoryEntity) {
        categoryDao.updateCategory(category)
    }

    suspend fun deleteCategory(category: CategoryEntity) {
        categoryDao.deleteCategory(category)
    }

    suspend fun insertWishlist(wishlist: WishlistEntity): Long {
        return wishlistDao.insertWishlist(wishlist)
    }

    suspend fun updateWishlist(wishlist: WishlistEntity) {
        wishlistDao.updateWishlist(wishlist)
    }

    suspend fun deleteWishlist(wishlist: WishlistEntity) {
        wishlistDao.deleteWishlist(wishlist)
    }

    suspend fun depositToWishlist(
        wishlist: WishlistEntity,
        depositAmount: Long,
        recordAsTransaction: Boolean,
        paymentMethod: String = "Tunai"
    ) {
        val newAmount = wishlist.currentAmount + depositAmount
        val isAchieved = newAmount >= wishlist.targetAmount
        val updated = wishlist.copy(
            currentAmount = newAmount,
            isAchieved = isAchieved
        )
        wishlistDao.updateWishlist(updated)

        if (recordAsTransaction) {
            val tx = TransactionEntity(
                title = "Setoran: ${wishlist.title}",
                amount = depositAmount,
                type = "EXPENSE",
                category = "Tabungan Impian",
                categoryIcon = "savings",
                dateMillis = System.currentTimeMillis(),
                note = "Setoran tabungan target ${wishlist.title}",
                paymentMethod = paymentMethod,
                relatedWishlistId = wishlist.id
            )
            transactionDao.insertTransaction(tx)
        }
    }

    suspend fun restoreBackup(backupData: BackupData) {
        transactionDao.clearAllTransactions()
        categoryDao.clearAllCategories()
        wishlistDao.clearAllWishlist()

        categoryDao.insertAllCategories(backupData.categories)
        transactionDao.insertAllTransactions(backupData.transactions)
        wishlistDao.insertAllWishlist(backupData.wishlists)
    }
}
