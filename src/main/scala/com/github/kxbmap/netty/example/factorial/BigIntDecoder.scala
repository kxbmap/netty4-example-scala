package com.github.kxbmap.netty.example
package factorial

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.{CorruptedFrameException, ByteToMessageDecoder}

class BigIntDecoder extends ByteToMessageDecoder {
  def decode(ctx: ChannelHandlerContext, in: ByteBuf): BigInt =
    if (in.readableBytes() < 5) null
    else {
      in.markReaderIndex()

      // Check the magic number.
      val magicNumber = in.readUnsignedByte()
      if (magicNumber != 'F') {
        in.resetReaderIndex()
        throw new CorruptedFrameException("Invalid magic number: " + magicNumber)
      }

      // Wait until the whole data is available.
      val dataLength = in.readInt()
      if (in.readableBytes() < dataLength) {
        in.resetReaderIndex()
        null
      }
      else BigInt(new Array[Byte](dataLength) tap in.readBytes)
    }
}
