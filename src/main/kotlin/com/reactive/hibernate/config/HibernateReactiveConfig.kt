package com.reactive.hibernate.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "hibernate.reactive")
data class HibernateReactiveConfig(
    val timeZone: String,
    val poolSize: String,
    val showSql: String,
    val formatSql: String,
    val databaseAction: String,
    val driver: String,
    val endpoint: String,
    val name: String,
    val userName: String,
    val password: String
)
