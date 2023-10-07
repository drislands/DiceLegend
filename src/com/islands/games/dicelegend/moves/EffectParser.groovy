package com.islands.games.dicelegend.moves

import java.util.regex.Pattern

class EffectParser {
    static Map<Pattern,Closure> PIECES = [:]
    static Map<String,Closure> STAT_MAP = [:]

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

    static {
        // TODO: make this more modular than just checking a single trait
        PIECES[~/WHEN:[^,]+/] = { Effect effect, String piece ->
            def whenString = piece.split('WHEN:')[1]

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
        // TODO: make this more modular than just updating stats of a move
        PIECES[~/THEN:[^,]+/] = { Effect effect, String piece ->
            def thenString = piece.split('THEN:')[1]
            def details = thenString.split(' ')

            def op = details[0]
            parseOperation(effect,op,details)
        }
    }

    static void parseOperation(Effect effect,String operation,details) {
        def action = null
        if(operation in ['ADD','SET','SUB']) {
            def stat = details[1]
            def value = details[2] as int

            def op
            switch (operation){
                case "ADD":
                    op = { int moveStat ->
                        moveStat + value
                    }
                    break
                case "SUB":
                    op = { int moveStat ->
                        moveStat - value
                    }
                    break
                case "SET":
                    op = { int moveStat ->
                        value
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
        }

        effect.actions << action
    }

    static void parseEffectPiece(Effect effect,String piece) {
        def foundPiece = PIECES.find { k, v ->
            piece ==~ k
        }
        foundPiece.value.call(effect,piece)
    }

    static Effect parseEffectText(String input) {
        ArrayList<String> pieces = input.split(',')
        Effect effect = new Effect(pieces[0])

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
