package com.github.kxbmap.netty.example
package factorial

import io.netty.channel.{MessageList, ChannelInboundHandlerAdapter, ChannelHandlerContext}
import java.util.logging.Level

class FactorialServerHandler extends ChannelInboundHandlerAdapter with Logging {

  private[this] var lastMultiplier: BigInt = 1
  private[this] var factorial: BigInt = 1

  override def messageReceived(ctx: ChannelHandlerContext, msgs: MessageList[AnyRef]) {
    import scala.collection.JavaConversions._
    for (msg <- msgs.cast[BigInt]()) {
      // Calculate the cumulative factorial and send it to the client.
      lastMultiplier = msg
      factorial *= msg
      ctx.write(factorial)
    }
    msgs.recycle()
  }

  override def channelInactive(ctx: ChannelHandlerContext) {
    logger.info(f"Factorial of $lastMultiplier%,d is: $factorial%,d")
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
    logger.log(Level.WARNING, "Unexpected exception from downstream.", cause)
    ctx.close()
  }
}
