package com.github.kxbmap.netty.example
package factorial

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.compression.{ZlibWrapper, ZlibCodecFactory}

class FactorialServerInitializer extends ChannelInitializer[SocketChannel] {
  def initChannel(ch: SocketChannel): Unit =
    (ch.pipeline() /: Seq(
      // Enable stream compression (you can remove these two if unnecessary)
      "deflater" -> ZlibCodecFactory.newZlibEncoder(ZlibWrapper.GZIP),
      "inflater" -> ZlibCodecFactory.newZlibDecoder(ZlibWrapper.GZIP),

      // Add the number codec first,
      "decoder" -> new BigIntDecoder(),
      "encoder" -> new NumberEncoder(),

      // and then business logic.
      // Please note we create a handler for every new channel
      // because it has stateful properties.
      "handler" -> new FactorialServerHandler()
    )) {
      case (pipeline, (name, handler)) => pipeline.addLast(name, handler)
    }
}
