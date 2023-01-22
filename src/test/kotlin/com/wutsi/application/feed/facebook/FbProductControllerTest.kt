package com.wutsi.application.feed.facebook

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.feed.Fixtures
import com.wutsi.checkout.manager.CheckoutManagerApi
import com.wutsi.checkout.manager.dto.GetBusinessResponse
import com.wutsi.enums.BusinessStatus
import com.wutsi.enums.StoreStatus
import com.wutsi.marketplace.manager.MarketplaceManagerApi
import com.wutsi.marketplace.manager.dto.GetOfferResponse
import com.wutsi.marketplace.manager.dto.GetStoreResponse
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
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class FbProductControllerTest {
    companion object {
        const val EMPTY_RESPONSE = """
            "id","title","description","availability","condition","price","sale_price","brand","google_product_category","link","image_link","additional_image_link"
        """
    }

    @LocalServerPort
    val port: Int = 0

    @MockBean
    private lateinit var membershipManagerApi: MembershipManagerApi

    @MockBean
    private lateinit var marketplaceManagerApi: MarketplaceManagerApi

    @MockBean
    private lateinit var checkoutManagerApi: CheckoutManagerApi

    @Value("\${wutsi.platform.storage.local.directory}")
    private lateinit var storageDir: String

    private val member = Fixtures.createMember(id = 1, business = true, storeId = 11L, businessId = 222L)
    private val business = Fixtures.createBusiness(id = member.businessId!!)
    private val store = Fixtures.createStore(id = member.storeId!!)
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
        doReturn(GetBusinessResponse(business)).whenever(checkoutManagerApi).getBusiness(any())
        doReturn(GetStoreResponse(store)).whenever(marketplaceManagerApi).getStore(any())
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
                    "/facebook/${member.id}/product.csv",
            )
        assertTrue(file.exists())
        assertEquals(response, file.readText())
    }

    @Test
    fun notBusiness() {
        // GIVEN
        doReturn(
            GetMemberResponse(member.copy(business = false)),
        ).whenever(membershipManagerApi).getMember(any())

        // WHEN
        val response = URL(url()).readText()

        // THEN
        assertEquals(EMPTY_RESPONSE.trimIndent(), response.trimIndent())
    }

    @Test
    fun noBusinessId() {
        // GIVEN
        doReturn(
            GetMemberResponse(member.copy(businessId = null)),
        ).whenever(membershipManagerApi).getMember(any())

        // WHEN
        val response = URL(url()).readText()

        // THEN
        assertEquals(EMPTY_RESPONSE.trimIndent(), response.trimIndent())
    }

    @Test
    fun businessNotActive() {
        // GIVEN
        doReturn(
            GetBusinessResponse(business.copy(status = BusinessStatus.INACTIVE.name)),
        ).whenever(checkoutManagerApi).getBusiness(any())

        // WHEN
        val response = URL(url()).readText()

        // THEN
        assertEquals(EMPTY_RESPONSE.trimIndent(), response.trimIndent())
    }

    @Test
    fun noStore() {
        // GIVEN
        doReturn(
            GetMemberResponse(member.copy(storeId = null)),
        ).whenever(membershipManagerApi).getMember(any())

        // WHEN
        val response = URL(url()).readText()

        // THEN
        assertEquals(EMPTY_RESPONSE.trimIndent(), response.trimIndent())
    }

    @Test
    fun storeNotActive() {
        // GIVEN
        doReturn(
            GetStoreResponse(store.copy(status = StoreStatus.INACTIVE.name)),
        ).whenever(marketplaceManagerApi).getStore(any())

        // WHEN
        val response = URL(url()).readText()

        // THEN
        assertEquals(EMPTY_RESPONSE.trimIndent(), response.trimIndent())
    }

    private fun url() = "http://localhost:$port/facebook/${member.id}/product.csv"
}
