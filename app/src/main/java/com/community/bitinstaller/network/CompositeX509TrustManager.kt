package com.community.bitinstaller.network

import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

/**
 * Composite TrustManager that tries system certificates first (allows AdGuard/debugging),
 * then falls back to pinned certificates for security.
 */
class CompositeX509TrustManager(
    private val systemTrustManager: X509TrustManager,
    private val pinnedTrustManager: X509TrustManager
) : X509TrustManager {

    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
        systemTrustManager.checkClientTrusted(chain, authType)
    }

    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
        try {
            // Try system trust first (allows user-installed certs like AdGuard)
            systemTrustManager.checkServerTrusted(chain, authType)
        } catch (systemException: CertificateException) {
            // Fall back to pinned certificates
            pinnedTrustManager.checkServerTrusted(chain, authType)
        }
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> =
        systemTrustManager.acceptedIssuers + pinnedTrustManager.acceptedIssuers
}
