package com.wutsi.application.feed.service

import com.wutsi.marketplace.manager.MarketplaceManagerApi
import com.wutsi.marketplace.manager.dto.Offer
import com.wutsi.marketplace.manager.dto.SearchOfferRequest
import com.wutsi.membership.manager.dto.Member
import com.wutsi.regulation.RegulationEngine
import org.slf4j.LoggerFactory

abstract class AbstractOfferLoader(
    private val marketplaceManagerApi: MarketplaceManagerApi,
    private val regulationEngine: RegulationEngine,
) : OfferLoader {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(AbstractOfferLoader::class.java)
        const val LIMIT = 1000
        const val MAX_OFFSET = 10
    }

    override fun load(member: Member): List<Offer> =
        member.storeId?.let {
            marketplaceManagerApi.searchOffer(
                request = createSearchRequest(it, regulationEngine.maxProducts(), 0),
            ).offers
                .mapNotNull { getOffer(it.product.id) }
        } ?: emptyList()

    override fun load(): List<Offer> {
        var offset = 0
        val result = mutableListOf<Offer>()
        while (offset < MAX_OFFSET) {
            val offers = marketplaceManagerApi.searchOffer(
                request = createSearchRequest(null, LIMIT, offset++),
            ).offers
                .mapNotNull { getOffer(it.product.id) }
            result.addAll(offers)

            if (offers.size < LIMIT) {
                break
            }
        }
        return result
    }

    protected open fun createSearchRequest(storeId: Long?, limit: Int, offset: Int) = SearchOfferRequest(
        storeId = storeId,
        limit = limit,
        offset = offset,
    )

    private fun getOffer(id: Long): Offer? =
        try {
            marketplaceManagerApi.getOffer(id).offer
        } catch (ex: Exception) {
            LOGGER.warn("Unable to get offer: $id", ex)
            null
        }
}
