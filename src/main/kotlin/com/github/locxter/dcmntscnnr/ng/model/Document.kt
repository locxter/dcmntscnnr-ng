package com.github.locxter.dcmntscnnr.ng.model

import com.github.locxter.dcmntscnnr.ng.lib.OpencvHelpers
import com.lowagie.text.Document
import com.lowagie.text.PageSize
import com.lowagie.text.Rectangle
import com.lowagie.text.pdf.PdfWriter
import java.io.File

data class Document(
    val pageSize: Rectangle = PageSize.A4,
    val pages: MutableList<Page> = mutableListOf()
) {
    fun cropPages() {
        for (page in pages) {
            page.crop()
        }
    }

    fun binarizePages() {
        for (page in pages) {
            page.binarize()
        }
    }

    fun createPdf(file: File) {
        val document = Document()
        val pdfWriter = PdfWriter.getInstance(document, file.outputStream())
        document.open()
        for (page in pages) {
            if (page.state.ordinal >= EState.STATE_RAW.ordinal) {
                val image = OpencvHelpers.matToOpenPdfImage(
                    when (page.state) {
                        EState.STATE_RAW -> page.rawImage
                        EState.STATE_CROPPED -> page.croppedImage
                        EState.STATE_BINARIZED -> page.binarizedImage
                        else -> page.rawImage
                    }
                )
                val actualPageSize = if (image.width / image.height <
                    ((pageSize.width / pageSize.height) + (pageSize.height / pageSize.width)) / 2
                ) {
                    pageSize
                } else {
                    pageSize.rotate()
                }
                image.scaleToFit(actualPageSize.width, actualPageSize.height)
                image.setAbsolutePosition(
                    (actualPageSize.width - image.scaledWidth) / 2,
                    (actualPageSize.height - image.scaledHeight) / 2
                )
                document.setPageSize(actualPageSize)
                document.newPage()
                document.add(image)
            }
        }
        document.close()
    }
}
