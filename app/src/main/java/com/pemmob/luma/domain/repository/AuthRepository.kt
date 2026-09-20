package com.pemmob.luma.domain.repository

import com.pemmob.luma.domain.model.UserDomainModel

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<UserDomainModel>
    suspend fun register(email: String, password: String, fullName: String): Result<UserDomainModel>
    suspend fun logout(): Result<Unit>
    suspend fun checkSession(): Boolean
    suspend fun getCurrentUser(): UserDomainModel?
}
