package com.github.kxbmap.netty.example
package localecho

import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}

class LocalEchoClientHandler extends SimpleChannelInboundHandler[AnyRef] {

  def channelRead0(ctx: ChannelHandlerContext, msg: AnyRef): Unit = {
    // Print as received
    println(msg)
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
    cause.printStackTrace()
    ctx.close()
  }
}
