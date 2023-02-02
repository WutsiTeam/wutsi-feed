package com.wutsi.application.feed.pinterest

import com.wutsi.application.feed.service.OfferLoader
import com.wutsi.marketplace.manager.MarketplaceManagerApi
import com.wutsi.marketplace.manager.dto.Offer
import com.wutsi.marketplace.manager.dto.SearchOfferRequest
import com.wutsi.membership.manager.dto.Member
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class POfferLoader(
    private val marketplaceManagerApi: MarketplaceManagerApi
) : OfferLoader {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(POfferLoader::class.java)
        private const val LIMIT = 1000
    }

    override fun load(member: Member): List<Offer> {
        var offset = 0
        val result = mutableListOf<Offer>()
        while (offset < 10) {
            val offers = marketplaceManagerApi.searchOffer(
                request = SearchOfferRequest(
                    limit = LIMIT,
                    offset = offset++
                ),
            ).offers
                .mapNotNull { getOffer(it.product.id) }
            result.addAll(offers)

            if (offers.size < LIMIT) {
                break
            }
        }
        return result
    }

    private fun getOffer(id: Long): Offer? =
        try {
            marketplaceManagerApi.getOffer(id).offer
        } catch (ex: Exception) {
            LOGGER.warn("Unable to get offer: $id", ex)
            null
        }
}
