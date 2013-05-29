package com.github.kxbmap.netty.example
package discard

import io.netty.buffer.ByteBuf
import io.netty.channel.{ChannelFuture, ChannelFutureListener, ChannelHandlerContext, ChannelInboundByteHandlerAdapter}
import java.util.logging.Level

class DiscardClientHandler(messageSize: Int) extends ChannelInboundByteHandlerAdapter with Logging {

  require(messageSize > 0, s"messageSize: $messageSize")

  private[this] var ctx: ChannelHandlerContext = _
  private[this] var content: ByteBuf = _

  override def channelActive(ctx: ChannelHandlerContext) {
    this.ctx = ctx

    // Initialize the message.
    content = ctx.alloc().directBuffer(messageSize).writeZero(messageSize)

    // Send the initial messages.
    generateTraffic()
  }

  def inboundBufferUpdated(ctx: ChannelHandlerContext, in: ByteBuf) {
    // Server is supposed to send nothing, but if it sends something, discard it.
    in.clear()
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
    logger.log(Level.WARNING, "Unexpected exception from downstream.", cause)
    ctx.close()
  }

  private def generateTraffic() {
    // Fill the outbound buffer up to 64KiB
    val out = ctx.nextOutboundByteBuffer()
    while (out.readableBytes() < 65536) {
      out.writeBytes(content, 0, content.readableBytes())
    }

    // Flush the outbound buffer to the socket.
    // Once flushed, generate the same amount of traffic again.
    ctx.flush().addListener(trafficGenerator)
  }

  private val trafficGenerator: ChannelFutureListener = {
    future: ChannelFuture =>
      if (future.isSuccess) generateTraffic()
  }

}
