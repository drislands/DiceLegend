package com.islands.games.dicelegend

import com.islands.games.dicelegend.exceptions.GameException
import com.islands.games.dicelegend.meta.Printable
import com.islands.games.dicelegend.moves.Effect
import com.islands.games.dicelegend.moves.Move

class Duel implements Printable {
    static Player player1
    static Player player2

    static gameState = GameState.WAITING

    // Represents the Player that is using the Move currently being evaluated
    static Player activePlayer
    static Player opposingPlayer


    static void reset() {
        gameState = GameState.WAITING
        player1 = null
        player2 = null
    }

    static boolean setMove(Player player, Move move) {
        if(gameState == GameState.GETTING_MOVES) {
            if(!(player in players)) {
                throw new GameException("Tried to set move for inactive player")
            }
            if(player.chosenMove) {
                return false
            }
            player.chosenMove = move
            if(player1.chosenMove && player2.chosenMove)
                gameState = GameState.READY_TO_PROCESS
            return true
        } else {
            debug "State is currently $gameState"
            return false
        }
    }

    static boolean startDuel(Player one,Player two) {
        if(gameState == GameState.WAITING) {
            player1 = one
            player2 = two

            one.reset()
            two.reset()

            gameState = GameState.GETTING_MOVES

            return true
        } else {
            return false
        }
    }

    static boolean processRound() {
        if(gameState != GameState.READY_TO_PROCESS) {
            throw new GameException("Game state not ready to process round")
        }
        if("Ice Wall" in players*.chosenMove*.name) {
            debug "Debugging."
        }
        gameState = GameState.PROCESSING_ROUND
        debug "Processing a round!"
        (activePlayer,opposingPlayer) = playerOrder
        debug "First player is $activePlayer.name."
        processMove()
        if(testOver()) {
            return true
        }

        swapPlayerOrder()
        processMove()
        if(testOver()) {
            return true
        }

        print "DEBUG: Active effects, before end of turn effects:"
        players.each { p ->
            print "> $p.name"
            p.effects.each { e ->
                print ">> $e.name"
            }
        }
        endOfTurnEffects()
        if(testOver()) {
            return true
        }

        gameState = GameState.GETTING_MOVES
        players.each { it.chosenMove = null }
        return false
    }

    static boolean testOver() {
        if(players.find { it.currentHealth == 0 }) {
            gameState = GameState.DUEL_OVER
            true
        } else {
            false
        }
    }


    static void processMove() {
        List<Effect> applicableEffects = []
        List<Effect> activeRemoval = []
        List<Effect> opposingRemoval = []
        applicableEffects.addAll activePlayer.effects.findAll {
            !it.trap
        }
        applicableEffects.addAll opposingPlayer.effects.findAll {
            it.trap
        }

        applicableEffects.each {
            if(it.valid(activePlayer.chosenMove)) {
                activePlayer.chosenMove = it.trigger(activePlayer.chosenMove)
                if(it.trap && (!it.endTurn)) opposingRemoval << it
                else activeRemoval << it
            }
        }
        activePlayer.effects.removeAll activeRemoval
        opposingPlayer.effects.removeAll opposingRemoval
        activePlayer.chosenMove.process(activePlayer,opposingPlayer)
    }



    static Player[] getPlayerOrder() {
        debug "Getting player order."
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

    static Player[] getPlayers() {
        [player1,player2]
    }

    static void swapPlayerOrder() {
        Player temp = activePlayer
        activePlayer = opposingPlayer
        opposingPlayer = temp
    }




    static void endOfTurnEffects() {
        debug "Processing end of turn effects..."
        activePlayer = player1
        opposingPlayer = player2
        endOfTurnEffects_byPlayer()

        activePlayer = player2
        opposingPlayer = player1
        endOfTurnEffects_byPlayer()
    }

    static void endOfTurnEffects_byPlayer() {
        if(activePlayer.chosenMove.name == 'Ice Wall') {
            debug "Debugging."
        }
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
