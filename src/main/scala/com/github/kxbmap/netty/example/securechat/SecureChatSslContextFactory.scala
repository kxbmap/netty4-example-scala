package com.github.kxbmap.netty.example
package securechat

import java.security.{KeyStore, Security}
import javax.net.ssl.{SSLContext, KeyManagerFactory}
import scala.util.control.NonFatal

/**
 * Creates a bogus [[javax.net.ssl.SSLContext]].  A client-side context created by this
 * factory accepts any certificate even if it is invalid.  A server-side context
 * created by this factory sends a bogus certificate defined in `SecureChatKeyStore`.
 * <p>
 * You will have to create your context differently in a real world application.
 *
 * <h3>Client Certificate Authentication</h3>
 *
 * To enable client certificate authentication:
 * <ul>
 * <li>Enable client authentication on the server side by calling
 *     [[javax.net.ssl.SSLEngine#setNeedClientAuth]] before creating
 *     [[io.netty.handler.ssl.SslHandler]].</li>
 * <li>When initializing an [[javax.net.ssl.SSLContext]] on the client side,
 *     specify the [[javax.net.ssl.KeyManager]] that contains the client certificate as
 *     the first argument of [[javax.net.ssl.SSLContext#init]].</li>
 * <li>When initializing an [[javax.net.ssl.SSLContext]] on the server side,
 *     specify the proper [[javax.net.ssl.TrustManager]] as the second argument of
 *     [[javax.net.ssl.SSLContext#init]]
 *     to validate the client certificate.</li>
 * </ul>
 */
object SecureChatSslContextFactory {

  private val Protocol = "TLS"

  lazy val serverContext: SSLContext = {
    val algorithm =
      Option(Security.getProperty("ssl.KeyManagerFactory.algorithm")).getOrElse("SunX509")

    try {
      val ks = KeyStore.getInstance("JKS") <| {
        _.load(SecureChatKeyStore.asInputStream, SecureChatKeyStore.keyStorePassword)
      }

      // Set up key manager factory to use our key store
      val kmf = KeyManagerFactory.getInstance(algorithm) <| {
        _.init(ks, SecureChatKeyStore.certificatePassword)
      }

      // Initialize the SSLContext to work with our key managers.
      SSLContext.getInstance(Protocol) <| {
        _.init(kmf.getKeyManagers, null, null)
      }
    } catch {
      case NonFatal(e) =>
        throw new Error("Failed to initialize the server-side SSLContext", e)
    }
  }

  lazy val clientContext =
    try
      SSLContext.getInstance(Protocol) <| {
        _.init(null, SecureChatTrustManagerFactory.engineGetTrustManagers(), null)
      }
    catch {
      case NonFatal(e) =>
        throw new Error("Failed to initialize the client-side SSLContext", e)
    }

}
