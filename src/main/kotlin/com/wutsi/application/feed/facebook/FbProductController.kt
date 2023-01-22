package com.wutsi.application.feed.facebook

import com.wutsi.checkout.manager.CheckoutManagerApi
import com.wutsi.checkout.manager.dto.Business
import com.wutsi.enums.BusinessStatus
import com.wutsi.marketplace.manager.MarketplaceManagerApi
import com.wutsi.marketplace.manager.dto.Offer
import com.wutsi.marketplace.manager.dto.SearchOfferRequest
import com.wutsi.membership.manager.MembershipManagerApi
import com.wutsi.membership.manager.dto.Member
import com.wutsi.platform.core.logging.KVLogger
import com.wutsi.platform.core.storage.StorageService
import com.wutsi.regulation.RegulationEngine
import org.slf4j.LoggerFactory
import org.springframework.util.FileCopyUtils
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("/facebook")
class FbProductController(
    private val membershipManagerApi: MembershipManagerApi,
    private val marketplaceManagerApi: MarketplaceManagerApi,
    private val checkoutManagerApi: CheckoutManagerApi,
    private val regulationEngine: RegulationEngine,
    private val mapper: FbProductMapper,
    private val writer: FbProductWriter,
    private val response: HttpServletResponse,
    private val logger: KVLogger,
    private val storage: StorageService,
) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(FbProductController::class.java)
    }

    @GetMapping("/{id}/product.csv", produces = ["application/csv"])
    fun product(@PathVariable id: Long) {
        // Get member
        val member = membershipManagerApi.getMember(id).member
        logger.add("business_id", member.businessId)
        logger.add("store_id", member.storeId)
        if (!member.business || member.businessId == null || member.storeId == null) {
            logger.add("business", false)
            return nothing()
        }

        // Get business
        val business = checkoutManagerApi.getBusiness(member.businessId!!).business
        logger.add("business_status", business.status)
        if (business.status != BusinessStatus.ACTIVE.name) {
            return nothing()
        }

        // Get Store
        val store = marketplaceManagerApi.getStore(member.storeId!!).store
        logger.add("store_status", store.status)
        if (store.status != BusinessStatus.ACTIVE.name) {
            return nothing()
        }

        // Generate
        generate(member, business)
    }

    private fun generate(member: Member, business: Business) {
        val now = LocalDate.now()
        val path = getPath(member.id, now)
        try {
            storage.get(storage.toURL(path), response.outputStream)
        } catch (ex: IOException) {
            generate(member, business, path)
        }
    }

    private fun generate(member: Member, business: Business, path: String) {
        val offers = getOffers(member).map { mapper.map(it, member, business) }
        val file = Files.createTempFile("facebook-product", "csv").toFile()
        logger.add("file", file)
        try {
            // Store locally
            val fout = FileOutputStream(file)
            fout.use {
                writer.write(offers, fout)
            }

            // Store in the cloud
            val fin = FileInputStream(file)
            fin.use {
                val url = storage.store(path, fin, "application/csv")
                logger.add("url", url)
            }

            // Write to response
            val xfin = FileInputStream(file)
            xfin.use {
                FileCopyUtils.copy(xfin, response.outputStream)
            }
        } finally {
            file.delete()
        }
    }

    private fun getOffers(member: Member): List<Offer> =
        marketplaceManagerApi.searchOffer(
            request = SearchOfferRequest(
                storeId = member.storeId,
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

    private fun nothing() {
        writer.write(emptyList(), response.outputStream)
    }

    private fun getPath(id: Long, date: LocalDate): String =
        "feed/" + date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd")) + "/facebook/$id/product.csv"
}
