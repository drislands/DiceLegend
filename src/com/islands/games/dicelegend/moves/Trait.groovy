package com.islands.games.dicelegend.moves

enum Trait {
    ATTACK,EFFECT,
    CONTACT,RANGED, // ATTACK only
    STACKING,PERSISTENT, // EFFECT only
    FAST,SLOW,
    FIRE,EARTH,WATER,AIR

    static Trait get(String name) {
        values().find {
            it.name() == name
        }
    }
}