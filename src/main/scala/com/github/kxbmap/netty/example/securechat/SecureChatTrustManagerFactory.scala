package com.github.kxbmap.netty.example
package securechat

import javax.net.ssl.{X509TrustManager, ManagerFactoryParameters, TrustManager, TrustManagerFactorySpi}
import java.security.KeyStore
import java.security.cert.X509Certificate

/**
 * Bogus [[javax.net.ssl.TrustManagerFactorySpi]] which accepts any certificate
 * even if it is invalid.
 */
object SecureChatTrustManagerFactory extends TrustManagerFactorySpi {

  private val DummyTrustManager = new X509TrustManager {
    def getAcceptedIssuers: Array[X509Certificate] = Array.empty

    def checkClientTrusted(chain: Array[X509Certificate], authType: String): Unit = {
      // Always trust - it is an example.
      // You should do something in the real world.
      // You will reach here only if you enabled client certificate auth,
      // as described in SecureChatSslContextFactory.
      Console.err.println(s"UNKNOWN CLIENT CERTIFICATE: ${chain(0).getSubjectDN}")
    }

    def checkServerTrusted(chain: Array[X509Certificate], authType: String): Unit = {
      // Always trust - it is an example.
      // You should do something in the real world.
      Console.err.println(s"UNKNOWN SERVER CERTIFICATE: ${chain(0).getSubjectDN}")
    }
  }

  def engineGetTrustManagers(): Array[TrustManager] = Array(DummyTrustManager)

  def engineInit(ks: KeyStore): Unit = () // Unused
  def engineInit(parameters: ManagerFactoryParameters): Unit = () // Unused
}
