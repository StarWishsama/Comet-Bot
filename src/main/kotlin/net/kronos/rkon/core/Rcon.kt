package net.kronos.rkon.core

import io.github.starwishsama.comet.utils.BotUtil
import net.kronos.rkon.core.ex.AuthenticationException
import java.io.IOException
import java.net.Socket
import java.nio.charset.Charset
import java.util.*

class Rcon(host: String, port: Int, password: ByteArray?) {
    private val sync = Any()
    private val rand = Random()
    var requestId = 0
        private set
    var socket: Socket? = null
        private set
    var charset: Charset = Charset.forName("UTF-8")

    /**
     * Connect to a rcon server
     *
     * @param host Rcon server address
     * @param port Rcon server port
     * @param password Rcon server password
     *
     * @throws IOException
     * @throws AuthenticationException Thrown if server rejects password
     */
    @Throws(IOException::class, AuthenticationException::class)
    fun connect(host: String, port: Int, password: ByteArray?) {
        require(host.trim { it <= ' ' }.isNotEmpty()) { "Host can't be null or empty" }
        require(!(port < 1 || port > 65535)) { "Port is out of range" }

        // Connect to the rcon server
        synchronized(sync) {

            // New random request id
            requestId = rand.nextInt()

            // We can't reuse a socket, so we need a new one
            socket = Socket(host, port)
        }

        // Send the auth packet
        // Anonymous sendAuth(Rcon rcon, byte[] password) for one-time use
        var response: RconPacket?
        synchronized(sync) { response = password?.let { RconPacket.sendAuth(this, it) } }


        // Check auth status
        if (response?.requestId != requestId) {
            if (response?.requestId == -1) {
                // Auth failed
                throw AuthenticationException("Password rejected by server")
            } else {
                // Something weird happened
                throw AuthenticationException("Auth response ID does not match the original ID")
            }
        }
    }

    /**
     * Disconnect from the current server
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    fun disconnect() {
        synchronized(sync) { socket?.close() }
    }

    /**
     * Send a command to the server
     *
     * @param payload The command to send
     * @return The payload of the response
     *
     * @throws IOException
     */
    @Throws(Exception::class)
    fun command(payload: String): String {
        require(payload.trim { it <= ' ' }.isNotEmpty()) { "Payload can't be null or empty" }
        val response = send(RconPacket.SERVERDATA_EXECCOMMAND, payload.toByteArray())
        if (response != null) {
            return BotUtil.sendMessage(String(response.payload, charset)).contentToString()
        }
        return "发生异常"
    }

    @Throws(IOException::class)
    private fun send(type: Int, payload: ByteArray): RconPacket? {
        synchronized(sync) { return RconPacket.send(this, type, payload) }
    }

    init {
        // Default charset is utf8

        // Connect to host
        connect(host, port, password)
    }
}