package com.islands.games.dicelegend.meta

trait Printable {
    static final boolean DEBUG = true

    static void debug(String msg) {
        if(DEBUG) println "DEBUG | $msg"
    }

    static void print(String msg) {
        PrintManager.managedPrint(msg)
    }

}