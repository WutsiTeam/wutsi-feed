package com.wutsi.application.feed.facebook

import com.wutsi.application.feed.service.OfferLoader
import com.wutsi.enums.ProductType
import com.wutsi.marketplace.manager.MarketplaceManagerApi
import com.wutsi.marketplace.manager.dto.Offer
import com.wutsi.marketplace.manager.dto.SearchOfferRequest
import com.wutsi.membership.manager.dto.Member
import com.wutsi.regulation.RegulationEngine
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class FBOfferLoader(
    private val marketplaceManagerApi: MarketplaceManagerApi,
    private val regulationEngine: RegulationEngine,
) : OfferLoader {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(FBOfferLoader::class.java)
    }

    override fun load(member: Member): List<Offer> =
        marketplaceManagerApi.searchOffer(
            request = SearchOfferRequest(
                storeId = member.storeId,
                types = listOf(ProductType.PHYSICAL_PRODUCT.name),
                limit = regulationEngine.maxProducts(),
            ),
        ).offers
            .mapNotNull { getOffer(it.product.id) }

    private fun getOffer(id: Long): Offer? =
        try {
            marketplaceManagerApi.getOffer(id).offer
        } catch (ex: Exception) {
            LOGGER.warn("Unable to get offer: $id", ex)
            null
        }
}
