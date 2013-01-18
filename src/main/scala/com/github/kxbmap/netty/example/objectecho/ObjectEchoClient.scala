package com.github.kxbmap.netty.example
package objectecho

import io.netty.bootstrap.Bootstrap
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.{NioSocketChannel, NioEventLoopGroup}
import io.netty.handler.codec.serialization.{ClassResolvers, ObjectDecoder, ObjectEncoder}
import io.netty.handler.logging.{LoggingHandler, LogLevel}

object ObjectEchoClient extends App with Usage {
  val (host, port, firstMessageSize) =
    parseOptions("<host> <port> [<first message size>]") {
      case List(h, p, s) => (h, p.toInt, s.toInt.ensuring(_ > 0))
      case List(h, p) => (h, p.toInt, 256)
    }

  val b = new Bootstrap()
  try
    b.group(new NioEventLoopGroup())
      .channel(classOf[NioSocketChannel])
      .remoteAddress(host, port)
      .handler { ch: SocketChannel =>
        ch.pipeline().addLast(
          new ObjectEncoder(),
          new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
          new LoggingHandler(LogLevel.INFO),
          new ObjectEchoClientHandler(firstMessageSize))
      }
      // Start the connection attempt.
      .connect().sync().channel().closeFuture().sync()
  finally
    b.shutdown()
}
