package com.github.kxbmap.netty.example
package discard

import io.netty.bootstrap.Bootstrap

/**
 * Keeps sending random data to the specified address.
 */
object DiscardClient extends App with Usage {
  val (host, port, firstMessageSize) =
    parseOptions("<host> <port> [<first message size>]") {
      case h :: p :: xs => (h, p.toInt, xs.headOption.map(_.toInt).getOrElse(256))
    }

  val group = new DefaultEventLoopGroup()
  try {
    val b = new Bootstrap()
    b.group(group)
      .channel(classOf[DefaultSocketChannel])
      .handler(new DiscardClientHandler(firstMessageSize))

    // Make the connection attempt.
    val f = b.connect(host, port).sync()

    // Wait until the connection is closed.
    f.channel().closeFuture().sync()
  }
  finally group.shutdownGracefully()

}
