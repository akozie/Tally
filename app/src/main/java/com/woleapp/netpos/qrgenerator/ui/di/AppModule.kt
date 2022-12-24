package com.woleapp.netpos.qrgenerator.ui.di

import android.app.Application
import com.google.gson.Gson
import com.woleapp.netpos.qrgenerator.BuildConfig
import com.woleapp.netpos.qrgenerator.ui.network.QRService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    @Named("baseUrl")
    fun providesBaseUrl(): String = BuildConfig.STRING_TALLY_BASE_URL


    @Provides
    @Singleton
    @Named("loggingInterceptor")
    fun providesLoggingInterceptor(): Interceptor = HttpLoggingInterceptor().apply {
        setLevel(HttpLoggingInterceptor.Level.BODY)
    }

    @Provides
    @Singleton
    @Named("headerInterceptor")
    fun providesHeaderInterceptor(): Interceptor =
        BasicAuthInterceptor(BuildConfig.STRING_AUTH_USER_NAME, BuildConfig.STRING_AUTH_PASSWORD)

    @Provides
    @Singleton
    @Named("defaultOkHttp")
    fun providesOKHTTPClient(
        @Named("loggingInterceptor") loggingInterceptor: Interceptor,
        @Named("headerInterceptor") headerInterceptor: Interceptor
    ): OkHttpClient =
        OkHttpClient().newBuilder()
            .connectTimeout(70, TimeUnit.SECONDS)
            .readTimeout(70, TimeUnit.SECONDS)
            .writeTimeout(70, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(headerInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()

    @Provides
    @Singleton
    @Named("defaultRetrofit")
    fun providesRetrofit(
        @Named("defaultOkHttp") okhttp: OkHttpClient,
        @Named("baseUrl") baseUrl: String
    ): Retrofit =
        Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .baseUrl(baseUrl)
            .client(okhttp)
            .build()

    @Provides
    @Singleton
    fun providesContactlessRegService(
        @Named("defaultRetrofit") retrofit: Retrofit
    ): QRService = retrofit.create(QRService::class.java)

    @Provides
    @Singleton
    fun providesGson(): Gson = Gson()
}