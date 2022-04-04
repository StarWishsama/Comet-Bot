package io.github.starwishsama.comet.test.util

import io.github.starwishsama.comet.utils.network.parseBiliURL
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class TestBiliBiliUtil {
    @Test
    fun testURLConvert() {
        assertEquals("av1234", parseBiliURL("https://www.bilibili.com/video/av1234"))
        assertEquals("bvSA3Ss", parseBiliURL("https://www.bilibili.com/video/bvSA3Ss"))

        // :(
        assertEquals("631571693968556041", parseBiliURL("https://t.bilibili.com/631571693968556041?tab=2"))
        assertEquals("BV1mS4y167M7", parseBiliURL("https://b23.tv/SGUggyB"))
    }
}