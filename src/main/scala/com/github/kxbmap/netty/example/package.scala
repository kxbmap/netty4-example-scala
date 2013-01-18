package com.github.kxbmap.netty

import io.netty.channel.{ChannelInitializer, Channel}

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
}
