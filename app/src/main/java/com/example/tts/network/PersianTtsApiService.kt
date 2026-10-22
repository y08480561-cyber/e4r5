package com.example.tts.network

import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class TtsResponse(
    val code: Int = 0,
    val message: String = "",
    val audio: String? = null
)

interface PersianTtsApiService {
    @GET("tts/convert")
    suspend fun convert(
        @Query("accessKey") accessKey: String = "FREE",
        @Query("language") language: String = "persian",
        @Query("speaker") speaker: String = "speaker1",
        @Query("text") text: String
    ): Response<TtsResponse>

    companion object {
        private const val BASE_URL = "https://textreadtts.com/"

        val instance: PersianTtsApiService by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
                .create(PersianTtsApiService::class.java)
        }
    }
}
