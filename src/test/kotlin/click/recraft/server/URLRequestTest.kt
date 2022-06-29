package click.recraft.server

import click.recraft.objective.UserName
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class URLRequestTest {
    // migration cape + exist player
    @Test
    fun checkURLRequestTestValid() {
        assertTrue(URLRequest.checkPlayer(UserName("Narikake")))
    }
    // exist player + no migraiont cape
    @Test
    fun checkURLRequestTestInvalid1() {
        assertFalse(URLRequest.checkPlayer(UserName("hankake")))
    }

    // Not exist player
    @Test
    fun checkURLRequestTestInvalid() {
        assertFalse(URLRequest.checkPlayer(UserName("_Narika3343")))
    }
}