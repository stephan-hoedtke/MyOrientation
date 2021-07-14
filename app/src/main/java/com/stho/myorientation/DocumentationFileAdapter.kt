package com.stho.myorientation

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Environment
import android.os.ParcelFileDescriptor
import androidx.core.content.FileProvider
import com.stho.myorientation.library.filter.IOrientationFilter
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream


class DocumentationFileAdapter(private val context: Context) {

    fun getBitmapFromPdf(filter: IOrientationFilter): Bitmap {
        val pdf = filter.pdf
        val file = loadFile(pdf, false)
        return openPDF(file, 0)
    }

    fun openPdf(filter: IOrientationFilter) {
        val pdf = filter.pdf
        val file = loadFile(pdf, true)
        val authority = BuildConfig.APPLICATION_ID + ".provider"
        val uri: Uri = FileProvider.getUriForFile(context, authority, file)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, "application/pdf")
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(intent)
    }

    fun openLink(filter: IOrientationFilter) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(filter.link))
        context.startActivity(intent)
    }


    private fun loadFile(fileName: String, overwrite: Boolean = true): File {
        val context = context
        val file = File(rootDir, fileName)
        if (file.exists() && overwrite) {
            file.delete()
        }
        if (!file.exists()) {
            createFileFromAssets(context, file)
        }
        return file
    }

    private val rootDir
        get() = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)

    companion object {

        private fun createFileFromAssets(context: Context, file: File) {
            // Since PdfRenderer cannot handle the compressed asset file directly, we copy it into
            // the cache directory.
            val asset: InputStream = context.assets.open(file.name)
            val outputStream = FileOutputStream(file)
            val size: Int = asset.available()
            val buffer = ByteArray(size)
            asset.read(buffer)
            asset.close()
            outputStream.write(buffer, 0, size)
            outputStream.close()
        }

        internal fun openPDF(file: File, pageIndex: Int = 0): Bitmap {
            val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(fileDescriptor)
            val page = renderer.openPage(pageIndex)
            val bitmap = Bitmap.createBitmap(3 * page.width, 3 * page.height, Bitmap.Config.ARGB_8888)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
            page.close()
            renderer.close()
            return bitmap
        }
    }
}

