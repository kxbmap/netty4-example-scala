package com.github.kxbmap.netty

import io.netty.channel.{ChannelFutureListener, ChannelFuture, ChannelInitializer, Channel}
import scala.util.{Failure, Success, Try}

package object example {

  type DefaultEventLoopGroup      = io.netty.channel.nio.NioEventLoopGroup
  type DefaultSocketChannel       = io.netty.channel.socket.nio.NioSocketChannel
  type DefaultServerSocketChannel = io.netty.channel.socket.nio.NioServerSocketChannel


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
    def onSuccess[U](pf: PartialFunction[Channel, U]) {
      onComplete {
        case Success(ch) if pf.isDefinedAt(ch) => pf(ch)
        case _ =>
      }
    }

    def onFailure[U](pf: PartialFunction[Throwable, U]) {
      onComplete {
        case Failure(cause) if pf.isDefinedAt(cause) => pf(cause)
        case _ =>
      }
    }

    def onComplete[U](f: Try[Channel] => U) {
      cf.addListener { future: ChannelFuture => f(
        if (future.isSuccess)
          Success(future.channel())
        else
          Failure(future.cause())
      )}
    }
  }

}
