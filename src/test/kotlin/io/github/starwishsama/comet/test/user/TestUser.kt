package io.github.starwishsama.comet.test.user

import io.github.starwishsama.comet.objects.CometUser
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class TestUser {
    @Test
    fun testCoolDown() {
        val fakeUser = CometUser.quickRegister(1)

        assertTrue("CometUser couldn't have cooldown!") { fakeUser.isNoCoolDown() }

        fakeUser.triggerCommandTime = System.currentTimeMillis() - 6000

        assertTrue("CometUser couldn't have cooldown!") { fakeUser.isNoCoolDown() }

        fakeUser.triggerCommandTime = System.currentTimeMillis()

        assertFalse("CometUser must have cooldown!") { fakeUser.isNoCoolDown() }
    }
}