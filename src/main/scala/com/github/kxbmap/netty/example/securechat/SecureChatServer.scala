package com.github.kxbmap.netty.example
package securechat

import io.netty.channel.nio.NioEventLoopGroup
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.socket.nio.NioServerSocketChannel

/**
 * Simple SSL chat server
 */
object SecureChatServer extends App {
  val port = args.headOption.map(_.toInt).getOrElse(8443)

  val bossGroup, workerGroup = new NioEventLoopGroup()
  try {
    val b = new ServerBootstrap()
      .group(bossGroup, workerGroup)
      .channel(classOf[NioServerSocketChannel])
      .childHandler(new SecureChatServerInitializer())

    b.bind(port).sync().channel().closeFuture().sync()
  } finally {
    bossGroup.shutdownGracefully()
    workerGroup.shutdownGracefully()
  }
}
