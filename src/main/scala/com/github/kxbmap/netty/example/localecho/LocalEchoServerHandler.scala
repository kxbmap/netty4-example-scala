package com.github.kxbmap.netty.example
package localecho

import io.netty.channel.{ChannelHandlerContext, ChannelInboundHandlerAdapter}

class LocalEchoServerHandler extends ChannelInboundHandlerAdapter {

  override def channelRead(ctx: ChannelHandlerContext, msg: AnyRef): Unit = {
    // Write back as received
    ctx.write(msg)
  }

  override def channelReadComplete(ctx: ChannelHandlerContext): Unit = {
    ctx.flush()
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
    cause.printStackTrace()
    ctx.close()
  }
}
