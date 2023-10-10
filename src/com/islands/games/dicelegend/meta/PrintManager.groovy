package com.islands.games.dicelegend.meta

class PrintManager {
    // the default print operation. Can be swapped for other code to allow for other printing.
    static Closure printOperation = { String msg ->
        println msg
    }

    static void managedPrint(String msg) {
        printOperation(msg)
    }
}
