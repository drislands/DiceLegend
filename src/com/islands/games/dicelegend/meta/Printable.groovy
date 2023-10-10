package com.islands.games.dicelegend.meta

trait Printable {
    static final boolean DEBUG = true

    static void debug(String msg) {
        if(DEBUG) debugOp.call(msg)
    }

    static void print(String msg) {
        printOp.call(msg)
    }

    /////////////////////////////

    static Closure printOp = { String msg ->
        println msg
    }

    static Closure debugOp = { String msg ->
        println "DEBUG | $msg"
    }
}