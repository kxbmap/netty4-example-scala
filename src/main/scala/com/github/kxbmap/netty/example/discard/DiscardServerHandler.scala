package com.github.kxbmap.netty.example
package discard

import io.netty.channel.{MessageList, ChannelInboundHandlerAdapter, ChannelHandlerContext}
import java.util.logging.Level

/**
 * Handles a server-side channel.
 */
class DiscardServerHandler extends ChannelInboundHandlerAdapter with Logging {

  override def messageReceived(ctx: ChannelHandlerContext, msgs: MessageList[AnyRef]) {
    // Discard the received data silently.
    msgs.releaseAllAndRecycle()
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
    logger.log(Level.WARNING, "Unexpected exception from downstream.", cause)
    ctx.close()
  }

}
