# Quovex — Architecture Map

**Version:** 2.0 | **Date:** 2026-08-22

---

## 1. Full System Architecture

```mermaid
graph TB
    subgraph USER["📱 User's Android Device"]
        APP["Quovex Android App\n(Kotlin + Jetpack Compose)"]
    end

    subgraph FIREBASE["🔥 Firebase Platform"]
        AUTH["Firebase Auth\n(Google Sign-In)"]
        FS["Firestore DB\n(users, materials, rooms,\nplans, leaderboard)"]
        STORAGE["Firebase Storage\n(PDFs, scanned images,\nnote source files)"]
        FCM["Firebase FCM\n(Push Notifications)"]
        CF["Cloud Functions\n(AI Gateway + PDF/URL extraction)"]
        ANALYTICS["Firebase Analytics\n+ Crashlytics"]
    end

    subgraph AI["🤖 AI Providers"]
        GROQ["Groq API\n(4 keys — Round Robin)\ngpt-oss-120b (vision/complex)\ngpt-oss-20b (chat/summarize/quiz)\nqwen3.6-27b (fallback)"]
        CEREBRAS["Cerebras API\n(4 keys — Failover)\ngpt-oss-120b (study plans, 128K ctx)\ngemma-4-31b (vision fallback)"]
    end

    subgraph ADMIN["🖥️ Admin Panel"]
        NEXTJS["Next.js 15 App\n(Vercel — free)"]
        FADMIN["Firebase Admin SDK"]
    end

    subgraph MONETIZE["💰 Monetization"]
        ADMOB["Google AdMob\n(Banner, Interstitial,\nRewarded Ads)"]
        BILLING["Google Play Billing\n(Premium Subscription)"]
    end

    subgraph ONDEVICE["⚙️ On-Device Services"]
        TIMER["TimerForegroundService"]
        BLOCKER["BlockerAccessibilityService"]
        MLKIT["ML Kit\n(OCR — Scan Notes\nFace Detection — Focus)"]
        WORKMANAGER["WorkManager\n(Reminders, Streak, Quotes)"]
    end

    APP -->|"Auth token"| AUTH
    APP -->|"Read/Write data"| FS
    APP -->|"Upload PDFs/Images"| STORAGE
    APP -->|"Register FCM token"| FCM
    APP -->|"All AI requests (JWT auth)"| CF
    APP -->|"Log events"| ANALYTICS
    APP -->|"Show ads"| ADMOB
    APP -->|"Purchase premium"| BILLING

    CF -->|"Key rotation\nRound-Robin"| GROQ
    CF -->|"Failover /\nStudy Planning"| CEREBRAS
    CF -->|"PDF text extraction"| STORAGE
    CF -->|"Update material status"| FS

    NEXTJS -->|"Admin queries"| FADMIN
    FADMIN -->|"Full admin access"| FS
    FADMIN -->|"Send notifications"| FCM
    FADMIN -->|"Manage users"| AUTH

    APP -..->|"Local processing"| TIMER
    APP -..->|"App blocking"| BLOCKER
    APP -..->|"Scan Notes + Focus detect"| MLKIT
    APP -..->|"Scheduled jobs"| WORKMANAGER
```

---

## 2. Learning Pipeline Architecture

```mermaid
flowchart TD
    A([Student imports material]) --> B{Input Method}

    B -->|Scan Notes| S1[CameraX + ML Kit OCR\nOn-device, offline capable]
    B -->|PDF Upload| S2[Upload to Firebase Storage]
    B -->|YouTube URL| S3[Cloud Function\nFetch transcript]
    B -->|Web URL| S4[Cloud Function\nScrape + extract text]
    B -->|Quick Text| S5[Text field\nMax 10K chars]

    S1 --> TX[Extracted text blob]
    S3 --> TX
    S4 --> TX
    S5 --> TX

    S2 --> CF_PDF[Cloud Function:\npdf-parse extraction\nChunk + process]
    CF_PDF --> TX

    TX --> CLASSIFY[POST /ai/classify\nGroq gpt-oss-20b\nSubject + Topic inference]
    CLASSIFY --> CONFIRM[Subject Inference\nConfirmation Screen]
    CONFIRM --> SUMMARIZE[POST /ai/summarize\nGroq gpt-oss-20b\nSummary + Key Points + Formulas]
    SUMMARIZE --> FLASHGEN[Flashcard Generation\nGroq gpt-oss-20b JSON schema]
    FLASHGEN --> QUIZGEN[Quiz Generation\nGroq gpt-oss-20b JSON schema]
    QUIZGEN --> PERSIST[Persist everywhere]

    PERSIST --> FS_NOTE[(Firestore:\nusers/{uid}/notes/{id})]
    PERSIST --> ROOM[(Room DB:\nnotes + flashcards tables)]
    PERSIST --> FSTORE[(Firebase Storage:\noriginal file)]
    PERSIST --> SHOW([Learning Material Detail])
```

