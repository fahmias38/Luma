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
        return runCatching {
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
        return runCatching {
            supabaseClient.auth.signUpWith(Email) {
                this.email = email
                this.password = password
                data = buildJsonObject {
                    put("full_name", fullName)
                }
            }
            val user = supabaseClient.auth.currentUserOrNull()
                ?: throw IllegalStateException("Pendaftaran berhasil. Silakan periksa email Anda jika konfirmasi email diaktifkan.")

            UserDomainModel(
                id = user.id,
                email = user.email ?: email,
                fullName = fullName
            )
        }
    }

    override suspend fun logout(): Result<Unit> {
        return runCatching {
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
}
