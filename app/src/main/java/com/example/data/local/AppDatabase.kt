package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.CategoryEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.WishlistEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        WishlistEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun wishlistDao(): WishlistDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nabung_by_zava_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(context.applicationContext))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        val DEFAULT_CATEGORIES = listOf(
            // Income categories
            CategoryEntity(name = "Gaji", type = "INCOME", icon = "payments", colorHex = "#2E7D32", isDefault = true),
            CategoryEntity(name = "Bisnis", type = "INCOME", icon = "store", colorHex = "#1565C0", isDefault = true),
            CategoryEntity(name = "Hadiah / Bonus", type = "INCOME", icon = "card_giftcard", colorHex = "#AD1457", isDefault = true),
            CategoryEntity(name = "Investasi", type = "INCOME", icon = "trending_up", colorHex = "#6A1B9A", isDefault = true),
            CategoryEntity(name = "Pemasukan Lain", type = "INCOME", icon = "account_balance_wallet", colorHex = "#00838F", isDefault = true),

            // Expense categories
            CategoryEntity(name = "Makanan & Minuman", type = "EXPENSE", icon = "restaurant", colorHex = "#D84315", isDefault = true),
            CategoryEntity(name = "Belanja & Kebutuhan", type = "EXPENSE", icon = "shopping_bag", colorHex = "#C2185B", isDefault = true),
            CategoryEntity(name = "Transportasi", type = "EXPENSE", icon = "directions_car", colorHex = "#283593", isDefault = true),
            CategoryEntity(name = "Tagihan & Pulsa", type = "EXPENSE", icon = "receipt_long", colorHex = "#E65100", isDefault = true),
            CategoryEntity(name = "Hiburan & Hobi", type = "EXPENSE", icon = "movie", colorHex = "#7B1FA2", isDefault = true),
            CategoryEntity(name = "Kesehatan", type = "EXPENSE", icon = "medical_services", colorHex = "#00695C", isDefault = true),
            CategoryEntity(name = "Edukasi & Buku", type = "EXPENSE", icon = "school", colorHex = "#4527A0", isDefault = true),
            CategoryEntity(name = "Tabungan Impian", type = "EXPENSE", icon = "savings", colorHex = "#2E7D32", isDefault = true),
            CategoryEntity(name = "Pengeluaran Lain", type = "EXPENSE", icon = "more_horiz", colorHex = "#455A64", isDefault = true)
        )

        private class DatabaseCallback(private val appContext: Context) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    val database = getDatabase(appContext)
                    database.categoryDao().insertAllCategories(DEFAULT_CATEGORIES)
                }
            }
        }
    }
}
