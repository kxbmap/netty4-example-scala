package com.github.kxbmap.netty.example
package factorial

import io.netty.buffer.{MessageBuf, ByteBuf}
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.{CorruptedFrameException, ByteToMessageDecoder}

class BigIntDecoder extends ByteToMessageDecoder {

  def decode(ctx: ChannelHandlerContext, in: ByteBuf, out: MessageBuf[AnyRef]) {
    // Wait until the length prefix is available
    if (in.readableBytes() >= 5) {
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
      } else {
        out.add(BigInt(new Array[Byte](dataLength) tap in.readBytes))
      }
    }
  }

}
