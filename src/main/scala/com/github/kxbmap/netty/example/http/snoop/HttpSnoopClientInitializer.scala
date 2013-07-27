package com.github.kxbmap.netty.example
package http.snoop

import com.github.kxbmap.netty.example.securechat.SecureChatSslContextFactory
import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.{HttpContentDecompressor, HttpClientCodec}
import io.netty.handler.logging.{LoggingHandler, LogLevel}
import io.netty.handler.ssl.SslHandler

class HttpSnoopClientInitializer(ssl: Boolean) extends ChannelInitializer[SocketChannel] {

  def initChannel(ch: SocketChannel): Unit = {
    // Create a default pipeline implementation.
    val p = ch.pipeline()

    p.addLast("log", new LoggingHandler(LogLevel.INFO))

    // Enable HTTPS if necessary.
    if (ssl) {
      val engine = SecureChatSslContextFactory.clientContext.createSSLEngine() <| {
        _.setUseClientMode(true)
      }
      p.addLast("ssl", new SslHandler(engine))
    }

    p.addLast("codec", new HttpClientCodec())

    // Remove the following line if you don't want automatic content decompression.
    p.addLast("inflater", new HttpContentDecompressor())

    // Uncomment the following line if you don't want to handle HttpChunks.
//    p.addLast("aggregator", new HttpObjectAggregator(1048576))

    p.addLast("handler", new HttpSnoopClientHandler())
  }
}
