package com.quovex.data.local.entity

/**
 * Room projection data class used for efficient aggregated deck statistics.
 *
 * This is NOT a @Entity — it is a result type for a multi-table SQL query.
 * It lives in the data layer only and must never be exposed to domain or presentation.
 *
 * The SQL query computes dueCards, masteredCards in a single pass using conditional aggregation,
 * eliminating N+1 per-deck queries.
 */
data class DeckStatsProjection(
    val deckId: Int,
    val title: String,
    val subject: String,
    val xpValue: Int,
    val totalCards: Int,
    val dueCards: Int,
    val masteredCards: Int
)
