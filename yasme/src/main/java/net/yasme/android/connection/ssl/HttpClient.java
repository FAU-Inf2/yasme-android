package net.yasme.android.connection.ssl;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by florianwinklmeier on 10.06.14.
 */

public class HttpClient {

    public static CloseableHttpClient createSSLClient() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {


        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        /*
        FileInputStream instream = new FileInputStream(new File("my.keystore"));
        try {
            trustStore.load(instream, "nopassword".toCharArray());
        } finally {
            instream.close();
        }
        */

        // Trust own CA and all self-signed certs
        SSLContext sslcontext = SSLContexts.custom()
                .loadTrustMaterial(trustStore, new TrustSelfSignedStrategy())
                .build();

        // Allow TLSv1 protocol only
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                sslcontext,
                new String[]{"TLSv1"},
                null,
                SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

        return HttpClients.custom().setSSLSocketFactory(sslsf).build();
    }
}
