package com.grand.duke.elliot.jjabkaotalk.sign_in

import io.reactivex.Flowable
import retrofit2.http.*

@Suppress("SpellCheckingInspection")
object FirebaseCustomTokenApi {
    interface FirebaseCustomTokenService {
        @GET("kakaoCustomAuth")
        fun getFirebaseKakaoCustomToken(
            @Query("token") token: String
        ): Flowable<FirebaseToken>

        @GET("naverCustomAuth")
        fun getFirebaseNaverCustomToken(
            @Query("token") token: String
        ): Flowable<FirebaseToken>
    }
}

data class FirebaseToken(val firebase_token: String)