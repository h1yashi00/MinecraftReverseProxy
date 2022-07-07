package click.recraft.check

import com.google.common.base.Ticker
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger


class ProxyThrottle(
    private val ticker: Ticker = Ticker.systemTicker(),
    private val throttleTime: Int,
    private val throttleLimit: Int,
){
    private val throttle = CacheBuilder.newBuilder()
        .ticker(ticker)
        .concurrencyLevel(Runtime.getRuntime().availableProcessors())
        .initialCapacity(100)
        .expireAfterWrite(throttleTime.toLong(), TimeUnit.MILLISECONDS)
        .build(object : CacheLoader<InetAddress, AtomicInteger>() {
            override fun load(key: InetAddress): AtomicInteger {
                return AtomicInteger()
            }
        })


    fun decThrottle(socketAddress: SocketAddress?) {
        if (socketAddress !is InetSocketAddress) {
            return
        }
        val address = socketAddress.address
        val throttleCount = throttle.getIfPresent(address)
        throttleCount?.decrementAndGet()
    }

    fun incThrottle(socketAddress: SocketAddress?): Boolean {
        if (socketAddress !is InetSocketAddress) {
            return false
        }
        val address = socketAddress.address
        val throttleCount = throttle.getUnchecked(address).incrementAndGet()
        return throttleCount > throttleLimit
    }
}