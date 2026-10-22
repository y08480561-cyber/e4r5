package com.example.tts.network

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface PiperTtsApiService {
    @GET("translate_tts")
    suspend fun synthesizeSpeech(
        @Query("ie") encoding: String = "UTF-8",
        @Query("q") text: String,
        @Query("tl") lang: String = "fa",
        @Query("client") client: String = "gtx"
    ): Response<ResponseBody>

    companion object {
        private const val BASE_URL = "https://translate.google.com/"

        val instance: PiperTtsApiService by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
                .create(PiperTtsApiService::class.java)
        }
    }
}
