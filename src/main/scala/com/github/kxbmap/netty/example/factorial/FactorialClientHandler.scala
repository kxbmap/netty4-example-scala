package com.github.kxbmap.netty.example
package factorial

import io.netty.buffer.MessageBuf
import io.netty.channel.{ChannelHandlerContext, ChannelInboundMessageHandlerAdapter}
import java.util.logging.Level
import scala.concurrent.Promise

class FactorialClientHandler(count: Int, answer: Promise[BigInt])
  extends ChannelInboundMessageHandlerAdapter[BigInt] with Logging {

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
    @annotation.tailrec
    def addNum(i: Int, out: MessageBuf[AnyRef]): Option[Int] =
      if (i > count) None
      else if (out.size() >= 4096) Some(i)
      else {
        out.add(Int.box(i))
        addNum(i + 1, out)
      }

    val next = addNum(start, ctx.nextOutboundMessageBuffer())
    val f = ctx.flush()

    for (i <- next)
      f onSuccess { case _ => sendNumbers(i) }
  }
}
