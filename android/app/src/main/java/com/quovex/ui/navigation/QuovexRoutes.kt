package com.quovex.ui.navigation

sealed class QuovexRoute(val route: String) {
    object Auth : QuovexRoute("auth")
    object Onboarding : QuovexRoute("onboarding")
    object Dashboard : QuovexRoute("dashboard")
    object Timer : QuovexRoute("timer")
    object Community : QuovexRoute("community")
    object Profile : QuovexRoute("profile")
    object AiChat : QuovexRoute("ai_chat?subject={subject}&topic={topic}&prompt={prompt}") {
        fun createRoute(subject: String = "", topic: String = "", prompt: String = ""): String {
            val encodedPrompt = java.net.URLEncoder.encode(prompt, "UTF-8")
            val encodedTopic = java.net.URLEncoder.encode(topic, "UTF-8")
            val encodedSubject = java.net.URLEncoder.encode(subject, "UTF-8")
            return "ai_chat?subject=$encodedSubject&topic=$encodedTopic&prompt=$encodedPrompt"
        }
    }
    object DocumentScanner : QuovexRoute("document_scanner")
    object ImageDoubt : QuovexRoute("image_doubt")

    // Knowledge Hub & Learning Materials
    object KnowledgeHub : QuovexRoute("knowledge_hub")
    object AddMaterial : QuovexRoute("add_material")
    object ImportUrl : QuovexRoute("import_url")
    object MaterialDetail : QuovexRoute("material_detail/{materialId}") {
        fun createRoute(materialId: Long) = "material_detail/$materialId"
    }

    // NCERT Official Resource Library
    object NcertBrowser : QuovexRoute("ncert_browser")
    object NcertBookDetail : QuovexRoute("ncert_book_detail/{bookId}") {
        fun createRoute(bookId: String) = "ncert_book_detail/$bookId"
    }
    object NcertChapterDetail : QuovexRoute("ncert_chapter_detail/{chapterId}") {
        fun createRoute(chapterId: String) = "ncert_chapter_detail/$chapterId"
    }
    object NcertPdfReader : QuovexRoute("ncert_pdf_reader/{chapterId}") {
        fun createRoute(chapterId: String) = "ncert_pdf_reader/$chapterId"
    }

    // Quovex Originals Educational Catalog
    object OriginalsBrowser : QuovexRoute("originals_browser")
    object OriginalBookDetail : QuovexRoute("original_book_detail/{bookId}") {
        fun createRoute(bookId: String) = "original_book_detail/$bookId"
    }
    object OriginalChapterReader : QuovexRoute("original_chapter_reader/{bookId}/{chapterNumber}") {
        fun createRoute(bookId: String, chapterNumber: Int) = "original_chapter_reader/$bookId/$chapterNumber"
    }

    // Practice Quiz
    object Quiz : QuovexRoute("quiz/{materialId}") {
        fun createRoute(materialId: Long) = "quiz/$materialId"
    }

    // Flashcards & Decks
    object DeckOverview : QuovexRoute("deck_overview/{deckId}") {
        fun createRoute(deckId: Int) = "deck_overview/$deckId"
    }

    object FlashcardPlayer : QuovexRoute("flashcard_player/{deckId}?reviewAll={reviewAll}") {
        fun createRoute(deckId: Int, reviewAll: Boolean = false) =
            "flashcard_player/$deckId?reviewAll=$reviewAll"
    }

    // Backward-compatible aliases
    object Library : QuovexRoute("knowledge_hub")
    object NoteDetail : QuovexRoute("material_detail/{noteId}") {
        fun createRoute(noteId: Long) = "material_detail/$noteId"
    }

    // Community — Live Study Room
    object StudyRoomLive : QuovexRoute("study_room_live/{roomId}") {
        fun createRoute(roomId: String) = "study_room_live/$roomId"
    }

    // AI Study Planner & Exam Schedule Engine
    object StudyPlanner : QuovexRoute("study_planner")

    // Google Play Billing & Premium Subscription Paywall
    object PremiumPaywall : QuovexRoute("premium_paywall")

    // Performance Analytics & Insights Center
    object Analytics : QuovexRoute("analytics")

    // Daily Diagnostic Quiz & Remedial Mastery
    object DailyDiagnosticQuiz : QuovexRoute("daily_diagnostic_quiz")

    // Streak Protection & Resilience Cemetery
    object Streak : QuovexRoute("streak")

    // Distraction Blocker & App Restriction Shield Settings
    object DistractionBlocker : QuovexRoute("distraction_blocker")
}

