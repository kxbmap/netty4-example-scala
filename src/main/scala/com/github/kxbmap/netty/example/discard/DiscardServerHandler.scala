package com.github.kxbmap.netty.example
package discard

import io.netty.channel.{SimpleChannelInboundHandler, ChannelHandlerContext}
import java.util.logging.Level

/**
 * Handles a server-side channel.
 */
class DiscardServerHandler extends SimpleChannelInboundHandler[AnyRef] with Logging {

  def channelRead0(ctx: ChannelHandlerContext, msg: AnyRef): Unit = {
    // discard
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
    logger.log(Level.WARNING, "Unexpected exception from downstream.", cause)
    ctx.close()
  }

}
