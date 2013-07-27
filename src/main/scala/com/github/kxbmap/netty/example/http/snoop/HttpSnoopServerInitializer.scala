package com.github.kxbmap.netty.example
package http.snoop

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.{HttpContentCompressor, HttpResponseEncoder, HttpRequestDecoder}

class HttpSnoopServerInitializer extends ChannelInitializer[SocketChannel] {

  def initChannel(ch: SocketChannel): Unit = {
    // Create a default pipeline implementation.
    val p = ch.pipeline()

    // Uncomment the following line if you want HTTPS
//    val engine = SecureChatSslContextFactory.serverContext.createSSLEngine() <| {
//      _.setUseClientMode(false)
//    }
//    p.addLast("ssl", new SslHandler(engine))

    p.addLast("decoder", new HttpRequestDecoder())
    // Uncomment the following line if you don't want to handle HttpChunks.
//    p.addLast("aggregator", new HttpObjectAggregator(1048576))
    p.addLast("encoder", new HttpResponseEncoder())
    // Remove the following line if you don't want automatic content compression.
    p.addLast("deflater", new HttpContentCompressor())
    p.addLast("handler", new HttpSnoopServerHandler())
  }
}
