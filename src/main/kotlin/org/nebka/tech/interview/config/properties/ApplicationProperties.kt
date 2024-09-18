package org.nebka.tech.interview.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "application")
data class ApplicationProperties(
    var poolSize: Int = 400,
    var awaitTime: Int = 30,
    var poolName: String = "pool"
)