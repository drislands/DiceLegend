package com.islands.games.dicelegend.meta

/**
 * With {@link Printable}, allows classes to print in the same way without having to update method calls everywhere.
 */
class PrintManager {
    // The default print operation. Can be swapped for other code to allow for other printing.
    static Closure printOperation = { String msg ->
        println msg
    }

    static void managedPrint(String msg) {
        printOperation(msg)
    }
}
