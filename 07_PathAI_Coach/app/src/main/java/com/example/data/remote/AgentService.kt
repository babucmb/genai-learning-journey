package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.AgentChatMessage
import com.example.data.model.TechRadarItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class AgentService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    suspend fun chatWithAgent(
        agentId: String,
        userQuery: String,
        candidateContext: String,
        userId: String = "",
        focusAreas: List<String> = listOf("Python", "AI/ML"),
        currentPath: String = "AIML",
        currentModuleInfo: String = ""
    ): String = withContext(Dispatchers.IO) {
        try {
            val req = JSONObject().apply {
                put("agentType", agentId)
                put("message", userQuery)
                if (currentModuleInfo.isNotBlank()) {
                    put("currentModuleInfo", JSONObject().apply {
                        put("title", currentModuleInfo)
                        put("skills", JSONArray(focusAreas))
                    })
                }
            }
            val resp = BackendClient.post("api/agents/chat", req)
            val reply = resp.optString("reply")
            val isLive = resp.optBoolean("live", true)
            if (reply.isNotBlank()) {
                return@withContext if (!isLive) {
                    "⚠️ [Offline / Template Content]\n\n$reply"
                } else {
                    reply
                }
            }
        } catch (e: Exception) {
            Log.w("AgentService", "Backend agent chat error: ${e.message}")
        }

        if (!BackendConfig.allowOfflineFallback) {
            return@withContext "Agent is currently offline. Please check your backend connection."
        }

        // Fallback realistic personalized responses when offline
        val focusTag = if (focusAreas.isNotEmpty()) focusAreas.first() else "AI/ML"
        val fallbackText = when (agentId) {
            "coach" -> """
                📋 **Coach Agent Recommendation (Path: $currentPath):**
                I analyzed your schedule against your $currentPath goal focusing on ${focusAreas.joinToString(", ")}:
                - **Focus Today**: Allocate 90 mins to $focusTag mechanics, 60 mins to hands-on implementation, and 30 mins to reviewing core algorithms.
                - **Adaptive Note**: If time is tight, prioritize your $focusTag deliverable—it covers your highest-priority gap.
                - **Pacing**: Regular consistency builds mastery. Completing today's atomic tasks keeps you on target for $currentPath.
            """.trimIndent()
            "tutor" -> """
                🧠 **Tutor Agent Explanation ($focusTag Focus):**
                In Scaled Dot-Product Attention:
                Attention(Q, K, V) = softmax( (Q * K^T) / sqrt(d_k) ) * V

                **Why divide by sqrt(d_k)?**
                For large projection dimensions d_k, the dot products grow large in magnitude, pushing the softmax function into regions where it has extremely small gradients (vanishing gradient problem). Dividing by sqrt(d_k) stabilizes the variance to 1.0, ensuring healthy backpropagation flow.

                **Quick Check**: If d_k = 64, what is the scaling factor? (Answer: sqrt(64) = 8).
            """.trimIndent()
            "project" -> """
                🛠️ **Project Agent Architecture Review ($currentPath):**
                To make your ${focusAreas.joinToString(" & ")} project stand out to hiring leads:
                1. **Hybrid Retrieval**: Combine dense semantic search (ChromaDB / Milvus) with lexical BM25 using Reciprocal Rank Fusion (RRF).
                2. **Latency Guardrail**: Add caching via Redis for semantic queries and stream responses using FastAPI's `StreamingResponse`.
                3. **Evaluation Metric**: Include automated RAGAS metrics (Faithfulness, Answer Relevance, Context Recall) in your GitHub README.
                4. **Deliverable**: Containerize the pipeline with a multi-stage Dockerfile and test with Prometheus latency metrics.
            """.trimIndent()
            "research" -> """
                📡 **Research Agent Tech Brief (${focusAreas.joinToString(", ")}):**
                **1. Test-Time Compute & Reasoning**:
                Models like DeepSeek-R1 and OpenAI o1 prove that allocating extra tokens during inference via chain-of-thought verification achieves massive gains in reasoning without exponential parameter scaling.

                **2. Practical Takeaway for Your Preparation**:
                Companies hiring for $currentPath are shifting interview questions from naive prompt templates to structured verification loops, evaluation harnesses, and inference engine optimizations (vLLM / TensorRT-LLM).
            """.trimIndent()
            else -> "Agent ready to assist with your personalized $currentPath learning path."
        }

        "⚠️ [Offline / Template Content]\n\n$fallbackText"
    }

    // --- Firestore Per-User Storage Helpers ---

    suspend fun saveChatHistory(uid: String, agentType: String, messages: List<AgentChatMessage>) = withContext(Dispatchers.IO) {
        if (uid.isBlank()) return@withContext
        try {
            val docRef = firestore.collection("chat_history").document("${uid}_${agentType}")
            val msgMaps = messages.takeLast(30).map { msg ->
                mapOf(
                    "id" to msg.id,
                    "agentId" to msg.agentId,
                    "sender" to msg.sender,
                    "message" to msg.message,
                    "timestamp" to msg.timestamp
                )
            }
            val data = mapOf(
                "uid" to uid,
                "agentType" to agentType,
                "messages" to msgMaps,
                "updatedAt" to System.currentTimeMillis()
            )
            docRef.set(data, SetOptions.merge()).awaitTask()
        } catch (e: Exception) {
            Log.w("AgentService", "Failed saving chat history to Firestore", e)
        }
    }

    suspend fun loadChatHistory(uid: String, agentType: String): List<AgentChatMessage> = withContext(Dispatchers.IO) {
        if (uid.isBlank()) return@withContext emptyList()
        try {
            val docRef = firestore.collection("chat_history").document("${uid}_${agentType}")
            val snapshot = docRef.get().awaitTask()
            if (snapshot.exists()) {
                val rawList = snapshot.get("messages") as? List<*> ?: return@withContext emptyList()
                return@withContext rawList.mapNotNull { item ->
                    val map = item as? Map<*, *> ?: return@mapNotNull null
                    AgentChatMessage(
                        id = map["id"]?.toString() ?: System.currentTimeMillis().toString(),
                        agentId = map["agentId"]?.toString() ?: agentType,
                        sender = map["sender"]?.toString() ?: "AGENT",
                        message = map["message"]?.toString() ?: "",
                        timestamp = (map["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()
                    )
                }
            }
        } catch (e: Exception) {
            Log.w("AgentService", "Failed loading chat history from Firestore", e)
        }
        emptyList()
    }

    suspend fun savePlanForUser(
        uid: String,
        currentPath: String,
        focusAreas: List<String>,
        summary: String
    ) = withContext(Dispatchers.IO) {
        if (uid.isBlank()) return@withContext
        try {
            val docRef = firestore.collection("plans").document("${uid}_${currentPath}")
            val data = mapOf(
                "uid" to uid,
                "currentPath" to currentPath,
                "focusAreas" to focusAreas,
                "summary" to summary,
                "updatedAt" to System.currentTimeMillis()
            )
            docRef.set(data, SetOptions.merge()).awaitTask()
        } catch (e: Exception) {
            Log.w("AgentService", "Failed saving plan to Firestore", e)
        }
    }

    suspend fun saveTaskForUser(
        uid: String,
        taskId: String,
        title: String,
        category: String,
        isCompleted: Boolean
    ) = withContext(Dispatchers.IO) {
        if (uid.isBlank()) return@withContext
        try {
            val docRef = firestore.collection("tasks").document("${uid}_${taskId}")
            val data = mapOf(
                "uid" to uid,
                "taskId" to taskId,
                "title" to title,
                "category" to category,
                "isCompleted" to isCompleted,
                "updatedAt" to System.currentTimeMillis()
            )
            docRef.set(data, SetOptions.merge()).awaitTask()
        } catch (e: Exception) {
            Log.w("AgentService", "Failed saving task to Firestore", e)
        }
    }

    suspend fun saveRadarFeedback(userId: String, radarId: String, liked: Boolean) = withContext(Dispatchers.IO) {
        if (userId.isBlank()) return@withContext
        try {
            val docRef = firestore.collection("radar_feedback").document("${userId}_${radarId}")
            val data = mapOf(
                "userId" to userId,
                "radarId" to radarId,
                "liked" to liked,
                "timestamp" to System.currentTimeMillis()
            )
            docRef.set(data, SetOptions.merge()).awaitTask()
        } catch (e: Exception) {
            Log.w("AgentService", "Failed saving radar feedback to Firestore", e)
        }
    }

    suspend fun loadRadarFeedback(userId: String): Map<String, Boolean> = withContext(Dispatchers.IO) {
        if (userId.isBlank()) return@withContext emptyMap()
        try {
            val querySnapshot = firestore.collection("radar_feedback")
                .whereEqualTo("userId", userId)
                .get()
                .awaitTask()
            val map = mutableMapOf<String, Boolean>()
            for (doc in querySnapshot.documents) {
                val radarId = doc.getString("radarId")
                val liked = doc.getBoolean("liked") ?: false
                if (!radarId.isNullOrBlank()) {
                    map[radarId] = liked
                }
            }
            map
        } catch (e: Exception) {
            Log.w("AgentService", "Failed loading radar feedback from Firestore", e)
            emptyMap()
        }
    }

    suspend fun fetchLatestTechRadar(
        focusAreas: List<String> = listOf("Python", "AI/ML")
    ): List<TechRadarItem> = withContext(Dispatchers.IO) {
        try {
            val req = JSONObject().apply {
                put("focusAreas", JSONArray(focusAreas))
            }
            val resp = BackendClient.post("api/research/tech-radar", req)
            val jsonArr = resp.optJSONArray("items")
            if (jsonArr != null && jsonArr.length() > 0) {
                val list = mutableListOf<TechRadarItem>()
                for (i in 0 until jsonArr.length()) {
                    val obj = jsonArr.getJSONObject(i)
                    list.add(
                        TechRadarItem(
                            id = obj.optString("id", "radar_$i"),
                            title = obj.optString("title"),
                            domain = obj.optString("domain"),
                            summary = obj.optString("summary"),
                            impact = obj.optString("impact", "Trending"),
                            source = obj.optString("source", "ArXiv / Industry"),
                            timeAgo = obj.optString("timeAgo", "Today")
                        )
                    )
                }
                if (list.isNotEmpty()) return@withContext list
            }
        } catch (e: Exception) {
            Log.w("AgentService", "Failed fetching radar from backend", e)
        }
        emptyList()
    }
}

