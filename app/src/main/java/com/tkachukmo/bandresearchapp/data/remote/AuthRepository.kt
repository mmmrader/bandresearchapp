package com.tkachukmo.bandresearchapp.data.remote

import com.tkachukmo.bandresearchapp.data.local.SessionStorage
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val supabase: SupabaseClient,
    private val sessionStorage: SessionStorage
) {
    suspend fun signIn(email: String, password: String): Result<Unit> {
        return try {
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            val session = supabase.auth.currentSessionOrNull()
            session?.let { sessionStorage.saveSession(it.accessToken) }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUp(email: String, password: String): Result<Unit> {
        return try {
            supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            val session = supabase.auth.currentSessionOrNull()
            session?.let { sessionStorage.saveSession(it.accessToken) }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            supabase.auth.resetPasswordForEmail(email)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signOut(): Result<Unit> {
        return try {
            supabase.auth.signOut()
            sessionStorage.clearSession()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun isLoggedIn(): Boolean {
        return supabase.auth.currentSessionOrNull() != null
    }

    suspend fun restoreSession(): Boolean {
        return try {
            val savedToken = sessionStorage.getSession()
            if (savedToken != null) {
                supabase.auth.refreshCurrentSession()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            sessionStorage.clearSession()
            false
        }
    }
}