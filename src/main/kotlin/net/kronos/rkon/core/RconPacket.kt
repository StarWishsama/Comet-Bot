package net.kronos.rkon.core

import net.kronos.rkon.core.ex.MalformedPacketException
import java.io.*
import java.net.SocketException
import java.nio.BufferUnderflowException
import java.nio.ByteBuffer
import java.nio.ByteOrder

class RconPacket private constructor(val requestId: Int, val type: Int, val payload: ByteArray) {

    companion object {
        const val SERVERDATA_EXECCOMMAND = 2
        const val SERVERDATA_AUTH = 3

        /**
         * Send a Rcon packet and fetch the response
         *
         * @param rcon Rcon instance
         * @param type The packet type
         * @param payload The payload (password, command, etc.)
         * @return A RconPacket object containing the response
         *
         * @throws IOException
         * @throws MalformedPacketException
         */
        @Throws(IOException::class)
        fun send(rcon: Rcon, type: Int, payload: ByteArray): RconPacket? {
            try {
                write(rcon.socket!!.getOutputStream(), rcon.requestId, type, payload)
            } catch (se: SocketException) {
                // Close the socket if something happens
                rcon.socket?.close()
                throw se
            }
            return rcon.socket?.getInputStream()?.let { read(it) }
        }

        /**
         * Send an Rcon authentication packet and fetch the response
         *
         * @param rcon Rcon instance
         * @param password Password to be authenticated
         * @return An RconPacket object containing the authentication response
         *
         * @throws IOException
         */
        @Throws(IOException::class)
        fun sendAuth(rcon: Rcon, password: ByteArray): RconPacket? {
            try {
                rcon.socket?.getOutputStream()?.let { write(it, rcon.requestId, SERVERDATA_AUTH, password) }
            } catch (se: SocketException) {
                // Close the socket if something happens
                rcon.socket?.close()
                throw se
            }

            // Ignore the SERVERDATA_RESPONSE_VALUE packet as it is empty
            rcon.socket?.getInputStream()?.let { read(it) }

            // Return the SERVERDATA_AUTH_RESPONSE packet
            return rcon.socket?.getInputStream()?.let { read(it) }
        }

        /**
         * Write a rcon packet on an outputstream
         *
         * @param out The OutputStream to write on
         * @param requestId The request id
         * @param type The packet type
         * @param payload The payload
         *
         * @throws IOException
         */
        @Throws(IOException::class)
        private fun write(out: OutputStream, requestId: Int, type: Int, payload: ByteArray) {
            val bodyLength = getBodyLength(payload.size)
            val packetLength = getPacketLength(bodyLength)
            val buffer = ByteBuffer.allocate(packetLength)
            buffer.order(ByteOrder.LITTLE_ENDIAN)
            buffer.putInt(bodyLength)
            buffer.putInt(requestId)
            buffer.putInt(type)
            buffer.put(payload)

            // Null bytes terminators
            buffer.put(0.toByte())
            buffer.put(0.toByte())

            // Woosh!
            out.write(buffer.array())
            out.flush()
        }

        /**
         * Read an incoming rcon packet
         *
         * @param inputStream
         * @return The read RconPacket
         *
         * @throws IOException
         * @throws MalformedPacketException
         */
        @Throws(IOException::class)
        private fun read(inputStream: InputStream): RconPacket {
            // Header is 3 4-bytes ints
            val header = ByteArray(4 * 3)

            // Read the 3 ints
            inputStream.read(header)
            return try {
                // Use a bytebuffer in little endian to read the first 3 ints
                val buffer = ByteBuffer.wrap(header)
                buffer.order(ByteOrder.LITTLE_ENDIAN)
                val length = buffer.int
                val requestId = buffer.int
                val type = buffer.int

                // Payload size can be computed now that we have its length
                val payload = ByteArray(length - 4 - 4 - 2)
                val dis = DataInputStream(inputStream)

                // Read the full payload
                dis.readFully(payload)

                // Read the null bytes
                dis.read(ByteArray(2))
                RconPacket(requestId, type, payload)
            } catch (e: BufferUnderflowException) {
                throw MalformedPacketException("Cannot read the whole packet")
            } catch (e: EOFException) {
                throw MalformedPacketException("Cannot read the whole packet")
            }
        }

        private fun getPacketLength(bodyLength: Int): Int {
            // 4 bytes for length + x bytes for body length
            return 4 + bodyLength
        }

        private fun getBodyLength(payloadLength: Int): Int {
            // 4 bytes for requestId, 4 bytes for type, x bytes for payload, 2 bytes for two null bytes
            return 4 + 4 + payloadLength + 2
        }
    }

}