package com.wutsi.application.feed.service

import com.wutsi.marketplace.manager.dto.Offer
import com.wutsi.membership.manager.dto.Member

interface OfferLoader {
    fun load(): List<Offer>
    fun load(merchant: Member): List<Offer>
}
