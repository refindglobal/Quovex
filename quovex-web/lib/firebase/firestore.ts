import { 
  collection, 
  doc, 
  onSnapshot, 
  setDoc, 
  updateDoc,
  deleteDoc, 
  getDocs, 
  query, 
  orderBy, 
  limit, 
  where 
} from 'firebase/firestore';
import { db } from './config';
import { UserProfile, TEST_FALLBACK_PROFILE } from './auth';

export interface StudySession {
  id: string;
  startTime: number;
  endTime: number;
  durationMinutes: number;
  focusScore?: number;
  subject: string;
  isCompleted: boolean;
}

export interface NoteItem {
  id: string;
  title: string;
  subject: string;
  content: string;
  keyPoints?: string[];
  formulas?: string[];
  flashcardCount?: number;
  sourceUrl?: string;
  createdAt: number;
  updatedAt: number;
}

export interface Flashcard {
  id: string;
  deckId: string;
  frontContent: string;
  backContent: string;
  repetitions: number;
  intervalDays: number;
  easeFactor: number;
  nextReviewDate: number;
  isRemedial?: boolean;
  concept?: string;
}

export interface FlashcardDeck {
  id: string;
  title: string;
  subject: string;
  cardCount: number;
  masteryPercentage: number;
  lastStudiedAt: number;
}

export interface StudyRoom {
  id: string;
  name: string;
  targetExam: string;
  activeMembers: number;
  isPrivate: boolean;
  memberAvatars?: number[];
}

export interface LeaderboardEntry {
  id?: string;
  userId: string;
  userName: string;
  avatarId: number;
  scholarRank: string;
  studyMinutes: number;
  xp: number;
  rank: number;
}

export interface QuovexOriginalBook {
  id: string;
  title: string;
  subtitle: string;
  subject: string;
  exam: string;
  totalChapters: number;
  approvalStatus: string;
  chapters?: any[];
}

export interface StudyPlanTask {
  id: string;
  dayNumber: number;
  dayName: string;
  title: string;
  subject: string;
  durationMinutes: number;
  isCompleted: boolean;
  priority: 'HIGH' | 'MEDIUM' | 'LOW';
  category: string;
}

export interface StudyPlan {
  id: string;
  userId: string;
  title: string;
  targetExam: string;
  dailyGoalHours: number;
  totalWeeks: number;
  status: string;
  tasks: StudyPlanTask[];
  createdAt: number;
  updatedAt: number;
}

export interface QuizMistakeRecord {
  questionText: string;
  studentAnswer: string;
  correctAnswer: string;
  explanation: string;
  concept: string;
}

export interface QuizResultRecord {
  id: string;
  subject: string;
  targetExam: string;
  score: number;
  totalQuestions: number;
  correctCount: number;
  timestamp: number;
  mistakes: QuizMistakeRecord[];
}

export interface AiMessage {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  createdAt: number;
  attachments?: string[];
  contextSnapshot?: Record<string, any>;
}

export interface AiConversation {
  id: string;
  userId: string;
  title: string;
  subject: string;
  topic?: string;
  sourceType: 'AI_TUTOR' | 'IMAGE_DOUBT' | 'NCERT_READER' | 'NOTE_EXPLAINER';
  sourceId?: string;
  createdAt: number;
  updatedAt: number;
  lastMessagePreview?: string;
}

// In-memory / LocalStorage State for Resilient Testing & Offline Mode
const localState: {
  profile: UserProfile;
  sessions: StudySession[];
  notes: NoteItem[];
  decks: FlashcardDeck[];
  cards: Record<string, Flashcard[]>;
  studyPlan: StudyPlan | null;
  quizHistory: QuizResultRecord[];
  conversations: AiConversation[];
  messages: Record<string, AiMessage[]>;
} = {
  profile: { ...TEST_FALLBACK_PROFILE },
  sessions: [],
  notes: [],
  decks: [],
  cards: {},
  studyPlan: null,
  quizHistory: [],
  conversations: [],
  messages: {},
};

// User Profile
export function subscribeToUserProfile(uid: string, callback: (profile: UserProfile | null) => void) {
  try {
    return onSnapshot(
      doc(db, 'users', uid),
      (snapshot) => {
        if (snapshot.exists()) {
          callback({ id: snapshot.id, ...snapshot.data() } as UserProfile);
        } else {
          callback(localState.profile);
        }
      },
      () => {
        callback(localState.profile);
      }
    );
  } catch (_) {
    callback(localState.profile);
    return () => {};
  }
}

