package com.wutsi.application.feed.service

import com.wutsi.application.feed.model.ProductModel
import com.wutsi.marketplace.manager.dto.Offer
import com.wutsi.membership.manager.dto.Member
import com.wutsi.membership.manager.dto.MemberSummary
import com.wutsi.regulation.Country
import com.wutsi.regulation.RegulationEngine
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.text.DecimalFormat

@Service
class Mapper(
    private val regulationEngine: RegulationEngine,
    @Value("\${wutsi.application.webapp-url}") private val webappUrl: String,
) {
    fun map(offer: Offer, member: Member): ProductModel {
        val country = regulationEngine.country(member.country)
        return map(offer, member.displayName, country)
    }

    fun map(offer: Offer, member: MemberSummary): ProductModel {
        val country = regulationEngine.country(member.country)
        return map(offer, member.displayName, country)
    }

    fun map(offer: Offer, brand: String, country: Country): ProductModel {
        val price = offer.price.referencePrice?.let { it } ?: offer.price.price
        val salesPrice = offer.price.referencePrice?.let { offer.price.price }

        return ProductModel(
            id = offer.product.id.toString(),
            title = offer.product.title,
            description = if (offer.product.description.isNullOrEmpty()) offer.product.summary else offer.product.description,
            availability = if (offer.product.outOfStock) "out of stock" else "in stock",
            condition = "new",
            link = "$webappUrl${offer.product.url}",
            price = formatMoney(price, country),
            salePrice = salesPrice?.let { formatMoney(salesPrice, country) },
            brand = brand,
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
