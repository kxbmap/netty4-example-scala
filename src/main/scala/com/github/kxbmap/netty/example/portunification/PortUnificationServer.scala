package com.github.kxbmap.netty.example
package portunification

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel

/**
 * Serves two protocols (HTTP and Factorial) using only one port, enabling
 * either SSL or GZIP dynamically on demand.
 *
 * Because SSL and GZIP are enabled on demand, 5 combinations per protocol
 * are possible: none, SSL only, GZIP only, SSL + GZIP, and GZIP + SSL.
 */
object PortUnificationServer extends App {

  val port = args.headOption.map(_.toInt).getOrElse(8080)

  val bossGroup, workerGroup = new NioEventLoopGroup()

  try {
    val b = new ServerBootstrap()
      .group(bossGroup, workerGroup)
      .channel(classOf[NioServerSocketChannel])
      .childHandler { ch: SocketChannel =>
        ch.pipeline().addLast(new PortUnificationServerHandler())
      }

    // Bind and start to accept incoming connections.
    b.bind(port).sync().channel().closeFuture().sync()
  } finally {
    bossGroup.shutdownGracefully()
    workerGroup.shutdownGracefully()
  }
}