export async function updateUserProfile(uid: string, data: Partial<UserProfile>) {
  Object.assign(localState.profile, data);
  try {
    const ref = doc(db, 'users', uid);
    await setDoc(ref, data, { merge: true });
  } catch (_) {}
}

// Sessions
export function subscribeToUserSessions(uid: string, callback: (sessions: StudySession[]) => void) {
  try {
    const q = query(collection(db, 'users', uid, 'sessions'), orderBy('startTime', 'desc'), limit(100));
    return onSnapshot(
      q,
      (snapshot) => {
        const list = snapshot.docs.map(d => ({ id: d.id, ...d.data() } as StudySession));
        callback(list.length > 0 ? list : localState.sessions);
      },
      () => {
        callback(localState.sessions);
      }
    );
  } catch (_) {
    callback(localState.sessions);
    return () => {};
  }
}

export async function saveUserSession(uid: string, session: StudySession) {
  localState.sessions = [session, ...localState.sessions.filter(s => s.id !== session.id)];
  try {
    const ref = doc(db, 'users', uid, 'sessions', session.id);
    await setDoc(ref, session, { merge: true });

    // Increment total focus XP
    const xpEarned = Math.max(10, Math.round(session.durationMinutes * 2) + ((session.focusScore || 0) >= 85 ? 50 : 0));
    localState.profile.xp = (localState.profile.xp || 0) + xpEarned;
    const userRef = doc(db, 'users', uid);
    await updateDoc(userRef, { xp: localState.profile.xp });
  } catch (_) {}
}

// Notes / Learning Materials
export function subscribeToUserNotes(uid: string, callback: (notes: NoteItem[]) => void) {
  try {
    const q = query(collection(db, 'users', uid, 'notes'), orderBy('updatedAt', 'desc'));
    return onSnapshot(
      q,
      (snapshot) => {
        const list = snapshot.docs.map(d => ({ id: d.id, ...d.data() } as NoteItem));
        callback(list.length > 0 ? list : localState.notes);
      },
      () => {
        callback(localState.notes);
      }
    );
  } catch (_) {
    callback(localState.notes);
    return () => {};
  }
}

export async function saveUserNote(uid: string, note: NoteItem) {
  localState.notes = [note, ...localState.notes.filter(n => n.id !== note.id)];
  try {
    const ref = doc(db, 'users', uid, 'notes', note.id);
    await setDoc(ref, note, { merge: true });
  } catch (_) {}
}

export async function deleteUserNote(uid: string, noteId: string) {
  localState.notes = localState.notes.filter(n => n.id !== noteId);
  try {
    await deleteDoc(doc(db, 'users', uid, 'notes', noteId));
  } catch (_) {}
}

// Flashcard Decks & Individual Cards
export function subscribeToFlashcardDecks(uid: string, callback: (decks: FlashcardDeck[]) => void) {
  try {
    const q = query(collection(db, 'users', uid, 'flashcard_decks'));
    return onSnapshot(
      q,
      (snapshot) => {
        const list = snapshot.docs.map(d => ({ id: d.id, ...d.data() } as FlashcardDeck));
        callback(list.length > 0 ? list : localState.decks);
      },
      () => {
        callback(localState.decks);
      }
    );
  } catch (_) {
    callback(localState.decks);
    return () => {};
  }
}

export async function saveFlashcardDeck(uid: string, deck: FlashcardDeck) {
  localState.decks = [deck, ...localState.decks.filter(d => d.id !== deck.id)];
  try {
    const ref = doc(db, 'users', uid, 'flashcard_decks', deck.id);
    await setDoc(ref, deck, { merge: true });
  } catch (_) {}
}

export function subscribeToDeckCards(uid: string, deckId: string, callback: (cards: Flashcard[]) => void) {
  try {
    const q = query(collection(db, 'users', uid, 'flashcard_decks', deckId, 'cards'));
    return onSnapshot(
      q,
      (snapshot) => {
        const list = snapshot.docs.map(d => ({ id: d.id, ...d.data() } as Flashcard));
        callback(list.length > 0 ? list : (localState.cards[deckId] || []));
      },
      () => {
        callback(localState.cards[deckId] || []);
      }
    );
  } catch (_) {
    callback(localState.cards[deckId] || []);
    return () => {};
  }
}

export async function saveFlashcard(uid: string, deckId: string, card: Flashcard) {
  if (!localState.cards[deckId]) localState.cards[deckId] = [];
  localState.cards[deckId] = [card, ...localState.cards[deckId].filter(c => c.id !== card.id)];
  try {
    const ref = doc(db, 'users', uid, 'flashcard_decks', deckId, 'cards', card.id);
    await setDoc(ref, card, { merge: true });
  } catch (_) {}
}

