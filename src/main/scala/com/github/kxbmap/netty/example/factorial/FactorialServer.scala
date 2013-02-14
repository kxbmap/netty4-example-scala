package com.github.kxbmap.netty.example
package factorial

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel

object FactorialServer extends App with Usage {
  val port = parseOptions("<port>") {
    case p :: Nil => p.toInt
  }

  val b = new ServerBootstrap()
  try b.group(new NioEventLoopGroup(), new NioEventLoopGroup())
    .channel(classOf[NioServerSocketChannel])
    .localAddress(port)
    .childHandler(new FactorialServerInitializer())
    .bind().sync()
    .channel().closeFuture().sync()
  finally b.shutdown()
}