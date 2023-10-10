package com.islands.games.dicelegend.moves

/**
 * Qualities that a {@link Move} can possess.
 */
enum Trait {
    ATTACK,EFFECT,TRAP, // All Moves must be one of these
    CONTACT,RANGED, // ATTACK only
    STACKING,PERSISTENT, // EFFECT only
    FAST,SLOW, // A Move may be one of these; FAST overrides SLOW
    FIRE,EARTH,WATER,AIR // All Moves must be one of these

    /**
     * Quick method to get a Trait by name.
     * @param name The name of the Trait, case-sensitive.
     * @return The found Trait.
     */
    static Trait get(String name) {
        values().find {
            it.name() == name
        }
    }
}