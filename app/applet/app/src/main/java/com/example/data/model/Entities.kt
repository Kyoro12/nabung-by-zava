package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
    INCOME,
    EXPENSE
}

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Long, // in Rupiah
    val type: String, // "INCOME" or "EXPENSE"
    val category: String,
    val categoryIcon: String = "category",
    val dateMillis: Long,
    val note: String = "",
    val paymentMethod: String = "Tunai",
    val relatedWishlistId: Long? = null
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: String, // "INCOME" or "EXPENSE"
    val icon: String,
    val colorHex: String,
    val isDefault: Boolean = false
)

@Entity(tableName = "wishlist")
data class WishlistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val targetAmount: Long,
    val currentAmount: Long = 0,
    val targetDateMillis: Long? = null,
    val note: String = "",
    val imagePath: String? = null,
    val isAchieved: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis()
)
