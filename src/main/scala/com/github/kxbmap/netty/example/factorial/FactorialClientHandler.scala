package com.github.kxbmap.netty.example
package factorial

import io.netty.channel.{ChannelFuture, SimpleChannelInboundHandler, ChannelHandlerContext}
import java.util.logging.Level
import scala.annotation.tailrec
import scala.concurrent.{Future, promise}

/**
 * Handler for a client-side channel.  This handler maintains stateful
 * information which is specific to a certain channel using member variables.
 * Therefore, an instance of this handler can cover only one channel.  You have
 * to create a new handler instance whenever you create a new channel and insert
 * this handler to avoid a race condition.
 */
class FactorialClientHandler(count: Int) extends SimpleChannelInboundHandler[BigInt] with Logging {

  require(count > 0, s"count: $count")

  private[this] var receivedMessages: Int = 0

  private val answer = promise[BigInt]()

  def factorial: Future[BigInt] = answer.future

  override def channelActive(ctx: ChannelHandlerContext): Unit = {
    sendNumbers(1)(ctx)
  }

  def channelRead0(ctx: ChannelHandlerContext, msg: BigInt): Unit = {
    receivedMessages += 1
    if (receivedMessages == count) {
      // Completes the answer after closing the connection.
      ctx.channel().close() onComplete { _ => answer success msg }
    }
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
    logger.log(Level.WARNING, "Unexpected exception from downstream.", cause)
    ctx.close() onComplete { _ => answer failure cause }
  }

  private def sendNumbers(start: Int)(implicit ctx: ChannelHandlerContext): Unit = {
    @tailrec
    def sendNumber(i: Int, remain: Int, lastWrite: ChannelFuture): Unit =
      if (i > count) ()
      else if (remain == 0)
        lastWrite onSuccess { case _ => sendNumbers(i) }
      else
        sendNumber(i + 1, remain - 1, ctx.write(Int.box(i)))

    // Do not send more than 4096 numbers.
    sendNumber(start, 4096, null)

    ctx.flush()
  }

}
