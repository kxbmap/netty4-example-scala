package com.github.kxbmap.netty.example
package factorial

import io.netty.channel.{MessageList, SimpleChannelInboundHandler, ChannelHandlerContext}
import java.util.logging.Level
import scala.concurrent.Promise

class FactorialClientHandler(count: Int, answer: Promise[BigInt]) extends SimpleChannelInboundHandler[BigInt] with Logging {

  require(count > 0, s"count: $count")

  private[this] var receivedMessages: Int = 0

  override def channelActive(ctx: ChannelHandlerContext) {
    sendNumbers(1)(ctx)
  }

  def messageReceived(ctx: ChannelHandlerContext, msg: BigInt) {
    receivedMessages += 1
    if (receivedMessages == count) {
      // Completes the answer after closing the connection.
      ctx.channel().close() onComplete { _ => answer success msg }
    }
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
    logger.log(Level.WARNING, "Unexpected exception from downstream.", cause)
    ctx.close() onComplete { _ => answer failure cause }
  }

  private def sendNumbers(start: Int)(implicit ctx: ChannelHandlerContext) {
    // Do not send more than 4096 numbers.
    val out = MessageList.newInstance[Integer](4096)

    @annotation.tailrec
    def addNum(i: Int): Option[Int] =
      if (i > count) None
      else if (out.size() >= 4096) Some(i)
      else {
        out.add(i)
        addNum(i + 1)
      }

    val next = addNum(start)
    val f = ctx.write(out)

    for (i <- next)
      f onSuccess { case _ => sendNumbers(i) }
  }

}
