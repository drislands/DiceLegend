package com.islands.games.dicelegend

import com.islands.games.dicelegend.exceptions.GameException
import com.islands.games.dicelegend.meta.Printable
import com.islands.games.dicelegend.moves.Effect
import com.islands.games.dicelegend.moves.Move

/**
 * The class that handles the game loop, and the storage of information pertinent to the active duel.
 */
class Duel implements Printable {
    // The challenging player and the player challenged, in order.
    static Player player1
    static Player player2

    static gameState = GameState.WAITING

    static Player trainingDummy = new Player("Training Dummy")
    static boolean practiceMode = false

    // Represents the Player that is using the Move currently being evaluated
    static Player activePlayer
    static Player opposingPlayer


    /**
     * Resets the duel back to the default state.
     */
    static void reset() {
        gameState = GameState.WAITING
        player1 = null
        player2 = null
        practiceMode = false
    }

    /**
     * Attempts to set the {@link Player#chosenMove chosenMove} for a given {@link Player}.
     * @return True if the attempt to set the move is valid, false if invalid.
     * @throws GameException Thrown if a move is set to a player not participating in the duel.
     */
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

    /**
     * Attempts to start a duel between two players.
     * @param one The challenging player.
     * @param two The player challenged.
     * @return True if the duel was successfully able to be started, false otherwise.
     */
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

    /**
     * Attempts to start a practice duel between one player and the {@link #trainingDummy}.
     * @param solo The activating player.
     * @return True if the practice duel was successfully able to be started, false otherwise.
     */
    static boolean startDuel(Player solo) {
        if(gameState == GameState.WAITING) {
            player1 = solo
            player2 = trainingDummy

            player1.reset()
            player2.reset()

            gameState = GameState.GETTING_MOVES

            // TODO: Add more training options besides just random moves.
            trainingDummy.chosenMove = RNGesus.randomMove

            practiceMode = true

            return true
        } else {
            return false
        }
    }

    /**
     * Processes the round, as long as each player has chosen their move.
     * @return True if the duel has ended with one or more defeated players, false otherwise.
     */
    static boolean processRound() {
        if(gameState != GameState.READY_TO_PROCESS) {
            throw new GameException("Game state not ready to process round")
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

        if(practiceMode) {
            player2.chosenMove = RNGesus.randomMove
        }

        return false
    }

    /**
     * Determines if the duel is over by way of checking each player's health.
     * @return True if someone is dead, false otherwise.
     */
    static boolean testOver() {
        if(players.find { it.currentHealth == 0 }) {
            gameState = GameState.DUEL_OVER
            true
        } else {
            false
        }
    }


    /**
     * Processes the active player's move, applying all relevant effects first.
     */
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


    /**
     * Checks the {@link Move#getSpeed() speed} of each Player's move to determine who goes first.
     * @return Both players, with the faster player being first in the List.
     */
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

    /**
     * Quick method to get the two players as a list.
     */
    static Player[] getPlayers() {
        [player1,player2]
    }

    /**
     * Swaps which player is active.
     */
    static void swapPlayerOrder() {
        Player temp = activePlayer
        activePlayer = opposingPlayer
        opposingPlayer = temp
    }


    /**
     * Processes all effects that are waiting for the end of the turn.
     */
    static void endOfTurnEffects() {
        debug "Processing end of turn effects..."
        activePlayer = player1
        opposingPlayer = player2
        endOfTurnEffects_byPlayer()

        activePlayer = player2
        opposingPlayer = player1
        endOfTurnEffects_byPlayer()
    }

    /**
     * Processes all effects that are waiting for the end of the turn, for the active player.
     */
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
