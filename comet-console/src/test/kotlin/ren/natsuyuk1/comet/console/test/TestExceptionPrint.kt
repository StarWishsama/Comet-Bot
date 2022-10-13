package ren.natsuyuk1.comet.console.test

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import ren.natsuyuk1.comet.console.util.Console

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestExceptionPrint {
    @Test
    fun test() {
        Console.initReader()
        Console.redirectToJLine()

        error("catch me")
    }

    @AfterAll
    fun clean() {
        Console.redirectToNull()
    }
}
