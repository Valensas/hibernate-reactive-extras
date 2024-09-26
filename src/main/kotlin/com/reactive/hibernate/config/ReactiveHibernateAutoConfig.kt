package com.reactive.hibernate.config

import jakarta.persistence.Entity
import org.hibernate.reactive.mutiny.Mutiny
import org.hibernate.reactive.provider.ReactiveServiceRegistryBuilder
import org.reflections.Reflections
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.hibernate.cfg.Configuration as HibernateConfig

@Configuration
@EnableConfigurationProperties(HibernateReactiveConfig::class)
class ReactiveHibernateAutoConfig(
    private val hibernateReactiveConfig: HibernateReactiveConfig,
    private val applicationContext: ApplicationContext
) {
    @Bean
    fun sessionFactory(): Mutiny.SessionFactory {
        val configuration = HibernateConfig()

        configuration.setProperty(
            "hibernate.connection.url",
            "jdbc:${hibernateReactiveConfig.driver}:@${hibernateReactiveConfig.endpoint}/${hibernateReactiveConfig.name}"
        )
        configuration.setProperty("hibernate.connection.username", hibernateReactiveConfig.userName)
        configuration.setProperty("hibernate.connection.password", hibernateReactiveConfig.password)
        configuration.setProperty("hibernate.jdbc.time_zone", hibernateReactiveConfig.timeZone)
        configuration.setProperty("hibernate.connection.pool_size", hibernateReactiveConfig.poolSize)
        configuration.setProperty("hibernate.show_sql", hibernateReactiveConfig.showSql)
        configuration.setProperty("hibernate.format_sql", hibernateReactiveConfig.formatSql)
        configuration.setProperty("jakarta.persistence.schema-generation.database.action", hibernateReactiveConfig.databaseAction)
        val entityClasses = findEntityAnnotatedClasses()
        entityClasses.forEach { entityClass ->
            configuration.addAnnotatedClass(entityClass)
        }

        val serviceRegistry = ReactiveServiceRegistryBuilder()
            .applySettings(configuration.properties)
            .build()

        return configuration.buildSessionFactory(serviceRegistry).unwrap(Mutiny.SessionFactory::class.java)
    }

    fun findEntityAnnotatedClasses(): Set<Class<*>> {
        val basePackages = applicationContext
            .getBeansWithAnnotation(
                SpringBootApplication::class.java
            ).values
            .map { it::class.java.packageName }

        val reflections = Reflections(basePackages)

        return reflections.getTypesAnnotatedWith(Entity::class.java)
    }
}
