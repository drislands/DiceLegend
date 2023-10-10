package com.islands.games.dicelegend

import com.islands.games.dicelegend.moves.Effect
import com.islands.games.dicelegend.moves.Move
import groovy.transform.Canonical

@Canonical
/**
 * The players that participate in a Duel.
 */
class Player {
    // TODO: make these configurable
    static int DEFAULT_HEALTH = 10
    static int DEFAULT_ARMOR = 0
    static int DEFAULT_DAMAGE = 0

    // The amount of damage you can take before being defeated.
    int baseHealth
    // How much damage you always mitigate.
    int baseArmor
    // How much damage you always inflict.
    int baseDamage

    int currentHealth


    String name


    Move chosenMove


    ArrayList<Effect> effects = []

    Player(String name) {
        this.name = name
        baseHealth = DEFAULT_HEALTH
        baseArmor = DEFAULT_ARMOR
        baseDamage = DEFAULT_DAMAGE

        currentHealth = baseHealth
    }

    /////////////////////

    /**
     * Use some fun math to bound the health addition/subtraction to 0 and the {@link Player#baseHealth} stat.
     */
    void addHealth(int add) {
        currentHealth = Math.max(0,Math.min(baseHealth,currentHealth + add))
    }

    /////////////////////

    /**
     * Set relevant stats back to defaults.
     */
    void reset() {
        currentHealth = baseHealth
        chosenMove = null
    }
}