---

## 3. Android App — Clean Architecture Layers

```mermaid
graph TB
    subgraph PRESENTATION["🖼️ Presentation Layer\n(Jetpack Compose + ViewModels)"]
        direction LR
        SCREENS["Screens\n~35 Composable screens"]
        VM["ViewModels\nStateFlow + viewModelScope"]
        UISTATE["UiState\nSealed classes"]
    end

    subgraph DOMAIN["⚙️ Domain Layer\n(Pure Kotlin — zero Android deps)"]
        direction LR
        USECASES["Use Cases\nStartSessionUseCase\nSendMessageUseCase\nGeneratePlanUseCase\nDeleteNoteUseCase\nSummarizeNoteUseCase\n..."]
        MODELS["Domain Models\nSession, ChatMessage\nStudyPlan, LearningMaterial\nFlashcard, QuizQuestion\n..."]
        REPOIFACE["Repository Interfaces\nSessionRepository\nAIRepository\nNoteRepository\nFlashcardRepository\n..."]
    end

    subgraph DATA["💾 Data Layer"]
        direction LR
        subgraph LOCAL["Local"]
            ROOM["Room DB\nnotes / sessions / flashcards\nchat_messages / study_plans\nquiz_results"]
            DATASTORE["DataStore\nPreferences"]
        end
        subgraph REMOTE["Remote"]
            RETROFIT["Retrofit\nAI Gateway API Service"]
            FIREBASE_DATA["Firebase SDK\nFirestore / Storage"]
        end
        REPOIMPL["Repository Implementations\nQuovexRepositoryImpl\nAIRepositoryImpl\n..."]
    end

    subgraph DI["💉 DI — Hilt Modules"]
        APPMODULE["AppModule"]
        NETMODULE["NetworkModule"]
        DBMODULE["DatabaseModule"]
        AIMODULE["AIModule"]
    end

    SCREENS --> VM
    VM --> UISTATE
    VM --> USECASES
    USECASES --> MODELS
    USECASES --> REPOIFACE
    REPOIFACE -..->|"implemented by"| REPOIMPL
    REPOIMPL --> ROOM
    REPOIMPL --> DATASTORE
    REPOIMPL --> RETROFIT
    REPOIMPL --> FIREBASE_DATA
    DI -..->|"injects"| REPOIMPL
    DI -..->|"injects"| VM
```

---

## 4. AI Request Flow (Key Rotation)

```mermaid
sequenceDiagram
    participant APP as Android App
    participant CF as Cloud Function (AI Gateway)
    participant KM as Key Manager
    participant GROQ as Groq API
    participant CEREBRAS as Cerebras API
    participant FS as Firestore

    APP->>CF: POST /ai/chat\n{message, subject, topic, materialContext, token}
    CF->>CF: Verify Firebase JWT token
    CF->>FS: Check user AI quota (queries today)
    alt Quota exceeded (free user)
        CF-->>APP: 429 RATE_LIMIT_EXCEEDED
    else Quota OK
        CF->>CF: Build contextual system prompt\n(subject + topic + material + mistakes)
        CF->>KM: Get next available Groq key
        KM-->>CF: groq_key_2 (Round-Robin)
        CF->>GROQ: Chat request with groq_key_2
        alt Groq success
            GROQ-->>CF: Streamed response (SSE)
            CF-->>APP: SSE stream
        else Groq 429 (key hit limit)
            KM->>KM: Mark groq_key_2 as cooldown (60 min)
            KM-->>CF: Next key: groq_key_3
            CF->>GROQ: Retry with groq_key_3
            alt All Groq keys exhausted
                CF->>CEREBRAS: Fallback to Cerebras gpt-oss-120b
                CEREBRAS-->>CF: Response
                CF-->>APP: SSE stream
            end
        end
        CF->>FS: Increment user query count
        CF->>FS: Update key usage counter
    end
```

---

## 5. Processing Locations Map

Where each operation actually runs:

