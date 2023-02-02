package com.wutsi.application.feed.facebook

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.application.feed.Fixtures
import com.wutsi.application.feed.service.AbstractOfferLoader
import com.wutsi.enums.ProductType
import com.wutsi.marketplace.manager.MarketplaceManagerApi
import com.wutsi.marketplace.manager.dto.GetOfferResponse
import com.wutsi.marketplace.manager.dto.SearchOfferRequest
import com.wutsi.marketplace.manager.dto.SearchOfferResponse
import com.wutsi.regulation.RegulationEngine
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
internal class FBOfferLoaderTest {
    @MockBean
    private lateinit var marketplaceManagerApi: MarketplaceManagerApi

    @Autowired
    private lateinit var loader: FBOfferLoader

    @Autowired
    protected lateinit var regulationEngine: RegulationEngine

    private val member = Fixtures.createMember(id = 1, business = true, storeId = 11L, businessId = 222L)
    private val offers = listOf(
        Fixtures.createOfferSummary(product = Fixtures.createProductSummary(1L)),
        Fixtures.createOfferSummary(product = Fixtures.createProductSummary(2L)),
        Fixtures.createOfferSummary(product = Fixtures.createProductSummary(3L)),
    )
    private val offer = Fixtures.createOffer(product = Fixtures.createProduct())

    @BeforeEach
    fun setUp() {
        doReturn(SearchOfferResponse(offers)).whenever(marketplaceManagerApi).searchOffer(any())
        doReturn(GetOfferResponse(offer)).whenever(marketplaceManagerApi).getOffer(any())
    }

    @Test
    fun loadMember() {
        val offers = loader.load(member)

        verify(marketplaceManagerApi).searchOffer(
            SearchOfferRequest(
                types = listOf(ProductType.PHYSICAL_PRODUCT.name),
                limit = regulationEngine.maxProducts(),
                storeId = member.storeId!!,
            ),
        )
        verify(marketplaceManagerApi, times(3)).getOffer(any())
        assertEquals(3, offers.size)
    }

    @Test
    fun loadAll() {
        val offers = loader.load()

        verify(marketplaceManagerApi).searchOffer(
            SearchOfferRequest(
                types = listOf(ProductType.PHYSICAL_PRODUCT.name),
                limit = AbstractOfferLoader.LIMIT,
                offset = 0,
            ),
        )
        verify(marketplaceManagerApi, times(3)).getOffer(any())
        assertEquals(3, offers.size)
    }
}
