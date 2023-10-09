package com.islands.games.dicelegend.meta

trait Printable {
    static final boolean PRINT_ALL = true

    static void print(args) {
        if(PRINT_ALL) println args
    }
}