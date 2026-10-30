const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read user profile", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
});

test("Authenticated user: cannot read another user's profile", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(BOB_UID).set({
      userId: BOB_UID,
      displayName: "Bob",
      dailyCalorieGoal: 2200,
      dailyProteinGoal: 140,
      dailyCarbGoal: 200,
      dailyFatGoal: 70,
      createdAt: new Date(),
      updatedAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).get());
});

test("Authenticated user: can read and write their own food scans", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const scanDoc = aliceDb.collection("users").doc(ALICE_UID).collection("food_scans").doc("scan_1");

  await assertSucceeds(
    scanDoc.set({
      id: "scan_1",
      userId: ALICE_UID,
      title: "Chicken Salad",
      mealType: "lunch",
      calories: 450,
      protein: 38,
      carbs: 15,
      fat: 22,
      fiber: 6,
      sugar: 4,
      confidence: 92,
      detectedItems: [
        { name: "Grilled Chicken", quantityGrams: 150, calories: 250, protein: 32, carbs: 0, fat: 5 }
      ],
      date: "2026-10-30",
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );

  await assertSucceeds(scanDoc.get());
});

test("Authenticated user: cannot write food scan into another user's collection", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobScanDoc = aliceDb.collection("users").doc(BOB_UID).collection("food_scans").doc("scan_2");

  await assertFails(
    bobScanDoc.set({
      id: "scan_2",
      userId: BOB_UID,
      title: "Intruder Meal",
      mealType: "dinner",
      calories: 500,
      protein: 20,
      carbs: 40,
      fat: 10,
      fiber: 2,
      sugar: 5,
      confidence: 85,
      date: "2026-10-30",
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});

test("Authenticated user: cannot create food scan with invalid mealType", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const scanDoc = aliceDb.collection("users").doc(ALICE_UID).collection("food_scans").doc("scan_invalid");

  await assertFails(
    scanDoc.set({
      id: "scan_invalid",
      userId: ALICE_UID,
      title: "Invalid Meal",
      mealType: "midnight_feast", // not in allowed enum
      calories: 450,
      protein: 38,
      carbs: 15,
      fat: 22,
      fiber: 6,
      sugar: 4,
      confidence: 92,
      date: "2026-10-30",
      createdAt: new Date(),
      updatedAt: new Date(),
    })
  );
});
