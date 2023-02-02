package com.wutsi.application.feed.service

import com.opencsv.CSVWriter
import com.wutsi.application.feed.model.ProductModel
import java.io.OutputStream
import java.io.OutputStreamWriter

abstract class AbstractProductWriter {
    fun write(items: List<ProductModel>, out: OutputStream) {
        val writer = OutputStreamWriter(out)
        val csv = CSVWriter(
            writer,
            CSVWriter.DEFAULT_SEPARATOR,
            CSVWriter.DEFAULT_QUOTE_CHARACTER,
            CSVWriter.DEFAULT_ESCAPE_CHARACTER,
            CSVWriter.DEFAULT_LINE_END,
        )
        csv.use {
            headers(csv)
            items.forEach {
                data(it, csv)
            }
        }
    }

    protected abstract fun headers(csv: CSVWriter)
    protected abstract fun data(item: ProductModel, csv: CSVWriter)
}
