package com.github.kxbmap.netty.example
package objectecho

import io.netty.channel.{ChannelHandlerContext, ChannelInboundMessageHandlerAdapter}
import java.util.logging.Level

class ObjectEchoServerHandler extends ChannelInboundMessageHandlerAdapter[List[Int]] with Logging {
  def messageReceived(ctx: ChannelHandlerContext, msg: List[Int]) {
    // Echo back the received object to the client.
    ctx.write(msg)
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
    logger.log(Level.WARNING, "Unexpected exception from downstream.", cause)
    ctx.close()
  }
}
