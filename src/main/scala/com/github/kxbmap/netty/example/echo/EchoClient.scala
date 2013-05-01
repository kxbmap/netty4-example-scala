package com.github.kxbmap.netty.example
package echo

import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.logging.{LoggingHandler, LogLevel}
import java.net.InetSocketAddress


object EchoClient extends App with Usage {
  val (host, port, firstMessageSize) =
    parseOptions("<host> <port> [<first message size>]") {
      case List(h, p, s) => (h, p.toInt, s.toInt)
      case List(h, p) => (h, p.toInt, 256)
    }

  // Configure the client
  val group = new NioEventLoopGroup()
  try {
    val b = new Bootstrap()
      .group(group)
      .channel(classOf[NioSocketChannel])
      .remoteAddress(new InetSocketAddress(host, port))
      .handler { ch: SocketChannel =>
        ch.pipeline().addLast(
          new LoggingHandler(LogLevel.INFO),
          new EchoClientHandler(firstMessageSize))
      }

    // Start the client.
    val f = b.connect().sync()

    // Wait until the connection is closed.
    f.channel().closeFuture().sync()
  }
  // Shut down the event loop to terminate all threads.
  finally group.shutdownGracefully()
}
