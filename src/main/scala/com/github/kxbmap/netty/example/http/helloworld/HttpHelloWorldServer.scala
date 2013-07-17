package com.github.kxbmap.netty.example
package http.helloworld

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel

object HttpHelloWorldServer extends App {
  val port = args.headOption.map(_.toInt).getOrElse(8080)

  val bossGroup, workerGroup = new NioEventLoopGroup()
  try {
    val b = new ServerBootstrap()
    b.option(ChannelOption.SO_BACKLOG, Int.box(1024))
      .group(bossGroup, workerGroup)
      .channel(classOf[NioServerSocketChannel])
      .childHandler(new HttpHelloWorldServerInitializer())

    val ch = b.bind(port).sync().channel()
    ch.closeFuture().sync()
  } finally {
    bossGroup.shutdownGracefully()
    workerGroup.shutdownGracefully()
  }
}
