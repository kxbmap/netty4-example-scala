package com.github.kxbmap.netty.example
package http.snoop

import io.netty.buffer.Unpooled
import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http.HttpHeaders.Names._
import io.netty.handler.codec.http.HttpHeaders._
import io.netty.handler.codec.http.HttpResponseStatus._
import io.netty.handler.codec.http.HttpVersion._
import io.netty.handler.codec.http.{CookieDecoder, ServerCookieEncoder, LastHttpContent, HttpObject, QueryStringDecoder, DefaultFullHttpResponse, HttpContent, HttpRequest}
import io.netty.util.CharsetUtil
import scala.collection.JavaConverters._


class HttpSnoopServerHandler extends SimpleChannelInboundHandler[AnyRef] {

  import HttpSnoopServerHandler._

  private[this] var request: HttpRequest = _

  /** Buffer that stores the response content */
  private val buf = new StringBuilder()

  override def channelReadComplete(ctx: ChannelHandlerContext): Unit = {
    ctx.flush()
  }

  def channelRead0(ctx: ChannelHandlerContext, msg: AnyRef): Unit =
    msg match {
      case request: HttpRequest =>
        this.request = request
        
        if (is100ContinueExpected(request)) {
          send100Continue(ctx)
        }

        buf.clear()
        buf ++= "WELCOME TO THE WILD WILD WEB SERVER\r\n"
        buf ++= "===================================\r\n"
        buf ++= s"VERSION: ${request.getProtocolVersion}\r\n"
        buf ++= s"HOSTNAME: ${getHost(request, "unknown")}\r\n"
        buf ++= s"REQUEST_URI: ${request.getUri}\r\n\r\n"

        val headers = request.headers().entries
        if (!headers.isEmpty) {
          for (h <- headers.asScala) {
            buf ++= s"HEADER: ${h.getKey} = ${h.getValue}\r\n"
          }
          buf ++= "\r\n"
        }

        val queryStringDecoder = new QueryStringDecoder(request.getUri)
        val params = queryStringDecoder.parameters()
        if (!params.isEmpty) {
          for {
            (key, values) <- params.asScala
            value         <- values.asScala
          } {
            buf ++= s"PARAM: $key = $value\r\n"
          }
          buf ++= "\r\n"
        }

        appendDecoderResult(buf, request)

      case trailer: LastHttpContent =>
        appendContent(buf, trailer)

        buf ++= "END OF CONTENT\r\n"

        if (!trailer.trailingHeaders().isEmpty) {
          buf ++= "\r\n"
          for {
            name  <- trailer.trailingHeaders().names().asScala
            value <- trailer.trailingHeaders().getAll(name).asScala
          } {
            buf ++= s"TRAILING HEADER: $name = $value\r\n"
          }
          buf ++= "\r\n"
        }

        writeResponse(trailer, ctx)

      case httpContent: HttpContent =>
        appendContent(buf, httpContent)

      case _ =>
    }


  private def writeResponse(currentObj: HttpObject, ctx: ChannelHandlerContext): Unit = {
    // Decide whether to close the connection or not.
    val keepAlive = isKeepAlive(request)
    // Build the response object.
    val response = new DefaultFullHttpResponse(
      HTTP_1_1, if (currentObj.getDecoderResult.isSuccess) OK else BAD_REQUEST,
      Unpooled.copiedBuffer(buf.toString(), CharsetUtil.UTF_8)) <| { r =>

      r.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8")

      if (keepAlive) {
        // Add 'Content-Length' header only for a keep-alive connection.
        r.headers().set(CONTENT_LENGTH, r.content().readableBytes())
        // Add keep alive header as per:
        // - http://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-01.html#Connection
        r.headers().set(CONNECTION, Values.KEEP_ALIVE)
      }

      // Encode the cookie.
      Option(r.headers().get(COOKIE)).fold {
        // Browser sent no cookie.  Add some.
        r.headers().add(SET_COOKIE, ServerCookieEncoder.encode("key1", "value1"))
        r.headers().add(SET_COOKIE, ServerCookieEncoder.encode("key2", "value2"))
        ()
      } { cookieString =>
        val cookies = CookieDecoder.decode(cookieString)
        if (!cookies.isEmpty) {
          // Reset the cookies if necessary.
          for (cookie <- cookies.asScala) {
            r.headers().add(SET_COOKIE, ServerCookieEncoder.encode(cookie))
          }
        }
      }
    }

    // Write the response.
    ctx.write(response)
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit = {
    cause.printStackTrace()
    ctx.close()
  }
}


object HttpSnoopServerHandler {

  private def appendContent(buf: StringBuilder, c: HttpContent): Unit = {
    val content = c.content()
    if (content.isReadable) {
      buf ++= s"CONTENT: ${content.toString(CharsetUtil.UTF_8)}\r\n"
      appendDecoderResult(buf, c)
    }
  }

  private def appendDecoderResult(buf: StringBuilder, o: HttpObject): Unit = {
    val result = o.getDecoderResult
    if (result.isFailure) {
      buf ++= s".. WITH DECODER FAILURE: ${result.cause()}\r\n"
    }
  }

  private def send100Continue(ctx: ChannelHandlerContext): Unit = {
    val response = new DefaultFullHttpResponse(HTTP_1_1, CONTINUE)
    ctx.write(response)
  }
}
