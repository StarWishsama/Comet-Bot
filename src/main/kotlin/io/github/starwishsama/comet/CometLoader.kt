package io.github.starwishsama.comet

import java.io.*


class CometLoader : ClassLoader() {

    private var rootDir: String? = null

    fun CometLoader(rootDir: String?) {
        this.rootDir = rootDir
    }

    @Throws(ClassNotFoundException::class)
    override fun findClass(name: String): Class<*>? {
        val classData = getClassData(name)
        return if (classData == null) {
            throw ClassNotFoundException()
        } else {
            defineClass(name, classData, 0, classData.size)
        }
    }

    private fun getClassData(className: String): ByteArray? {
        val path = classNameToPath(className)
        try {
            val ins: InputStream = FileInputStream(path)
            val baos = ByteArrayOutputStream()
            val bufferSize = 4096
            val buffer = ByteArray(bufferSize)
            var bytesNumRead: Int
            while (ins.read(buffer).also { bytesNumRead = it } != -1) {
                baos.write(buffer, 0, bytesNumRead)
            }
            return baos.toByteArray()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    private fun classNameToPath(className: String): String {
        return (rootDir + File.separatorChar
                + className.replace('.', File.separatorChar) + ".class")
    }
}