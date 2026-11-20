const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_fitquest_1";
const BOB_UID = "bob_fitquest_2";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function pastDate(msAgo = 5000) {
  return new Date(Date.now() - msAgo);
}

function validUserProfile(uid, ts = pastDate()) {
  return {
    userId: uid,
    username: "FitRunner",
    avatarId: "bolt",
    interests: ["Walking", "Running", "Hydration"],
    dailyStepGoal: 10000,
    dailyWaterGoalMl: 2500,
    dailyRunGoalKm: 5.0,
    dailySportsGoalMin: 60,
    totalXp: 0,
    level: 1,
    currentStreak: 1,
    bestStreak: 1,
    longestRunKm: 0.0,
    bestDailyScore: 0,
    totalActivities: 0,
    totalStepsLifetime: 0,
    unlockedBadges: [],
    activeDates: ["2026-11-20"],
    lastActiveDate: "2026-11-20",
    onboardingCompleted: true,
    publicProfile: true,
    notificationsEnabled: true,
    region: "Local",
    createdAt: ts,
    updatedAt: ts,
  };
}

function validPublicProfile(uid, ts = pastDate(), publicFlag = true) {
  return {
    userId: uid,
    username: "FitRunner",
    usernameLower: "fitrunner",
    avatarId: "bolt",
    region: "Local",
    totalXp: 1200,
    weeklyXp: 450,
    weeklyRunningKm: 12.5,
    weeklySteps: 34200,
    weeklySportsMinutes: 90,
    todayHealthScore: 780,
    level: 4,
    currentStreak: 5,
    badgeCount: 2,
    unlockedBadges: ["hydration_hero", "first_5k"],
    publicProfile: publicFlag,
    createdAt: ts,
    updatedAt: ts,
  };
}

function validDailyLog(dateKey = "2026-11-20", ts = pastDate()) {
  return {
    dateKey,
    steps: 7200,
    waterMl: 1500,
    runningKm: 3.0,
    sportsMinutes: 30,
    completedChallengeCount: 1,
    healthScore: 685,
    xpEarnedToday: 240,
    createdAt: ts,
    updatedAt: ts,
  };
}

function validActivityLog(ts = pastDate()) {
  return {
    activityType: "RUNNING",
    durationSeconds: 1800,
    distanceKm: 5.2,
    calories: 390,
    stepsEstimated: 6200,
    xpEarned: 150,
    personalBest: true,
    dateKey: "2026-11-20",
    createdAt: ts,
    updatedAt: ts,
  };
}

function validWaterLog(ts = pastDate()) {
  return {
    amountMl: 250,
    xpEarned: 15,
    dateKey: "2026-11-20",
    createdAt: ts,
  };
}

function validChallenge(creatorId, ts = pastDate(), publicFlag = true) {
  return {
    creatorId,
    title: "7-Day Step Showdown",
    description: "Walk 50,000 steps this week!",
    challengeType: "FRIEND_BATTLE",
    metricType: "STEPS",
    targetValue: 50000,
    rewardXp: 300,
    durationDays: 7,
    status: "ACTIVE",
    publicChallenge: publicFlag,
    participantIds: [creatorId],
    combinedProgress: 0,
    teamName: "",
    createdAt: ts,
    updatedAt: ts,
  };
}

function validFeedPost(authorId, ts = pastDate()) {
  return {
    authorId,
    authorUsername: "FitRunner",
    authorAvatarId: "bolt",
    message: "Just completed a 5.2 km run!",
    category: "RUN",
    xpEarned: 150,
    publicFeed: true,
    fireCount: 0,
    clapCount: 0,
    boltCount: 0,
    reactedUserIds: [],
    createdAt: ts,
    updatedAt: ts,
  };
}

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

test("Unauthenticated users cannot read or write any FitQuest collection", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("public_profiles").where("publicProfile", "==", true).get());
  await assertFails(unauthDb.collection("challenges").where("publicChallenge", "==", true).get());
  await assertFails(unauthDb.collection("feed_posts").where("publicFeed", "==", true).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).set(validUserProfile(ALICE_UID)));
});

test("UserProfile: owner create/update works; cross-user, ghost fields, @DocumentId id, and future timestamps fail", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = pastDate(10000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set(validUserProfile(ALICE_UID, created))
  );
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());

  // Partial update preserving createdAt & userId
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      totalXp: 500,
      level: 2,
      updatedAt: pastDate(1000),
    })
  );

  // Cross-user read & write must fail (PII isolation)
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).update({ totalXp: 9999, updatedAt: pastDate(1000) }));

  // Immutable createdAt change must fail
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      createdAt: pastDate(500),
      updatedAt: pastDate(500),
    })
  );

  // Ghost field shadow update must fail
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      isAdmin: true,
      updatedAt: pastDate(500),
    })
  );

  // Tampered future timestamp must fail
  await assertFails(
    bobDb.collection("users").doc(BOB_UID).set(
      validUserProfile(BOB_UID, new Date(Date.now() + 60000))
    )
  );

  // Extra @DocumentId 'id' key must fail
  await assertFails(
    bobDb.collection("users").doc(BOB_UID).set({
      ...validUserProfile(BOB_UID, created),
      id: BOB_UID,
    })
  );
});

