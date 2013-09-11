package com.github.kxbmap.netty.example
package portunification

import com.github.kxbmap.netty.example.factorial.{BigIntDecoder, NumberEncoder, FactorialServerHandler}
import com.github.kxbmap.netty.example.http.snoop.HttpSnoopServerHandler
import com.github.kxbmap.netty.example.securechat.SecureChatSslContextFactory
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import io.netty.handler.codec.compression.{ZlibWrapper, ZlibCodecFactory}
import io.netty.handler.codec.http.{HttpContentCompressor, HttpResponseEncoder, HttpRequestDecoder}
import io.netty.handler.ssl.SslHandler
import java.util

/**
 * Manipulates the current pipeline dynamically to switch protocols or enable
 * SSL or GZIP.
 */
class PortUnificationServerHandler private (detectSsl: Boolean, detectGzip: Boolean) extends ByteToMessageDecoder {

  def this() = this(true, true)

  def decode(ctx: ChannelHandlerContext, in: ByteBuf, out: util.List[AnyRef]): Unit =
    if (in.readableBytes() >= 5) {
      if (isSsl(in)) {
        enableSsl(ctx)
      } else {
        val magic1 = in.getUnsignedByte(in.readerIndex())
        val magic2 = in.getUnsignedByte(in.readerIndex() + 1)
        if (isGzip(magic1, magic2)) {
          enableGzip(ctx)
        } else if (isHttp(magic1, magic2)) {
          switchToHttp(ctx)
        } else if (isFactorial(magic1)) {
          switchToFactorial(ctx)
        } else {
          // Unknown protocol; discard everything and close the connection.
          in.clear()
          ctx.close()
        }
      }
    }

  private def isSsl(buf: ByteBuf): Boolean =
    detectSsl && SslHandler.isEncrypted(buf)

  private def isGzip(magic1: Int, magic2: Int): Boolean =
    detectGzip && magic1 == 31 && magic2 == 139

  private def isHttp(magic1: Int, magic2: Int): Boolean =
    magic1 == 'G' && magic2 == 'E' || // GET
    magic1 == 'P' && magic2 == 'O' || // POST
    magic1 == 'P' && magic2 == 'U' || // PUT
    magic1 == 'H' && magic2 == 'E' || // HEAD
    magic1 == 'O' && magic2 == 'P' || // OPTIONS
    magic1 == 'P' && magic2 == 'A' || // PATCH
    magic1 == 'D' && magic2 == 'E' || // DELETE
    magic1 == 'T' && magic2 == 'R' || // TRACE
    magic1 == 'C' && magic2 == 'O'    // CONNECT

  private def isFactorial(magic1: Int): Boolean =
    magic1 == 'F'

  private def enableSsl(ctx: ChannelHandlerContext): Unit = {
    val p = ctx.pipeline()
    val engine = SecureChatSslContextFactory.serverContext.createSSLEngine() <| {
      _.setUseClientMode(false)
    }
    p.addLast("ssl", new SslHandler(engine))
    p.addLast("unificationA", new PortUnificationServerHandler(false, detectGzip))
    p.remove(this)
  }

  private def enableGzip(ctx: ChannelHandlerContext): Unit = {
    val p = ctx.pipeline()
    p.addLast("gzipdeflater", ZlibCodecFactory.newZlibEncoder(ZlibWrapper.GZIP))
    p.addLast("gzipinflater", ZlibCodecFactory.newZlibDecoder(ZlibWrapper.GZIP))
    p.addLast("unificationB", new PortUnificationServerHandler(detectSsl, false))
    p.remove(this)
  }

  private def switchToHttp(ctx: ChannelHandlerContext): Unit = {
    val p = ctx.pipeline()
    p.addLast("decoder", new HttpRequestDecoder())
    p.addLast("encoder", new HttpResponseEncoder())
    p.addLast("deflater", new HttpContentCompressor())
    p.addLast("handler", new HttpSnoopServerHandler())
    p.remove(this)
  }

  private def switchToFactorial(ctx: ChannelHandlerContext): Unit = {
    val p = ctx.pipeline()
    p.addLast("decoder", new BigIntDecoder())
    p.addLast("encoder", new NumberEncoder())
    p.addLast("handler", new FactorialServerHandler())
    p.remove(this)
  }
}
