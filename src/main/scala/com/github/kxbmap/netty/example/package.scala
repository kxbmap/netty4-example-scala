package com.github.kxbmap.netty

import io.netty.channel.{ChannelFutureListener, ChannelFuture, ChannelInitializer, Channel}
import scala.util.{Failure, Success, Try}

package object example {

  implicit final class Tapper[T](val obj: T) extends AnyVal {
    def <|[U](f: T => U): T = { f(obj); obj }
  }

  import scala.language.implicitConversions

  implicit def ToChannelInitializer[C <: Channel, U](f: C => U): ChannelInitializer[C] =
    new ChannelInitializer[C] {
      def initChannel(ch: C): Unit = f(ch)
    }

  implicit def ToChannelFutureListener[U](f: ChannelFuture => U): ChannelFutureListener =
    new ChannelFutureListener {
      def operationComplete(future: ChannelFuture): Unit = { f(future) }
    }


  implicit final class ChannelFutureOps(val cf: ChannelFuture) extends AnyVal {

    def onSuccess[U](pf: PartialFunction[Channel, U]): Unit =
      onComplete {
        case Success(ch) if pf.isDefinedAt(ch) => pf(ch)
        case _ =>
      }

    def onFailure[U](pf: PartialFunction[Throwable, U]): Unit =
      onComplete {
        case Failure(cause) if pf.isDefinedAt(cause) => pf(cause)
        case _ =>
      }

    def onComplete[U](f: Try[Channel] => U): Unit =
      cf.addListener { future: ChannelFuture => f(
        if (future.isSuccess)
          Success(future.channel())
        else
          Failure(future.cause())
      )}

    def closeOnComplete(): Unit         = cf.addListener(ChannelFutureListener.CLOSE)
    def closeOnFailure(): Unit          = cf.addListener(ChannelFutureListener.CLOSE_ON_FAILURE)
    def fireExceptionOnFailure(): Unit  = cf.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE)
  }

}
