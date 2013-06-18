package com.github.kxbmap.netty.example
package objectecho

import io.netty.bootstrap.Bootstrap
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.serialization.{ClassResolvers, ObjectDecoder, ObjectEncoder}

object ObjectEchoClient extends App with Usage {
  val (host, port, firstMessageSize) =
    parseOptions("<host> <port> [<first message size>]") {
      case List(h, p, s) => (h, p.toInt, s.toInt)
      case List(h, p)    => (h, p.toInt, 256)
    }

  val group = new NioEventLoopGroup()
  try new Bootstrap()
    .group(group)
    .channel(classOf[NioSocketChannel])
    .remoteAddress(host, port)
    .handler { ch: SocketChannel =>
      ch.pipeline().addLast(
        new ObjectEncoder(),
        new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
        new ObjectEchoClientHandler(firstMessageSize))
    }
    // Start the connection attempt.
    .connect().sync().channel().closeFuture().sync()
  finally
    group.shutdownGracefully()
}
