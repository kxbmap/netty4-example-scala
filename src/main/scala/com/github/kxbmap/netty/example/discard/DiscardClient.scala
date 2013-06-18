package com.github.kxbmap.netty.example
package discard

import io.netty.bootstrap.Bootstrap
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel

/**
 * Keeps sending random data to the specified address.
 */
object DiscardClient extends App with Usage {
  val (host, port, firstMessageSize) =
    parseOptions("<host> <port> [<first message size>]") {
      case List(h, p, s) => (h, p.toInt, s.toInt)
      case List(h, p)    => (h, p.toInt, 256)
    }

  val group = new NioEventLoopGroup()
  try {
    val b = new Bootstrap()
    b.group(group)
      .channel(classOf[NioSocketChannel])
      .handler(new DiscardClientHandler(firstMessageSize))

    // Make the connection attempt.
    val f = b.connect(host, port).sync()

    // Wait until the connection is closed.
    f.channel().closeFuture().sync()
  }
  finally group.shutdownGracefully()

}
