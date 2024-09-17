package com.reactive.hibernate.repository

import io.smallrye.mutiny.coroutines.awaitSuspending
import jakarta.transaction.Transactional
import org.hibernate.LockMode
import org.hibernate.reactive.mutiny.Mutiny.SessionFactory
import org.springframework.stereotype.Repository
import java.util.Optional

data class Pageable(
    val pageNumber: Int,
    val pageSize: Int,
)

@Repository
abstract class BaseRepository<T : Any>(
    protected val sessionFactory: SessionFactory,
    protected val entityClass: Class<T>,
) {
    @Transactional
    suspend fun count(): Long? =
        sessionFactory
            .withTransaction { session, _ ->
                session
                    .createSelectionQuery("select count(e) from ${entityClass.simpleName} e", Long::class.javaObjectType)
                    .singleResult
            }.awaitSuspending()

    @Transactional
    suspend fun add(entity: T): T =
        sessionFactory
            .withTransaction { session, _ ->
                session.persist(entity).replaceWith(entity)
            }.awaitSuspending()

    @Transactional
    suspend fun update(
        entity: T,
        id: Long,
        updateEntity: (T) -> T,
    ): T {
        val existingEntity =
            findByIdWithLock(id).orElseThrow {
                IllegalArgumentException("${entityClass.simpleName} with id $id not found")
            }

        val updatedEntity = updateEntity(existingEntity)

        return sessionFactory
            .withTransaction { session, _ ->
                session.merge(updatedEntity).replaceWith(updatedEntity)
            }.awaitSuspending()
    }

    @Transactional
    suspend fun existsById(id: Long): Boolean =
        sessionFactory
            .withTransaction { session, _ ->
                session.find(entityClass, id)
            }.awaitSuspending()
            .let { it != null }

    suspend fun findByIdWithLock(id: Long): Optional<T> =
        sessionFactory
            .withTransaction { session, _ ->
                session.find(entityClass, id, LockMode.PESSIMISTIC_WRITE)
            }.awaitSuspending()
            .let { Optional.ofNullable(it) }

    @Transactional
    suspend fun findById(id: Long): T? =
        sessionFactory
            .withTransaction { session, _ ->
                session.find(entityClass, id)
            }.awaitSuspending()

    @Transactional
    suspend fun findUserByAnyField(
        fieldName: String,
        value: Any,
    ): List<T> =
        sessionFactory
            .withTransaction { session, _ ->
                session
                    .createSelectionQuery("from ${entityClass.simpleName} e where e.$fieldName = :value", entityClass)
                    .setParameter("value", value)
                    .resultList
            }.awaitSuspending()

    @Transactional
    suspend fun findAll(): List<T> =
        sessionFactory
            .withTransaction { session, _ ->
                session
                    .createSelectionQuery("select e from ${entityClass.simpleName} e", entityClass)
                    .resultList
            }.awaitSuspending()

    @Transactional
    suspend fun findAll(pageable: Pageable): List<T> {
        val pageNumber = pageable.pageNumber
        val pageSize = pageable.pageSize

        return sessionFactory
            .withTransaction { session, _ ->
                session
                    .createSelectionQuery("from ${entityClass.simpleName} e", entityClass)
                    .apply {
                        setFirstResult(pageNumber * pageSize)
                        setMaxResults(pageSize)
                    }.resultList
            }.awaitSuspending()
    }

    @Transactional
    suspend fun deleteById(id: Long) {
        val entityName = entityClass.simpleName ?: throw IllegalArgumentException("Invalid entity class")

        sessionFactory
            .withTransaction { session, _ ->
                val query = session.createMutationQuery("DELETE FROM $entityName e WHERE e.id = :id")
                query.setParameter("id", id)
                query.executeUpdate()
            }.awaitSuspending()
    }

    @Transactional
    suspend fun deleteAll() {
        val entityName = entityClass.simpleName ?: throw IllegalArgumentException("Invalid entity class")

        sessionFactory
            .withTransaction { session, _ ->
                val query = session.createMutationQuery("DELETE FROM $entityName")
                query.executeUpdate()
            }.awaitSuspending()
    }
}
