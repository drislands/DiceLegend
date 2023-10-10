package com.islands.games.dicelegend

import com.islands.games.dicelegend.meta.Printable
import com.islands.games.dicelegend.moves.Effect
import com.islands.games.dicelegend.moves.Move
import com.islands.games.dicelegend.moves.MoveParser

class Tester implements Printable {
    static void main(args) {
        MoveParser.parseMoveList('moves.txt')

        //'test Bob and Dingus for one rocket punch each'()
        //'test Bob with rocket punch and Dingus with rock throw'()
        //'test Bob with 3 clump earth and then rock throw versus Dingus with fire fists then rocket punch'()
        repl()
    }

    // The Big One

    static String read() {
        System.in.newReader().readLine()
    }

    static void repl() {
        String input
        print "What is player 1's name? "
        input = read()
        Duel.player1 = new Player(input)
        print "What is player 2's name? "
        input = read()
        Duel.player2 = new Player(input)


        def pick = 1
        do {
            if(pick == 1) {
                print "What move will $Duel.player1.name do? > "
                input = read()
                Duel.player1.chosenMove = MoveParser.getMove(input)
                pick = 2
            } else if(pick == 2){
                print "What move will $Duel.player2.name do? > "
                input = read()
                Duel.player2.chosenMove = MoveParser.getMove(input)
                pick = 3
            } else {
                Duel.processRound()
                print "Active effects:"
                print "> $Duel.player1.name:"
                Duel.player1.effects.each {
                    print ">> $it.name"
                }
                print "> $Duel.player2.name:"
                Duel.player2.effects.each {
                    print ">> $it.name"
                }
                pick = 1
                if(Duel.player1.currentHealth == 0) {
                    println "$Duel.player1.name died!"
                    input = 'Q'
                }
                if(Duel.player2.currentHealth == 0) {
                    println "$Duel.player2.name died!"
                    input = 'Q'
                }
                println "Next turn commencing!"
            }
        } while(input.toUpperCase() != 'Q')

    }

    // Duel tests

    static void 'test Bob with 3 clump earth and then rock throw versus Dingus with fire fists then rocket punch'() {
        def bob = new Player("Bob")
        def dingus = new Player("Dingus")

        def clump = MoveParser.getMove("Clump Earth")
        def rock = MoveParser.getMove("Rock Throw")
        def fists = MoveParser.getMove("Fire Fists")
        def punch = MoveParser.getMove("Rocket Punch")

        3.times {
            clump.process(bob,dingus)
        }
        fists.process(dingus,bob)
        bob.chosenMove = rock
        dingus.chosenMove = punch

        setPlayers(bob,dingus)
        oneRound(bob,dingus)
    }

    static void 'test Bob and Dingus for one rocket punch each'() {
        twoMoveTest("Rocket Punch","Rocket Punch")
    }

    static void 'test Bob with rocket punch and Dingus with rock throw'() {
        twoMoveTest("Rocket Punch","Rock Throw")
    }

    static void twoMoveTest(String move1, String move2) {
        def bob = new Player("Bob")
        def dingus = new Player("Dingus")

        def m1 = MoveParser.getMove(move1)
        def m2 = MoveParser.getMove(move2)
        print "Bob using $m1"
        print "Dingus using $m2"
        bob.chosenMove = m1
        dingus.chosenMove = m2

        setPlayers(bob,dingus)
        oneRound(bob,dingus)
    }

    static void setPlayers(Player one,Player two) {
        Duel.player1 = one
        Duel.player2 = two
    }

    static void oneRound(Player one,Player two) {

        print "Starting health:"
        print "> $one.currentHealth"
        print "> $two.currentHealth"
        Duel.processRound()
        print "Resulting health:"
        print "> $one.currentHealth"
        print "> $two.currentHealth"
    }

    // Move Parser tests

    static void "test rocket punch and fire fists"() {
        def rocketPunch = MoveParser.getMove("Rocket Punch")
        println rocketPunch

        def fireFists = MoveParser.getMove("Fire Fists")
        println "Is the effect of FF valid when choosing Rocket Punch?"
        def valid = fireFists.effects.first().valid(rocketPunch)
        println "> ${valid?"Yes!":"No..."}"
        if(valid) println fireFists.effects.first().trigger(rocketPunch)
    }

    static void "test gale strike and air acceleration"() {
        def galeStrike = MoveParser.getMove('Gale Strike')
        println galeStrike

        Move airAccel = MoveParser.getMove('Air Acceleration')
        println "Is the effect if AA valid when choosing Gale Strike?"
        def valid = airAccel.effects.first().valid(galeStrike)
        println "> ${valid?"Yes!":"No..."}"
        if(valid) println airAccel.effects.first().trigger(galeStrike)
    }

    static void "test flame wall and rocket punch"() {
        def attack = MoveParser.getMove('Rocket Punch')
        println attack

        def wall = MoveParser.getMove("Flame Wall")
        println "Are the two effects of FW valid when defending Rocket Punch?"
        wall.effects.each {
            def valid = it.valid(attack)
            println "> ${valid?"Yes!":"No..."}"
            if(valid) println it.trigger(attack)
        }
    }

    static void "test flame wall and rock throw"() {
        def attack = MoveParser.getMove('Rock Throw')
        println attack

        def wall = MoveParser.getMove("Flame Wall")
        println "Are the two effects of FW valid when defending Rocket Punch?"
        wall.effects.each {
            def valid = it.valid(attack)
            println "> ${valid?"Yes!":"No..."}"
            if(valid) println it.trigger(attack)
        }

    }

    static void "test flame wall and water jet"() {
        def attack = MoveParser.getMove('Water Jet')
        println attack

        def wall = MoveParser.getMove("Flame Wall")
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

        def attack = MoveParser.getMove('Water Jet')
        println attack

        def wall = MoveParser.getMove('Stone Wall')
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
        def attack = MoveParser.getMove('Rocket Punch')
        println attack

        def wall = MoveParser.getMove("Ice Wall")
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

        def wall = MoveParser.getMove("Ice Wall")
        testPlayer.chosenMove = wall
        testDummy.chosenMove = MoveParser.getMove("Rocket Punch")
        testPlayer.effects << wall.effects.find { it.name == "IceWallHeal" }

        println "Dummy is doing $testDummy.chosenMove.name!"

        println "Bob's health before end of turn: $testPlayer.currentHealth"
        Duel.endOfTurnEffects()
        println "Bob's health at end of turn: $testPlayer.currentHealth"
    }
}
