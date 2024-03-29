package ren.natsuyuk1.utils.test.json

import com.jayway.jsonpath.JsonPath
import org.junit.jupiter.api.TestInstance
import ren.natsuyuk1.comet.utils.json.JsonPathMap
import ren.natsuyuk1.comet.utils.json.formatByTemplate
import kotlin.test.Test
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestJsonPath {
    @Test
    fun test() {
        val testJson = """{"test":2,"test2":3, "test3":[114,514,1919], "test4": {"ksm": "arisa"}}"""
        val jsonMap = JsonPathMap(JsonPath.parse(testJson))

        val r = jsonMap.formatByTemplate("测试 {{ test3[1] }} {{ test4.ksm }}")

        assertEquals("测试 514 arisa", r)
    }
}
