package com.example.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

object ImageUtils {

    fun saveImageToInternalStorage(context: Context, sourceUri: Uri, prefix: String = "img"): String? {
        return try {
            val imagesDir = File(context.filesDir, "user_images").apply {
                if (!exists()) mkdirs()
            }
            val fileName = "${prefix}_${UUID.randomUUID()}.jpg"
            val destFile = File(imagesDir, fileName)

            val inputStream: InputStream? = context.contentResolver.openInputStream(sourceUri)
            val outputStream = FileOutputStream(destFile)

            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun deleteInternalImage(path: String?) {
        if (path.isNullOrBlank()) return
        try {
            val file = File(path)
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
