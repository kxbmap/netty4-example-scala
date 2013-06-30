package com.github.kxbmap.netty.example
package discard

import io.netty.buffer.ByteBuf
import io.netty.channel.{MessageList, ChannelInboundHandlerAdapter, ChannelFuture, ChannelFutureListener, ChannelHandlerContext}
import java.util.logging.Level

class DiscardClientHandler(messageSize: Int) extends ChannelInboundHandlerAdapter with Logging {

  require(messageSize > 0, s"messageSize: $messageSize")

  private[this] var ctx: ChannelHandlerContext = _
  private[this] var content: ByteBuf = _

  override def channelActive(ctx: ChannelHandlerContext): Unit = {
    this.ctx = ctx

    // Initialize the message.
    content = ctx.alloc().directBuffer(messageSize).writeZero(messageSize)

    // Send the initial messages.
    generateTraffic()
  }

  override def channelInactive(ctx: ChannelHandlerContext): Unit = {
    content.release()
  }

  override def messageReceived(ctx: ChannelHandlerContext, msgs: MessageList[AnyRef]): Unit = {
    // Server is supposed to send nothing, but if it sends something, discard it.
    msgs.releaseAllAndRecycle()
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
    logger.log(Level.WARNING, "Unexpected exception from downstream.", cause)
    ctx.close()
  }

  private def generateTraffic(): Unit = {
    // Flush the outbound buffer to the socket.
    // Once flushed, generate the same amount of traffic again.
    ctx.write(content.duplicate().retain()).addListener(trafficGenerator)
  }

  private val trafficGenerator: ChannelFutureListener = {
    future: ChannelFuture =>
      if (future.isSuccess) generateTraffic()
  }

}
