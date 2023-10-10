package com.islands.games.dicelegend.moves

import com.islands.games.dicelegend.Duel
import com.islands.games.dicelegend.exceptions.GameException

import java.util.regex.Pattern

/**
 * Parses text from a the EFFECT/END part of a {@link Move} definition into an {@link Effect}.
 */
class EffectParser {
    static Map<Pattern,Closure> PIECES = [:]
    static Map<String,Closure> STAT_MAP = [:]

    // This map allows for dynamically updating the values of a given move.
    static {
        STAT_MAP['HIT'] = { Move move, Closure operation ->
            move.hitDice = operation(move.hitDice) as int
        }
        STAT_MAP['AUTO'] = { Move move, Closure operation ->
            move.autoHits = operation(move.autoHits) as int
        }
        STAT_MAP['DMG'] = { Move move, Closure operation ->
            move.damagePerHit = operation(move.damagePerHit) as int
        }
        STAT_MAP['SELF'] = { Move move, Closure operation ->
            move.selfDamage = operation(move.selfDamage) as int
        }
        STAT_MAP['HEAL'] = { Move move, Closure operation ->
            move.healPerHit = operation(move.healPerHit) as int
        }
        STAT_MAP['TOTAL'] = { Move move, Closure operation ->
            move.finalDamageMod = operation(move.finalDamageMod) as int
        }
    }

    /**
     * Defines the {@link Pattern}s to match against in an Effect definition, and the code to execute when found.
     */
    static {
        // TODO: make this more modular than just checking a single trait
        /**
         * Checks for a WHEN: clause in an Effect definition. It does one of two things:
         * - When STACKS is the first word, it sets the remainder of the string to be the name of the other effect to
         *   watch for.
         * - Otherwise, the word denotes the {@link Trait} to confirm a checked {@link Move} has.
         */
        PIECES[~/WHEN:[^,]+/] = { Effect effect, String piece ->
            def whenString = piece.split('WHEN:')[1]

            if(whenString.startsWith('STACKS') ) {
                def stackName = whenString.split('STACKS ')[1]
                effect.watchStackName = stackName
            } else {
                def negate = false
                if(whenString.startsWith('!')) {
                    whenString = whenString[1..-1]
                    negate = true
                }
                Trait t = Trait.get(whenString)

                effect.conditions << { Move m ->
                    if(negate)
                        !m.traits.contains(t)
                    else
                        m.traits.contains(t)
                }
            }
        }
        // TODO: make this more modular than just updating stats of a move
        /**
         * Checks for a THEN: clause in an Effect definition. See {@link EffectParser#parseOperation} for details.
         */
        PIECES[~/THEN:[^,]+/] = { Effect effect, String piece ->
            def thenString = piece.split('THEN:')[1]
            def details = thenString.split(' ')

            def op = details[0]
            parseOperation(effect,op,details)
        }
        /**
         * Checks for a SET: clause in an Effect definition. This sets certain variables of the generated Effect to a
         * set value.
         */
        PIECES[~/SET:[^,]+/] = { Effect effect, String piece ->
            def splits = piece.split('SET:')[1].split(' ')
            def traitToSet = splits[0]
            def placeholderValue = splits[1]
            def value = null
            if(placeholderValue == 'TRUE') {
                value = true
            } else if(placeholderValue == 'FALSE') {
                value = false
            } else {
                // TODO: determine if we want to allow any other values?
                throw new GameException("Value of $placeholderValue is not valid for SET clause")
            }

            switch(traitToSet) {
                case 'TRAP':
                    effect.trap = value
                    break
                // TODO: add other traits that could be changed
            }
        }
    }

    /**
     * Used with a THEN: clause in an Effect definition to modify the stats of a checked {@link Move} by generating an
     * action to be evaluated with that Move.
     * @param effect The effect to which the generated operation will belong.
     * @param operation For most clauses, the basic mathematical operation to perform: add, subtract, set.
     *        Can also be GAIN to grant a trait to the Move, or HEALNOW to apply healing to the user.
     * @param details The text from the Effect definition, specifying what stat to modify and by how much.
     */
    static void parseOperation(Effect effect,String operation,details) {
        def action = null
        if(operation in ['ADD','SET','SUB']) {
            def stat = details[1]
            def literalValue = details[2]
            Closure<Integer> value
            // When the word STACKS is used in place of a number, the value we ultimately use with the operation is
            //  determined at time of evaluation -- a count of the instances of the Effect named in the watchStackName
            //  variable, as owned by the activating Player.
            if(literalValue == "STACKS") {
                value = {
                    Duel.activePlayer.effects.findAll {
                        it.name == effect.watchStackName
                    }.size()
                }
            } else {
                value = { literalValue as int }
            }

            def op
            switch (operation){
                case "ADD":
                    op = { int moveStat ->
                        moveStat + value()
                    }
                    break
                case "SUB":
                    op = { int moveStat ->
                        moveStat - value()
                    }
                    break
                case "SET":
                    op = { int moveStat ->
                        value()
                    }
                    break
            }

            action = { Move m ->
                STAT_MAP[stat](m,op)
            }
        } else if(operation == 'GAIN') {
            def addedTrait = Trait.get(details[1])

            action = { Move m ->
                m.traits << addedTrait
            }
        } else if(operation == 'HEALNOW') {
            def literalValue = details[1]
            Closure<Integer> value
            if(literalValue == "STACKS") {
                value = {
                    Duel.activePlayer.effects.findAll {
                        it.name == effect.watchStackName
                    }.size()
                }
            } else {
                value = { literalValue as int }
            }
            action = { Move p ->
                Duel.activePlayer.addHealth(value())
            }
        }

        effect.actions << action
    }

    /**
     * Reads a clause in an {@link Effect} definition and parses it using the {@link EffectParser#PIECES} definitions.
     * @param effect The ultimate Effect to add the piece to.
     * @param piece The clause to parse into an individual piece of the Effect.
     */
    static void parseEffectPiece(Effect effect,String piece) {
        def foundPiece = PIECES.find { k, v ->
            piece ==~ k
        }
        foundPiece.value.call(effect,piece)
    }

    /**
     * Reads an {@link Effect} definition and parses it into an Effect object.
     * @param input The text of the definition.
     * @param endTurn Indicates if the Effect is for end-of-turn. Typically not the case.
     * @return The generated {@link Effect}.
     */
    static Effect parseEffectText(String input,boolean endTurn=false) {
        ArrayList<String> pieces = input.split(',')
        Effect effect = new Effect(pieces[0])
        effect.endTurn = endTurn

        pieces[1..-1].each {
            parseEffectPiece(effect,it)
        }

        effect
    }


    static void main(args) {
        println "Testing effect parsing!"

        def fireFists_STR = "EFFECT=FireFists,WHEN:CONTACT,WHEN:ATTACK,THEN:ADD AUTO 2"
        Effect fireFists = parseEffectText(fireFists_STR)
        println fireFists
    }
}
