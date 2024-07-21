package com.opennxt.net.buf

import com.opennxt.ext.readString
import io.netty.buffer.ByteBuf

/**
 * A utility class for reading packets
 *
 * @author Graham
 */
class GamePacketReader(
    val buffer: ByteBuf
) {

    /**
     * The current bit index.
     */
    private var bitIndex: Int = 0

    /**
     * The current mode.
     */
    private var mode = AccessMode.BYTE_ACCESS

    /**
     * Gets a bit from the buffer.
     *
     * @return The value.
     * @throws IllegalStateException If the reader is not in bit access mode.
     */
    fun getBit(): Int = getBits(1)

    /**
     * Gets the length of this reader.
     *
     * @return The length of this reader.
     */
    fun getLength(): Int {
        checkByteAccess()
        return buffer.writableBytes()
    }

    /**
     * Gets a signed smart from the buffer.
     *
     * @return The smart.
     * @throws IllegalStateException If this reader is not in byte access mode.
     */
    fun getSignedSmart(): Int {
        checkByteAccess()
        val peek = buffer.getByte(buffer.readerIndex()).toInt()
        return if (peek < 128) {
            buffer.readByte() - 64
        } else buffer.readShort() - 49152
    }

    /**
     * Gets a string from the buffer.
     *
     * @return The string.
     * @throws IllegalStateException If this reader is not in byte access mode.
     */
    fun getString(): String {
        checkByteAccess()
        return buffer.readString()
    }

    /**
     * Checks that this reader is in the bit access mode.
     *
     * @throws IllegalStateException If the reader is not in bit access mode.
     */
    private fun checkBitAccess() {
        if (mode !== AccessMode.BIT_ACCESS) {
            throw IllegalArgumentException("For bit-based calls to work, the mode must be bit access.")
        }
    }

    /**
     * Checks that this reader is in the byte access mode.
     *
     * @throws IllegalStateException If the reader is not in byte access mode.
     */
    private fun checkByteAccess() {
        if (mode !== AccessMode.BYTE_ACCESS) {
            throw IllegalArgumentException("For byte-based calls to work, the mode must be byte access.")
        }
    }

    /**
     * Reads a standard data type from the buffer with the specified order and transformation.
     *
     * @param type           The data type.
     * @param order          The data order.
     * @param transformation The data transformation.
     * @return The value.
     * @throws IllegalStateException    If this reader is not in byte access mode.
     * @throws IllegalArgumentException If the combination is invalid.
     */
    private operator fun get(type: DataType, order: DataOrder, transformation: DataTransformation): Long {
        checkByteAccess()
        var longValue: Long = 0
        val length = type.bytes
        when (order) {
            DataOrder.BIG -> for (i in length - 1 downTo 0) {
                if (i == 0 && transformation !== DataTransformation.NONE) {
                    if (transformation === DataTransformation.ADD) {
                        longValue = longValue or (buffer.readByte().toLong() - 128 and 0xFFL)
                    } else if (transformation === DataTransformation.NEGATE) {
                        longValue = longValue or (-buffer.readByte().toLong() and 0xFFL)
                    } else if (transformation === DataTransformation.SUBTRACT) {
                        longValue = longValue or (128 - buffer.readByte().toLong() and 0xFFL)
                    } else {
                        throw IllegalArgumentException("Unknown transformation.")
                    }
                } else {
                    longValue = longValue or (buffer.readByte().toLong() and 0xFFL shl i * 8)
                }
            }
            DataOrder.INVERSED_MIDDLE -> {
                if (transformation !== DataTransformation.NONE) {
                    throw IllegalArgumentException("Inversed middle endian cannot be transformed.")
                }
                if (type !== DataType.INT) {
                    throw IllegalArgumentException("Inversed middle endian can only be used with an integer.")
                }
                longValue = longValue or (buffer.readByte().toInt() and 0xFF shl 16).toLong()
                longValue = longValue or (buffer.readByte().toInt() and 0xFF shl 24).toLong()
                longValue = longValue or (buffer.readByte().toInt() and 0xFF).toLong()
                longValue = longValue or (buffer.readByte().toInt() and 0xFF shl 8).toLong()
            }
            DataOrder.LITTLE -> for (i in 0 until length) {
                if (i == 0 && transformation !== DataTransformation.NONE) {
                    if (transformation === DataTransformation.ADD) {
                        longValue = longValue or (buffer.readByte().toLong() - 128 and 0xFFL)
                    } else if (transformation === DataTransformation.NEGATE) {
                        longValue = longValue or (-buffer.readByte().toLong() and 0xFFL)
                    } else if (transformation === DataTransformation.SUBTRACT) {
                        longValue = longValue or (128 - buffer.readByte().toLong() and 0xFFL)
                    } else {
                        throw IllegalArgumentException("Unknown transformation.")
                    }
                } else {
                    longValue = longValue or (buffer.readByte().toLong() and 0xFFL shl i * 8)
                }
            }
            DataOrder.MIDDLE -> {
                if (transformation !== DataTransformation.NONE) {
                    throw IllegalArgumentException("Middle endian cannot be transformed.")
                }
                if (type !== DataType.INT) {
                    throw IllegalArgumentException("Middle endian can only be used with an integer.")
                }
                longValue = longValue or (buffer.readByte().toInt() and 0xFF shl 8).toLong()
                longValue = longValue or (buffer.readByte().toInt() and 0xFF).toLong()
                longValue = longValue or (buffer.readByte().toInt() and 0xFF shl 24).toLong()
                longValue = longValue or (buffer.readByte().toInt() and 0xFF shl 16).toLong()
            }
            else -> throw IllegalArgumentException("Unknown order.")
        }
        return longValue
    }

    /**
     * Gets the specified amount of bits from the buffer.
     *
     * @param amount The amount of bits.
     * @return The value.
     * @throws IllegalStateException    If the reader is not in bit access mode.
     * @throws IllegalArgumentException If the number of bits is not between 1 and 31 inclusive.
     */
    fun getBits(amount: Int): Int {
        var amount = amount
        checkBitAccess()

        var bytePos = bitIndex shr 3
        var bitOffset = 8 - (bitIndex and 7)
        var value = 0
        bitIndex += amount

        while (amount > bitOffset) {
            value += buffer.getByte(bytePos++).toInt() and DataConstants.BIT_MASK[bitOffset] shl amount - bitOffset
            amount -= bitOffset
            bitOffset = 8
        }
        if (amount == bitOffset) {
            value += buffer.getByte(bytePos).toInt() and DataConstants.BIT_MASK[bitOffset]
        } else {
            value += buffer.getByte(bytePos).toInt() shr bitOffset - amount and DataConstants.BIT_MASK[amount]
        }
        return value
    }

    /**
     * Gets bytes.
     *
     * @param bytes The target byte array.
     * @throws IllegalStateException If this reader is not in byte access mode.
     */
    fun getBytes(bytes: ByteArray) {
        checkByteAccess()
        for (i in bytes.indices) {
            bytes[i] = buffer.readByte()
        }
    }

    /**
     * Gets bytes with the specified transformation.
     *
     * @param transformation The transformation.
     * @param bytes          The target byte array.
     * @throws IllegalStateException If this reader is not in byte access mode.
     */
    fun getBytes(transformation: DataTransformation, bytes: ByteArray) {
        if (transformation === DataTransformation.NONE) {
            getBytesReverse(bytes)
        } else {
            for (i in bytes.indices) {
                bytes[i] = getSigned(DataType.BYTE, transformation).toByte()
            }
        }
    }

    /**
     * Gets bytes in reverse.
     *
     * @param bytes The target byte array.
     * @throws IllegalStateException If this reader is not in byte access mode.
     */
    fun getBytesReverse(bytes: ByteArray) {
        checkByteAccess()
        for (i in bytes.indices.reversed()) {
            bytes[i] = buffer.readByte()
        }
    }

    /**
     * Gets bytes in reverse with the specified transformation.
     *
     * @param transformation The transformation.
     * @param bytes          The target byte array.
     * @throws IllegalStateException If this reader is not in byte access mode.
     */
    fun getBytesReverse(transformation: DataTransformation, bytes: ByteArray) {
        if (transformation === DataTransformation.NONE) {
            getBytesReverse(bytes)
        } else {
            for (i in bytes.indices.reversed()) {
                bytes[i] = getSigned(DataType.BYTE, transformation).toByte()
            }
        }
    }

    /**
     * Gets a signed data type from the buffer with the specified order and transformation.
     *
     * @param type           The data type.
     * @param order          The byte order.
     * @param transformation The data transformation.
     * @return The value.
     * @throws IllegalStateException    If this reader is not in byte access mode.
     * @throws IllegalArgumentException If the combination is invalid.
     */
    @JvmOverloads
    fun getSigned(
        type: DataType,
        order: DataOrder = DataOrder.BIG,
        transformation: DataTransformation = DataTransformation.NONE
    ): Long {
        var longValue = get(type, order, transformation)
        if (type !== DataType.LONG) {
            val max = (Math.pow(2.0, type.bytes.toDouble() * 8 - 1) - 1).toInt()
            if (longValue > max) {
                longValue -= ((max + 1) * 2).toLong()
            }
        }
        return longValue
    }

    /**
     * Gets a signed data type from the buffer with the specified transformation.
     *
     * @param type           The data type.
     * @param transformation The data transformation.
     * @return The value.
     * @throws IllegalStateException    If this reader is not in byte access mode.
     * @throws IllegalArgumentException If the combination is invalid.
     */
    fun getSigned(type: DataType, transformation: DataTransformation): Long {
        return getSigned(type, DataOrder.BIG, transformation)
    }

    /**
     * Gets an unsigned data type from the buffer with the specified order and transformation.
     *
     * @param type           The data type.
     * @param order          The byte order.
     * @param transformation The data transformation.
     * @return The value.
     * @throws IllegalStateException    If this reader is not in byte access mode.
     * @throws IllegalArgumentException If the combination is invalid.
     */
    @JvmOverloads
    fun getUnsigned(
        type: DataType,
        order: DataOrder = DataOrder.BIG,
        transformation: DataTransformation = DataTransformation.NONE
    ): Long {
        val longValue = get(type, order, transformation)
        return longValue and -0x1L
    }

    /**
     * Gets an unsigned data type from the buffer with the specified transformation.
     *
     * @param type           The data type.
     * @param transformation The data transformation.
     * @return The value.
     * @throws IllegalStateException    If this reader is not in byte access mode.
     * @throws IllegalArgumentException If the combination is invalid.
     */
    fun getUnsigned(type: DataType, transformation: DataTransformation): Long {
        return getUnsigned(type, DataOrder.BIG, transformation)
    }

    /**
     * Switches this builder's mode to the bit access mode.
     *
     * @throws IllegalStateException If the builder is already in bit access mode.
     */
    fun switchToBitAccess() {
        if (mode === AccessMode.BIT_ACCESS) {
            return
        }

        mode = AccessMode.BIT_ACCESS
        bitIndex = buffer.readerIndex() * 8
    }

    /**
     * Switches this builder's mode to the byte access mode.
     *
     * @throws IllegalStateException If the builder is already in byte access mode.
     */
    fun switchToByteAccess() {
        if (mode === AccessMode.BYTE_ACCESS) {
            return
        }

        mode = AccessMode.BYTE_ACCESS
        buffer.readerIndex((bitIndex + 7) / 8)
    }

}