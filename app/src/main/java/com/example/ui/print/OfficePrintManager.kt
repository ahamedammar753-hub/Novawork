package com.example.ui.print

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.widget.Toast
import java.io.FileOutputStream
import java.io.IOException

object OfficePrintManager {

    fun printDocument(
        context: Context,
        jobName: String,
        title: String,
        contentLines: List<String>
    ) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
        if (printManager == null) {
            Toast.makeText(context, "Printing not supported on this device", Toast.LENGTH_SHORT).show()
            return
        }

        val adapter = object : PrintDocumentAdapter() {
            private var pdfDocument: PdfDocument? = null

            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes,
                cancellationSignal: CancellationSignal?,
                callback: LayoutResultCallback,
                extras: Bundle?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback.onLayoutCancelled()
                    return
                }

                val info = PrintDocumentInfo.Builder("$jobName.pdf")
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(1)
                    .build()

                callback.onLayoutFinished(info, true)
            }

            override fun onWrite(
                pages: Array<out PageRange>?,
                destination: ParcelFileDescriptor,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback
            ) {
                pdfDocument = PdfDocument()
                val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 at 72dpi
                val page = pdfDocument!!.startPage(pageInfo)

                val canvas: Canvas = page.canvas
                val paint = Paint().apply {
                    color = Color.BLACK
                    textSize = 14f
                    isAntiAlias = true
                }

                // Title
                val titlePaint = Paint().apply {
                    color = Color.rgb(15, 23, 42)
                    textSize = 20f
                    isFakeBoldText = true
                    isAntiAlias = true
                }

                canvas.drawText("NOVA Office - $title", 40f, 60f, titlePaint)

                var y = 100f
                for (line in contentLines) {
                    if (y > 780f) break
                    canvas.drawText(line, 40f, y, paint)
                    y += 24f
                }

                pdfDocument!!.finishPage(page)

                try {
                    pdfDocument!!.writeTo(FileOutputStream(destination.fileDescriptor))
                    callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                } catch (e: IOException) {
                    callback.onWriteFailed(e.message)
                } finally {
                    pdfDocument!!.close()
                    pdfDocument = null
                }
            }
        }

        printManager.print(jobName, adapter, PrintAttributes.Builder().build())
    }
}
