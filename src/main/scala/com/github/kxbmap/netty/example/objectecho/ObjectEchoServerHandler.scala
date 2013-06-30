package com.github.kxbmap.netty.example
package objectecho

import io.netty.channel.{MessageList, ChannelInboundHandlerAdapter, ChannelHandlerContext}
import java.util.logging.Level

class ObjectEchoServerHandler extends ChannelInboundHandlerAdapter with Logging {

  override def messageReceived(ctx: ChannelHandlerContext, msgs: MessageList[AnyRef]): Unit = {
    // Echo back the received object to the client.
    ctx.write(msgs)
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
    logger.log(Level.WARNING, "Unexpected exception from downstream.", cause)
    ctx.close()
  }
}
