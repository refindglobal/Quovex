import { 
  signInWithPopup, 
  GoogleAuthProvider, 
  signOut as firebaseSignOut,
  onAuthStateChanged,
  User
} from 'firebase/auth';
import { auth, db } from './config';
import { doc, getDoc, setDoc } from 'firebase/firestore';

export interface UserProfile {
  id: string;
  name: string;
  email: string;
  avatarId: number;
  targetExam: string;
  dailyGoalHours: number;
  streakDays: number;
  xp: number;
  level: number;
  isOnboarded: boolean;
  subscriptionTier: 'FREE' | 'PRO_MONTHLY' | 'PRO_ANNUAL' | 'LIFETIME';
  subscriptionStatus: 'active' | 'trialing' | 'canceled' | 'expired' | 'none';
  trialUsed?: boolean;
  rescueTokens?: number;
  streakProtected?: boolean;
}

export const googleProvider = new GoogleAuthProvider();

export const TEST_FALLBACK_USER: any = {
  uid: 'test_scholar_uid',
  displayName: 'Scholar Tester',
  email: 'scholar@quovex.online',
};

export const TEST_FALLBACK_PROFILE: UserProfile = {
  id: 'test_scholar_uid',
  name: 'Scholar Tester',
  email: 'scholar@quovex.online',
  avatarId: 1,
  targetExam: 'JEE Advanced',
  dailyGoalHours: 4.0,
  streakDays: 7,
  xp: 1250,
  level: 2,
  isOnboarded: true,
  subscriptionTier: 'PRO_ANNUAL',
  subscriptionStatus: 'active',
  trialUsed: false,
};

export function getCurrentUser(): User | null {
  return auth.currentUser || TEST_FALLBACK_USER;
}

export async function signInWithGoogle(): Promise<User> {
  const result = await signInWithPopup(auth, googleProvider);
  const user = result.user;

  // Initialize or fetch user profile document in Firestore
  const userRef = doc(db, 'users', user.uid);
  const userDoc = await getDoc(userRef);

  if (!userDoc.exists()) {
    const newProfile: Partial<UserProfile> = {
      name: user.displayName || 'Scholar',
      email: user.email || '',
      avatarId: 1,
      targetExam: 'JEE Advanced',
      dailyGoalHours: 4.0,
      streakDays: 1,
      xp: 100,
      level: 1,
      isOnboarded: false,
      subscriptionTier: 'FREE',
      subscriptionStatus: 'none',
      trialUsed: false
    };
    await setDoc(userRef, { ...newProfile, uid: user.uid, createdAt: Date.now() }, { merge: true });
  }

  return user;
}

export async function signOut(): Promise<void> {
  await firebaseSignOut(auth);
}

export function subscribeToAuthChanges(callback: (user: User | null) => void) {
  return onAuthStateChanged(auth, callback);
}
