package click.recraft.cache

import click.recraft.objective.UserName
import click.recraft.server.URLRequest
import com.google.common.base.Ticker
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import java.net.URL
import java.util.concurrent.TimeUnit

class PlayerUUIDCache (
    private val ticker: Ticker = Ticker.systemTicker(),
    private val expireTime: Int,
)
{
    private val playerNameCache = CacheBuilder.newBuilder()
        .ticker(ticker)
        .concurrencyLevel(Runtime.getRuntime().availableProcessors())
        .initialCapacity(100)
        .expireAfterWrite(expireTime.toLong(), TimeUnit.MILLISECONDS)
        .build(object: CacheLoader<UserName, String>(){
            override fun load(user: UserName): String {
                return getPlayerUUID(user);
            }

            private fun getPlayerUUID(user: UserName): String {
                val urlProfile = URL("https://api.mojang.com/users/profiles/minecraft/${user.name}")
                val jsonProfile = URLRequest.requestURL(urlProfile)
                return jsonProfile?.get("id").toString()
            }
        })
    fun isCached(user: UserName): String? {
        return playerNameCache.getIfPresent(user)
    }

    fun getUUID(user: UserName): String {
        return playerNameCache.get(user)
    }
}