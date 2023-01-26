package com.wutsi.application.feed

import com.wutsi.checkout.manager.dto.Business
import com.wutsi.enums.BusinessStatus
import com.wutsi.enums.ProductType
import com.wutsi.enums.StoreStatus
import com.wutsi.marketplace.manager.dto.Event
import com.wutsi.marketplace.manager.dto.FileSummary
import com.wutsi.marketplace.manager.dto.Offer
import com.wutsi.marketplace.manager.dto.OfferPrice
import com.wutsi.marketplace.manager.dto.OfferSummary
import com.wutsi.marketplace.manager.dto.PictureSummary
import com.wutsi.marketplace.manager.dto.Product
import com.wutsi.marketplace.manager.dto.ProductSummary
import com.wutsi.marketplace.manager.dto.Store
import com.wutsi.marketplace.manager.dto.StoreSummary
import com.wutsi.membership.manager.dto.Category
import com.wutsi.membership.manager.dto.CategorySummary
import com.wutsi.membership.manager.dto.Member
import com.wutsi.membership.manager.dto.Place
import com.wutsi.membership.manager.dto.PlaceSummary
import java.time.OffsetDateTime
import java.time.ZoneOffset

object Fixtures {
    fun createStore(id: Long, status: StoreStatus = StoreStatus.ACTIVE) = Store(
        id = id,
        status = status.name,
    )

    fun createMember(
        id: Long = -1,
        phoneNumber: String = "+237670000010",
        displayName: String = "Waxville",
        business: Boolean = false,
        storeId: Long? = null,
        businessId: Long? = null,
        country: String = "CM",
        superUser: Boolean = false,
        active: Boolean = true,
        pictureUrl: String = "https://static6.depositphotos.com/1005993/633/v/450/depositphotos_6338152-stock-illustration-real-estate-logo.jpg",
    ) = Member(
        id = id,
        active = active,
        phoneNumber = phoneNumber,
        business = business,
        storeId = storeId,
        businessId = businessId,
        country = country,
        email = "info@waxville.com",
        displayName = displayName,
        language = "en",
        pictureUrl = pictureUrl,
        superUser = superUser,
        biography = "Notre mission est de valoriser le pagne en l’utilisant pour créer des modèles branchés pour le rendre accessible aux petits et grands budgets",
        city = Place(
            id = 111,
            name = "Yaounde",
            longName = "Yaounde, Cameroun",
        ),
        category = Category(
            id = 555,
            title = "Achat de vente de detail",
        ),
        facebookId = "ray.sponsible",
        twitterId = "ray.sponsible",
        youtubeId = "ray.sponsible",
        instagramId = "ray.sponsible",
        website = "https://www.ray-sponsible.com",
        whatsapp = true,
    )

    fun createPlaceSummary(id: Long = -1, name: String = "Yaounde") = PlaceSummary(
        id = id,
        name = name,
    )

    fun createCategorySummary(id: Long = -1, title: String = "Art") = CategorySummary(
        id = id,
        title = title,
    )

    fun createProductSummary(
        id: Long = -1,
        title: String = "Product",
        thumbnailUrl: String? = null,
        published: Boolean = true,
        price: Long = 15000,
        type: ProductType = ProductType.PHYSICAL_PRODUCT,
        event: Event? = null,
        quantity: Int = 10,
    ) = ProductSummary(
        id = id,
        title = title,
        thumbnailUrl = thumbnailUrl,
        status = if (published) "PUBLISHED" else "DRAFT",
        price = price,
        type = type.name,
        event = event,
        outOfStock = quantity <= 0,
        quantity = quantity,
    )

    fun createFileSummary(
        id: Long,
        name: String = "$id.png",
        url: String = "https://www.img.com/$id.png",
        contentSize: Int = 12000,
        contentType: String = "image/png",
    ) = FileSummary(
        id = id,
        url = url,
        name = name,
        contentType = contentType,
        contentSize = contentSize,
        created = OffsetDateTime.of(2020, 1, 1, 10, 30, 0, 0, ZoneOffset.UTC),
    )

    fun createProduct(
        id: Long = -1,
        storeId: Long = -1,
        accountId: Long = -1,
        title: String = "Product A",
        quantity: Int = 10,
        price: Long = 20000L,
        summary: String = "This is a summary",
        description: String = "This is a long description",
        pictures: List<PictureSummary> = emptyList(),
        published: Boolean = true,
        type: ProductType = ProductType.PHYSICAL_PRODUCT,
        event: Event? = null,
        files: List<FileSummary> = emptyList(),
        url: String = "/p/$id",
    ) = Product(
        id = id,
        store = StoreSummary(
            id = storeId,
            accountId = accountId,
            currency = "XAF",
        ),
        title = title,
        quantity = quantity,
        price = price,
        summary = summary,
        description = description,
        thumbnail = if (pictures.isEmpty()) null else pictures[0],
        pictures = pictures,
        status = if (published) "PUBLISHED" else "DRAFT",
        type = type.name,
        event = event,
        files = files,
        outOfStock = quantity <= 0,
        url = url,
        category = com.wutsi.marketplace.manager.dto.CategorySummary(
            id = 12343,
            title = "Foo",
            longTitle = "Foo > Bar",
        ),
    )

    fun createPictureSummary(
        id: Long = -1,
        url: String = "http://www.google.com/1.png",
    ) = PictureSummary(
        id = id,
        url = url,
    )

    fun createPictureSummaryList(size: Int): List<PictureSummary> {
        val pictures = mutableListOf<PictureSummary>()
        for (i in 0..size) {
            pictures.add(
                PictureSummary(
                    id = i.toLong(),
                    url = "https://img.com/$i.png",
                ),
            )
        }
        return pictures
    }

    fun createBusiness(
        id: Long = 1,
        accountId: Long = 11,
        country: String = "CM",
        currency: String = "XAF",
        status: BusinessStatus = BusinessStatus.ACTIVE,
    ) = Business(
        id = id,
        accountId = accountId,
        country = country,
        currency = currency,
        totalSales = 30000,
        totalOrders = 100,
        status = status.name,
    )

    fun createOfferSummary(
        product: ProductSummary,
        price: OfferPrice = createOfferPrice(productId = -1, price = 2000),
    ) = OfferSummary(
        product = product,
        price = price,
    )

    fun createOffer(
        product: Product,
        price: OfferPrice = createOfferPrice(),
    ) = Offer(
        product = product,
        price = price,
    )

    fun createOfferPrice(
        productId: Long = -1,
        discountId: Long? = null,
        savings: Long = 0,
        price: Long = 1500,
        referencePrice: Long? = null,
        expires: OffsetDateTime? = null,
    ) = OfferPrice(
        productId = productId,
        discountId = discountId,
        savings = savings,
        price = price,
        referencePrice = referencePrice,
        expires = expires,
        savingsPercentage = if (referencePrice != null && referencePrice > 0) (100 * savings / referencePrice).toInt() else 0,
    )
}
