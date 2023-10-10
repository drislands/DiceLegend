package com.islands.games.dicelegend.meta

/**
 * Classes that implement this trait will print in the same way to the same destination.
 */
trait Printable {
    static final boolean DEBUG = true

    static void debug(String msg) {
        if(DEBUG) println "DEBUG | $msg"
    }

    static void print(String msg) {
        PrintManager.managedPrint(msg)
    }

}