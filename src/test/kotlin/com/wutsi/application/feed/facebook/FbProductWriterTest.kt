package com.wutsi.application.feed.facebook

import com.wutsi.application.feed.model.ProductModel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream

internal class FbProductWriterTest {
    private val writer = FbProductWriter()
    private val product = ProductModel(
        id = "123",
        title = "This is a product",
        brand = "Nike",
        price = "1,000 XAF",
        salePrice = "500 XAF",
        googleProductCategory = 12343,
        description = "This is the description",
        imageLink = "https://img.com/1.png",
        availability = "in stock",
        condition = "new",
        link = "https://www.wutsi.me/p/123/this-is-a-product",
        additionalImageLink = listOf("https://img.com/2.png", "https://img.com/3.png"),
    )

    @Test
    fun write() {
        val out = ByteArrayOutputStream()
        writer.write(listOf(product), out)

        assertEquals(
            """
                "id","title","description","availability","condition","price","sale_price","brand","google_product_category","link","image_link","additional_image_link"
                "123","This is a product","This is the description","in stock","new","1,000 XAF","500 XAF","Nike","12343","https://www.wutsi.me/p/123/this-is-a-product","https://img.com/1.png","https://img.com/2.png|https://img.com/3.png"
            """.trimIndent(),
            out.toString().trimIndent(),
        )
    }
}
