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
     * Returns null on failure.
     */
    fun getSystemIdentity(username: String, password: String): String? {
        return try {
            socket = Socket(host, port).apply {
                soTimeout = timeout
                tcpNoDelay = true
            }
            inputStream = BufferedInputStream(socket!!.getInputStream())
            outputStream = BufferedOutputStream(socket!!.getOutputStream())

            // Send /login
            sendCommand("/login", "name=$username", "password=$password")
            val loginReply = readReply()

            // Check for errors
            val error = loginReply?.find { it.key == "!trap" }
            if (error != null) {
                return null
            }

            // Get system identity
            sendCommand("/system identity get name")
            val identityReply = readReply()
            if (identityReply != null && identityReply.isNotEmpty()) {
                val retValue = identityReply.find { it.key == "ret" }
                retValue?.value
            } else {
                null
            }
        } catch (e: SocketTimeoutException) {
            null
        } catch (e: IOException) {
            null
        } catch (e: Exception) {
            null
        } finally {
            close()
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
            outputStream?.close()
            socket?.close()
        } catch (e: Exception) {
            // Ignore
        }
        inputStream = null
        outputStream = null
        socket = null
    }

    data class WordPair(val key: String, val value: String)
}
