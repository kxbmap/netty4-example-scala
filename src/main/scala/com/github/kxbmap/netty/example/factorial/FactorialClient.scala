package com.github.kxbmap.netty.example
package factorial

import io.netty.bootstrap.Bootstrap
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import scala.concurrent.Await
import scala.concurrent.duration.Duration

object FactorialClient extends App with Usage {
  val (host, port, count) =
    parseOptions("<host> <port> <count>") {
      case List(h, p, c) => (h, p.toInt, c.toInt)
    }

  val group = new NioEventLoopGroup()
  try {
    val b = new Bootstrap()
      .group(group)
      .channel(classOf[NioSocketChannel])
      .remoteAddress(host, port)
      .handler(new FactorialClientInitializer(count))

    // Make a new connection.
    val f = b.connect().sync()

    // Get the handler instance to retrieve the answer.
    val handler = f.channel().pipeline().last().asInstanceOf[FactorialClientHandler]

    // Print out the answer.
    val answer = Await.result(handler.factorial, Duration.Inf)
    Console.err.println(f"Factorial of $count%,d is: $answer%,d")
  }
  finally group.shutdownGracefully()
}
