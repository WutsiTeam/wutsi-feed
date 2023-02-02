package com.wutsi.application.feed.facebook

import com.wutsi.application.feed.service.AbstractOfferLoader
import com.wutsi.enums.ProductType
import com.wutsi.marketplace.manager.MarketplaceManagerApi
import com.wutsi.marketplace.manager.dto.SearchOfferRequest
import com.wutsi.regulation.RegulationEngine
import org.springframework.stereotype.Service

@Service
class FBOfferLoader(
    marketplaceManagerApi: MarketplaceManagerApi,
    regulationEngine: RegulationEngine,
) : AbstractOfferLoader(marketplaceManagerApi, regulationEngine) {
    override fun createSearchRequest(storeId: Long?, limit: Int, offset: Int) = SearchOfferRequest(
        storeId = storeId,
        limit = limit,
        offset = offset,
        types = listOf(ProductType.PHYSICAL_PRODUCT.name),
    )
}
