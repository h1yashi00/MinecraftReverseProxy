package click.recraft.tesutil

import javax.naming.NoInitialContextException

class TimeCheck {
    private var invokeTime: Long = 0
    fun start() {
        if (invokeTime != 0L) {
            throw NoInitialContextException()
        }
        invokeTime = System.currentTimeMillis()
        println("<Start Timer>")
    }
    fun fin() {
        println("<Fin Timer> takes ${(System.currentTimeMillis() - invokeTime)}ms")
        invokeTime = 0
    }
}