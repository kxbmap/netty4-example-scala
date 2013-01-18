package com.github.kxbmap.netty.example
package objectecho

import io.netty.channel.{ChannelHandlerContext, ChannelInboundMessageHandlerAdapter}
import java.util.logging.Level

class ObjectEchoClientHandler(firstMessageSize: Int)
  extends ChannelInboundMessageHandlerAdapter[List[Int]] with Logging {

  private val firstMessage = (0 until firstMessageSize).toList


  override def channelActive(ctx: ChannelHandlerContext) {
    // Send the first message if this handler is a client-side handler.
    ctx.write(firstMessage)
  }

  def messageReceived(ctx: ChannelHandlerContext, msg: List[Int]) {
    ctx.write(msg)
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
    logger.log(Level.WARNING, "Unexpected exception from downstream.", cause)
    ctx.close()
  }
}
