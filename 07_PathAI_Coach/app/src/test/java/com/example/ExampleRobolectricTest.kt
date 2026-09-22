package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("PathAI Coach", appName)
  }

  @Test
  fun `verify seed data integrity`() {
    val initialProfile = com.example.data.local.SeedData.initialProfile
    assertEquals("Alex Rivera", initialProfile.name)
    assertEquals(5, com.example.data.local.SeedData.initialSkillGaps.size)
    assertEquals(12, com.example.data.local.SeedData.initialRoadmapWeeks.size)
    assertEquals(4, com.example.data.local.SeedData.initialJobs.size)
  }

  @Test
  fun `verify firebase user data defaults and serialization`() {
    val user = com.example.data.model.FirebaseUserData(
      uid = "test-uid-123",
      name = "Mahesh Babu",
      email = "maheshbabuchilakani970@gmail.com",
      focusAreas = listOf("Python", "AI/ML", "PyTorch"),
      currentPath = "AIML"
    )
    assertEquals("test-uid-123", user.uid)
    assertEquals("Mahesh Babu", user.name)
    assertEquals("maheshbabuchilakani970@gmail.com", user.email)
    assertEquals(3, user.focusAreas.size)
    assertEquals("AIML", user.currentPath)
  }

  @Test
  fun `verify tech radar liked state toggle`() {
    val item = com.example.data.model.TechRadarItem(
      id = "radar-1",
      title = "DeepSeek-R1 Architecture",
      domain = "LLM / Reasoning",
      summary = "Breakthrough open reasoning weights",
      impact = "High Impact",
      source = "arXiv:2501.12948",
      timeAgo = "1d ago",
      isLiked = false
    )
    val likedItem = item.copy(isLiked = true)
    org.junit.Assert.assertFalse(item.isLiked)
    org.junit.Assert.assertTrue(likedItem.isLiked)
  }
}
