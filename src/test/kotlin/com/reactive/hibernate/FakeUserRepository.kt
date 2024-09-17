package com.reactive.hibernate

import com.reactive.hibernate.repository.BaseRepository
import org.hibernate.reactive.mutiny.Mutiny
import org.springframework.stereotype.Repository

@Repository
class FakeUserRepository(
    sessionFactory: Mutiny.SessionFactory,
) : BaseRepository<FakeUser>(sessionFactory, FakeUser::class.java)
