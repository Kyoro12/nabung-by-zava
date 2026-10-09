package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CategoryEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.WishlistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY dateMillis DESC, id DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    fun getTransactionById(id: Long): Flow<TransactionEntity?>

    @Query("SELECT * FROM transactions ORDER BY dateMillis DESC, id DESC LIMIT :limit")
    fun getRecentTransactions(limit: Int): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE dateMillis BETWEEN :startMillis AND :endMillis ORDER BY dateMillis DESC")
    fun getTransactionsBetween(startMillis: Long, endMillis: Long): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTransactions(transactions: List<TransactionEntity>)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)

    @Query("DELETE FROM transactions")
    suspend fun clearAllTransactions()
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY isDefault DESC, name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE type = :type ORDER BY isDefault DESC, name ASC")
    fun getCategoriesByType(type: String): Flow<List<CategoryEntity>>

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoriesCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCategories(categories: List<CategoryEntity>)

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

    @Query("DELETE FROM categories")
    suspend fun clearAllCategories()
}

@Dao
interface WishlistDao {
    @Query("SELECT * FROM wishlist ORDER BY isAchieved ASC, createdAtMillis DESC")
    fun getAllWishlist(): Flow<List<WishlistEntity>>

    @Query("SELECT * FROM wishlist WHERE id = :id LIMIT 1")
    fun getWishlistById(id: Long): Flow<WishlistEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWishlist(item: WishlistEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllWishlist(items: List<WishlistEntity>)

    @Update
    suspend fun updateWishlist(item: WishlistEntity)

    @Delete
    suspend fun deleteWishlist(item: WishlistEntity)

    @Query("DELETE FROM wishlist WHERE id = :id")
    suspend fun deleteWishlistById(id: Long)

    @Query("DELETE FROM wishlist")
    suspend fun clearAllWishlist()
}
