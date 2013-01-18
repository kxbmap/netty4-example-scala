package com.github.kxbmap.netty.example
package echo

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelOption
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.{NioServerSocketChannel, NioEventLoopGroup}
import io.netty.handler.logging.{LogLevel, LoggingHandler}
import java.net.InetSocketAddress

object EchoServer extends App with Usage {
  val port = parseOptions("<port>") {
    case p :: Nil => p.toInt
  }

  // Configure the server.
  val b = new ServerBootstrap()
  try {
    b.group(new NioEventLoopGroup(), new NioEventLoopGroup())
      .channel(classOf[NioServerSocketChannel])
      .option(ChannelOption.SO_BACKLOG, Int.box(100))
      .localAddress(new InetSocketAddress(port))
      .childOption(ChannelOption.TCP_NODELAY, Boolean.box(true))
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
  }
  // Shut down all event loops to terminate all threads.
  finally b.shutdown()
}
