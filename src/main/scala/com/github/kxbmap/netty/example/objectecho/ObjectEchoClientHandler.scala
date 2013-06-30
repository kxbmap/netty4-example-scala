package com.github.kxbmap.netty.example
package objectecho

import io.netty.channel.{MessageList, ChannelInboundHandlerAdapter, ChannelHandlerContext}
import java.util.logging.Level

class ObjectEchoClientHandler(firstMessageSize: Int) extends ChannelInboundHandlerAdapter with Logging {

  require(firstMessageSize > 0, s"firstMessageSize: $firstMessageSize")

  private val firstMessage = (0 until firstMessageSize).toList


  override def channelActive(ctx: ChannelHandlerContext): Unit = {
    // Send the first message if this handler is a client-side handler.
    ctx.write(firstMessage)
  }

  override def messageReceived(ctx: ChannelHandlerContext, msgs: MessageList[AnyRef]): Unit = {
    // Echo back the received object to the server.
    ctx.write(msgs)
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
    logger.log(Level.WARNING, "Unexpected exception from downstream.", cause)
    ctx.close()
  }
}
