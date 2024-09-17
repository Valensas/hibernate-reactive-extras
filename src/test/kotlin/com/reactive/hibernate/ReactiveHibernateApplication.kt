package com.reactive.hibernate

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ReactiveHibernateApplication

fun main(args: Array<String>) {
    runApplication<ReactiveHibernateApplication>(*args)
}
