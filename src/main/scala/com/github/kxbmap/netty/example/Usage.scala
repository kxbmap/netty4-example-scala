package com.github.kxbmap.netty.example

import scala.reflect.NameTransformer
import scala.util.control.NonFatal

trait Usage {
  this: App =>

  private def showUsage(usage: String): Nothing = {
    Console.err.println(
      s"Usage: ${getClass.getSimpleName stripSuffix NameTransformer.MODULE_SUFFIX_STRING} $usage")
    sys.exit()
  }

  def parseOptions[T](usage: String)(p: PartialFunction[List[String], T]): T =
    try p(args.toList) catch {
      case NonFatal(_) => showUsage(usage)
    }
}
