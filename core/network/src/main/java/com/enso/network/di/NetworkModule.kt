package com.enso.network.di

import android.content.Context
import com.enso.network.api.LottoApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context) = context

    @Provides
    fun provideApiUrl() = "https://www.dhlottery.co.kr"

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val connectTimeOutSec = (180).toLong()
        val readTimeOutSec = (180).toLong()

        return OkHttpClient.Builder()
            .readTimeout(connectTimeOutSec, TimeUnit.SECONDS)
            .connectTimeout(readTimeOutSec, TimeUnit.SECONDS)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
            .addInterceptor(
                Interceptor { chain ->
                    with(chain) {
                        try {
                            val newRequest = request()
                                .newBuilder()
                                .build()
                            proceed(newRequest)
                        } catch (e: Exception) {
                            proceed(request())
                        }
                    }
                }
            )
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, baseUrl: String): Retrofit {
        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Singleton
    @Provides
    fun providePostsLottoService(retrofit: Retrofit): LottoApi {
        return retrofit.create(LottoApi::class.java)
    }
}