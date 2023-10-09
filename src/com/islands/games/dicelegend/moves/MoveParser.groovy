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
            def effect = EffectParser.parseEffectText(
                    // Stripping out the "EFFECT=" part
                    piece.split('=')[1]
            )
            effect.trap = move.traits.contains(Trait.TRAP)
            move.effects << effect
            return
        }
        if(piece.startsWith("END=")) {
            def effect =  EffectParser.parseEffectText(
                    piece.split('=')[1],true
            )
            effect.trap = move.traits.contains(Trait.TRAP)
            move.effects << effect
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


        println getMove("Ice Wall")
    }

}
