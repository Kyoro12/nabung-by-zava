package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.backup.BackupManager
import com.example.data.model.CategoryEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.WishlistEntity
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Nabung By. Zava", appName)
    }

    @Test
    fun `test BackupManager JSON export and import with Android JSONObject`() {
        val txs = listOf(
            TransactionEntity(
                id = 1,
                title = "Gaji",
                amount = 5_000_000,
                type = "INCOME",
                category = "Gaji",
                categoryIcon = "payments",
                dateMillis = 1700000000000L,
                note = "Gaji bulanan",
                paymentMethod = "Transfer Bank"
            )
        )
        val cats = listOf(
            CategoryEntity(
                id = 1,
                name = "Gaji",
                type = "INCOME",
                icon = "payments",
                colorHex = "#2E7D32"
            )
        )
        val wishlists = listOf(
            WishlistEntity(
                id = 1,
                title = "Kamera",
                targetAmount = 10_000_000,
                currentAmount = 2_500_000,
                note = "Impian foto bareng"
            )
        )

        val json = BackupManager.exportToJson(txs, cats, wishlists)
        val parsed = BackupManager.parseFromJson(json)

        assertEquals(1, parsed.transactions.size)
        assertEquals("Gaji", parsed.transactions[0].title)
        assertEquals(5_000_000L, parsed.transactions[0].amount)
        assertEquals(1, parsed.wishlists.size)
        assertEquals("Kamera", parsed.wishlists[0].title)
        assertEquals(2_500_000L, parsed.wishlists[0].currentAmount)
    }
}
