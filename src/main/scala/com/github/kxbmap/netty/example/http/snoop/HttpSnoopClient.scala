package com.github.kxbmap.netty.example
package http.snoop

import java.net.URI
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.bootstrap.Bootstrap
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.http.{DefaultCookie, ClientCookieEncoder, HttpHeaders, HttpMethod, HttpVersion, DefaultHttpRequest}

object HttpSnoopClient extends App with Usage {
  val uri = parseOptions("<URL>") {
    case s :: Nil => new URI(s)
  }

  val scheme = uri.getScheme ?? "http"
  val host = uri.getHost ?? "localhost"
  val (port, ssl) = (scheme.toLowerCase, uri.getPort) match {
    case ("http", -1)  => (80, false)
    case ("http", p)   => (p, false)
    case ("https", -1) => (443, true)
    case ("https", p)  => (p, true)
    case _ =>
      Console.err.println("Only HTTP(S) is supported.")
      sys.exit()
  }

  // Configure the client.
  val group = new NioEventLoopGroup()
  try {
    val b = new Bootstrap()
      .group(group)
      .channel(classOf[NioSocketChannel])
      .handler(new HttpSnoopClientInitializer(ssl))

    // Make the connection attempt.
    val ch = b.connect(host, port).sync().channel()

    // Prepare the HTTP request.
    val request =
      new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, uri.getRawPath) <| { r =>
        import HttpHeaders.Names._
        import HttpHeaders.Values._
        r.headers().set(HOST, host)
        r.headers().set(CONNECTION, CLOSE)
        r.headers().set(ACCEPT_ENCODING, GZIP)

        // Set some example cookies.
        r.headers().set(COOKIE, ClientCookieEncoder.encode(
          new DefaultCookie("my-cookie", "foo"),
          new DefaultCookie("another-cookie", "bar")
        ))
      }

    // Send the HTTP request.
    ch.writeAndFlush(request)

    // Wait for the server to close the connection.
    ch.closeFuture().sync()
  } finally {
    // Shut down executor threads to exit.
    group.shutdownGracefully()
  }
}
