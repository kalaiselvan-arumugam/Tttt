``` java
public static CloseableHttpClient validateSSL(CloseableHttpClient httpClient, CredentialsProvider credentialsProvider) throws Exception {
    int timeOut = 30000; // Milliseconds (30 seconds)
    try {
        RequestConfig config = RequestConfig.custom()
            .setConnectTimeout(timeOut)
            .setConnectionRequestTimeout(timeOut)
            .setSocketTimeout(timeOut)
            .build();

        // Load JKS truststore
        SSLContext sslContext = SSLContexts.custom()
            .loadTrustMaterial(
                new File(Constants.API_CERTIFICATE_FILE_PATH),
                getJksPassword() // Securely retrieve password
            )
            .build();

        // Enforce TLS 1.2+, secure ciphers, and hostname verification
        SSLConnectionSocketFactory sslConSocFactory = new SSLConnectionSocketFactory(
            sslContext,
            new String[]{"TLSv1.2", "TLSv1.3"},
            new String[]{"TLS_AES_256_GCM_SHA384", "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384"},
            SSLConnectionSocketFactory.getDefaultHostnameVerifier());

        httpClient = HttpClients.custom()
            .setSSLSocketFactory(sslConSocFactory)
            .setDefaultCredentialsProvider(credentialsProvider)
            .setDefaultRequestConfig(config)
            .build();

    } catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException |
             CertificateException | IOException ex) {
        logger.error("SSL configuration failed: {}", ex.getMessage());
        throw new Exception("Failed to configure SSL", ex);
    }
    return httpClient;
}

// Securely fetch JKS password (example using environment variable)
private static char[] getJksPassword() {
    String password = System.getenv("JKS_PASSWORD");
    return password != null ? password.toCharArray() : new char[0];
}
```
