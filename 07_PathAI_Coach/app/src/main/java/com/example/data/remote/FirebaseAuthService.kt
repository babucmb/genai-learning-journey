package com.example.data.remote

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.example.R
import com.example.data.model.FirebaseUserData
import com.google.android.gms.tasks.Task
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resumeWithException

suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { cont ->
    addOnSuccessListener { result ->
        if (cont.isActive) cont.resume(result, null)
    }
    addOnFailureListener { exception ->
        if (cont.isActive) cont.resumeWithException(exception)
    }
    addOnCanceledListener {
        if (cont.isActive) cont.cancel()
    }
}

class FirebaseAuthService {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    suspend fun signInWithEmail(email: String, pass: String): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        try {
            val result = auth.signInWithEmailAndPassword(email.trim(), pass).awaitTask()
            val user = result.user ?: return@withContext Result.failure(Exception("Authentication succeeded but user is null"))
            Result.success(user)
        } catch (e: Exception) {
            Log.e("FirebaseAuthService", "Sign in with email failed", e)
            Result.failure(e)
        }
    }

    suspend fun signUpWithEmail(email: String, pass: String, name: String): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        try {
            val result = auth.createUserWithEmailAndPassword(email.trim(), pass).awaitTask()
            val user = result.user ?: return@withContext Result.failure(Exception("Registration succeeded but user is null"))
            
            if (name.isNotBlank()) {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name.trim())
                    .build()
                user.updateProfile(profileUpdates).awaitTask()
            }
            Result.success(user)
        } catch (e: Exception) {
            Log.e("FirebaseAuthService", "Sign up with email failed", e)
            Result.failure(e)
        }
    }

    suspend fun signInWithGoogle(context: Context): Result<FirebaseUser> = withContext(Dispatchers.IO) {
        try {
            val credentialManager = CredentialManager.create(context)
            val serverClientId = context.getString(R.string.default_web_client_id)

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(serverClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val credentialResult = credentialManager.getCredential(context = context, request = request)
            val credential = credentialResult.credential

            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data)
                val authCredential = GoogleAuthProvider.getCredential(googleIdToken.idToken, null)
                val authResult = auth.signInWithCredential(authCredential).awaitTask()
                val user = authResult.user ?: return@withContext Result.failure(Exception("Google Sign-In returned null user"))
                Result.success(user)
            } else {
                Result.failure(Exception("Unsupported credential type: ${credential.type}"))
            }
        } catch (e: GetCredentialCancellationException) {
            Result.failure(Exception("Google sign-in was canceled"))
        } catch (e: GetCredentialException) {
            Log.w("FirebaseAuthService", "Credential Manager exception", e)
            Result.failure(Exception(e.localizedMessage ?: "Google Sign-In unavailable on this device configuration. Please use Email/Password."))
        } catch (e: Exception) {
            Log.e("FirebaseAuthService", "Google Sign-In failed", e)
            Result.failure(e)
        }
    }

    fun signOut() {
        try {
            auth.signOut()
        } catch (e: Exception) {
            Log.e("FirebaseAuthService", "Sign out error", e)
        }
    }

    suspend fun getOrCreateUserDocument(
        user: FirebaseUser,
        defaultFocusAreas: List<String> = listOf("Python", "AI/ML"),
        defaultPath: String = "AIML"
    ): FirebaseUserData = withContext(Dispatchers.IO) {
        val userDocRef = firestore.collection("users").document(user.uid)
        try {
            val snapshot = userDocRef.get().awaitTask()
            if (snapshot.exists()) {
                val eduMap = snapshot.get("education") as? Map<*, *>
                val education = if (eduMap != null) {
                    com.example.data.model.UserEducation(
                        degree = eduMap["degree"]?.toString() ?: "B.Tech",
                        major = eduMap["major"]?.toString() ?: "Computer Science & Engineering",
                        college = eduMap["college"]?.toString() ?: "National Institute of Technology",
                        graduation_year = eduMap["graduation_year"]?.toString() ?: "2025",
                        cgpa = eduMap["cgpa"]?.toString() ?: "8.6 / 10"
                    )
                } else com.example.data.model.UserEducation()

                val skills = (snapshot.get("skills") as? List<*>)?.mapNotNull { it?.toString() }
                    ?: listOf("Python", "PyTorch", "NumPy", "Pandas", "Scikit-Learn", "Git", "REST APIs")

                val experienceLevel = snapshot.getString("experience_level") ?: "Fresher"

                val prefMap = snapshot.get("job_preferences") as? Map<*, *>
                val jobPreferences = if (prefMap != null) {
                    com.example.data.model.UserJobPreferences(
                        roles = (prefMap["roles"] as? List<*>)?.mapNotNull { it?.toString() } ?: listOf("AI/ML Intern", "Python Developer"),
                        locations = (prefMap["locations"] as? List<*>)?.mapNotNull { it?.toString() } ?: listOf("Remote", "Bengaluru", "Hyderabad"),
                        work_mode = (prefMap["work_mode"] as? List<*>)?.mapNotNull { it?.toString() } ?: listOf("Remote", "Hybrid"),
                        min_stipend_or_salary = prefMap["min_stipend_or_salary"]?.toString() ?: "₹25,000 / month",
                        willing_to_relocate = prefMap["willing_to_relocate"] as? Boolean ?: true
                    )
                } else com.example.data.model.UserJobPreferences()

                val resumeUrl = snapshot.getString("resume_url")

                val notifMap = snapshot.get("notifications") as? Map<*, *>
                val notifications = if (notifMap != null) {
                    com.example.data.model.NotificationPreferences(
                        job_alerts_enabled = notifMap["job_alerts_enabled"] as? Boolean ?: true,
                        alert_time = notifMap["alert_time"]?.toString() ?: "09:00",
                        timezone = notifMap["timezone"]?.toString() ?: "Asia/Kolkata"
                    )
                } else com.example.data.model.NotificationPreferences()

                val existing = FirebaseUserData(
                    uid = snapshot.getString("uid") ?: user.uid,
                    name = snapshot.getString("name") ?: user.displayName.orEmpty().ifBlank { user.email?.substringBefore("@") ?: "Engineer" },
                    email = snapshot.getString("email") ?: user.email.orEmpty(),
                    education = education,
                    skills = skills,
                    experience_level = experienceLevel,
                    job_preferences = jobPreferences,
                    resume_url = resumeUrl,
                    notifications = notifications,
                    focusAreas = (snapshot.get("focusAreas") as? List<*>)?.mapNotNull { it?.toString() } ?: defaultFocusAreas,
                    currentPath = snapshot.getString("currentPath") ?: defaultPath,
                    createdAt = snapshot.getLong("createdAt") ?: System.currentTimeMillis(),
                    updatedAt = snapshot.getLong("updatedAt") ?: System.currentTimeMillis()
                )
                userDocRef.update("updatedAt", System.currentTimeMillis()).awaitTask()
                existing
            } else {
                val newProfile = FirebaseUserData(
                    uid = user.uid,
                    name = user.displayName.takeIf { !it.isNullOrBlank() } ?: user.email?.substringBefore("@") ?: "Engineer",
                    email = user.email.orEmpty(),
                    focusAreas = defaultFocusAreas,
                    currentPath = defaultPath,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )

                saveFullUserProfile(newProfile)
                newProfile
            }
        } catch (e: Exception) {
            Log.w("FirebaseAuthService", "Failed accessing Firestore users collection; returning fallback profile", e)
            FirebaseUserData(
                uid = user.uid,
                name = user.displayName.takeIf { !it.isNullOrBlank() } ?: user.email?.substringBefore("@") ?: "Engineer",
                email = user.email.orEmpty(),
                focusAreas = defaultFocusAreas,
                currentPath = defaultPath
            )
        }
    }

    suspend fun saveFullUserProfile(userData: FirebaseUserData): Result<Unit> = withContext(Dispatchers.IO) {
        if (userData.uid.isBlank()) return@withContext Result.failure(Exception("User UID is empty"))
        try {
            val userDocRef = firestore.collection("users").document(userData.uid)
            val dataMap = mapOf(
                "uid" to userData.uid,
                "name" to userData.name,
                "email" to userData.email,
                "education" to mapOf(
                    "degree" to userData.education.degree,
                    "major" to userData.education.major,
                    "college" to userData.education.college,
                    "graduation_year" to userData.education.graduation_year,
                    "cgpa" to userData.education.cgpa
                ),
                "skills" to userData.skills,
                "experience_level" to userData.experience_level,
                "job_preferences" to mapOf(
                    "roles" to userData.job_preferences.roles,
                    "locations" to userData.job_preferences.locations,
                    "work_mode" to userData.job_preferences.work_mode,
                    "min_stipend_or_salary" to userData.job_preferences.min_stipend_or_salary,
                    "willing_to_relocate" to userData.job_preferences.willing_to_relocate
                ),
                "resume_url" to userData.resume_url,
                "notifications" to mapOf(
                    "job_alerts_enabled" to userData.notifications.job_alerts_enabled,
                    "alert_time" to userData.notifications.alert_time,
                    "timezone" to userData.notifications.timezone
                ),
                "focusAreas" to userData.focusAreas,
                "currentPath" to userData.currentPath,
                "createdAt" to userData.createdAt,
                "updatedAt" to System.currentTimeMillis()
            )
            userDocRef.set(dataMap, SetOptions.merge()).awaitTask()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseAuthService", "Failed saving full user profile to Firestore", e)
            Result.failure(e)
        }
    }

    fun validateProfileForJobAlerts(user: FirebaseUserData): Pair<Boolean, String> {
        if (user.education.degree.isBlank() || user.education.college.isBlank()) {
            return false to "Please specify your degree and college in Education before enabling Job Alerts."
        }
        if (user.skills.isEmpty()) {
            return false to "Please add at least 1 technical skill to your profile."
        }
        if (user.job_preferences.roles.isEmpty()) {
            return false to "Please specify at least 1 target role in Job Preferences."
        }
        if (user.experience_level.isBlank()) {
            return false to "Please select your Experience Level (e.g. Fresher, Intern)."
        }
        return true to "Profile is valid for automated job alerts."
    }

    suspend fun updateUserFocusAreas(uid: String, focusAreas: List<String>, currentPath: String) = withContext(Dispatchers.IO) {
        if (uid.isBlank()) return@withContext
        try {
            val userDocRef = firestore.collection("users").document(uid)
            val updates = mapOf(
                "focusAreas" to focusAreas,
                "currentPath" to currentPath,
                "updatedAt" to System.currentTimeMillis()
            )
            userDocRef.update(updates).awaitTask()
        } catch (e: Exception) {
            Log.w("FirebaseAuthService", "Failed to update focus areas in Firestore", e)
        }
    }
}
