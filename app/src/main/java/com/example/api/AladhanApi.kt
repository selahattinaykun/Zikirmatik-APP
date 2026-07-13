package com.example.api

import com.squareup.moshi.JsonClass
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class PrayerResponse(val data: PrayerData)

@JsonClass(generateAdapter = true)
data class PrayerData(val timings: Timings)

@JsonClass(generateAdapter = true)
data class Timings(
    val Fajr: String,
    val Dhuhr: String,
    val Asr: String,
    val Maghrib: String,
    val Isha: String
)

interface AladhanApi {
    @GET("timings")
    suspend fun getTimings(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("method") method: Int = 13 // Diyanet
    ): PrayerResponse
}

object RetrofitInstance {
    val api: AladhanApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.aladhan.com/v1/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(AladhanApi::class.java)
    }
}
