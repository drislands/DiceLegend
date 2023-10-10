package com.islands.games.dicelegend

/**
 * States the game can be in.
 */
enum GameState {
    WAITING(false),
    GETTING_MOVES,
    READY_TO_PROCESS,
    PROCESSING_ROUND,
    DUEL_OVER

    GameState(boolean active) {
        duelActive = active
    }
    GameState() {
        duelActive = true
    }
    boolean duelActive
}