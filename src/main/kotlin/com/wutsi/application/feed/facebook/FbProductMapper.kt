package com.wutsi.application.feed.facebook

import com.wutsi.checkout.manager.dto.Business
import com.wutsi.marketplace.manager.dto.Offer
import com.wutsi.membership.manager.dto.Member
import com.wutsi.regulation.Country
import com.wutsi.regulation.RegulationEngine
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.text.DecimalFormat

@Service
class FbProductMapper(
    private val regulationEngine: RegulationEngine,
    @Value("\${wutsi.application.webapp-url}") private val webappUrl: String,
) {
    fun map(offer: Offer, member: Member, business: Business): FbProduct {
        val country = regulationEngine.country(business.country)
        val price = offer.price.referencePrice?.let { it } ?: offer.price.price
        val salesPrice = offer.price.referencePrice?.let { offer.price.price }

        return FbProduct(
            id = offer.product.id.toString(),
            title = offer.product.title,
            description = if (offer.product.description.isNullOrEmpty()) offer.product.summary else offer.product.description,
            availability = if (offer.product.outOfStock) "out of stock" else "in stock",
            condition = "new",
            link = "$webappUrl${offer.product.url}",
            price = formatMoney(price, country),
            salesPrice = salesPrice?.let { formatMoney(salesPrice, country) },
            brand = member.displayName,
            imageLink = offer.product.thumbnail?.url,
            additionalImageLink = offer.product.pictures
                .filter { it.url != offer.product.thumbnail?.url }
                .map { it.url },
            googleProductCategory = offer.product.category?.id,
        )
    }

    private fun formatMoney(amount: Long, country: Country): String =
        DecimalFormat(country.numberFormat).format(amount) + " ${country.currency}"
}
