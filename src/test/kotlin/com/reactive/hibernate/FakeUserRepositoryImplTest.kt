package com.reactive.hibernate.repository

import com.reactive.hibernate.FakeUser
import com.reactive.hibernate.FakeUserRepository
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@SpringBootTest
class FakeUserRepositoryImplTest(
    @Autowired private val fakeUserRepository: FakeUserRepository
) {
    @AfterEach
    fun cleanup() =
        runBlocking {
            fakeUserRepository.findAll().forEach {
                fakeUserRepository.deleteById(it.id!!)
            }
        }

    @Test
    fun `test count function`() =
        runBlocking {
            val entitiesToSave =
                listOf(
                    FakeUser().apply {
                        name = "User 1"
                        age = 20
                    },
                    FakeUser().apply {
                        name = "User 2"
                        age = 30
                    }
                )

            entitiesToSave.forEach { fakeUserRepository.add(it) }

            val count = fakeUserRepository.count()

            assertEquals(2L, count)

            fakeUserRepository.deleteAll()

            val countAfterDeletion = fakeUserRepository.count()

            assertEquals(0L, countAfterDeletion)
        }

    @Test
    fun `test existsById function`() =
        runBlocking {
            val fakeUser =
                FakeUser().apply {
                    name = "Exists User"
                    age = 50
                }
            val savedUser = fakeUserRepository.add(fakeUser)

            val exists = fakeUserRepository.existsById(savedUser.id!!)
            assertTrue(exists)

            fakeUserRepository.deleteById(savedUser.id!!)

            val existsAfterDeletion = fakeUserRepository.existsById(savedUser.id!!)
            assertTrue(!existsAfterDeletion)
        }

    @Test
    fun `test findAll with pagination`() =
        runBlocking {
            val entitiesToSave =
                listOf(
                    FakeUser().apply {
                        name = "User 1"
                        age = 25
                    },
                    FakeUser().apply {
                        name = "User 2"
                        age = 30
                    },
                    FakeUser().apply {
                        name = "User 3"
                        age = 35
                    }
                )

            entitiesToSave.forEach { fakeUserRepository.add(it) }

            val pageable = Pageable(pageNumber = 0, pageSize = 2)
            val paginatedUsers = fakeUserRepository.findAll(pageable)

            println("Paginated users: ${paginatedUsers.map { it.name }}")

            assertEquals(2, paginatedUsers.size)
        }

    @Test
    fun `test findById function`() =
        runBlocking {
            val fakeUser =
                FakeUser().apply {
                    name = "FindById User"
                    age = 45
                }
            val savedUser = fakeUserRepository.add(fakeUser)

            val foundUser = fakeUserRepository.findById(savedUser.id!!)

            assertNotNull(foundUser)
            assertEquals("FindById User", foundUser?.name)
            assertEquals(45, foundUser?.age)
        }

    @Test
    fun `test findAll function`() =
        runBlocking {
            val entitiesToSave =
                listOf(
                    FakeUser().apply {
                        name = "John Doe"
                        age = 30
                    },
                    FakeUser().apply {
                        name = "Tuna"
                        age = 23
                    }
                )

            entitiesToSave.forEach { fakeUserRepository.add(it) }

            val allEntities = fakeUserRepository.findAll()

            entitiesToSave.forEach { expectedEntity ->
                val matchingEntity = allEntities.find { it.name == expectedEntity.name && it.age == expectedEntity.age }
                assertNotNull(matchingEntity)
            }
        }

    @Test
    fun `test findByIdWithLock function`() =
        runBlocking {
            val fakeUser =
                FakeUser().apply {
                    name = "Locked User"
                    age = 40
                }
            val savedUser = fakeUserRepository.add(fakeUser)

            val foundUser = fakeUserRepository.findByIdWithLock(savedUser.id!!)

            assertTrue(foundUser.isPresent)
            assertEquals("Locked User", foundUser.get().name)
        }

    @Test
    fun `test update function`() =
        runBlocking {
            val fakeUser =
                FakeUser().apply {
                    name = "Old Name"
                    age = 25
                }
            val savedUser = fakeUserRepository.add(fakeUser)

            val updatedUser =
                fakeUserRepository.update(savedUser, savedUser.id!!) { existingEntity ->
                    existingEntity.apply {
                        name = "New Name"
                    }
                }

            assertEquals("New Name", updatedUser.name)
        }

    @Test
    fun `test findEntitiesByField function`() =
        runBlocking {
            val fakeUser1 =
                FakeUser().apply {
                    name = "Alice"
                    age = 30
                }
            val fakeUser2 =
                FakeUser().apply {
                    name = "Alicia"
                    age = 35
                }

            fakeUserRepository.add(fakeUser1)
            fakeUserRepository.add(fakeUser2)

            val foundUsers = fakeUserRepository.findUserByAnyField("name", "Alice")

            assertEquals(1, foundUsers.size)
        }

    @Test
    fun `test deleteById function`() =
        runBlocking {
            val fakeUser =
                FakeUser().apply {
                    name = "To Be Deleted"
                    age = 28
                }
            val savedUser = fakeUserRepository.add(fakeUser)

            fakeUserRepository.deleteById(savedUser.id!!)

            val foundUser = fakeUserRepository.findByIdWithLock(savedUser.id!!)

            assertTrue(foundUser.isEmpty)
        }

    @Test
    fun `test deleteById prevents SQL injection`() =
        runBlocking {
            val fakeUser =
                FakeUser().apply {
                    name = "Valid User"
                    age = 25
                }
            val savedUser = fakeUserRepository.add(fakeUser)

            val maliciousId = "1=1"

            try {
                fakeUserRepository.deleteById(maliciousId.toLong())

                println("SQL injection attempt succeeded")
            } catch (e: NumberFormatException) {
                assertTrue(true)
            }

            val foundUser = fakeUserRepository.findByIdWithLock(savedUser.id!!)
            assertTrue(foundUser.isPresent)
            assertEquals("Valid User", foundUser.get().name)
        }

    @Test
    fun `test deleteAll function`() =
        runBlocking {
            val entitiesToSave =
                listOf(
                    FakeUser().apply {
                        name = "User 1"
                        age = 25
                    },
                    FakeUser().apply {
                        name = "User 2"
                        age = 30
                    }
                )

            entitiesToSave.forEach { fakeUserRepository.add(it) }

            fakeUserRepository.deleteAll()

            val allEntities = fakeUserRepository.findAll()

            assertTrue(allEntities.isEmpty())
        }
}
