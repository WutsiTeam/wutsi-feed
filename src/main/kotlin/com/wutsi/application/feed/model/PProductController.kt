package com.wutsi.application.feed.model

import com.wutsi.application.feed.facebook.FbProductWriter
import com.wutsi.application.feed.pinterest.POfferLoader
import com.wutsi.application.feed.service.Mapper
import com.wutsi.membership.manager.MembershipManagerApi
import com.wutsi.membership.manager.dto.Member
import com.wutsi.platform.core.logging.KVLogger
import com.wutsi.platform.core.storage.StorageService
import org.springframework.util.FileCopyUtils
import org.springframework.web.bind.annotation.GetMapping
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
@RequestMapping("/pinterest")
class PProductController(
    private val offerLoader: POfferLoader,
    private val membershipManagerApi: MembershipManagerApi,
    private val mapper: Mapper,
    private val writer: FbProductWriter,
    private val response: HttpServletResponse,
    private val logger: KVLogger,
    private val storage: StorageService,
) {
    @GetMapping("/product.csv", produces = ["application/csv"])
    fun product() {
        val now = LocalDate.now()
        val path = getPath(now)
        try {
            storage.get(storage.toURL(path), response.outputStream)
        } catch (ex: IOException) {
            generate(path)
        }
    }

    private fun generate(path: String) {
        val members = mutableMapOf<Long, Member>()
        val offers = offerLoader.load().map {
            val member = getMember(it.product.store.accountId, members)
            mapper.map(it, member)
        }

        val file = Files.createTempFile("pinterest-product", "csv").toFile()
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

    private fun getMember(id: Long, members: MutableMap<Long, Member>): Member {
        var member = members[id]
        if (member == null) {
            val tmp = membershipManagerApi.getMember(id).member
            members[id] = tmp
            return tmp
        } else {
            return member
        }
    }

    private fun getPath(date: LocalDate): String =
        "feed/" + date.format(DateTimeFormatter.ofPattern("yyyy/MM/dd")) + "/pinterest/product.csv"
}
