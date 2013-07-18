package com.github.kxbmap.netty.example
package securechat

import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelFuture
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import scala.annotation.tailrec

/**
 * Simple SSL chat client
 */
object SecureChatClient extends App with Usage {
  val (host, port) =
    parseOptions("<host> <port>") {
      case List(h, p) => (h, p.toInt)
    }

  val group = new NioEventLoopGroup()
  try {
    val b = new Bootstrap()
      .group(group)
      .channel(classOf[NioSocketChannel])
      .handler(new SecureChatClientInitializer())

    // Start the connection attempt.
    val ch = b.connect(host, port).sync().channel()

    // Read commands from the stdin.
    @tailrec
    def readWriteFlushLoop(last: ChannelFuture = null): Option[ChannelFuture] =
      readLine() match {
        case null => Option(last)
        case line =>
          // Sends the received line to the server.
          val future = ch.writeAndFlush(s"$line\r\n")

          // If user typed the 'bye' command, wait until the server closes
          // the connection.
          if ("bye" == line.toLowerCase) {
            ch.closeFuture().sync()
            Some(future)
          }
          else readWriteFlushLoop(future)
      }

    val lastWriteFuture = readWriteFlushLoop()

    // Wait until all messages are flushed before closing the channel.
    lastWriteFuture.foreach(_.sync())

  } finally {
    // The connection is closed automatically on shutdown.
    group.shutdownGracefully()
  }
}
