package com.islands.games.dicelegend.moves

import com.islands.games.dicelegend.Duel
import com.islands.games.dicelegend.Player

import java.util.regex.Pattern

/**
 * Get tags
 * attack/defend/heal/trigger
 *
 */
class MoveParser {
    static List<Move> moves = []
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
        PIECES[~/SELF=\d+/] = { Move move, String piece ->
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
        PIECES[~/TOTAL=\d+/] = { Move move, String piece ->
            def self = piece.split('=')[1]
            move.finalDamageMod = self as int
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
            move.effects << EffectParser.parseEffectText(
                    // Stripping out the "EFFECT=" part
                    piece.split('=')[1]
            )
            return
        }
        if(piece.startsWith("END=")) {
            move.effects << EffectParser.parseEffectText(
                    piece.split('=')[1],true
            )
            return
        }
        def foundPiece = PIECES.find { k, v ->
            piece ==~ k
        }
        if(!foundPiece) {
            println "Error! Piece '$piece' doesn't match with any known rules!!!"
            System.exit(1)
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

    static void parseMoveList(String path) {
        def moveFile = new File(path)

        def moveText = moveFile.text

        moveText.eachLine {
            if((!it.startsWith('#')) && it ) {
                println "Parsing one..."
                Move m = parseMoveText(it)
                moves << m
                println "> Successfully added $m.name!"
            }
        }

    }

    static Move getMove(String name) {
        moves.find { it.name == name }
    }

    //////////////////////////////////////

    static void main(args) {
        println "Testing move parsing!"

        parseMoveList('moves.txt')
        println "Total of ${moves.size()} moves parsed!"


        "test ice wall and blessed fountain"()
    }

    static void "test rocket punch and fire fists"() {
        def rocketPunch = getMove("Rocket Punch")
        println rocketPunch

        def fireFists = getMove("Fire Fists")
        println "Is the effect of FF valid when choosing Rocket Punch?"
        def valid = fireFists.effects.first().valid(rocketPunch)
        println "> ${valid?"Yes!":"No..."}"
        if(valid) println fireFists.effects.first().trigger(rocketPunch)
    }

    static void "test gale strike and air acceleration"() {
        def galeStrike = getMove('Gale Strike')
        println galeStrike

        Move airAccel = getMove('Air Acceleration')
        println "Is the effect if AA valid when choosing Gale Strike?"
        def valid = airAccel.effects.first().valid(galeStrike)
        println "> ${valid?"Yes!":"No..."}"
        if(valid) println airAccel.effects.first().trigger(galeStrike)
    }

    static void "test flame wall and rocket punch"() {
        def attack = getMove('Rocket Punch')
        println attack

        def wall = getMove("Flame Wall")
        println "Are the two effects of FW valid when defending Rocket Punch?"
        wall.effects.each {
            def valid = it.valid(attack)
            println "> ${valid?"Yes!":"No..."}"
            if(valid) println it.trigger(attack)
        }
    }

    static void "test flame wall and rock throw"() {
        def attack = getMove('Rock Throw')
        println attack

        def wall = getMove("Flame Wall")
        println "Are the two effects of FW valid when defending Rocket Punch?"
        wall.effects.each {
            def valid = it.valid(attack)
            println "> ${valid?"Yes!":"No..."}"
            if(valid) println it.trigger(attack)
        }

    }

    static void "test flame wall and water jet"() {
        def attack = getMove('Water Jet')
        println attack

        def wall = getMove("Flame Wall")
        println "Are the two effects of FW valid when defending Rocket Punch?"
        wall.effects.each {
            def valid = it.valid(attack)
            println "> ${valid?"Yes!":"No..."}"
            if(valid)
                println it.trigger(attack)
        }

    }

    static void "test stone wall with water jet and a clump earth"() {
        Player testPlayer = new Player("Bob")
        Duel.activePlayer = testPlayer

        def attack = getMove('Water Jet')
        println attack

        def wall = getMove('Stone Wall')
        /*
        println "Does the mitigation effects of SW work on Water Jet?"
        wall.effects.each {
            def valid = it.valid(attack)
            println "> ${valid?"Yes!":"No..."}"
            if(valid)
                println it.trigger(attack)
        }
         */

        testPlayer.effects << new Effect('Clump Earth')
        testPlayer.effects << new Effect('Clump Earth')

        println "Added two blank effects named Clump Earth! Let's see if the mitigation effects change!"
        wall.effects.each {
            def valid = it.valid(attack)
            println "> ${valid?"Yes!":"No..."}"
            if(valid) {
                attack = it.trigger(attack)
                println attack
            }
        }
    }

    static void "test ice wall and rocket punch"() {
        def attack = getMove('Rocket Punch')
        println attack

        def wall = getMove("Ice Wall")
        println "Will damage be mitigated?"
        wall.effects.each {
            if(!it.endTurn) {
                def valid = it.valid(attack)
                println "> ${valid?"Yes!":"No..."}"
                if(valid)
                    println it.trigger(attack)
            }
        }
    }

    static void "test ice wall and blessed fountain"() {
        Player testPlayer = new Player("Bob")
        testPlayer.currentHealth = 5
        Player testDummy = new Player("Dummy")
        Duel.player1 = testPlayer
        Duel.player2 = testDummy

        testPlayer.effects << new Effect("Blessed Fountain")
        testPlayer.effects << new Effect("Blessed Fountain")

        def wall = getMove("Ice Wall")
        Duel.lastMovePlayed1 = wall
        Duel.lastMovePlayed2 = getMove("Rocket Punch")
        testPlayer.effects << wall.effects.find { it.name == "IceWallHeal" }

        println "Dummy is doing $Duel.lastMovePlayed2.name!"

        println "Bob's health before end of turn: $testPlayer.currentHealth"
        Duel.endOfTurnEffects()
        println "Bob's health at end of turn: $testPlayer.currentHealth"
    }
}
