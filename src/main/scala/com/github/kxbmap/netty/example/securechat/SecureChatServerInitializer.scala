package com.github.kxbmap.netty.example
package securechat

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.string.{StringEncoder, StringDecoder}
import io.netty.handler.codec.{DelimiterBasedFrameDecoder, Delimiters}
import io.netty.handler.ssl.SslHandler

/**
 * Creates a newly configured [[io.netty.channel.ChannelPipeline]] for a new channel.
 */
class SecureChatServerInitializer extends ChannelInitializer[SocketChannel] {

  def initChannel(ch: SocketChannel): Unit = {
    val pipeline = ch.pipeline()

    // Add SSL handler first to encrypt and decrypt everything.
    // In this example, we use a bogus certificate in the server side
    // and accept any invalid certificates in the client side.
    // You will need something more complicated to identify both
    // and server in the real world.
    //
    // Read SecureChatSslContextFactory
    // if you need client certificate authentication.

    val engine = SecureChatSslContextFactory.serverContext.createSSLEngine() <| {
      _.setUseClientMode(false)
    }
    pipeline.addLast("ssl", new SslHandler(engine))

    // On top of the SSL handler, add the text line codec.
    pipeline.addLast("framer", new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter():_*))
    pipeline.addLast("decoder", new StringDecoder())
    pipeline.addLast("encoder", new StringEncoder())

    // and then business logic.
    pipeline.addLast("handler", new SecureChatServerHandler())
  }
}
