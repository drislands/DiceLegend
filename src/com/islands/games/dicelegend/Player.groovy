package com.islands.games.dicelegend

import com.islands.games.dicelegend.moves.Effect

class Player {
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


    ArrayList<Effect> effects = []

    Player(String name) {
        this.name = name
        baseHealth = DEFAULT_HEALTH
        baseArmor = DEFAULT_ARMOR
        baseDamage = DEFAULT_DAMAGE
    }

    void addHealth(int add) {
        currentHealth = Math.min(baseHealth,currentHealth + add)
    }
}
