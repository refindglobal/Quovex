package com.quovex.ui.navigation

sealed class QuovexRoute(val route: String) {
    object Auth : QuovexRoute("auth")
    object Onboarding : QuovexRoute("onboarding")
    object Dashboard : QuovexRoute("dashboard")
    object Timer : QuovexRoute("timer")
    object Library : QuovexRoute("library")
    object Community : QuovexRoute("community")
    object Profile : QuovexRoute("profile")
    object AiChat : QuovexRoute("ai_chat")
    object AiSummarizer : QuovexRoute("ai_summarizer")

    /**
     * Deck Overview — shows stats before the user starts studying.
     * Navigation: Library → DeckOverview → FlashcardPlayer
     */
    object DeckOverview : QuovexRoute("deck_overview/{deckId}") {
        fun createRoute(deckId: Int) = "deck_overview/$deckId"
    }

    /**
     * Flashcard study player.
     * reviewAll=false (default): loads only due cards (nextReviewDate <= now)
     * reviewAll=true: loads all cards in the deck regardless of due state
     */
    object FlashcardPlayer : QuovexRoute("flashcard_player/{deckId}?reviewAll={reviewAll}") {
        fun createRoute(deckId: Int, reviewAll: Boolean = false) =
            "flashcard_player/$deckId?reviewAll=$reviewAll"
    }

    /**
     * Note Detail & Edit screen.
     */
    object NoteDetail : QuovexRoute("note_detail/{noteId}") {
        fun createRoute(noteId: Long) = "note_detail/$noteId"
    }

    /**
     * Document Scanner screen (ML Kit on-device OCR).
     */
    object DocumentScanner : QuovexRoute("document_scanner")

    /**
     * Vision AI Image Doubt Solver screen.
     */
    object ImageDoubt : QuovexRoute("image_doubt")
}
