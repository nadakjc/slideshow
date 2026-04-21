package com.example.slideshow.data

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FolderRepository(private val appContext: Context) {

    suspend fun listImages(treeUri: Uri, recursive: Boolean): List<Uri> =
        withContext(Dispatchers.IO) {
            val root = DocumentFile.fromTreeUri(appContext, treeUri) ?: return@withContext emptyList()
            val out = ArrayList<Pair<String, Uri>>()
            collect(root, recursive, out)
            out.sortedBy { it.first.lowercase() }.map { it.second }
        }

    private fun collect(dir: DocumentFile, recursive: Boolean, out: MutableList<Pair<String, Uri>>) {
        for (child in dir.listFiles()) {
            when {
                child.isDirectory -> if (recursive) collect(child, true, out)
                child.isFile && isImage(child.type, child.name) ->
                    out.add((child.name ?: "") to child.uri)
            }
        }
    }

    private fun isImage(mime: String?, name: String?): Boolean {
        if (mime != null && mime.startsWith("image/")) return true
        val ext = name?.substringAfterLast('.', "")?.lowercase() ?: return false
        return ext in IMAGE_EXTENSIONS
    }

    private companion object {
        val IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png", "webp", "heic", "heif", "gif", "bmp")
    }
}
