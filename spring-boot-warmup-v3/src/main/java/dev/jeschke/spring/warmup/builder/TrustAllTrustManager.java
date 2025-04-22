package dev.jeschke.spring.warmup.builder;

import java.net.Socket;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedTrustManager;

@SuppressWarnings("java:S4830")
public class TrustAllTrustManager extends X509ExtendedTrustManager {
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

    @Override
    public void checkClientTrusted(final X509Certificate[] chain, final String authType, final Socket socket) {
        // We don't want to check
    }

    @Override
    public void checkServerTrusted(final X509Certificate[] chain, final String authType, final Socket socket) {
        // We don't want to check
    }

    @Override
    public void checkClientTrusted(final X509Certificate[] chain, final String authType, final SSLEngine engine) {
        // We don't want to check
    }

    @Override
    public void checkServerTrusted(final X509Certificate[] chain, final String authType, final SSLEngine engine) {
        // We don't want to check
    }
}