export async function deleteFlashcard(uid: string, deckId: string, cardId: string) {
  if (localState.cards[deckId]) {
    localState.cards[deckId] = localState.cards[deckId].filter(c => c.id !== cardId);
  }
  try {
    await deleteDoc(doc(db, 'users', uid, 'flashcard_decks', deckId, 'cards', cardId));
  } catch (_) {}
}

// Study Rooms
export function subscribeToStudyRooms(callback: (rooms: StudyRoom[]) => void) {
  try {
    const q = query(collection(db, 'study_rooms'), limit(30));
    return onSnapshot(
      q,
      (snapshot) => {
        const list = snapshot.docs.map(d => ({ id: d.id, ...d.data() } as StudyRoom));
        callback(list);
      },
      () => {
        callback([]);
      }
    );
  } catch (_) {
    callback([]);
    return () => {};
  }
}

export async function createStudyRoom(room: StudyRoom) {
  try {
    const ref = doc(db, 'study_rooms', room.id);
    await setDoc(ref, room);
  } catch (_) {}
}

// Weekly Leaderboard
export function subscribeToWeeklyLeaderboard(callback: (entries: LeaderboardEntry[]) => void) {
  try {
    const q = query(collection(db, 'leaderboard_weekly'), orderBy('xp', 'desc'), limit(25));
    return onSnapshot(
      q,
      (snapshot) => {
        const list = snapshot.docs.map((d, index) => ({
          id: d.id,
          rank: index + 1,
          ...d.data(),
        } as LeaderboardEntry));
        callback(list);
      },
      () => {
        callback([]);
      }
    );
  } catch (_) {
    callback([]);
    return () => {};
  }
}

// Quovex Originals (Published Only)
export function subscribeToQuovexOriginals(callback: (books: QuovexOriginalBook[]) => void) {
  try {
    const q = query(collection(db, 'quovex_originals'), where('approvalStatus', '==', 'PUBLISHED'));
    return onSnapshot(
      q,
      (snapshot) => {
        const list = snapshot.docs.map(d => ({ id: d.id, ...d.data() } as QuovexOriginalBook));
        callback(list);
      },
      () => {
        callback([]);
      }
    );
  } catch (_) {
    callback([]);
    return () => {};
  }
}

// Study Plan
export function subscribeToStudyPlan(uid: string, callback: (plan: StudyPlan | null) => void) {
  try {
    const q = query(collection(db, 'users', uid, 'study_plans'), orderBy('updatedAt', 'desc'), limit(1));
    return onSnapshot(
      q,
      (snapshot) => {
        if (!snapshot.empty) {
          callback({ id: snapshot.docs[0].id, ...snapshot.docs[0].data() } as StudyPlan);
        } else {
          callback(localState.studyPlan);
        }
      },
      () => {
        callback(localState.studyPlan);
      }
    );
  } catch (_) {
    callback(localState.studyPlan);
    return () => {};
  }
}

export async function saveStudyPlan(uid: string, plan: StudyPlan) {
  localState.studyPlan = plan;
  try {
    const ref = doc(db, 'users', uid, 'study_plans', plan.id);
    await setDoc(ref, plan, { merge: true });
  } catch (_) {}
}

export async function toggleStudyPlanTask(uid: string, planId: string, taskId: string, isCompleted: boolean) {
  if (localState.studyPlan && localState.studyPlan.tasks) {
    localState.studyPlan.tasks = localState.studyPlan.tasks.map(t => t.id === taskId ? { ...t, isCompleted } : t);
  }
  try {
    const planRef = doc(db, 'users', uid, 'study_plans', planId);
    const snap = await (await import('firebase/firestore')).getDoc(planRef);
    if (snap.exists()) {
      const plan = snap.data() as StudyPlan;
      const updatedTasks = plan.tasks.map(t => t.id === taskId ? { ...t, isCompleted } : t);
      await updateDoc(planRef, { tasks: updatedTasks, updatedAt: Date.now() });
    }
  } catch (_) {}
}

// Diagnostic Quiz Results & History
export function subscribeToQuizHistory(uid: string, callback: (history: QuizResultRecord[]) => void) {
  try {
    const q = query(collection(db, 'users', uid, 'quiz_history'), orderBy('timestamp', 'desc'), limit(20));
    return onSnapshot(
      q,
      (snapshot) => {
        const list = snapshot.docs.map(d => ({ id: d.id, ...d.data() } as QuizResultRecord));
        callback(list.length > 0 ? list : localState.quizHistory);
      },
      () => {
        callback(localState.quizHistory);
      }
    );
  } catch (_) {
    callback(localState.quizHistory);
    return () => {};
  }
}

