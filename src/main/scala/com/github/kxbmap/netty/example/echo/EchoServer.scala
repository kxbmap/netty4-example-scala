package com.github.kxbmap.netty.example
package echo

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelOption
import io.netty.channel.socket.SocketChannel
import io.netty.handler.logging.{LogLevel, LoggingHandler}
import java.net.InetSocketAddress


object EchoServer extends App with Usage {
  val port = parseOptions("<port>") {
    case p :: Nil => p.toInt
  }

  // Configure the server.
  val bossGroup, workerGroup = new DefaultEventLoopGroup()
  try {
    val b = new ServerBootstrap()
      .group(bossGroup, workerGroup)
      .channel(classOf[DefaultServerSocketChannel])
      .option(ChannelOption.SO_BACKLOG, Int.box(100))
      .localAddress(new InetSocketAddress(port))
      .handler(new LoggingHandler(LogLevel.INFO))
      .childHandler { ch: SocketChannel =>
        ch.pipeline().addLast(
          new LoggingHandler(LogLevel.INFO),
          new EchoServerHandler())
      }

    // Start the server.
    val f = b.bind().sync()

    // Wait until the server socket is closed.
    f.channel().closeFuture().sync()
  } finally {
    // Shut down all event loops to terminate all threads.
    bossGroup.shutdownGracefully()
    workerGroup.shutdownGracefully()
  }
}
