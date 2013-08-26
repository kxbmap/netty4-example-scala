package com.github.kxbmap.netty.example
package filetransfer

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.{DefaultFileRegion, ChannelHandlerContext, SimpleChannelInboundHandler, ChannelOption}
import io.netty.handler.codec.LineBasedFrameDecoder
import io.netty.handler.codec.string.{StringDecoder, StringEncoder}
import io.netty.handler.logging.{LogLevel, LoggingHandler}
import io.netty.util.CharsetUtil
import java.io.{FileInputStream, File}

object FileServer extends App {
  val port = args.headOption.map(_.toInt).getOrElse(8080)

  val bossGroup, workerGroup = new NioEventLoopGroup()
  try {
    // Configure the server.
    val b = new ServerBootstrap()
      .group(bossGroup, workerGroup)
      .channel(classOf[NioServerSocketChannel])
      .option(ChannelOption.SO_BACKLOG, Int.box(100))
      .handler(new LoggingHandler(LogLevel.INFO))
      .childHandler { ch: SocketChannel =>
        ch.pipeline().addLast(
          new StringEncoder(CharsetUtil.UTF_8),
          new LineBasedFrameDecoder(8192),
          new StringDecoder(CharsetUtil.UTF_8),
          new FileHandler())
      }

    // Start the server.
    val f = b.bind(port).sync()

    // Wait until the server socket is closed.
    f.channel().closeFuture().sync()
  } finally {
    bossGroup.shutdownGracefully()
    workerGroup.shutdownGracefully()
  }

  final class FileHandler extends SimpleChannelInboundHandler[String] {
    def channelRead0(ctx: ChannelHandlerContext, msg: String): Unit = {
      val file = new File(msg)
      (file.exists(), file.isFile) match {
        case (true, true) =>
          ctx.write(s"$file ${file.length()}\n")
          val fis = new FileInputStream(file)
          val region = new DefaultFileRegion(fis.getChannel, 0, file.length())
          ctx.write(region)
          ctx.writeAndFlush("\n")
          fis.close()

        case (false, _) => ctx.writeAndFlush(s"File not found: $file\n")
        case (_, false) => ctx.writeAndFlush(s"Not a file: $file\n")
      }
    }

    override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
      cause.printStackTrace()
      ctx.close()
    }
  }
}
