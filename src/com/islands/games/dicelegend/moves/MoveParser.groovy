package com.islands.games.dicelegend.moves

import com.islands.games.dicelegend.Duel
import com.islands.games.dicelegend.Player
import com.islands.games.dicelegend.meta.Printable

import java.util.regex.Pattern

/**
 * Parses text from a {@link Move} definition into a Move object.
 */
class MoveParser implements Printable {
    static List<Move> moves = []
    static Map<Pattern,Closure> PIECES = [:]

    /**
     * Defines the {@link Pattern}s to match against in a Move definition, and the code to execute when found.
     * TRAITS corresponds with all {@link Trait}s the Move will have, while the other pieces set the base values of
     * the generated Move.
     */
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

    /**
     * Reads a clause in a {@link Move} definition and parses it using the {@link MoveParser#PIECES} definitions except
     * when the clause starts with EFFECT or END, triggering the {@link EffectParser} class methods.
     * @param move The ultimate Move to add the piece to.
     * @param piece The clause to parse into an individual piece of the Move.
     */
    static void parseMovePiece(Move move,String piece) {
        if(piece.startsWith("EFFECT=")) {
            def effect = EffectParser.parseEffectText(
                    // Stripping out the "EFFECT=" part
                    piece.split('=')[1]
            )
            effect.trap = move.traits.contains(Trait.TRAP)
            effect.stackable = move.traits.contains(Trait.STACKING)
            move.effects << effect
            return
        }
        if(piece.startsWith("END=")) {
            def effect =  EffectParser.parseEffectText(
                    // Stripping out the "END=" part
                    piece.split('=')[1],true
            )
            effect.trap = move.traits.contains(Trait.TRAP)
            effect.stackable = move.traits.contains(Trait.STACKING)
            move.effects << effect
            return
        }
        def foundPiece = PIECES.find { k, v ->
            piece ==~ k
        }
        if(!foundPiece) {
            println "Error! Piece '$piece' doesn't match with any known rules!!!"
            // Just shut it down right away. There's no point loading up the whole program if any of the moves
            // can't be parsed correctly.
            System.exit(1)
        }
        foundPiece.value.call(move,piece)
    }

    /**
     * Reads a {@link Move} definition and parses it into a {@link Move} object.
     * @param input The text of the definition.
     * @return The generated {@link Move}.
     */
    static Move parseMoveText(String input) {
        ArrayList<String> pieces = input.split(';')
        Move move = new Move(pieces[0])

        pieces[1..-1].each {
            parseMovePiece(move,it)
        }

        move
    }

    /**
     * Reads the text from a file and converts it into {@link Move} objects.
     *
     * Lines starting with # are ignored, as are empty lines.
     * @param path Path to the {@link File} containing the definitions, one per line.
     */
    static void parseMoveList(String path) {
        def moveFile = new File(path)

        def moveText = moveFile.text

        moveText.eachLine {
            if((!it.startsWith('#')) && it ) {
                debug "Parsing one..."
                Move m = parseMoveText(it)
                moves << m
                debug "> Successfully added $m.name!"
            }
        }

    }

    /**
     * Quick method to get an already-parsed {@link Move} from the list.
     * @param name The name of the Move, as defined at creation.
     * @return The found Move.
     */
    static Move getMove(String name) {
        moves.find { it.name == name }
    }

    //////////////////////////////////////

    static void main(args) {
        debug "Testing move parsing!"

        parseMoveList('moves.txt')
        debug "Total of ${moves.size()} moves parsed!"


        println getMove("Ice Wall")
    }

}
