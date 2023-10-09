package com.islands.games.dicelegend.exceptions

class GameException extends Exception {
    GameException(String msg) {
        super(msg)
    }
    GameException(String msg,Throwable cause) {
        super(msg,cause)
    }
}
