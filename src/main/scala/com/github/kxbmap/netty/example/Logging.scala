package com.github.kxbmap.netty
package example

import java.util.logging.Logger

trait Logging {
  protected lazy val logger = Logger.getLogger(getClass.getName)
}
