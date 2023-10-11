package com.islands.games.dicelegend

import com.islands.games.dicelegend.meta.Printable

/**
 * Randomizer class for all random results.
 */
class RNGesus implements Printable {
    static Random randomizer = new Random()
    // TODO: make these configurable
    static int DIE_FACES = 10
    static int HIT_THRESHOLD = 6

    /**
     * Heads or tails, true or false.
     */
    static boolean flipCoin() {
        randomizer.nextBoolean()
    }

    /**
     * Roll a die with the a certain number of sides.
     * @return The result, from 1 to {@link #DIE_FACES}.
     */
    static int rollDie() {
        randomizer.nextInt(DIE_FACES) + 1
    }

    /**
     * Uses the {@link #rollDie()} method along with the {@link #HIT_THRESHOLD} to determine a hit.
     * @return True if the roll meets the threshold, false if not.
     */
    static boolean rollForHit() {
        def result = rollDie()
        result >= HIT_THRESHOLD
    }

    /**
     * Calls {@link #rollForHit()} a number of times and returns how many hit.
     */
    static int rollHits(int numberOfDice) {
        int sum = 0

        debug "Rolling $numberOfDice dice!"
        numberOfDice.times {
            if(rollForHit()) {
                debug "> hit!"
                sum++
            } else {
                debug "> miss..."
            }
        }
        debug "Total of $sum hits!"

        sum
    }

    static void init(ConfigObject conf) {
        DIE_FACES = conf.faces
        HIT_THRESHOLD = conf.hitThreshold
    }

}
