package com.islands.games.dicelegend.moves

import groovy.transform.Canonical

/**
 * The individual techniques that players can use.
 */
@Canonical
class Move {
    ArrayList<Trait> traits = []

    int hitDice        = 0
    int autoHits       = 0
    int damagePerHit   = 0
    int selfDamage     = 0
    int healPerHit     = 0
    int finalDamageMod = 0

    ArrayList<Effect> effects = []

    String name

    Move(String name) {
        this.name = name
    }

    Move clone(updates) {
        def clone = new Move(this.name)
        clone.traits = traits
        clone.hitDice = hitDice
        clone.autoHits = autoHits
        clone.damagePerHit = damagePerHit
        clone.selfDamage = selfDamage
        clone.healPerHit = healPerHit
        clone.effects = effects

        updates.each {
            it(clone)
        }

        clone
    }
}
