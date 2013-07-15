package com.github.kxbmap.netty.example
package echo

import io.netty.buffer.{Unpooled, ByteBuf}
import io.netty.channel.{ChannelInboundHandlerAdapter, ChannelHandlerContext}
import java.util.logging.Level

class EchoClientHandler(firstMessageSize: Int) extends ChannelInboundHandlerAdapter with Logging {

  require(firstMessageSize > 0, s"firstMessageSize: $firstMessageSize")

  private val firstMessage: ByteBuf =
    Unpooled.buffer(firstMessageSize) <| { buf =>
      for (i <- 0 until buf.capacity())
        buf.writeByte(i)
    }

  override def channelActive(ctx: ChannelHandlerContext): Unit = {
    ctx.writeAndFlush(firstMessage)
  }

  override def channelRead(ctx: ChannelHandlerContext, msg: AnyRef): Unit = {
    ctx.write(msg)
  }

  override def channelReadComplete(ctx: ChannelHandlerContext): Unit = {
    ctx.flush()
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
    // Close the connection when an exception is raised.
    logger.log(Level.WARNING, "Unexpected exception from downstream.", cause)
    ctx.close()
  }
}
