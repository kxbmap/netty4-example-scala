package com.github.kxbmap.netty.example
package discard

import io.netty.buffer.ByteBuf
import io.netty.channel.{SimpleChannelInboundHandler, ChannelHandlerContext}
import java.util.logging.Level

class DiscardClientHandler(messageSize: Int) extends SimpleChannelInboundHandler[AnyRef] with Logging {

  require(messageSize > 0, s"messageSize: $messageSize")

  private[this] var content: ByteBuf = _

  override def channelActive(ctx: ChannelHandlerContext): Unit = {
    // Initialize the message.
    content = ctx.alloc().directBuffer(messageSize).writeZero(messageSize)

    // Send the initial messages.
    generateTraffic()(ctx)
  }

  override def channelInactive(ctx: ChannelHandlerContext): Unit = {
    content.release()
  }

  def channelRead0(ctx: ChannelHandlerContext, msg: AnyRef): Unit = {
    // Server is supposed to send nothing, but if it sends something, discard it.
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
    logger.log(Level.WARNING, "Unexpected exception from downstream.", cause)
    ctx.close()
  }

  private def generateTraffic()(implicit ctx: ChannelHandlerContext): Unit = {
    // Flush the outbound buffer to the socket.
    // Once flushed, generate the same amount of traffic again.
    ctx.writeAndFlush(content.duplicate().retain()) onSuccess {
      case _ => generateTraffic()
    }
  }

}
