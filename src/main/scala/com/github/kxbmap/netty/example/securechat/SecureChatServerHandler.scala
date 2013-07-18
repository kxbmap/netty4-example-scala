package com.github.kxbmap.netty.example
package securechat

import io.netty.channel.group.DefaultChannelGroup
import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.ssl.SslHandler
import io.netty.util.concurrent.GlobalEventExecutor
import java.net.InetAddress
import java.util.logging.Level
import scala.collection.JavaConverters._

/**
 * Handles a server-side channel.
 */
class SecureChatServerHandler extends SimpleChannelInboundHandler[String] with Logging {

  import SecureChatServerHandler.channels

  override def channelActive(ctx: ChannelHandlerContext): Unit = {
    val sslHandler = ctx.pipeline().get(classOf[SslHandler])

    // Once session is secured, send a greeting and register the channel to the global channel
    // list so the channel received the messages from others.
    sslHandler.handshakeFuture() onComplete { _ =>
      ctx.writeAndFlush(
        s"Welcome to ${InetAddress.getLocalHost.getHostName} secure chat service!\n")
      ctx.writeAndFlush(
        s"Your session is protected by ${sslHandler.engine().getSession.getCipherSuite} cipher suite\n")

      channels.add(ctx.channel())
    }
  }

  def channelRead0(ctx: ChannelHandlerContext, msg: String): Unit = {
    // Send the received message to all channels.
    for (c <- channels.asScala) {
      val line =
        if (c != ctx.channel())
          s"[${ctx.channel().remoteAddress()}] $msg\n"
        else
          s"[you] $msg\n"

      c.writeAndFlush(line)
    }

    // Close the connection if the client has sent 'bye'.
    if ("bye" == msg.toLowerCase) {
      ctx.close()
    }
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
    logger.log(Level.WARNING, "Unexpected exception from downstream.", cause)
    ctx.close()
  }
}

object SecureChatServerHandler {
  private val channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE)
}
