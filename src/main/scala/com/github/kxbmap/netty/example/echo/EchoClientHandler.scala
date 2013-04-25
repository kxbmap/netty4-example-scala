package com.github.kxbmap.netty.example
package echo

import io.netty.buffer.{Unpooled, ByteBuf}
import io.netty.channel.{ChannelHandlerContext, ChannelInboundByteHandlerAdapter}
import java.util.logging.Level

class EchoClientHandler(firstMessageSize: Int) extends ChannelInboundByteHandlerAdapter with Logging {

  require(firstMessageSize > 0, s"firstMessageSize: $firstMessageSize")

  private val firstMessage: ByteBuf =
    Unpooled.buffer(firstMessageSize) tap { buf =>
      for (i <- 0 until buf.capacity())
        buf.writeByte(i)
    }

  override def channelActive(ctx: ChannelHandlerContext) {
    ctx.write(firstMessage)
  }

  def inboundBufferUpdated(ctx: ChannelHandlerContext, in: ByteBuf) {
    val out = ctx.nextOutboundByteBuffer()
    out.writeBytes(in)
    ctx.flush()
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
    // Close the connection when an exception is raised.
    logger.log(Level.WARNING, "Unexpected exception from downstream.", cause)
    ctx.close()
  }
}
