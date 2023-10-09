package com.islands.games.dicelegend.moves

import com.islands.games.dicelegend.Player
import com.islands.games.dicelegend.RNGesus
import com.islands.games.dicelegend.meta.Printable
import groovy.transform.Canonical

/**
 * The individual techniques that players can use.
 */
@Canonical
class Move implements Printable {
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

    /**
     * Applies damage and healing per the stats of the move.
     * @param user
     * @param foe
     */
    void process(Player user,Player foe) {
        print "Processing move $name, used by $user.name against $foe.name!"
        if(!traits.contains(Trait.ATTACK)) {
            user.effects.addAll(effects)
        }
        def hits = autoHits
        def heal = -selfDamage
        def damage

        hits += RNGesus.rollHits(hitDice)

        damage = hits * damagePerHit
        damage += finalDamageMod

        heal += (hits * healPerHit)

        print "> SELF:"
        if(heal) {
            if(heal > 0) print ">> Healing for $heal!"
            else print ">> Ouch! Hurt for $heal!"
        }
        print "> FOE:"
        if(damage) {
            print ">> Struck for $damage!"
        } else {
            print ">> No damage this time :/"
        }
        user.addHealth(heal)
        foe.addHealth(Math.min(0,-damage)) // Make sure that damage mitigation doesn't heal the foe
    }

    int getSpeed() {
        if(traits.contains(Trait.FAST)) {
            print "Fast move!"
            2
        } else if (traits.contains(Trait.SLOW)) {
            print "Slow move..."
            0
        } else {
            1
        }
    }

    Move clone(updates) {
        def clone = new Move(this.name)
        clone.traits = traits
        clone.hitDice = hitDice
        clone.autoHits = autoHits
        clone.damagePerHit = damagePerHit
        clone.selfDamage = selfDamage
        clone.healPerHit = healPerHit
        clone.finalDamageMod = finalDamageMod
        clone.effects = effects

        updates.each {
            it(clone)
        }

        clone
    }
}
