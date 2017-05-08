package org.jglr.voxlin.utils

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

class ResourceLocation(val path: String) {
    private var text: String? = null
    private var read: Boolean = false

    fun readText(): String {
        if (!read) {
            val reader = BufferedReader(InputStreamReader(newInputStream()))

            val lines = reader.lines()
            val builder = StringBuilder()
            lines.forEach { builder.append(it).append("\n") }
            reader.close()
            text = builder.toString()

            read = true
        }
        return text!!
    }

    fun reload() {
        read = false
    }

    fun newInputStream(): InputStream? {
        return javaClass.getResourceAsStream("/" + path)
    }
}
