package com.github.kxbmap.netty.example
package localecho

import io.netty.bootstrap.{Bootstrap, ServerBootstrap}
import io.netty.channel.ChannelFuture
import io.netty.channel.local.{LocalChannel, LocalServerChannel, LocalEventLoopGroup, LocalAddress}
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.handler.logging.{LogLevel, LoggingHandler}
import scala.annotation.tailrec

object LocalEcho extends App {
  val id = args.headOption.getOrElse("1")

  // Address to bind on / connect to.
  val addr = new LocalAddress(id)

  val serverGroup = new LocalEventLoopGroup()
  val clientGroup = new NioEventLoopGroup() // NIO event loops are also OK
  try {
    // Note that we can use any event loop to ensure certain local channels
    // are handled by the same event loop thread which drives a certain socket channel
    // to reduce the communication latency between socket channels and local channels.
    val sb = new ServerBootstrap()
      .group(serverGroup)
      .channel(classOf[LocalServerChannel])
      .handler { ch: LocalServerChannel =>
        ch.pipeline().addLast(new LoggingHandler(LogLevel.INFO))
      }
      .childHandler { ch: LocalChannel =>
        ch.pipeline().addLast(
          new LoggingHandler(LogLevel.INFO),
          new LocalEchoServerHandler())
      }

    val cb = new Bootstrap()
      .group(clientGroup)
      .channel(classOf[LocalChannel])
      .handler { ch: LocalChannel =>
        ch.pipeline().addLast(
          new LoggingHandler(LogLevel.INFO),
          new LocalEchoClientHandler())
      }

    // Start the server.
    sb.bind(addr).sync()

    // Start the client.
    val ch = cb.connect(addr).sync().channel()

    // Read commands from the stdin.
    println("Enter text (quit to end)")
    @tailrec
    def readWriteFlushLoop(last: ChannelFuture = null): Option[ChannelFuture] =
      readLine() match {
        case x if x == null || "quit".equalsIgnoreCase(x) => Option(last)
        case line =>
          // Sends the received line to the server.
          val f = ch.writeAndFlush(line)
          readWriteFlushLoop(f)
      }

    val lastWriteFuture = readWriteFlushLoop()

    // Wait until all messages are flushed before closing the channel.
    lastWriteFuture.foreach(_.awaitUninterruptibly())

  } finally {
    serverGroup.shutdownGracefully()
    clientGroup.shutdownGracefully()
  }
}
