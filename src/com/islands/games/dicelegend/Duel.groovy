package com.islands.games.dicelegend

import com.islands.games.dicelegend.moves.Move

class Duel {
    static Player player1
     static Move lastMovePlayed1
    static Player player2
     static Move lastMovePlayed2

    // Represents the Player that is using the Move currently being evaluated
    static Player activePlayer







    static void endOfTurnEffects() {
        activePlayer = player1
        endOfTurnEffects_byPlayer(lastMovePlayed1,lastMovePlayed2)

        activePlayer = player2
        endOfTurnEffects_byPlayer(lastMovePlayed2,lastMovePlayed1)
    }

    static void endOfTurnEffects_byPlayer(Move m1,Move m2) {
        def effects = activePlayer.effects.findAll {
            it.endTurn
        }
        if(effects) {
            effects.each {
                if (it.valid(m2)) {
                    it.trigger(m1)
                }
            }
            activePlayer.effects.removeAll(effects)
        }
    }
}
