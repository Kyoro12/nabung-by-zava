package com.example.data.backup

import com.example.data.model.CategoryEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.WishlistEntity
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BackupData(
    val version: Int,
    val exportedAt: Long,
    val transactions: List<TransactionEntity>,
    val categories: List<CategoryEntity>,
    val wishlists: List<WishlistEntity>
)

object BackupManager {

    fun exportToJson(
        transactions: List<TransactionEntity>,
        categories: List<CategoryEntity>,
        wishlists: List<WishlistEntity>
    ): String {
        val root = JSONObject()
        root.put("appName", "Nabung By. Zava")
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())

        val txArray = JSONArray()
        for (tx in transactions) {
            val obj = JSONObject()
            obj.put("id", tx.id)
            obj.put("title", tx.title)
            obj.put("amount", tx.amount)
            obj.put("type", tx.type)
            obj.put("category", tx.category)
            obj.put("categoryIcon", tx.categoryIcon)
            obj.put("dateMillis", tx.dateMillis)
            obj.put("note", tx.note)
            obj.put("paymentMethod", tx.paymentMethod)
            if (tx.relatedWishlistId != null) {
                obj.put("relatedWishlistId", tx.relatedWishlistId)
            }
            txArray.put(obj)
        }
        root.put("transactions", txArray)

        val catArray = JSONArray()
        for (cat in categories) {
            val obj = JSONObject()
            obj.put("id", cat.id)
            obj.put("name", cat.name)
            obj.put("type", cat.type)
            obj.put("icon", cat.icon)
            obj.put("colorHex", cat.colorHex)
            obj.put("isDefault", cat.isDefault)
            catArray.put(obj)
        }
        root.put("categories", catArray)

        val wArray = JSONArray()
        for (w in wishlists) {
            val obj = JSONObject()
            obj.put("id", w.id)
            obj.put("title", w.title)
            obj.put("targetAmount", w.targetAmount)
            obj.put("currentAmount", w.currentAmount)
            if (w.targetDateMillis != null) {
                obj.put("targetDateMillis", w.targetDateMillis)
            }
            obj.put("note", w.note)
            obj.put("isAchieved", w.isAchieved)
            obj.put("createdAtMillis", w.createdAtMillis)
            wArray.put(obj)
        }
        root.put("wishlists", wArray)

        return root.toString(2)
    }

    fun parseFromJson(jsonString: String): BackupData {
        val root = JSONObject(jsonString)
        val version = root.optInt("version", 1)
        val exportedAt = root.optLong("exportedAt", System.currentTimeMillis())

        val txList = mutableListOf<TransactionEntity>()
        val txArray = root.optJSONArray("transactions")
        if (txArray != null) {
            for (i in 0 until txArray.length()) {
                val o = txArray.getJSONObject(i)
                txList.add(
                    TransactionEntity(
                        id = o.optLong("id", 0),
                        title = o.getString("title"),
                        amount = o.getLong("amount"),
                        type = o.getString("type"),
                        category = o.getString("category"),
                        categoryIcon = o.optString("categoryIcon", "category"),
                        dateMillis = o.getLong("dateMillis"),
                        note = o.optString("note", ""),
                        paymentMethod = o.optString("paymentMethod", "Tunai"),
                        relatedWishlistId = if (o.has("relatedWishlistId")) o.getLong("relatedWishlistId") else null
                    )
                )
            }
        }

        val catList = mutableListOf<CategoryEntity>()
        val catArray = root.optJSONArray("categories")
        if (catArray != null) {
            for (i in 0 until catArray.length()) {
                val o = catArray.getJSONObject(i)
                catList.add(
                    CategoryEntity(
                        id = o.optLong("id", 0),
                        name = o.getString("name"),
                        type = o.getString("type"),
                        icon = o.optString("icon", "category"),
                        colorHex = o.optString("colorHex", "#4CAF50"),
                        isDefault = o.optBoolean("isDefault", false)
                    )
                )
            }
        }

        val wList = mutableListOf<WishlistEntity>()
        val wArray = root.optJSONArray("wishlists")
        if (wArray != null) {
            for (i in 0 until wArray.length()) {
                val o = wArray.getJSONObject(i)
                wList.add(
                    WishlistEntity(
                        id = o.optLong("id", 0),
                        title = o.getString("title"),
                        targetAmount = o.getLong("targetAmount"),
                        currentAmount = o.optLong("currentAmount", 0),
                        targetDateMillis = if (o.has("targetDateMillis")) o.getLong("targetDateMillis") else null,
                        note = o.optString("note", ""),
                        isAchieved = o.optBoolean("isAchieved", false),
                        createdAtMillis = o.optLong("createdAtMillis", System.currentTimeMillis())
                    )
                )
            }
        }

        return BackupData(
            version = version,
            exportedAt = exportedAt,
            transactions = txList,
            categories = catList,
            wishlists = wList
        )
    }

    fun exportTransactionsToCsv(transactions: List<TransactionEntity>): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val sb = StringBuilder()
        sb.append("ID,Tanggal,Judul,Tipe,Kategori,Nominal (IDR),Metode Pembayaran,Catatan\n")

        for (tx in transactions) {
            val dateStr = dateFormat.format(Date(tx.dateMillis))
            val cleanTitle = escapeCsv(tx.title)
            val cleanCategory = escapeCsv(tx.category)
            val cleanMethod = escapeCsv(tx.paymentMethod)
            val cleanNote = escapeCsv(tx.note)
            val typeStr = if (tx.type == "INCOME") "Pemasukan" else "Pengeluaran"

            sb.append("${tx.id},\"$dateStr\",\"$cleanTitle\",$typeStr,\"$cleanCategory\",${tx.amount},\"$cleanMethod\",\"$cleanNote\"\n")
        }
        return sb.toString()
    }

    private fun escapeCsv(value: String): String {
        return value.replace("\"", "\"\"")
    }
}
