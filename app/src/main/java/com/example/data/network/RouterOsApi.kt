package com.example.data.network

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.IOException
import java.net.Socket
import java.net.SocketTimeoutException

/**
 * Minimal RouterOS API client for querying system identity.
 * Uses TCP port 8728 (not intercepted by hotspot).
 */
class RouterOsApi(
    private val host: String = "192.168.88.1",
    private val port: Int = 8728,
    private val timeout: Int = 3000
) {
    private var socket: Socket? = null
    private var inputStream: BufferedInputStream? = null
    private var outputStream: BufferedOutputStream? = null

    /**
     * Connect to RouterOS API and return system identity.
     * Tries multiple credential combinations automatically.
     * Returns null on failure.
     */
    fun getSystemIdentity(): String? {
        val credentialSets = listOf(
            arrayOf("admin", ""),
            arrayOf("admin", "admin"),
            arrayOf("admin", "123456"),
            arrayOf("admin", "password"),
            arrayOf("msr_read", "msr_read"),
        )
        for (creds in credentialSets) {
            val result = tryLogin(creds[0], creds[1])
            if (result != null) return result
        }
        return null
    }

    private fun tryLogin(username: String, password: String): String? {
        return try {
            socket = Socket(host, port).apply {
                soTimeout = timeout
                tcpNoDelay = true
            }
            inputStream = BufferedInputStream(socket!!.getInputStream())
            outputStream = BufferedOutputStream(socket!!.getOutputStream())

            sendCommand("/login", "name=$username", "password=$password")
            val loginReply = readReply()

            val error = loginReply?.find { it.key == "!trap" }
            if (error != null) {
                close()
                return null
            }

            sendCommand("/system identity get name")
            val identityReply = readReply()
            close()
            if (identityReply != null && identityReply.isNotEmpty()) {
                val retValue = identityReply.find { it.key == "ret" }
                retValue?.value
            } else {
                null
            }
        } catch (e: Exception) {
            close()
            null
        }
    }

    private fun sendCommand(varargs words: String) {
        val output = outputStream ?: return
        for (word in words) {
            val bytes = word.toByteArray(Charsets.UTF_8)
            writeLength(bytes.size)
            output.write(bytes)
        }
        writeLength(0) // End of sentence
        output.flush()
    }

    private fun writeLength(length: Int) {
        val output = outputStream ?: return
        val bytes = ByteArray(4)
        bytes[0] = (length and 0xFF).toByte()
        bytes[1] = ((length shr 8) and 0xFF).toByte()
        bytes[2] = ((length shr 16) and 0xFF).toByte()
        bytes[3] = ((length shr 24) and 0xFF).toByte()
        output.write(bytes)
    }

    private fun readLength(): Int {
        val input = inputStream ?: return -1
        val bytes = ByteArray(4)
        var read = 0
        while (read < 4) {
            val b = input.read()
            if (b == -1) return -1
            bytes[read] = b.toByte()
            read++
        }
        return (bytes[0].toInt() and 0xFF) or
                ((bytes[1].toInt() and 0xFF) shl 8) or
                ((bytes[2].toInt() and 0xFF) shl 16) or
                ((bytes[3].toInt() and 0xFF) shl 24)
    }

    private fun readWord(): String? {
        val length = readLength()
        if (length <= 0) return null
        val input = inputStream ?: return null
        val bytes = ByteArray(length)
        var offset = 0
        while (offset < length) {
            val read = input.read(bytes, offset, length - offset)
            if (read == -1) return null
            offset += read
        }
        return String(bytes, Charsets.UTF_8)
    }

    private fun readReply(): List<WordPair>? {
        val result = mutableListOf<WordPair>()
        while (true) {
            val word = readWord() ?: break
            if (word.isEmpty()) break

            val eqIndex = word.indexOf('=')
            if (eqIndex > 0) {
                val key = word.substring(0, eqIndex)
                val value = word.substring(eqIndex + 1)
                result.add(WordPair(key, value))
            } else {
                result.add(WordPair(word, ""))
            }

            // Check for !done
            if (word.startsWith("!done")) {
                break
            }
        }
        return result
    }

    private fun close() {
        try {
            inputStream?.close()
        } catch (_: Exception) {}
        try {
            outputStream?.close()
        } catch (_: Exception) {}
        try {
            socket?.close()
        } catch (_: Exception) {}
        inputStream = null
        outputStream = null
        socket = null
    }

    data class WordPair(val key: String, val value: String)
}
