package com.wutsi.application.feed

import com.wutsi.platform.core.WutsiApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@WutsiApplication
@SpringBootApplication
class Application

fun main(vararg args: String) {
    org.springframework.boot.runApplication<Application>(*args)
}
