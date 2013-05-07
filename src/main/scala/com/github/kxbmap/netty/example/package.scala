package com.github.kxbmap.netty

import io.netty.channel.{ChannelFutureListener, ChannelFuture, ChannelInitializer, Channel}
import scala.util.{Failure, Success, Try}

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


  implicit final class ChannelFutureOps(val cf: ChannelFuture) extends AnyVal {
    def onSuccess[U](pf: PartialFunction[Channel, U]): ChannelFuture =
      cf.addListener { future: ChannelFuture =>
        if (future.isSuccess && pf.isDefinedAt(future.channel()))
          pf(future.channel())
      }

    def onFailure[U](pf: PartialFunction[Throwable, U]): ChannelFuture =
      cf.addListener { future: ChannelFuture =>
        if (!future.isSuccess && pf.isDefinedAt(future.cause()))
          pf(future.cause())
      }

    def onComplete[U](f: Try[Channel] => U): ChannelFuture =
      cf.addListener { future: ChannelFuture => f(
        if (future.isSuccess)
          Success(future.channel())
        else
          Failure(future.cause())
      )}
  }

}
