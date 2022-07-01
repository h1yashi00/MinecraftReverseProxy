package click.recraft.check

import com.google.common.base.Ticker
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.net.InetAddress
import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit


internal class ProxyThrottleTest {
    private class FixTicker: Ticker() {
        var value = 0.toLong()
        override fun read(): Long {
            return value
        }
    }
    @Test
    fun throttleTest() {
        val ticker = FixTicker()
        val throttle = ProxyThrottle(ticker, 10, 3)
        val address = InetSocketAddress(InetAddress.getLocalHost(), 0)
        assertFalse(throttle.incThrottle(address)) // 1
        assertFalse(throttle.incThrottle(address)) // 2
        assertFalse(throttle.incThrottle(address)) // 3
        assertTrue(throttle.incThrottle(address))  // 4 > throttleLimit

        throttle.decThrottle(address)
        throttle.decThrottle(address)
        assertFalse(throttle.incThrottle(address)) // 2
        assertTrue(throttle.incThrottle(address)) // 3

        ticker.value += TimeUnit.MILLISECONDS.toNanos( 50 );// now expire the throttle. throttleCount is 0
        assertFalse(throttle.incThrottle(address))  // expect 1
    }
}