| Operation | Where It Runs | Offline? |
|---|---|---|
| OCR (Scan Notes) | Android — ML Kit on-device | ✅ Yes |
| Face detection (Focus) | Android — ML Kit on-device | ✅ Yes |
| Image compression | Android — Bitmap processing | ✅ Yes |
| Base64 encoding | Android — java.util.Base64 | ✅ Yes |
| SM-2 spaced repetition | Android — Room + domain layer | ✅ Yes |
| Focus timer | Android — TimerForegroundService | ✅ Yes |
| App blocking | Android — AccessibilityService | ✅ Yes |
| Quiz mistake → flashcard | Android — local domain logic | ✅ Yes |
| PDF text extraction | Cloud Function — pdf-parse | ❌ No |
| URL scraping | Cloud Function — readability | ❌ No |
| YouTube transcript | Cloud Function — YouTube API | ❌ No |
| AI classification | Cloud Function → Groq | ❌ No |
| AI summarization | Cloud Function → Groq | ❌ No |
| Flashcard AI generation | Cloud Function → Groq | ❌ No |
| Quiz AI generation | Cloud Function → Groq | ❌ No |
| Image doubt solving | Cloud Function → Groq vision | ❌ No |
| AI chat response | Cloud Function → Groq | ❌ No |
| Study plan generation | Cloud Function → Cerebras | ❌ No |
| Push notifications | Firebase FCM | ❌ No |
| Premium validation | Cloud Function → Play Billing | ❌ No |

---

## 6. Study Session Flow

```mermaid
flowchart TD
    A([User taps Start Session]) --> B[TimerScreen\nSet subject + duration]
    B --> C[TimerForegroundService starts]
    C --> D{Session active}

    D -->|User opens blocked app| E[BlockerAccessibilityService\nDetects app launch]
    E --> F[Show BlockedOverlay]
    F --> G[Log distraction attempt]
    G --> D

    D -->|Focus Detection ON| H{ML Kit\nFace Detection}
    H -->|Eyes closed or looks away| I[Alert: Sound + Flash]
    I --> D
    H -->|Focused| D

    D -->|Timer ends| J[Session Complete!]
    J --> K[SessionSummaryScreen\nDuration + Focus Score\n+ Distractions resisted]
    K --> L[(Save to Firestore)]
    K --> M[(Save to Room DB)]
    K --> N[Update streak + XP\n+ Leaderboard]
    N --> O{In Study Room?}
    O -->|Yes| P[Update room\nparticipant timer]
    O -->|No| Q([Done])
    P --> Q
```

---

## 7. Knowledge Hub — Data Relationships

```mermaid
graph LR
    subgraph SUBJECT["📐 Physics"]
        M1["Learning Material\nNewton's Laws"]
        M2["Learning Material\nThermodynamics"]
    end

    subgraph LINKED1["Linked to Newton's Laws"]
        D1["Flashcard Deck\n24 cards"]
        Q1["Quiz\n5 questions"]
    end

    subgraph LINKED2["Linked to Thermodynamics"]
        D2["Flashcard Deck\n18 cards"]
        Q2["Quiz\n0 questions"]
    end

    M1 --> D1
    M1 --> Q1
    M2 --> D2
    M2 --> Q2

    D1 -->|"SM-2 mistakes"| REM["Remedial Cards\n(auto-added to deck)"]
    Q1 -->|"Wrong answers"| REM
```

---

## 8. Data Storage Map

```mermaid
graph LR
    subgraph CLOUD["☁️ Cloud (Firebase)"]
        U["users/{uid}\nProfile + streak + premium"]
        S["users/{uid}/sessions/\nSession history"]
        N["users/{uid}/notes/\nLearning Material metadata"]
        ST["Firebase Storage\nPDFs + scan images"]
        P["study_plans/{planId}\nAI-generated plans"]
        R["study_rooms/{roomId}\nRoom + participants"]
        L["leaderboard/\nWeekly rankings"]
        CF2["config/\nfeature_flags\nai_key_usage\nexam_catalog"]
    end

    subgraph LOCAL["📱 Local (Room DB + DataStore)"]
        RS["sessions table"]
        SP["study_plans table"]
        CM["chat_messages table"]
        FL["flashcards table\n(SM-2 algorithm)"]
        NC["notes table\n(Learning Material cache)"]
        QR["quiz_results table"]
        DS["DataStore\nstreak + quota + prefs"]
    end

    U <-..->|"sync"| RS
    S <-..->|"sync"| RS
    P <-..->|"sync"| SP
    N -..->|"cache"| NC
```

---

