package com.wutsi.application.feed.service

import com.wutsi.marketplace.manager.dto.Product
import java.io.OutputStream

interface ProductWriter {
    fun write(items: List<Product>, out: OutputStream)
}
