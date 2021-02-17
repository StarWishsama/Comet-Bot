package io.github.starwishsama.comet.objects.config

import java.io.File

class DataFile(
    val file: File,
    val priority: FilePriority,
    val initAction: (File) -> Unit = {}
) {
    fun exists(): Boolean = file.exists()

    fun createNewFile(): Boolean = file.createNewFile()

    enum class FilePriority {
        /**
         * 只有使用某些功能时才会生成
         */
        LOW,

        /**
         * 初始化时不需要立即生成
         */
        NORMAL,

        /**
         * 初始化时的必需文件
         */
        HIGH
    }
}