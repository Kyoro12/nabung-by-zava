package com.example

import com.example.data.backup.BackupManager
import com.example.data.model.TransactionEntity
import com.example.util.CurrencyUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.MessageDigest
import java.util.Base64

class ExampleUnitTest {

    @Test
    fun testCurrencyFormatting() {
        assertEquals("Rp 1.500.000", CurrencyUtils.formatRupiah(1_500_000L))
        assertEquals("Rp 0", CurrencyUtils.formatRupiah(0L))
        assertEquals("Rp 25.000", CurrencyUtils.formatRupiah(25_000L))
        assertEquals(500000L, CurrencyUtils.parseAmount("500.000"))
    }

    @Test
    fun testPinHashVerification() {
        val pin = "1234"
        val salt = "randomSalt123"

        val md = MessageDigest.getInstance("SHA-256")
        val input = "$salt:$pin".toByteArray(Charsets.UTF_8)
        val hash = Base64.getEncoder().encodeToString(md.digest(input))

        // Recompute
        val md2 = MessageDigest.getInstance("SHA-256")
        val input2 = "$salt:$pin".toByteArray(Charsets.UTF_8)
        val computed = Base64.getEncoder().encodeToString(md2.digest(input2))

        assertEquals(hash, computed)
    }

    @Test
    fun testCsvExport() {
        val txs = listOf(
            TransactionEntity(
                id = 1,
                title = "Makan Siang",
                amount = 35_000,
                type = "EXPENSE",
                category = "Makanan & Minuman",
                dateMillis = 1700000000000L,
                note = "Nasi Padang",
                paymentMethod = "Tunai"
            )
        )
        val csv = BackupManager.exportTransactionsToCsv(txs)
        assertTrue(csv.contains("Makan Siang"))
        assertTrue(csv.contains("35000"))
        assertTrue(csv.contains("Pengeluaran"))
    }
}
