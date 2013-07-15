package com.github.kxbmap.netty.example
package echo

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.{ChannelInboundHandlerAdapter, ChannelHandlerContext}
import java.util.logging.Level

@Sharable
class EchoServerHandler extends ChannelInboundHandlerAdapter with Logging {

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
