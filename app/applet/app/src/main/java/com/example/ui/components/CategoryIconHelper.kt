package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.ui.graphics.vector.ImageVector

object CategoryIconHelper {

    fun getIconForName(iconName: String): ImageVector {
        return when (iconName.lowercase()) {
            "payments" -> Icons.Default.Payments
            "store" -> Icons.Default.Store
            "card_giftcard" -> Icons.Default.CardGiftcard
            "trending_up" -> Icons.Default.TrendingUp
            "account_balance_wallet" -> Icons.Default.AccountBalanceWallet
            "restaurant" -> Icons.Default.Restaurant
            "shopping_bag" -> Icons.Default.ShoppingBag
            "directions_car" -> Icons.Default.DirectionsCar
            "receipt_long" -> Icons.Default.ReceiptLong
            "movie" -> Icons.Default.Movie
            "medical_services" -> Icons.Default.MedicalServices
            "school" -> Icons.Default.School
            "savings" -> Icons.Default.Savings
            else -> Icons.Default.Category
        }
    }

    val availableIcons = listOf(
        "payments" to "Gaji / Pembayaran",
        "store" to "Toko / Usaha",
        "card_giftcard" to "Hadiah",
        "trending_up" to "Investasi",
        "account_balance_wallet" to "Dompet",
        "restaurant" to "Makanan",
        "shopping_bag" to "Belanja",
        "directions_car" to "Transportasi",
        "receipt_long" to "Tagihan",
        "movie" to "Hiburan",
        "medical_services" to "Kesehatan",
        "school" to "Pendidikan",
        "savings" to "Tabungan",
        "more_horiz" to "Lainnya"
    )
}
