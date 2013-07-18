package com.github.kxbmap.netty.example
package securechat

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.ssl.SslHandler
import io.netty.handler.codec.{Delimiters, DelimiterBasedFrameDecoder}
import io.netty.handler.codec.string.{StringEncoder, StringDecoder}

/**
 * Creates a newly configured [[io.netty.channel.ChannelPipeline]] for a new channel.
 */
class SecureChatClientInitializer extends ChannelInitializer[SocketChannel] {

  def initChannel(ch: SocketChannel): Unit = {
    val pipeline = ch.pipeline()

    // Add SSL handler first to encrypt and decrypt everything.
    // In this example, we use a bogus certificate in the server side
    // and accept any invalid certificates in the client side.
    // You will need something more complicated to identify both
    // and server in the real world.

    val engine = SecureChatSslContextFactory.clientContext.createSSLEngine() <| {
      _.setUseClientMode(true)
    }
    pipeline.addLast("ssl", new SslHandler(engine))

    // On top of the SSL handler, add the text line codec.
    pipeline.addLast("framer", new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter():_*))
    pipeline.addLast("decoder", new StringDecoder())
    pipeline.addLast("encoder", new StringEncoder())

    // and then business logic.
    pipeline.addLast("handler", new SecureChatClientHandler())
  }
}