test("User subcollections (daily_logs, activities, water_logs, friends): strict owner isolation & subcollection list", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const ts = pastDate(5000);

  // Empty subcollection list must succeed for owner
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("daily_logs").get());
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("activities").get());
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("water_logs").get());
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("friends").get());

  // Owner writes succeed
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("daily_logs").doc("2026-11-20").set(validDailyLog("2026-11-20", ts))
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("daily_logs").doc("2026-11-20").update({
      waterMl: 2000,
      healthScore: 720,
      updatedAt: pastDate(1000),
    })
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("activities").doc("act_1").set(validActivityLog(ts))
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("water_logs").doc("w_1").set(validWaterLog(ts))
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("friends").doc(BOB_UID).set({
      friendUid: BOB_UID,
      friendUsername: "BobSprinter",
      friendAvatarId: "runner",
      createdAt: ts,
    })
  );

  // Cross-user access denied
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("daily_logs").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("activities").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("water_logs").doc("w_1").delete());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("friends").get());

  // Score > 1000 cap rejected by rule
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("daily_logs").doc("2026-11-20").update({
      healthScore: 1500,
      updatedAt: pastDate(500),
    })
  );
});

test("PublicProfile: filtered leaderboard query succeeds; unfiltered query fails; private profiles hidden", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const ts = pastDate(5000);

  await assertSucceeds(
    aliceDb.collection("public_profiles").doc(ALICE_UID).set(validPublicProfile(ALICE_UID, ts, true))
  );
  await assertSucceeds(
    bobDb.collection("public_profiles").doc(BOB_UID).set(validPublicProfile(BOB_UID, ts, false))
  );

  // Filtered query matches Kotlin leaderboard repository
  await assertSucceeds(
    aliceDb.collection("public_profiles").where("publicProfile", "==", true).get()
  );

  // Unfiltered query must fail
  await assertFails(aliceDb.collection("public_profiles").get());

  // Bob's private profile cannot be read by Alice, but can be read by Bob
  await assertFails(aliceDb.collection("public_profiles").doc(BOB_UID).get());
  await assertSucceeds(bobDb.collection("public_profiles").doc(BOB_UID).get());

  // Cross-user write rejected
  await assertFails(
    bobDb.collection("public_profiles").doc(ALICE_UID).update({ weeklyXp: 9999, updatedAt: pastDate(1000) })
  );
});

test("Challenges: join, progress, terminal COMPLETED locking, and query alignment", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const ts = pastDate(10000);

  await assertSucceeds(
    aliceDb.collection("challenges").doc("chal_1").set(validChallenge(ALICE_UID, ts, true))
  );

  // Filtered queries succeed; unfiltered fails
  await assertSucceeds(aliceDb.collection("challenges").where("publicChallenge", "==", true).get());
  await assertSucceeds(aliceDb.collection("challenges").where("participantIds", "array-contains", ALICE_UID).get());
  await assertFails(aliceDb.collection("challenges").get());

  // Bob joins public challenge (Action 1)
  await assertSucceeds(
    bobDb.collection("challenges").doc("chal_1").update({
      participantIds: [ALICE_UID, BOB_UID],
      updatedAt: pastDate(3000),
    })
  );

  // Bob logs progress and marks COMPLETED (Action 2)
  await assertSucceeds(
    bobDb.collection("challenges").doc("chal_1").update({
      combinedProgress: 50000,
      status: "COMPLETED",
      updatedAt: pastDate(2000),
    })
  );

  // Terminal state lock: no updates allowed after COMPLETED
  await assertFails(
    aliceDb.collection("challenges").doc("chal_1").update({
      combinedProgress: 60000,
      status: "COMPLETED",
      updatedAt: pastDate(1000),
    })
  );
});

test("FeedPosts: create, filtered feed list, emoji reaction action, and owner-only delete", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const ts = pastDate(10000);

  await assertSucceeds(
    aliceDb.collection("feed_posts").doc("post_1").set(validFeedPost(ALICE_UID, ts))
  );

  // Filtered query succeeds; unfiltered fails
  await assertSucceeds(bobDb.collection("feed_posts").where("publicFeed", "==", true).get());
  await assertFails(bobDb.collection("feed_posts").get());

  // Bob reacts with fire emoji (+1 allowed)
  await assertSucceeds(
    bobDb.collection("feed_posts").doc("post_1").update({
      fireCount: 1,
      reactedUserIds: [BOB_UID],
      updatedAt: pastDate(1000),
    })
  );

  // Reaction stuffing (+10) or editing message must fail
  await assertFails(
    bobDb.collection("feed_posts").doc("post_1").update({
      fireCount: 11,
      updatedAt: pastDate(500),
    })
  );
  await assertFails(
    bobDb.collection("feed_posts").doc("post_1").update({
      message: "Hacked post",
      updatedAt: pastDate(500),
    })
  );

  // Cross-user delete fails; owner delete succeeds
  await assertFails(bobDb.collection("feed_posts").doc("post_1").delete());
  await assertSucceeds(aliceDb.collection("feed_posts").doc("post_1").delete());
});
