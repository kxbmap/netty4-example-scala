package com.github.kxbmap.netty.example
package factorial

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel

object FactorialServer extends App with Usage {
  val port = args.headOption.map(_.toInt).getOrElse(8080)

  val bossGroup, workerGroup = new NioEventLoopGroup()

  try new ServerBootstrap()
    .group(bossGroup, workerGroup)
    .channel(classOf[NioServerSocketChannel])
    .localAddress(port)
    .childHandler(new FactorialServerInitializer())
    .bind().sync()
    .channel().closeFuture().sync()
  finally {
    bossGroup.shutdownGracefully()
    workerGroup.shutdownGracefully()
  }
}