package com.islands.games.dicelegend

import com.islands.games.dicelegend.meta.Printable
import com.islands.games.dicelegend.moves.Effect

class Duel implements Printable {
    static Player player1
    static Player player2

    // Represents the Player that is using the Move currently being evaluated
    static Player activePlayer
    static Player opposingPlayer


    static void processRound() {
        print "Processing a round!"
        (activePlayer,opposingPlayer) = playerOrder
        print "First player is $activePlayer.name."
        processMove()

        swapPlayerOrder()
        processMove()

        endOfTurnEffects()
    }


    static void processMove() {
        List<Effect> applicableEffects = []
        applicableEffects.addAll activePlayer.effects.findAll {
            !it.trap
        }
        applicableEffects.addAll opposingPlayer.effects.findAll {
            it.trap
        }

        applicableEffects.each {
            if(it.valid(activePlayer.chosenMove)) {
                activePlayer.chosenMove = it.trigger(activePlayer.chosenMove)
            }
        }
        activePlayer.chosenMove.process(activePlayer,opposingPlayer)
    }



    static Player[] getPlayerOrder() {
        print "Getting player order."
        if(player1.chosenMove.speed > player2.chosenMove.speed) {
            [player1,player2]
        } else if(player2.chosenMove.speed > player1.chosenMove.speed) {
            [player2,player1]
        } else {
            if(RNGesus.flipCoin()) {
                [player1,player2]
            } else {
                [player2,player1]
            }
        }
    }

    static void swapPlayerOrder() {
        Player temp = activePlayer
        activePlayer = opposingPlayer
        opposingPlayer = temp
    }




    static void endOfTurnEffects() {
        activePlayer = player1
        opposingPlayer = player2
        endOfTurnEffects_byPlayer()

        activePlayer = player2
        opposingPlayer = player1
        endOfTurnEffects_byPlayer()
    }

    static void endOfTurnEffects_byPlayer() {
        def effects = activePlayer.effects.findAll {
            it.endTurn
        }
        if(effects) {
            effects.each {
                if (it.valid(opposingPlayer.chosenMove)) {
                    it.trigger(activePlayer.chosenMove)
                }
            }
            activePlayer.effects.removeAll(effects)
            // Also remove all traps.
            activePlayer.effects.removeIf {
                it.trap
            }
        }
    }
}
