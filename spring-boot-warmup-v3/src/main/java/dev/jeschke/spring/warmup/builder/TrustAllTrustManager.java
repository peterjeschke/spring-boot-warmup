package dev.jeschke.spring.warmup.builder;

import java.security.cert.X509Certificate;
import javax.net.ssl.X509TrustManager;

@SuppressWarnings("java:S4830")
public class TrustAllTrustManager implements X509TrustManager {
    @Override
    public void checkClientTrusted(final X509Certificate[] chain, final String authType) {
        // Don't check certificates
    }

    @Override
    public void checkServerTrusted(final X509Certificate[] chain, final String authType) {
        // Don't check certificates
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }
}
