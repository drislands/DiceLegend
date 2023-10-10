package com.islands.games.dicelegend

import com.islands.games.dicelegend.meta.Printable

class RNGesus implements Printable {
    static Random randomizer = new Random()
    static int DIE_FACES = 10
    static final int DEFAULT_THRESHOLD = 6
    static final int TESTING_THRESHOLD = 1
    static int HIT_THRESHOLD = TESTING_THRESHOLD

    static boolean flipCoin() {
        randomizer.nextBoolean()
    }

    static int rollDie() {
        randomizer.nextInt(DIE_FACES) + 1
    }

    static boolean rollForHit() {
        def result = rollDie()
        result >= HIT_THRESHOLD
    }

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

}