## 9. Auth + Onboarding Flow

```mermaid
flowchart TD
    A([App Launch]) --> B{Firebase Auth\nSession exists?}
    B -->|No| C[WelcomeScreen]
    B -->|Yes| D{Firestore profile\nexists?}
    D -->|Yes| HOME([HomeScreen])
    D -->|No| ON1

    C --> GAUTH[Google Sign-In]
    GAUTH --> E{New user?}
    E -->|Yes| ON1
    E -->|No| HOME

    ON1[Step 1: Personal Setup\nName + Avatar + Grade]
    ON1 --> ON2[Step 2: Exam Setup\nExam type + Date]
    ON2 --> ON3[Step 3: Subjects\n+ Self-assessed level]
    ON3 --> ON4[Step 4: Schedule\nDaily hours + Time preference]
    ON4 --> ON5[Step 5: Permissions\nNotifications + Alarms]
    ON5 --> ON6[Step 6: Notifications\nReminder time]
    ON6 --> ON7[Step 7: Ready Screen\nProfile summary]
    ON7 --> WRITE[(Write to Firestore\nusers/{uid})]
    WRITE --> HOME
```

---

## 10. Student Classification System

```mermaid
flowchart TD
    A([Onboarding complete]) --> B["Layer 1: Stage\nPrimary / Middle /\nCompetitive / College"]
    B --> C["Layer 2: Exam\nJEE / NEET / SAT /\nClass 10 / IELTS..."]
    C --> D["Layer 3: AI Level Quiz\n10 adaptive MCQs\nper subject via Groq"]
    D --> E["Level 1-5 per subject"]

    E --> G["Study Room\nAuto-matched to\nexam-specific room"]
    E --> H["Leaderboard\nFiltered to same\nexam group only"]
    E --> I["AI Chat\nContext-aware of\nexam + level"]
    E --> J["Flashcards\nDifficulty scaled\nto subject level"]
    E --> K["Study Plan\nExtra time for\nweakest subjects"]

    L["Re-assessment\nevery 2 weeks\nbased on quiz scores"] -..->|"Update levels"| E
```

---

## 11. Content Ecosystem & Content Studio Architecture Map

```mermaid
graph TB
    subgraph HUBS["🏛️ Knowledge Hub Ecosystems"]
        OFFICIAL["📚 Official Resources (NCERT)\n(Class 9-12 Hierarchy)\nRead Portal / Study with Quovex AI"]
        ORIGINALS["✦ Quovex Originals\n(Multi-Agent Synthesized Books)\nRead Book / Flashcards / Quiz"]
        MYMAT["📁 My Materials\n(User Imported Scans, PDFs, YouTube)\nSummary / Concepts / Cards / Quiz"]
    end

    subgraph STUDIO["🏭 Content Studio (Admin Panel)"]
        DEMAND["📈 Demand Intelligence\n(Tutor doubts + Quiz mistake clusters)"]
        REQUEST["📝 Admin Book Request\n(Curriculum, Class, Target Time)"]
        RESEARCH["🔍 Research & Evidence Pack\n(Verified facts + Provenance)"]
        DEBATE["⚔️ Multi-Agent Debate\n(Agent A vs Agent B → Synthesis)"]
        WRITING["✍️ Original Writing\n(Pedagogical synthesis + Clean Math)"]
        VALIDATION["🧪 Multi-Tier Validation\n(Fact + Math + Curriculum + Consistency)"]
        REVIEW["👁️ Human Review & Approval\n(Version history v1, v2 → Publish)"]
    end

    subgraph ENGINE["⚡ Quovex Learning Engine"]
        SUMM["Summary & Key Concepts"]
        CARDS["Spaced Repetition (SM-2)"]
        QUIZ["Active Recall Quiz"]
        TUTOR["Contextual Quovex AI Tutor"]
        MASTERY["Mastery Tracker"]
    end

    DEMAND -->|"Admin triggers"| REQUEST
    REQUEST --> RESEARCH
    RESEARCH --> DEBATE
    DEBATE --> WRITING
    WRITING --> VALIDATION
    VALIDATION --> REVIEW
    REVIEW -->|"Admin approves"| ORIGINALS

    OFFICIAL -->|"Study with AI"| ENGINE
    ORIGINALS --> ENGINE
    MYMAT --> ENGINE

    ENGINE --> SUMM
    ENGINE --> CARDS
    ENGINE --> QUIZ
    ENGINE --> TUTOR
    ENGINE --> MASTERY
```

