package com.github.kxbmap.netty.example
package objectecho

import io.netty.channel.{ChannelInboundHandlerAdapter, ChannelHandlerContext}
import java.util.logging.Level

class ObjectEchoClientHandler(firstMessageSize: Int) extends ChannelInboundHandlerAdapter with Logging {

  require(firstMessageSize > 0, s"firstMessageSize: $firstMessageSize")

  private val firstMessage = (0 until firstMessageSize).toList


  override def channelActive(ctx: ChannelHandlerContext): Unit = {
    // Send the first message if this handler is a client-side handler.
    ctx.writeAndFlush(firstMessage)
  }

  override def channelRead(ctx: ChannelHandlerContext, msg: AnyRef): Unit = {
    // Echo back the received object to the server.
    ctx.write(msg)
  }

  override def channelReadComplete(ctx: ChannelHandlerContext): Unit = {
    ctx.flush()
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
    logger.log(Level.WARNING, "Unexpected exception from downstream.", cause)
    ctx.close()
  }
}
