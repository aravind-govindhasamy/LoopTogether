package com.example.data

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiPart(
    @Json(name = "text") val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    @Json(name = "parts") val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @Json(name = "contents") val contents: List<GeminiContent>,
    @Json(name = "systemInstruction") val systemInstruction: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    @Json(name = "content") val content: GeminiContent?
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<GeminiCandidate>?
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val apiService: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    /**
     * Call the server-side Gemini 3.5 Flash model with standard text instructions.
     * Incorporates protective mock fallbacks if network offline, or api key not active.
     */
    suspend fun generateAiContent(prompt: String, systemPrompt: String? = null): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "Gemini API Key is placeholder. Triggering intelligent local helper fallback.")
            return getSimulatedAiResponse(prompt)
        }

        val request = GeminiRequest(
            contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)))),
            systemInstruction = systemPrompt?.let {
                GeminiContent(parts = listOf(GeminiPart(text = it)))
            }
        )

        return try {
            val response = apiService.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "I'm grooving to this beat too! What should we play next?"
        } catch (e: Exception) {
            Log.e(TAG, "Gemini API call error: ${e.localizedMessage}. Triggering local database fallback.")
            getSimulatedAiResponse(prompt)
        }
    }

    /**
     * Dynamic local simulated response matching the musical theme when the API key is not active.
     */
    private fun getSimulatedAiResponse(prompt: String): String {
        val lowercasePrompt = prompt.lowercase()
        return when {
            lowercasePrompt.contains("recommend") || lowercasePrompt.contains("playlist") || lowercasePrompt.contains("suggest") -> {
                listOf(
                    "🎵 Here are some recommended tracks for your mood:\n- 'Blinding Lights' by The Weeknd\n- 'Starboy' by The Weeknd ft. Daft Punk\n- 'Levitating' by Dua Lipa\n- 'Nightcall' by Kavinsky (Synthwave vibe)",
                    "🔥 Add these hits to your collaborative queue:\n- 'Stay' by Kid LAROI & Justin Bieber\n- 'Take On Me' by a-ha\n- 'As It Was' by Harry Styles\nWant me to queue one of these?",
                    "💿 Vibe recommendations:\n- 'Intro' by The xx\n- 'Strobe' by deadmau5\n- 'Midnight City' by M83\nPerfect ambient tunes for a chill listening party!"
                ).random()
            }
            lowercasePrompt.contains("hello") || lowercasePrompt.contains("hey") || lowercasePrompt.contains("hi") -> {
                "👋 Hey Loopers! I am LoopDJ, your Gemini-powered music assistant. Request a genre, song suggestion, or ask me about music history!"
            }
            lowercasePrompt.contains("skip") || lowercasePrompt.contains("vote") -> {
                "🗳️ Did you know? You can vote to skip tracks in the queue panel. If more than 50% of active members agree, we immediately skip to the next track!"
            }
            lowercasePrompt.contains("queue") -> {
                "📝 You can search for any song from the Search section and tap '+' to append it to our collaborative room queue!"
            }
            else -> {
                listOf(
                    "🎵 Keep the beat rolling! LoopTogether is all about synchronization. Tap play/pause anytime to coordinate the room.",
                    "⚡ Fun music fact: Listening to music releases dopamine which enhances spatial and collaboration performance in synchronized rooms!",
                    "🎸 This room's energy level is off the charts. What track are you feeling right now?",
                    "🔊 Pro-Tip: Transfer room ownership or lock playback from room controls if you are the host!"
                ).random()
            }
        }
    }
}
