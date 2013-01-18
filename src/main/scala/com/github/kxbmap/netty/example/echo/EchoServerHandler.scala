package com.github.kxbmap.netty.example
package echo

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.{ChannelHandlerContext, ChannelInboundByteHandlerAdapter}
import java.util.logging.Level

@Sharable
class EchoServerHandler extends ChannelInboundByteHandlerAdapter with Logging {

  def inboundBufferUpdated(ctx: ChannelHandlerContext, in: ByteBuf) {
    val out = ctx.nextOutboundByteBuffer()
    out.discardReadBytes()
    out.writeBytes(in)
    ctx.flush()
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
    // Close the connection when an exception is raised.
    logger.log(Level.WARNING, "Unexpected exception from downstream.", cause)
    ctx.close()
  }
}
