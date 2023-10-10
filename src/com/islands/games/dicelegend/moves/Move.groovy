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
     * @param user The {@link Player} using the Move.
     * @param foe The opposing Player to whom damage will be done, presumably.
     */
    void process(Player user,Player foe) {
        print "$user.name used $name!"
        if(!attack) {
            if(traits.contains(Trait.STACKING))
                user.effects.addAll(effects)
            else {
                if(!(user.effects.find { it.name in effects*.name }))
                    user.effects.addAll(effects)
            }
        }
        def hits = autoHits
        def heal = -selfDamage
        def damage

        hits += RNGesus.rollHits(hitDice)
        if(autoHits) {
            print "> Rolled $hitDice dice, plus $autoHits auto-hits! Total of $hits hits at $damagePerHit per hit!"
        } else {
            if(hits) {
                print "> Rolled $hitDice dice! Total of $hits hits at $damagePerHit per hit!"
            } else if (hits && finalDamageMod < 1) {
                print "> Rolled $hitDice dice, but all missed!"
            }
        }

        damage = hits * damagePerHit
        damage += finalDamageMod

        heal += (hits * healPerHit)

        debug "> SELF:"
        if(heal) {
            if(heal > 0) print ">> Healing self for $heal!"
            else print ">> Ouch! Hurt self for $heal!"
        }
        debug "> FOE:"
        if(damage) {
            print ">> Struck $foe.name for $damage!"
        } else {
            if(attack)
                print ">> No damage this time :/"
        }
        user.addHealth(heal)
        foe.addHealth(Math.min(0,-damage)) // Make sure that damage mitigation doesn't heal the foe
    }

    /**
     * Quick method to determine if the Move is an attack.
     */
    boolean isAttack() {
        traits.contains(Trait.ATTACK)
    }

    /**
     * Convert the speed of the Move into an integer for easy comparison, as determined by what {@link Trait}s it has.
     * @return 2 if fast, 1 is normal, 0 if slow.
     */
    int getSpeed() {
        if(traits.contains(Trait.FAST)) {
            debug "> $name: Fast move!"
            2
        } else if (traits.contains(Trait.SLOW)) {
            debug "> $name: Slow move..."
            0
        } else {
            1
        }
    }

    /**
     * Copy the Move into a new object, modified by the actions of the triggering {@link Effect}.
     * @param updates The triggering Effect's {@link Effect#actions}.
     * @return A copy of the original Move, modified as described.
     */
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