export async function saveQuizResult(uid: string, record: QuizResultRecord) {
  localState.quizHistory = [record, ...localState.quizHistory];
  try {
    const ref = doc(db, 'users', uid, 'quiz_history', record.id);
    await setDoc(ref, record);
  } catch (_) {}
}

// ── Persistent AI Conversations & Messages (Section 12 of spec) ────────────
export function subscribeToAiConversations(uid: string, callback: (convs: AiConversation[]) => void) {
  try {
    const q = query(
      collection(db, 'users', uid, 'ai_conversations'),
      orderBy('updatedAt', 'desc'),
      limit(30)
    );
    return onSnapshot(
      q,
      (snapshot) => {
        const list = snapshot.docs.map(d => ({ id: d.id, ...d.data() } as AiConversation));
        callback(list.length > 0 ? list : localState.conversations);
      },
      () => {
        callback(localState.conversations);
      }
    );
  } catch (_) {
    callback(localState.conversations);
    return () => {};
  }
}

export function subscribeToAiMessages(
  uid: string,
  conversationId: string,
  callback: (messages: AiMessage[]) => void
) {
  try {
    const q = query(
      collection(db, 'users', uid, 'ai_conversations', conversationId, 'messages'),
      orderBy('createdAt', 'asc'),
      limit(100)
    );
    return onSnapshot(
      q,
      (snapshot) => {
        const list = snapshot.docs.map(d => ({ id: d.id, ...d.data() } as AiMessage));
        callback(list.length > 0 ? list : (localState.messages[conversationId] || []));
      },
      () => {
        callback(localState.messages[conversationId] || []);
      }
    );
  } catch (_) {
    callback(localState.messages[conversationId] || []);
    return () => {};
  }
}

export async function saveAiConversation(uid: string, conv: Partial<AiConversation> & { id: string }) {
  const existingIdx = localState.conversations.findIndex(c => c.id === conv.id);
  const updatedConv = {
    userId: uid,
    title: conv.title || 'Study Discussion',
    subject: conv.subject || 'Physics',
    sourceType: conv.sourceType || 'AI_TUTOR',
    createdAt: conv.createdAt || Date.now(),
    updatedAt: Date.now(),
    lastMessagePreview: conv.lastMessagePreview || '',
    ...conv,
  } as AiConversation;

  if (existingIdx >= 0) {
    localState.conversations[existingIdx] = { ...localState.conversations[existingIdx], ...updatedConv };
  } else {
    localState.conversations = [updatedConv, ...localState.conversations];
  }

  try {
    const ref = doc(db, 'users', uid, 'ai_conversations', conv.id);
    await setDoc(ref, updatedConv, { merge: true });
  } catch (_) {}
}

export async function saveAiMessage(uid: string, conversationId: string, message: AiMessage) {
  if (!localState.messages[conversationId]) localState.messages[conversationId] = [];
  localState.messages[conversationId] = [
    ...localState.messages[conversationId].filter(m => m.id !== message.id),
    message,
  ];

  // Update conversation updatedAt & lastMessagePreview
  const conv = localState.conversations.find(c => c.id === conversationId);
  if (conv) {
    conv.updatedAt = message.createdAt;
    conv.lastMessagePreview = message.content.slice(0, 100);
  }

  try {
    const msgRef = doc(db, 'users', uid, 'ai_conversations', conversationId, 'messages', message.id);
    await setDoc(msgRef, message, { merge: true });

    const convRef = doc(db, 'users', uid, 'ai_conversations', conversationId);
    await updateDoc(convRef, {
      updatedAt: message.createdAt,
      lastMessagePreview: message.content.slice(0, 100),
    });
  } catch (_) {}
}

export async function deleteAiConversation(uid: string, conversationId: string) {
  localState.conversations = localState.conversations.filter(c => c.id !== conversationId);
  delete localState.messages[conversationId];
  try {
    await deleteDoc(doc(db, 'users', uid, 'ai_conversations', conversationId));
  } catch (_) {}
}

// Rescue Token
export async function useRescueToken(uid: string): Promise<boolean> {
  const currentTokens = localState.profile.rescueTokens ?? 1;
  if (currentTokens > 0) {
    localState.profile.rescueTokens = currentTokens - 1;
    localState.profile.streakProtected = true;
    try {
      const userRef = doc(db, 'users', uid);
      await updateDoc(userRef, {
        rescueTokens: currentTokens - 1,
        streakProtected: true,
      });
    } catch (_) {}
    return true;
  }
  return false;
}
