package ren.natsuyuk1.utils.test.file

import org.junit.jupiter.api.Test
import ren.natsuyuk1.comet.utils.file.isType
import ren.natsuyuk1.utils.test.isCI
import java.io.File
import kotlin.test.assertTrue

class TestFileUtils {
    @Test
    fun testIsType() {
        if (isCI()) {
            return
        }

        val target = File("./resources/test.jpg")
        assertTrue(target.isType("image/jpeg"), "Specified file type should be `image/jpeg`!")
    }
}
