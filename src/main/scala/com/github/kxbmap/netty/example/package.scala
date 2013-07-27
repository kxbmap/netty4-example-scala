package com.github.kxbmap.netty

import io.netty.channel.{ChannelFutureListener, ChannelFuture, ChannelInitializer, Channel}
import io.netty.util.concurrent.{GenericFutureListener, Future}
import scala.util.{Failure, Success, Try}

package object example {

  import scala.language.implicitConversions


  implicit final class Tapper[T](val obj: T) extends AnyVal {
    def <|[U](f: T => U): T = { f(obj); obj }
  }

  implicit final class AnyRefOps[A <: AnyRef](val obj: A) extends AnyVal {
    def ??[B >: A](default: => B): B = if (obj ne null) obj else default
  }


  implicit def toChannelInitializer[C <: Channel, U](f: C => U): ChannelInitializer[C] =
    new ChannelInitializer[C] {
      def initChannel(ch: C): Unit = f(ch)
    }


  implicit def toGenericFutureListener[T, U](f: Future[T] => U): GenericFutureListener[Future[T]] =
    new GenericFutureListener[Future[T]] {
      def operationComplete(future: Future[T]): Unit = f(future)
    }

  implicit final class GenericFutureOps[T](val gf: Future[T]) extends AnyVal {

    def onSuccess[U](pf: PartialFunction[T, U]): Unit =
      onComplete {
        case Success(v) if pf.isDefinedAt(v) => pf(v)
        case _ =>
      }

    def onFailure[U](pf: PartialFunction[Throwable, U]): Unit =
      onComplete {
        case Failure(cause) if pf.isDefinedAt(cause) => pf(cause)
        case _ =>
      }

    def onComplete[U](f: Try[T] => U): Unit =
      gf.addListener { future: Future[T] => f(
        if (future.isSuccess)
          Success(future.getNow)
        else
          Failure(future.cause())
      )}
  }


  implicit def toChannelFutureListener[U](f: ChannelFuture => U): ChannelFutureListener =
    new ChannelFutureListener {
      def operationComplete(future: ChannelFuture): Unit = f(future)
    }

  implicit final class ChannelFutureOps(val cf: ChannelFuture) extends AnyVal {

    def onSuccess[U](pf: PartialFunction[Channel, U]): Unit =
      onComplete {
        case Success(ch) if pf.isDefinedAt(ch) => pf(ch)
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
