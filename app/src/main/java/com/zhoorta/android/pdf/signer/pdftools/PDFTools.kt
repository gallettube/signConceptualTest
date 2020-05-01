package com.zhoorta.android.pdf.signer.pdftools

import android.content.Context
import android.util.Log
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject
import com.tom_roush.pdfbox.util.PDFBoxResourceLoader
import java.io.File

class PDFTools(context: Context?) {
    private var doc: PDDocument? = null
    private var source: File? = null
    fun open(file: File?) {
        try {
            doc = PDDocument.load(file)
            source = file
            Log.d("signer", "open pdf done")
        } catch (ex: Exception) {
            Log.e("signer", "open pdf exception")
            Log.e("signer", ex.toString())
        }
    }

    fun insertImage(imagePath: String?, xpos: Int, ypos: Int, width: Int, height: Int, pagenumber: Int) {
        try {
            val page = doc!!.getPage(pagenumber - 1)
            val pdImage = PDImageXObject.createFromFile(imagePath, doc)
            val contentStream = PDPageContentStream(doc, page, true, false)
            contentStream.drawImage(pdImage, xpos.toFloat(), ypos.toFloat(), width.toFloat(), height.toFloat())
            contentStream.close()
            doc!!.save(source!!.path)
            doc!!.close()
            Log.d("signer", "insertImage done")
        } catch (ex: Exception) {
            Log.e("signer", "insertImage exception")
            Log.e("signer", ex.toString())
        }
    }

    init {
        PDFBoxResourceLoader.init(context)
    }
}