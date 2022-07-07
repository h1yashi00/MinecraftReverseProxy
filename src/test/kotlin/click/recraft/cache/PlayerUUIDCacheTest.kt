package click.recraft.cache

import click.recraft.objective.UserName
import click.recraft.tesutil.TimeCheck
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class PlayerUUIDCacheTest {
    @Test
    fun expireTest() {
        val playerUUIDCache= PlayerUUIDCache(expireTime = 10) // 10ms
        val user = UserName("Narikake")
        // takes 300~400ms
        val timecheck = TimeCheck().apply { start() }
        assertEquals("14687d5d039949dbb3023cce4f47bc86", playerUUIDCache.getUUID(user))
        timecheck.fin()

        // cached 0~10ms
        val timecheck2 = TimeCheck().apply { start() }
        assertEquals("14687d5d039949dbb3023cce4f47bc86", playerUUIDCache.getUUID(user))
        timecheck2.fin()

        // expire the cache
        Thread.sleep(20)
        assertNull(playerUUIDCache.isCached(user))

        // cache was removed takes 300~400ms
        val timecheck3 = TimeCheck().apply { start() }
        assertEquals("14687d5d039949dbb3023cce4f47bc86", playerUUIDCache.getUUID(user))
        timecheck3.fin()
    }

    @Test
    fun playerUUidCacheTest() {
        val playerUUIDCache= PlayerUUIDCache(expireTime = 1000) // 1 sec
        val user = UserName("Narikake")
        assertEquals("14687d5d039949dbb3023cce4f47bc86", playerUUIDCache.getUUID(user))

        val nullUser = UserName("fdsjfieuwoir09")
        assertEquals("null", playerUUIDCache.getUUID(nullUser))

        val notch = UserName("notch")
        assertEquals("069a79f444e94726a5befca90e38aaf5", playerUUIDCache.getUUID(notch))
    }
}