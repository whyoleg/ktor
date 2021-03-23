package io.ktor.utils.io

import io.ktor.utils.io.bits.Memory
import io.ktor.utils.io.core.*
import io.ktor.utils.io.core.internal.ChunkBuffer

/**
 * Await for [desiredSpace] will be available for write and invoke [block] function providing [Memory] instance and
 * the corresponding range suitable for wiring in the memory. The block function should return number of bytes were
 * written, possibly 0.
 *
 * Similar to [ByteReadChannel.read], this function may invoke block function with lesser memory range when the
 * specified [desiredSpace] is bigger that the buffer's capacity
 * or when it is impossible to represent all [desiredSpace] bytes as a single memory range
 * due to internal implementation reasons.
 */
@ExperimentalIoApi
public suspend inline fun ByteWriteChannel.write(
    desiredSpace: Int = 1,
    block: (freeSpace: Memory, startOffset: Long, endExclusive: Long) -> Int
): Int {
    val buffer = requestWriteBuffer()
    var bytesWritten = 0
    try {
        bytesWritten = block(buffer.memory, buffer.writePosition.toLong(), buffer.limit.toLong())
        buffer.commitWritten(bytesWritten)
        return bytesWritten
    } finally {
        completeWriting(buffer)
    }
}

@Suppress("DEPRECATION")
@Deprecated("Use writeMemory instead.")
public interface WriterSession {
    public fun request(min: Int): ChunkBuffer?
    public fun written(n: Int)
    public fun flush()
}

@Suppress("DEPRECATION")
@Deprecated("Use writeMemory instead.")
public interface WriterSuspendSession : WriterSession {
    public suspend fun tryAwait(n: Int)
}

@Suppress("DEPRECATION")
internal interface HasWriteSession {
    public fun beginWriteSession(): WriterSuspendSession?
    public fun endWriteSession(written: Int)
}

@PublishedApi
internal fun requestWriteBuffer(): Buffer = ChunkBuffer.Pool.borrow().also {
    it.resetForWrite()
    it.reserveEndGap(Buffer.ReservedSize)
}

@PublishedApi
internal suspend fun ByteWriteChannel.completeWriting(buffer: Buffer) {
    if (buffer is ChunkBuffer) {
        writeFully(buffer)
        buffer.release(ChunkBuffer.Pool)
        return
    }

    throw UnsupportedOperationException("Only ChunkBuffer instance is supported.")
}
