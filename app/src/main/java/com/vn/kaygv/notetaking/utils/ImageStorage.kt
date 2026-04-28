package com.vn.kaygv.notetaking.utils

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import java.io.File
import java.util.UUID

object ImageStorage {
    fun copyToInternal(context: Context, uri: Uri): String {
        val input = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Cannot open input stream")

        val imagesDir = File(context.filesDir, "images").apply { mkdirs() }

        val file = File(imagesDir, "${UUID.randomUUID()}.jpg")

        input.use { inputStream ->
            file.outputStream().use { output ->
                inputStream.copyTo(output)
            }
        }

        return file.toURI().toString() // returns file://...
    }

    fun deleteImagesFromContent(content: String) {
        val regex = Regex("""!\[]\((file://.*?)\)""")

        regex.findAll(content).forEach {
            val path = it.groupValues[1].toUri().path ?: return@forEach
            File(path).delete()
        }
    }

    fun delete(url: String) {
        val path = url.toUri().path ?: return
        File(path).delete()
    }

}