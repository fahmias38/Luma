package com.pemmob.luma.data.repository

import com.pemmob.luma.domain.model.UserDomainModel
import com.pemmob.luma.domain.repository.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<UserDomainModel> {
        return safeApiCall {
            supabaseClient.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            val user = supabaseClient.auth.currentUserOrNull()
                ?: throw IllegalStateException("Pengguna tidak ditemukan setelah login berhasil.")

            val fullName = user.userMetadata?.get("full_name")?.toString()?.replace("\"", "")
            UserDomainModel(
                id = user.id,
                email = user.email ?: email,
                fullName = fullName
            )
        }
    }

    override suspend fun register(
        email: String,
        password: String,
        fullName: String
    ): Result<UserDomainModel> {
        return safeApiCall {
            // 1. Pendaftaran Akun Baru ke Supabase Database
            supabaseClient.auth.signUpWith(Email) {
                this.email = email
                this.password = password
                data = buildJsonObject {
                    put("full_name", fullName)
                }
            }

            val user = supabaseClient.auth.currentUserOrNull()

            // 2. Sign Out agar tidak langsung masuk, mewajibkan login manual
            runCatching { supabaseClient.auth.signOut() }

            val metaName = user?.userMetadata?.get("full_name")?.toString()?.replace("\"", "")
            UserDomainModel(
                id = user?.id ?: "",
                email = user?.email ?: email,
                fullName = metaName ?: fullName
            )
        }
    }

    override suspend fun logout(): Result<Unit> {
        return safeApiCall {
            supabaseClient.auth.signOut()
        }
    }

    override suspend fun checkSession(): Boolean {
        return try {
            val session = supabaseClient.auth.currentSessionOrNull()
            session != null
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getCurrentUser(): UserDomainModel? {
        return try {
            val user = supabaseClient.auth.currentUserOrNull() ?: return null
            val fullName = user.userMetadata?.get("full_name")?.toString()?.replace("\"", "")
            UserDomainModel(
                id = user.id,
                email = user.email ?: "",
                fullName = fullName
            )
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun updateFullName(fullName: String): Result<UserDomainModel> {
        return safeApiCall {
            supabaseClient.auth.updateUser {
                data = buildJsonObject {
                    put("full_name", fullName)
                }
            }
            val user = supabaseClient.auth.currentUserOrNull()
                ?: throw IllegalStateException("Pengguna tidak ditemukan.")
            UserDomainModel(
                id = user.id,
                email = user.email ?: "",
                fullName = fullName
            )
        }
    }

    override suspend fun updateEmail(newEmail: String): Result<UserDomainModel> {
        return safeApiCall {
            supabaseClient.auth.updateUser {
                email = newEmail
            }
            val user = supabaseClient.auth.currentUserOrNull()
                ?: throw IllegalStateException("Pengguna tidak ditemukan.")
            val fullName = user.userMetadata?.get("full_name")?.toString()?.replace("\"", "")
            UserDomainModel(
                id = user.id,
                email = user.email ?: newEmail,
                fullName = fullName
            )
        }
    }

    override suspend fun updatePassword(newPassword: String): Result<Unit> {
        return safeApiCall {
            supabaseClient.auth.updateUser {
                password = newPassword
            }
            Unit
        }
    }

    private inline fun <T> safeApiCall(block: () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (e: Throwable) {
            Result.failure(mapException(e))
        }
    }

    private fun mapException(e: Throwable): Throwable {
        val msg = e.message ?: ""
        return if (e is java.net.UnknownHostException || msg.contains("Unable to resolve host", ignoreCase = true) || msg.contains("UnknownHostException", ignoreCase = true)) {
            Exception("Tidak dapat terhubung ke server Supabase. Mohon periksa kembali SUPABASE_URL di SupabaseModule.kt dan pastikan koneksi internet aktif.")
        } else {
            e
        }
    }
}
