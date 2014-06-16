package net.yasme.android.connection.ssl;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * Created by florianwinklmeier on 10.06.14.
 */

public class HttpClient {

    public static CloseableHttpClient createSSLClient() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {


    /*
    // << NEW
        KeyStore trustStore = null;

        try {
            trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        }catch(Exception e){

        }

        try {

            CertificateFactory cf = CertificateFactory.getInstance("X.509");

            InputStream caInput = new BufferedInputStream(new FileInputStream("/local/yasme_ca.pem"));

            Certificate ca = cf.generateCertificate(caInput);

            System.out.println("ca="+((X509Certificate)ca).getSubjectDN());
            caInput.close();

            trustStore.load(null,null);
            trustStore.setCertificateEntry("ca",ca);

            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(trustStore);

            SSLContext context = SSLContext.getInstance("TLSv1");
            context.init(null,tmf.getTrustManagers(),null);


            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                    context,
                    new String[] {"TLSv1"},
                    null,
                    null
            );

            return HttpClients.custom().setSSLSocketFactory(sslsf).build();

        }catch(Exception e){
            e.getMessage();
            e.printStackTrace();
        }

        return null;
    // NEW >>
     */


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
