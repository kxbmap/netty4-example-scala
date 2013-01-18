package com.github.kxbmap.netty.example
package factorial

import io.netty.channel.{ChannelHandlerContext, ChannelInboundMessageHandlerAdapter}
import java.util.logging.Level

class FactorialServerHandler extends ChannelInboundMessageHandlerAdapter[BigInt] with Logging {

  private[this] var lastMultiplier: BigInt = 1
  private[this] var factorial: BigInt = 1

  def messageReceived(ctx: ChannelHandlerContext, msg: BigInt) {
    // Calculate the cumulative factorial and send it to the client.
    lastMultiplier = msg
    factorial *= msg
    ctx.write(factorial)
  }

  override def channelInactive(ctx: ChannelHandlerContext) {
    logger.info(f"Factorial of $lastMultiplier%,d is: $factorial%,d")
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
    logger.log(Level.WARNING, "Unexpected exception from downstream.", cause)
    ctx.close()
  }
}
