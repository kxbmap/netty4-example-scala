package com.github.kxbmap.netty.example
package factorial

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.compression.{ZlibCodecFactory, ZlibWrapper}

class FactorialClientInitializer(count: Int) extends ChannelInitializer[SocketChannel] {
  def initChannel(ch: SocketChannel): Unit =
    (ch.pipeline() /: Seq(
      // Enable stream compression (you can remove these two if unnecessary)
      "deflater" -> ZlibCodecFactory.newZlibEncoder(ZlibWrapper.GZIP),
      "inflater" -> ZlibCodecFactory.newZlibDecoder(ZlibWrapper.GZIP),

      // Add the number codec first,
      "decoder" -> new BigIntDecoder(),
      "encoder" -> new NumberEncoder(),

      // and then business logic.
      "handler" -> new FactorialClientHandler(count)
    )) {
      case (pipeline, (name, handler)) => pipeline.addLast(name, handler)
    }
}