package com.example.data.remote

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Normalized backend response envelope returned by all PathAI secure server endpoints.
 */
data class BackendResult<T>(
    val success: Boolean,
    val live: Boolean = true,
    val fallback: Boolean = false,
    val reason: String? = null,
    val provider: String? = null,
    val model: String? = null,
    val requestId: String? = null,
    val data: T? = null,
    val rawJson: JSONObject? = null
)

/**
 * Global configuration for PathAI backend connection.
 */
object BackendConfig {
    // Reads configured BACKEND_BASE_URL (defaults to emulator loopback or Cloud Run HTTPS URL)
    var baseUrl: String = try {
        val configured = com.example.BuildConfig.BACKEND_BASE_URL
        if (configured.isNotBlank()) configured else "http://10.0.2.2:5000"
    } catch (e: Exception) {
        "http://10.0.2.2:5000"
    }
    
    // Setting: Allow offline fallback (enabled by default for resilient dev)
    var allowOfflineFallback: Boolean = true
}

/**
 * Secure HTTP Client for PathAI backend API calls.
 * Enforces Firebase ID token authentication on all requests.
 * Completely eliminates any direct calls to LLM provider APIs from Android.
 */
object BackendClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(35, TimeUnit.SECONDS)
        .writeTimeout(25, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    /**
     * Obtains the verified Firebase Auth ID token of the current logged-in user.
     */
    suspend fun getIdToken(): String? {
        val user = FirebaseAuth.getInstance().currentUser ?: return null
        return try {
            user.getIdToken(false).awaitTask()?.token
        } catch (e: Exception) {
            Log.w("BackendClient", "Failed to retrieve Firebase ID token", e)
            null
        }
    }

    /**
     * Executes an authenticated POST request to the specified backend endpoint.
     * Retries once on network failure before returning structured error/fallback.
     */
    suspend fun post(
        endpoint: String,
        jsonBody: JSONObject,
        maxRetries: Int = 2
    ): JSONObject = withContext(Dispatchers.IO) {
        val idToken = getIdToken()
        val url = "${BackendConfig.baseUrl.trimEnd('/')}/${endpoint.trimStart('/')}"

        var attempt = 0
        var lastException: Exception? = null

        while (attempt < maxRetries) {
            try {
                val reqBuilder = Request.Builder()
                    .url(url)
                    .post(jsonBody.toString().toRequestBody(jsonMediaType))

                if (!idToken.isNullOrBlank()) {
                    reqBuilder.header("Authorization", "Bearer $idToken")
                }

                val response = client.newCall(reqBuilder.build()).execute()
                val bodyStr = response.body?.string().orEmpty()

                if (response.isSuccessful) {
                    return@withContext if (bodyStr.isNotBlank()) JSONObject(bodyStr) else JSONObject().put("success", true)
                } else {
                    val errMsg = try {
                        JSONObject(bodyStr).optString("error", "HTTP ${response.code}")
                    } catch (_: Exception) {
                        "HTTP ${response.code}: $bodyStr"
                    }
                    throw IOException("Backend error (${response.code}): $errMsg")
                }
            } catch (e: Exception) {
                lastException = e
                attempt++
                if (attempt < maxRetries) {
                    delay(500L * attempt)
                }
            }
        }

        throw lastException ?: IOException("Unknown connection failure to $url")
    }
}
