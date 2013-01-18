package com.github.kxbmap.netty

import io.netty.channel.{ChannelFutureListener, ChannelFuture, ChannelInitializer, Channel}
import scala.concurrent.{Promise, Future}
import scala.util.control.NonFatal

package object example {

  implicit final class Tapper[T](val obj: T) extends AnyVal {
    def tap(f: T => Any): T = {
      if (obj != null) f(obj)
      obj
    }
  }

  import scala.language.implicitConversions

  implicit def ToChannelInitializer[C <: Channel, U](f: C => U): ChannelInitializer[C] =
    new ChannelInitializer[C] {
      def initChannel(ch: C) { f(ch) }
    }

  implicit def ToChannelFutureListener[U](f: ChannelFuture => U): ChannelFutureListener =
    new ChannelFutureListener {
      def operationComplete(future: ChannelFuture) { f(future) }
    }

  implicit def NettyFutureToFuture(nettyFuture: ChannelFuture): Future[Channel] = {
    val p = Promise[Channel]()
    nettyFuture.addListener { future: ChannelFuture =>
      if (future.isSuccess)
        p success future.channel()
      else future.cause() match {
        case NonFatal(e) => p failure e
        case fatal => throw fatal
      }
    }
    p.future
  }
}
