package net.yasme.android.connection.ssl;


import android.content.Context;
import android.util.Log;

import net.yasme.android.R;
import net.yasme.android.ui.AbstractYasmeActivity;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * Created by florianwinklmeier on 10.06.14.
 */

public class HttpClient {

    public static Context context;

    public static CloseableHttpClient createSSLClient() {


        SSLConnectionSocketFactory sslsf = null;

        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            CertificateFactory cf = CertificateFactory.getInstance("X.509");

            //TODO: testen, ob das so funktioniert
            //String yasme_ca = "raw/yasme_ca.pem";
            //InputStream caInput = ClassLoader.getSystemResourceAsStream(yasme_ca);

            InputStream caInput = context.getResources().openRawResource(R.raw.yasme_ca);

            Certificate ca = cf.generateCertificate(caInput);

            Log.d("HttpClient","ca=" + ((X509Certificate) ca).getSubjectDN());
            caInput.close();

            trustStore.load(null, null);
            trustStore.setCertificateEntry("ca", ca);

            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(trustStore);

            SSLContext context = SSLContext.getInstance("TLSv1");
            context.init(null, tmf.getTrustManagers(), null);


            sslsf = new SSLConnectionSocketFactory(
                    context,
                    new String[]{"TLSv1"},
                    null,
                    null
            );

        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return HttpClients.custom().setSSLSocketFactory(sslsf).build();
    }
}
