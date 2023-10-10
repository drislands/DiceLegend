package com.islands.games.dicelegend.exceptions

/**
 * Exception thrown when an issue with game state comes up.
 */
class GameException extends Exception {
    GameException(String msg) {
        super(msg)
    }
    GameException(String msg,Throwable cause) {
        super(msg,cause)
    }
}
