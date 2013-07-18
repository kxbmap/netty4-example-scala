package com.github.kxbmap.netty.example
package securechat

import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}
import java.util.logging.Level

/**
 * Handles a client-side channel.
 */
class SecureChatClientHandler extends SimpleChannelInboundHandler[String] with Logging {

  def channelRead0(ctx: ChannelHandlerContext, msg: String): Unit = {
    Console.err.println(msg)
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
    logger.log(Level.WARNING, "Unexpected exception from downstream.", cause)
    ctx.close()
  }
}
