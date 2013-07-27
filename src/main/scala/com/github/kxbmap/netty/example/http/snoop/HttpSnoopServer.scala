package com.github.kxbmap.netty.example
package http.snoop

import io.netty.channel.nio.NioEventLoopGroup
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.socket.nio.NioServerSocketChannel

/**
 * An HTTP server that sends back the content of the received HTTP request
 * in a pretty plaintext form.
 */
object HttpSnoopServer extends App {
  val port = args.headOption.map(_.toInt).getOrElse(8080)

  // Configure the server.
  val bossGroup, workerGroup = new NioEventLoopGroup()
  try {
    val b = new ServerBootstrap()
    b.group(bossGroup, workerGroup)
      .channel(classOf[NioServerSocketChannel])
      .childHandler(new HttpSnoopServerInitializer())

    val ch = b.bind(port).sync().channel()
    ch.closeFuture().sync()
  } finally {
    bossGroup.shutdownGracefully()
    workerGroup.shutdownGracefully()
  }
}
