package com.wutsi.application.feed.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.wutsi.checkout.manager.CheckoutManagerApi
import com.wutsi.checkout.manager.CheckoutManagerApiBuilder
import com.wutsi.platform.core.security.feign.FeignApiKeyRequestInterceptor
import com.wutsi.platform.core.security.feign.FeignAuthorizationRequestInterceptor
import com.wutsi.platform.core.tracing.feign.FeignTracingRequestInterceptor
import com.wutsi.platform.core.util.feign.Custom5XXErrorDecoder
import com.wutsi.platform.core.util.feign.FeignAcceptLanguageInterceptor
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import javax.annotation.PostConstruct

@Configuration
class CheckoutManagerApiConfiguration(
    private val authorizationRequestInterceptor: FeignAuthorizationRequestInterceptor,
    private val tracingRequestInterceptor: FeignTracingRequestInterceptor,
    private val apiKeyInterceptor: FeignApiKeyRequestInterceptor,
    private val mapper: ObjectMapper,
    private val env: Environment,
) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(CheckoutManagerApiConfiguration::class.java)
    }

    @PostConstruct
    fun init() {
        val environment = environment()
        LOGGER.info("Endpoint: ${environment.name} = ${environment.url}")
    }

    @Bean
    fun checkoutManagerApi(): CheckoutManagerApi =
        CheckoutManagerApiBuilder().build(
            env = environment(),
            mapper = mapper,
            interceptors = listOf(
                tracingRequestInterceptor,
                authorizationRequestInterceptor,
                apiKeyInterceptor,
                FeignAcceptLanguageInterceptor(),
            ),
            errorDecoder = Custom5XXErrorDecoder(),
        )

    private fun environment(): com.wutsi.checkout.manager.Environment =
        if (env.acceptsProfiles(Profiles.of("prod"))) {
            com.wutsi.checkout.manager.Environment.PRODUCTION
        } else {
            com.wutsi.checkout.manager.Environment.SANDBOX
        }
}
