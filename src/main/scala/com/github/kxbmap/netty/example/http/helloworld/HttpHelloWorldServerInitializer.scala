package com.github.kxbmap.netty.example
package http.helloworld

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.HttpServerCodec

class HttpHelloWorldServerInitializer extends ChannelInitializer[SocketChannel] {

  def initChannel(ch: SocketChannel): Unit = {
    val p = ch.pipeline()

    // Uncomment the following line if you want HTTPS
//    val engine = SecureChatSslContextFactory.serverContext.createSSLEngine()
//    engine.setUseClientMode(false)
//    p.addLast("ssl", new SslHandler(engine))

    p.addLast("codec", new HttpServerCodec())
    p.addLast("handler", new HttpHelloWorldServerHandler())
  }
}
