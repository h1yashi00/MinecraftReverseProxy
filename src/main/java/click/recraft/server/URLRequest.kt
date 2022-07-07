package click.recraft.server

import click.recraft.cache.PlayerUUIDCache
import click.recraft.objective.UserName
import click.recraft.sql.Database
import io.netty.util.CharsetUtil
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.SocketTimeoutException
import java.net.URL
import java.util.*
import javax.net.ssl.HttpsURLConnection

object URLRequest {
    private val playerUUIDCache = PlayerUUIDCache(expireTime = 1000 * 60 * 60) // 1 hours ((1sec * 60)min * 60)hour
    fun checkPlayer(user: UserName): Boolean {
        val uuid = playerUUIDCache.getUUID(user)
        if (uuid == "null") return false // player was not found Mojang api URL
        if (Database.getPlayer(uuid)) {
            return true
        }
        val urlSessionProfile = URL("https://sessionserver.mojang.com/session/minecraft/profile/$uuid")
        val jsonSessionProfile = requestURL(urlSessionProfile) ?: return false
        val properties = jsonSessionProfile.get("properties").toString().removePrefix("[").removeSuffix("]")
        val capeEncodedBase64 = JSONObject(properties).get("value").toString()
        val data = Base64.getDecoder().decode(capeEncodedBase64);
        val decodedStr = String(data, CharsetUtil.UTF_8)
        val textures = JSONObject(decodedStr).get("textures").toString()

        try {
            val cape     = JSONObject(textures).get("CAPE").toString()
            val capeTextureURL = JSONObject(cape).get("url").toString()
            if (capeTextureURL != "http://textures.minecraft.net/texture/2340c0e03dd24a11b15a8b33c2a7e9e32abb2051b2481d0ba7defd635ca7a933") {
                return false
            }
        } catch (e: Exception) {
            return false
        }

        return true
    }
    fun requestURL(url: URL): JSONObject? {
        val https = url.openConnection() as HttpsURLConnection
        https.requestMethod = "GET"
        https.connectTimeout = 1000
        try {
            https.connect()
        } catch (e: SocketTimeoutException) {
            println(e.message)
            return null
        }

        if (https.inputStream.available() == 0) return null
        val reader = BufferedReader(InputStreamReader(https.inputStream))
        val stringBuilder = StringBuilder()
        reader.readLines().forEach {
            stringBuilder.append(it)
        }
        return JSONObject(stringBuilder.toString() ?: return null)
    }
}