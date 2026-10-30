package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.FoodScan
import com.example.data.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.UUID

class NutritionRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun saveAndObserveUserProfile_authenticatedOwner_succeeds() = runBlocking {
    val uid = signInTestUser(ALICE_EMAIL)
    val repository = NutritionRepository(firestore)

    val profile = UserProfile(
      userId = uid,
      displayName = "Alice Nutritionist",
      dailyCalorieGoal = 2100,
      dailyProteinGoal = 130,
      dailyCarbGoal = 220,
      dailyFatGoal = 70,
      dietPreference = "High-Protein",
      units = "metric"
    )

    val saveResult = withTimeout(DEFAULT_TIMEOUT_MS) { repository.saveUserProfile(profile) }
    assertTrue(saveResult.exceptionOrNull()?.message ?: "Save profile failed", saveResult.isSuccess)

    val observed = withTimeout(FLOW_TIMEOUT_MS) {
      repository.observeUserProfile(uid).first { it != null }
    }
    assertNotNull(observed)
    assertEquals("Alice Nutritionist", observed?.displayName)
    assertEquals(2100, observed?.dailyCalorieGoal)
  }

  @Test
  fun saveAndObserveFoodScan_authenticatedOwner_succeeds() = runBlocking {
    val uid = signInTestUser(ALICE_EMAIL)
    val repository = NutritionRepository(firestore)

    val scan = FoodScan(
      id = "scan_${UUID.randomUUID()}",
      userId = uid,
      title = "Quinoa Salmon Bowl",
      mealType = "lunch",
      calories = 620,
      protein = 38.0,
      carbs = 45.0,
      fat = 24.0,
      fiber = 8.0,
      sugar = 3.0,
      confidence = 94,
      date = "2026-10-30"
    )

    val saveResult = withTimeout(DEFAULT_TIMEOUT_MS) { repository.saveFoodScan(scan) }
    assertTrue(saveResult.isSuccess)

    val observed = withTimeout(FLOW_TIMEOUT_MS) {
      repository.observeTodayScans(uid, "2026-10-30").first { list -> list.any { it.title == "Quinoa Salmon Bowl" } }
    }
    assertTrue(observed.any { it.calories == 620 })
  }

  @Test
  fun getFoodScan_crossUserAccess_failsWithPermissionDenied() = runBlocking {
    val aliceUid = signInTestUser(ALICE_EMAIL)
    val aliceRepo = NutritionRepository(firestore)
    val scanId = "scan_${UUID.randomUUID()}"
    val scan = FoodScan(
      id = scanId,
      userId = aliceUid,
      title = "Alice Secret Dish",
      mealType = "dinner",
      calories = 500,
      protein = 25.0,
      carbs = 40.0,
      fat = 15.0,
      fiber = 4.0,
      sugar = 2.0,
      confidence = 90,
      date = "2026-10-30"
    )
    aliceRepo.saveFoodScan(scan)

    signInTestUser(BOB_EMAIL)
    val bobRepo = NutritionRepository(firestore)

    try {
      withTimeout(FLOW_TIMEOUT_MS) {
        bobRepo.observeTodayScans(aliceUid, "2026-10-30").first()
      }
      fail("Expected cross-user query to fail with permission denied")
    } catch (e: Exception) {
      val firestoreException = checkNotNull(e.unwrapFirestoreException()) {
        "Expected nested FirebaseFirestoreException, got $e"
      }
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException.code)
    }
  }

  @Test
  fun observeTodayScans_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
    val uid = signInTestUser(ALICE_EMAIL)
    auth.signOut()

    val repository = NutritionRepository(firestore)
    try {
      withTimeout(FLOW_TIMEOUT_MS) {
        repository.observeTodayScans(uid, "2026-10-30").first()
      }
      fail("Expected unauthenticated query to fail with permission denied")
    } catch (e: Exception) {
      val firestoreException = checkNotNull(e.unwrapFirestoreException()) {
        "Expected nested FirebaseFirestoreException, got $e"
      }
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException.code)
    }
  }

  private companion object {
    const val ALICE_EMAIL = "alice_test@example.com"
    const val BOB_EMAIL = "bob_test@example.com"
    const val DEFAULT_TIMEOUT_MS = 5000L
    const val FLOW_TIMEOUT_MS = 4000L
  }
}
