package com.pemmob.luma.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    private const val SUPABASE_URL = "https://tdqqjuzexzrtnqgvppyj.supabase.co"
    private const val SUPABASE_ANON_KEY = "sb_publishable_cY1huNZIrsCMBhlHzrLLeA_w_G0EaMx"

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_ANON_KEY
        ) {
            install(Auth) {
                autoLoadFromStorage = true
                alwaysAutoRefresh = true
            }
        }
    }

    @Provides
    @Singleton
    fun provideSupabaseAuth(supabaseClient: SupabaseClient): Auth {
        return supabaseClient.auth
    }
}
