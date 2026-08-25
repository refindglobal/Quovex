import * as admin from 'firebase-admin';

// Initialize Firebase Admin SDK singleton
if (!admin.apps.length) {
  const projectId = process.env.FIREBASE_PROJECT_ID || process.env.NEXT_PUBLIC_FIREBASE_PROJECT_ID || 'quovex-f3104';

  if (process.env.FIREBASE_SERVICE_ACCOUNT_KEY) {
    try {
      const serviceAccount = JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT_KEY);
      admin.initializeApp({
        credential: admin.credential.cert(serviceAccount),
        projectId: serviceAccount.project_id || projectId,
      });
    } catch {
      admin.initializeApp({ projectId });
    }
  } else {
    // Default to project quovex-f3104 (connects to emulator if FIRESTORE_EMULATOR_HOST is set)
    admin.initializeApp({ projectId });
  }
}

let firestoreInstance: admin.firestore.Firestore | null = null;

export function getAdminFirestore(): admin.firestore.Firestore {
  if (!firestoreInstance) {
    firestoreInstance = admin.firestore();
    try {
      firestoreInstance.settings({ ignoreUndefinedProperties: true });
    } catch (_) {}
  }
  return firestoreInstance;
}

export function getAdminAuth() {
  return admin.auth();
}

export { admin };
