package com.wutsi.application.feed.service

import com.wutsi.application.feed.Fixtures
import com.wutsi.regulation.Country
import com.wutsi.regulation.RegulationEngine
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertEquals
import kotlin.test.assertNull

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class MapperTest {
    @Autowired
    private lateinit var mapper: Mapper

    @Autowired
    private lateinit var regulationEngine: RegulationEngine

    @Value("\${wutsi.application.webapp-url}")
    private lateinit var webappUrl: String

    private val member = Fixtures.createMember()

    private val offer = Fixtures.createOffer(
        product = Fixtures.createProduct(
            id = 11L,
            quantity = 100,
            title = "Beautiful Neckless",
            price = 10000L,
            description = "This is the description of the product",
            summary = "This is the summary of the product",
            pictures = listOf(
                Fixtures.createPictureSummary(url = "https://www.img/com/1.png"),
                Fixtures.createPictureSummary(url = "https://www.img/com/2.png"),
                Fixtures.createPictureSummary(url = "https://www.img/com/3.png"),
            ),
            url = "/p/11/beautiful-neckless",
        ),
        price = Fixtures.createOfferPrice(
            productId = 11L,
            price = 10000L,
            referencePrice = null,
        ),
    )

    private lateinit var country: Country

    @BeforeEach
    fun setUp() {
        country = regulationEngine.country("CM")
    }

    @Test
    fun product() {
        val fb = mapper.map(offer, member.displayName, country)

        assertEquals(offer.product.title, fb.title)
        assertEquals("in stock", fb.availability)
        assertEquals("new", fb.condition)
        assertEquals(offer.product.description, fb.description)
        assertEquals("10,000 XAF", fb.price)
        assertNull(fb.salePrice)
        assertEquals("$webappUrl${offer.product.url}", fb.link)
        assertEquals(offer.product.thumbnail?.url, fb.imageLink)
        assertEquals(listOf("https://www.img/com/2.png", "https://www.img/com/3.png"), fb.additionalImageLink)
        assertEquals(offer.product.category?.id, fb.googleProductCategory)
        assertEquals(member.displayName, fb.brand)
    }

    @Test
    fun savings() {
        val fb = mapper.map(
            offer.copy(price = offer.price.copy(referencePrice = 10000L, price = 9000L)),
            member.displayName,
            country,
        )

        assertEquals("10,000 XAF", fb.price)
        assertEquals("9,000 XAF", fb.salePrice)
    }

    @Test
    fun outOfStock() {
        val fb = mapper.map(
            offer.copy(product = offer.product.copy(quantity = 0, outOfStock = true)),
            member.displayName,
            country,
        )

        assertEquals("out of stock", fb.availability)
    }

    @Test
    fun nullDescription() {
        val fb = mapper.map(
            offer.copy(product = offer.product.copy(description = null)),
            member.displayName,
            country,
        )

        assertEquals(offer.product.summary, fb.description)
    }

    @Test
    fun emptyDescription() {
        val fb = mapper.map(
            offer.copy(product = offer.product.copy(description = "")),
            member.displayName,
            country,
        )

        assertEquals(offer.product.summary, fb.description)
    }
}
