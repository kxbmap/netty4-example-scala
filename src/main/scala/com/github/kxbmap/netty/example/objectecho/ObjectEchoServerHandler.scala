package com.github.kxbmap.netty.example
package objectecho

import io.netty.channel.{ChannelInboundHandlerAdapter, ChannelHandlerContext}
import java.util.logging.Level

class ObjectEchoServerHandler extends ChannelInboundHandlerAdapter with Logging {

  override def channelRead(ctx: ChannelHandlerContext, msg: AnyRef): Unit = {
    // Echo back the received object to the client.
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
