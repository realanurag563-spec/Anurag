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

test("Unauthenticated user cannot read or write any user data", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice",
      selectedTrackId: "class_10",
      selectedSubject: "Science",
      totalXp: 0,
      gems: 0,
      streakDays: 1,
      hearts: 5,
      completedLessonsCount: 0,
      testsCompletedCount: 0,
      unlockedMilestones: [],
      dailyGoalMinutes: 20,
      createdAt: new Date(Date.now() - 1000),
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("lesson_progress").get());
});

test("User profile: owner create, partial update, and cross-user isolation", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const past = new Date(Date.now() - 5000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice",
      selectedTrackId: "class_10",
      selectedSubject: "Science",
      totalXp: 50,
      gems: 10,
      streakDays: 1,
      hearts: 5,
      completedLessonsCount: 1,
      testsCompletedCount: 0,
      unlockedMilestones: ["first_lesson"],
      dailyGoalMinutes: 25,
      createdAt: past,
      updatedAt: past,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      totalXp: 120,
      gems: 18,
      unlockedMilestones: ["first_lesson", "xp_100"],
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      isAdmin: true,
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      createdAt: new Date(Date.now() - 500),
      updatedAt: new Date(Date.now() - 500),
    })
  );

  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).update({
      totalXp: 9999,
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});

test("Lesson progress, study schedule, and practice test attempts enforce schema and ownership", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const past = new Date(Date.now() - 5000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("lesson_progress").doc("lesson_atom_1").set({
      userId: ALICE_UID,
      lessonId: "lesson_atom_1",
      trackId: "class_9",
      subject: "Science",
      title: "Atomic Structure",
      xpEarned: 60,
      stars: 3,
      bestAccuracyPercent: 100,
      completed: true,
      createdAt: past,
      updatedAt: past,
    })
  );

  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("lesson_progress").doc("lesson_atom_2").set({
      id: "lesson_atom_2",
      userId: ALICE_UID,
      lessonId: "lesson_atom_2",
      trackId: "class_9",
      subject: "Science",
      title: "Molecules",
      xpEarned: 40,
      stars: 2,
      bestAccuracyPercent: 80,
      completed: true,
      createdAt: past,
      updatedAt: past,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("study_schedule").doc("sched_1").set({
      userId: ALICE_UID,
      trackId: "upsc_cse",
      subject: "Polity",
      topicTitle: "Fundamental Rights & Constitutional Remedies",
      dayLabel: "Today",
      durationMinutes: 30,
      activityType: "LESSON",
      completed: false,
      sortOrder: 0,
      createdAt: past,
      updatedAt: past,
    })
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("study_schedule").doc("sched_1").update({
      completed: true,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("test_attempts").doc("attempt_1").set({
      userId: ALICE_UID,
      trackId: "ssc_cgl",
      subject: "Quantitative Aptitude",
      testTitle: "Tier-1 Speed Drill",
      score: 8,
      totalQuestions: 10,
      xpAwarded: 120,
      durationSeconds: 320,
      createdAt: past,
      updatedAt: past,
    })
  );
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("test_attempts").doc("attempt_bad").set({
      userId: ALICE_UID,
      trackId: "ssc_cgl",
      subject: "Quantitative Aptitude",
      testTitle: "Invalid Score",
      score: 12,
      totalQuestions: 10,
      xpAwarded: 120,
      durationSeconds: 100,
      createdAt: past,
      updatedAt: past,
    })
  );

  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("lesson_progress").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("lesson_progress").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("study_schedule").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("test_attempts").get());
});

test("Doubt sessions and messages enforce parent existence and terminal status locking", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const past = new Date(Date.now() - 5000);

  const sessionRef = aliceDb.collection("users").doc(ALICE_UID).collection("doubt_sessions").doc("sess_1");

  await assertFails(
    sessionRef.collection("messages").doc("msg_orphan").set({
      userId: ALICE_UID,
      senderRole: "STUDENT",
      senderName: "Alice",
      text: "Help with trigonometry!",
      createdAt: past,
      updatedAt: past,
    })
  );

  await assertSucceeds(
    sessionRef.set({
      userId: ALICE_UID,
      tutorId: "tutor_meera",
      tutorName: "Dr. Meera Nair",
      tutorSpecialty: "Physics & Calculus",
      subject: "Physics",
      topic: "Electromagnetic Induction",
      status: "ACTIVE",
      lastMessage: "Session started",
      createdAt: past,
      updatedAt: past,
    })
  );

  await assertSucceeds(
    sessionRef.collection("messages").doc("msg_1").set({
      userId: ALICE_UID,
      senderRole: "STUDENT",
      senderName: "Alice",
      text: "Why is Lenz's law negative?",
      createdAt: past,
      updatedAt: past,
    })
  );

  await assertSucceeds(
    sessionRef.update({
      status: "RESOLVED",
      lastMessage: "Thanks, understood!",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertFails(
    sessionRef.update({
      status: "ACTIVE",
      updatedAt: new Date(Date.now() - 500),
    })
  );

  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("doubt_sessions").get());
});
