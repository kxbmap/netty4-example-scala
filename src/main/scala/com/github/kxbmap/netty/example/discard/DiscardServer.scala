package com.github.kxbmap.netty.example
package discard

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel

/**
 * Discards any incoming data.
 */
object DiscardServer extends App {
  val port = args.headOption.map(_.toInt).getOrElse(8080)

  val bossGroup, workerGroup = new NioEventLoopGroup()
  try {
    val b = new ServerBootstrap()
    b.group(bossGroup, workerGroup)
      .channel(classOf[NioServerSocketChannel])
      .childHandler { ch: SocketChannel =>
        ch.pipeline().addLast(new DiscardServerHandler())
      }

    // Bind and start to accept incoming connections.
    val f = b.bind(port).sync()

    // Wait until the server socket is closed.
    // In this example, this does not happen, but you can do that to gracefully
    // shut down your server.
    f.channel().closeFuture().sync()
  } finally {
    bossGroup.shutdownGracefully()
    workerGroup.shutdownGracefully()
  }

}
