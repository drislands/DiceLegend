package com.islands.games.dicelegend.moves

import java.util.regex.Pattern

/**
 * Get tags
 * attack/defend/heal/trigger
 *
 */
class MoveParser {
    static Map<Pattern,Closure> PIECES = [:]

    static {
        PIECES[~/HIT=\d+/] = { Move move, String piece ->
            def dice = piece.split('=')[1]
            move.hitDice = dice as int
        }
        PIECES[~/DMG=\d+/] = { Move move, String piece ->
            def damage = piece.split('=')[1]
            move.damagePerHit = damage as int
        }
        PIECES[~/SLF=\d+/] = { Move move, String piece ->
            def self = piece.split('=')[1]
            move.selfDamage = self as int
        }
        PIECES[~/AUTO=\d+/] = { Move move, String piece ->
            def self = piece.split('=')[1]
            move.autoHits = self as int
        }
        PIECES[~/HEAL=\d+/] = { Move move, String piece ->
            def self = piece.split('=')[1]
            move.healPerHit = self as int
        }
        PIECES[~/TRAITS=([A-Z]+,)*[A-Z]+/] = { Move move, String piece ->
            def traits = piece.split('=')[1].split(',')
            traits.each {
                move.traits << Trait.get(it)
            }
        }
    }

    static void parseMovePiece(Move move,String piece) {
        if(piece.startsWith("EFFECT=")) {
            move.effect = EffectParser.parseEffectText(
                    // Stripping out the "EFFECT=" part
                    piece.split('=')[1]
            )
            return
        }
        def foundPiece = PIECES.find { k, v ->
            piece ==~ k
        }
        foundPiece.value.call(move,piece)
    }

    static Move parseMoveText(String input) {
        ArrayList<String> pieces = input.split(';')
        Move move = new Move(pieces[0])

        pieces[1..-1].each {
            parseMovePiece(move,it)
        }

        move
    }

    static void main(args) {
        println "Testing move parsing!"


        "test gale strike and air acceleration"()
    }

    static void "test rocket punch and fire fists"() {
        def rocketPunch_STR = "Rocket Punch;HIT=3;DMG=1;TRAITS=CONTACT,FAST,FIRE,ATTACK"
        Move rocketPunch = parseMoveText(rocketPunch_STR)
        println rocketPunch

        def fireFists_STR = "Fire Fists;SLF=1;TRAITS=EFFECT,SLOW;EFFECT=FireFists,WHEN:CONTACT,WHEN:ATTACK,THEN:ADD AUTO 2"
        Move fireFists = parseMoveText(fireFists_STR)
        println "Is the effect of FF valid when choosing Rocket Punch?"
        def valid = fireFists.effect.valid(rocketPunch)
        println "> ${valid?"Yes!":"No..."}"
        println fireFists.effect.trigger(rocketPunch)
    }

    static void "test gale strike and air acceleration"() {
        def galeStrike_STR = "Gale Strike;AUTO=1;DMG=1;TRAITS=CONTACT,AIR,ATTACK"
        Move galeStrike = parseMoveText(galeStrike_STR)
        println galeStrike

        def airAcceleration_STR = "Air Acceleration;TRAITS=EFFECT,SLOW;EFFECT=AirAcceleration,WHEN:ATTACK,THEN:ADD HIT 1,THEN:GAIN FAST"
        Move airAccel = parseMoveText(airAcceleration_STR)
        println "Is the effect if AA valid when choosing Gale Strike?"
        def valid = airAccel.effect.valid(galeStrike)
        println "> ${valid?"Yes!":"No..."}"
        println airAccel.effect.trigger(galeStrike)
    }
}
