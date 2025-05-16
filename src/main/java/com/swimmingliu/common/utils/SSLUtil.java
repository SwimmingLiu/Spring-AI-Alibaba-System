package com.swimmingliu.common.utils;

import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.*;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

@Slf4j
public class SSLUtil {

    public static SSLContext createTrustAllSSLContext() throws Exception {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new TrustManager[]{new X509TrustManager() {
            @Override
            public X509Certificate[] getAcceptedIssuers() { return null; }
            @Override
            public void checkClientTrusted(X509Certificate[] certs, String authType) { }
            @Override
            public void checkServerTrusted(X509Certificate[] certs, String authType) { }
        }}, new SecureRandom());
        return sslContext;
    }

    public static void trustAllHosts() throws Exception {
        SSLContext sslContext = createTrustAllSSLContext();
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
    }
}