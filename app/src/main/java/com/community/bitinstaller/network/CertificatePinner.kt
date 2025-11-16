package com.community.bitinstaller.network

import okhttp3.CertificatePinner

object CertificatePinner {
    fun create(): CertificatePinner {
        return CertificatePinner.Builder()
            .add("api.github.com", "sha256/ORlAAK4LlCCPKGI0XxVFXxPP8SkBKk8JqKuchvKKhSE=")
            .add("api.github.com", "sha256/k2v657xBsOVe1PQRwOsHsw3bsGT2VzIqz5K+59sNQws=")
            .add("api.github.com", "sha256/WoiWRyIOVNa9ihaBciRSC7XHjliYS9VwUGOIud4PB18=")
            .build()
    }
}
