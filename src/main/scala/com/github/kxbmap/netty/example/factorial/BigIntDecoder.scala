package com.github.kxbmap.netty.example
package factorial

import io.netty.buffer.ByteBuf
import io.netty.channel.{MessageList, ChannelHandlerContext}
import io.netty.handler.codec.{CorruptedFrameException, ByteToMessageDecoder}

/**
 * Decodes the binary representation of a [[scala.BigInt]] prepended
 * with a magic number ('F' or 0x46) and a 32-bit integer length prefix into a
 * [[scala.BigInt]] instance.  For example, { 'F', 0, 0, 0, 1, 42 } will be
 * decoded into new BigInteger("42").
 */
class BigIntDecoder extends ByteToMessageDecoder {

  def decode(ctx: ChannelHandlerContext, in: ByteBuf, out: MessageList[AnyRef]): Unit =
    // Wait until the length prefix is available
    if (in.readableBytes() >= 5) {
      in.markReaderIndex()

      // Check the magic number.
      val magicNumber = in.readUnsignedByte()
      if (magicNumber != 'F') {
        in.resetReaderIndex()
        throw new CorruptedFrameException(s"Invalid magic number: $magicNumber")
      }

      // Wait until the whole data is available.
      val dataLength = in.readInt()
      if (in.readableBytes() < dataLength) {
        in.resetReaderIndex()
      } else {
        // Convert the received data into a new BigInt.
        out.add(BigInt(new Array[Byte](dataLength) <| in.readBytes))
      }
    }

}
