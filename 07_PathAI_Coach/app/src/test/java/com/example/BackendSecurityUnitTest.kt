//Backend Security unit testing
package com.example

import com.example.data.remote.BackendConfig 
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BackendSecurityUnitTest {

    @Test
    fun testBackendConfigDefaults() {
        assertNotNull(BackendConfig.baseUrl)
        assertTrue("Base URL must point to backend host", BackendConfig.baseUrl.contains("10.0.2.2") || BackendConfig.baseUrl.contains("localhost") || BackendConfig.baseUrl.startsWith("http"))
        assertTrue("Allow offline fallback default is true", BackendConfig.allowOfflineFallback)
    }

    @Test
    fun testBackendConfigUrlFormatting() {
        BackendConfig.baseUrl = "http://10.0.2.2:5000"
        assertEquals("http://10.0.2.2:5000", BackendConfig.baseUrl)
        
        BackendConfig.allowOfflineFallback = false
        assertFalse(BackendConfig.allowOfflineFallback)
        BackendConfig.allowOfflineFallback = true
    }

    @Test
    fun testNoHardcodedKeysInClientConfig() {
        // Assert that client-side BuildConfig does not contain hardcoded private keys
        val configClass = Class.forName("com.example.BuildConfig")
        val fields = configClass.declaredFields.map { it.name }
        assertFalse("GEMINI_API_KEY should not be present in BuildConfig", fields.contains("GEMINI_API_KEY"))
        assertFalse("HUGGINGFACE_API_KEY should not be present in BuildConfig", fields.contains("HUGGINGFACE_API_KEY"))
    }
}
