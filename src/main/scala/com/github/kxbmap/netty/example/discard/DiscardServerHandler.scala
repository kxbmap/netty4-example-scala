package com.github.kxbmap.netty.example
package discard

import io.netty.buffer.ByteBuf
import io.netty.channel.{ChannelHandlerContext, ChannelInboundByteHandlerAdapter}
import java.util.logging.Level

/**
 * Handles a server-side channel.
 */
class DiscardServerHandler extends ChannelInboundByteHandlerAdapter with Logging {

  def inboundBufferUpdated(ctx: ChannelHandlerContext, in: ByteBuf) {
    // Discard the received data silently.
    in.clear()
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
    logger.log(Level.WARNING, "Unexpected exception from downstream.", cause)
    ctx.close()
  }

}
