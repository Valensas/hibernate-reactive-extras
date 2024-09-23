package com.reactive.hibernate.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "database")
class DatabaseReactiveConfig(
    val driver: String,
    val endpoint: String,
    val name: String,
    val userName: String,
    val password: String,
)
