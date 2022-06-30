package click.recraft

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import java.util.concurrent.TimeUnit

object CacheData {
    val ipPlayerNameCache = CacheBuilder.newBuilder()
        .maximumSize(1000)
        .expireAfterAccess(1, TimeUnit.HOURS)
        .build(object: CacheLoader<String, String>() {
            override fun load(key: String): String? {
                return getIpPlayerName(key)
            }
        })

    private val ipNames = HashMap<String, String>()
    private fun getIpPlayerName(ipAddress: String): String? {
        return ipNames[ipAddress]
    }
    fun setIpPlayerName(ipAddress: String, playerName: String) {
        ipNames[ipAddress] = playerName
    }
}