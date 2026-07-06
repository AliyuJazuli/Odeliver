package com.hydr.odeliver.repository

import com.hydr.odeliver.UserDao
import com.hydr.odeliver.UserEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao
) {
    fun getUserById(uid: String): Flow<UserEntity?> = userDao.getUserById(uid)

    suspend fun upsertUser(user: UserEntity) = userDao.upsertUser(user)

    suspend fun clearAllUsers() = userDao.clearAllUsers()
}
