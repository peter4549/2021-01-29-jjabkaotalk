package com.grand.duke.elliot.jjabkaotalk.module

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.module.Module
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

fun getNetWorkModule(url: String): Module = module {
    single {
        OkHttpClient.Builder().addInterceptor(get() as HttpLoggingInterceptor).build()
    }

    single {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    single {
        Retrofit.Builder()
            .client(get())
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .baseUrl(url)
            .build()
    }
}