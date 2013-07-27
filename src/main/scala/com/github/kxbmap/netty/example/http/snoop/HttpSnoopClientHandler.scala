package com.github.kxbmap.netty.example
package http.snoop

import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http.{HttpHeaders, LastHttpContent, HttpContent, HttpResponse, HttpObject}
import io.netty.util.CharsetUtil
import scala.collection.JavaConverters._

class HttpSnoopClientHandler extends SimpleChannelInboundHandler[HttpObject] {

  def channelRead0(ctx: ChannelHandlerContext, msg: HttpObject): Unit =
    msg match {
      case response: HttpResponse =>
        println(s"STATUS: ${response.getStatus}")
        println(s"VERSION: ${response.getProtocolVersion}")
        println()

        if (!response.headers().isEmpty) {
          for {
            name  <- response.headers().names().asScala
            value <- response.headers().getAll(name).asScala
          } {
            println(s"HEADER: $name = $value")
          }
          println()
        }

        if (HttpHeaders.isTransferEncodingChunked(response)) {
          println("CHUNKED CONTENT {")
        } else {
          println("CONTENT {")
        }

      case content: HttpContent =>
        println(content.content().toString(CharsetUtil.UTF_8))
        Console.flush()

        if (content.isInstanceOf[LastHttpContent]) {
          println("} END OF CONTENT")
        }

      case _ =>
    }
}
