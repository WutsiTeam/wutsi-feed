package com.wutsi.application.feed.pinterest

import com.wutsi.application.feed.service.AbstractOfferLoader
import com.wutsi.marketplace.manager.MarketplaceManagerApi
import com.wutsi.regulation.RegulationEngine
import org.springframework.stereotype.Service

@Service
class POfferLoader(
    marketplaceManagerApi: MarketplaceManagerApi,
    regulationEngine: RegulationEngine,
) : AbstractOfferLoader(marketplaceManagerApi, regulationEngine)
