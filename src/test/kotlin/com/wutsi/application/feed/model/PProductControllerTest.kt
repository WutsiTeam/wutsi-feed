package com.wutsi.application.feed.model

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.feed.Fixtures
import com.wutsi.marketplace.manager.MarketplaceManagerApi
import com.wutsi.marketplace.manager.dto.GetOfferResponse
import com.wutsi.marketplace.manager.dto.SearchOfferResponse
import com.wutsi.membership.manager.MembershipManagerApi
import com.wutsi.membership.manager.dto.GetMemberResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.server.LocalServerPort
import java.io.File
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class PProductControllerTest {
    @LocalServerPort
    val port: Int = 0

    @MockBean
    private lateinit var membershipManagerApi: MembershipManagerApi

    @MockBean
    private lateinit var marketplaceManagerApi: MarketplaceManagerApi

    @Value("\${wutsi.platform.storage.local.directory}")
    private lateinit var storageDir: String

    private val member = Fixtures.createMember(id = 1, business = true, storeId = 11L, businessId = 222L)
    private val offers = listOf(
        Fixtures.createOfferSummary(product = Fixtures.createProductSummary(1L)),
        Fixtures.createOfferSummary(product = Fixtures.createProductSummary(2L)),
        Fixtures.createOfferSummary(product = Fixtures.createProductSummary(3L)),
    )
    private val offer = Fixtures.createOffer(product = Fixtures.createProduct())
    private val now = LocalDate.now()

    @BeforeEach
    fun setUp() {
        File("$storageDir/feed").deleteRecursively()

        doReturn(GetMemberResponse(member)).whenever(membershipManagerApi).getMember(any())
        doReturn(SearchOfferResponse(offers)).whenever(marketplaceManagerApi).searchOffer(any())
        doReturn(GetOfferResponse(offer)).whenever(marketplaceManagerApi).getOffer(any())
    }

    @Test
    fun product() {
        // WHEN
        val response = URL(url()).readText()

        // THEN
        val file =
            File(
                "$storageDir/feed/" +
                    now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd")) +
                    "/pinterest/product.csv",
            )
        kotlin.test.assertTrue(file.exists())
        kotlin.test.assertEquals(response, file.readText())
    }

    private fun url() = "http://localhost:$port/pinterest/product.csv"
}
