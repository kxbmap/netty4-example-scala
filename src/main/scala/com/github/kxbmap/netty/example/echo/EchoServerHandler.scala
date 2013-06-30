package com.github.kxbmap.netty.example
package echo

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.{MessageList, ChannelInboundHandlerAdapter, ChannelHandlerContext}
import java.util.logging.Level

@Sharable
class EchoServerHandler extends ChannelInboundHandlerAdapter with Logging {

  override def messageReceived(ctx: ChannelHandlerContext, msgs: MessageList[AnyRef]): Unit = {
    ctx.write(msgs)
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
    // Close the connection when an exception is raised.
    logger.log(Level.WARNING, "Unexpected exception from downstream.", cause)
    ctx.close()
  }
}
