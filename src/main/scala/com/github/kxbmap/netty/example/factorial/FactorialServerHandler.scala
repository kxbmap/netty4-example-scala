package com.github.kxbmap.netty.example
package factorial

import io.netty.channel.{SimpleChannelInboundHandler, ChannelHandlerContext}
import java.util.logging.Level

/**
 * Handler for a server-side channel.  This handler maintains stateful
 * information which is specific to a certain channel using member variables.
 * Therefore, an instance of this handler can cover only one channel.  You have
 * to create a new handler instance whenever you create a new channel and insert
 * this handler  to avoid a race condition.
 */
class FactorialServerHandler extends SimpleChannelInboundHandler[BigInt] with Logging {

  private[this] var lastMultiplier: BigInt = 1
  private[this] var factorial: BigInt = 1

  def channelRead0(ctx: ChannelHandlerContext, msg: BigInt): Unit = {
    lastMultiplier = msg
    factorial *= msg
    ctx.writeAndFlush(factorial)
  }

  override def channelInactive(ctx: ChannelHandlerContext): Unit = {
    logger.info(f"Factorial of $lastMultiplier%,d is: $factorial%,d")
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
    logger.log(Level.WARNING, "Unexpected exception from downstream.", cause)
    ctx.close()
  }
}
