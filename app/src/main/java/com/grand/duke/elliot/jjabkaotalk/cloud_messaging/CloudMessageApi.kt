package com.grand.duke.elliot.jjabkaotalk.cloud_messaging

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.okhttp.ResponseBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.HeaderMap
import retrofit2.http.POST

object CloudMessageApi {

    private fun createRetrofit(): Retrofit {
        val builder = OkHttpClient.Builder()
        builder.addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })

        return Retrofit.Builder()
            .baseUrl("https://fcm.googleapis.com/fcm/")
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .addConverterFactory(GsonConverterFactory.create())
            .client(builder.build())
            .build()
    }

    interface CloudMessagingService {
        @POST("send")
        fun request(
            @HeaderMap headers: Map<String, String> = mapOf(
                "Authorization" to "key=$SERVER_KEY",
                "project_id" to SENDER_ID
            ),
            @Body requestBody: CloudMessage
        ): Call<ResponseBody>
    }

    fun getCloudMessagingService(): CloudMessagingService =
        createRetrofit().create(CloudMessagingService::class.java)
}