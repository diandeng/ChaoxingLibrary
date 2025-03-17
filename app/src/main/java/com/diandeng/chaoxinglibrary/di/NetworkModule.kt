// di/NetworkModule.kt
package com.diandeng.chaoxinglibrary.di

import com.diandeng.chaoxinglibrary.data.network.OfficeApiService
import com.diandeng.chaoxinglibrary.data.network.Passport2ApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class Passport2Retrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class OfficeRetrofit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    @Passport2Retrofit
    fun providePassport2Retrofit() : Retrofit{
        return Retrofit.Builder()
            .baseUrl("https://passport2.chaoxing.com/")
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @OfficeRetrofit
    fun provideOfficeRetrofit() : Retrofit{
        return Retrofit.Builder()
            .baseUrl("https://office.chaoxing.com/")
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @Passport2Retrofit
    fun providePassport2ApiService(@Passport2Retrofit retrofit: Retrofit): Passport2ApiService {
        return retrofit.create(Passport2ApiService::class.java)
    }

    @Provides
    @Singleton
    @OfficeRetrofit
    fun provideOfficeApiService(@OfficeRetrofit retrofit: Retrofit): OfficeApiService {
        return retrofit.create(OfficeApiService::class.java)
    }
}