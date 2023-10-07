package com.islands.games.dicelegend.moves

import groovy.transform.Canonical

@Canonical
class Effect {
    String name
    boolean oneTurn
    boolean stackable

    def conditions = []
    def actions = []

    Effect(String name) {
        this.name = name
    }

    boolean valid(Move move) {
        def valid = true
        conditions.each {
            if(!it(move)) {
                valid = false
            }
        }
        valid
    }

    Move trigger(Move move) {
        move.clone(actions)
    }
}
