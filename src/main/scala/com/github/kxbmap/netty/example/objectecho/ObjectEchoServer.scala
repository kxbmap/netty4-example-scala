package com.github.kxbmap.netty.example
package objectecho

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.{NioServerSocketChannel, NioEventLoopGroup}
import io.netty.handler.codec.serialization.{ClassResolvers, ObjectDecoder, ObjectEncoder}
import io.netty.handler.logging.{LoggingHandler, LogLevel}
import java.net.InetSocketAddress

object ObjectEchoServer extends App with Usage {
  val port = parseOptions("<port>") {
    case p :: Nil => p.toInt
  }

  val b = new ServerBootstrap()
  try
    b.group(new NioEventLoopGroup(), new NioEventLoopGroup())
      .channel(classOf[NioServerSocketChannel])
      .localAddress(new InetSocketAddress(port))
      .handler(new LoggingHandler(LogLevel.INFO))
      .childHandler { ch: SocketChannel =>
        ch.pipeline().addLast(
          new ObjectEncoder(),
          new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
          new LoggingHandler(LogLevel.INFO),
          new ObjectEchoServerHandler())
      }
      // Bind and start to accept incoming connections.
      .bind().sync().channel().closeFuture().sync()
  finally
    b.shutdown()
}
