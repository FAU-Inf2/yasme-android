package net.yasme.android.connection;

import net.yasme.android.connection.ssl.HttpClient;
import net.yasme.android.entities.MessageKey;
import net.yasme.android.exception.RestServiceException;
import net.yasme.android.exception.Error;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

public class KeyTask extends ConnectionTask {

    private static KeyTask instance;
    private URI uri;
    private String accessToken;

    public static KeyTask getInstance(String accessToken) {
        if (instance == null) {
            instance = new KeyTask(accessToken);
        }
        return instance;
    }

    private KeyTask(String accessToken) {

        try {
            this.uri = new URIBuilder().setScheme(serverScheme).
                    setHost(serverHost).setPort(serverPort).setPath("/msgkey").build();
            this.accessToken = accessToken;
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    //TODO: Exception Handling

    public boolean saveKey(MessageKey messageKey) throws RestServiceException {

        URI requestURI = uri;
        try {

            CloseableHttpClient httpClient = HttpClient.createSSLClient();
            HttpPost httpPost = new HttpPost(requestURI);

            ObjectWriter ow = new ObjectMapper().writer()
                    .withDefaultPrettyPrinter();

            StringEntity se = new StringEntity(ow.writeValueAsString(messageKey));
            httpPost.setEntity(se);

            httpPost.setHeader("Content-type", "application/json");
            httpPost.setHeader("Accept", "application/json");

            httpPost.setHeader("userId", Long.toString(messageKey.getCreator()));
            httpPost.setHeader("Authorization", accessToken);

            HttpResponse httpResponse = httpClient.execute(httpPost);

            switch (httpResponse.getStatusLine().getStatusCode()) {

                case 200:
                    /**** DEBUG *******/
                    BufferedReader rd = new BufferedReader(new InputStreamReader(
                            httpResponse.getEntity().getContent()));
                    System.out.println("[???]: " + rd.readLine());
                    /**** DEBUG*END ***/
                    return true;
                case 400:
                    throw new RestServiceException(Error.ERROR);
                case 401:
                    throw new RestServiceException(Error.UNAUTHORIZED);
                case 403:
                    throw new RestServiceException(Error.ERROR);
                case 406:
                    throw new RestServiceException(Error.ERROR);
                default:
                    throw new RestServiceException(Error.ERROR);
            }

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean deleteKey(long chatId, long keyId, long userId, String accessToken) {

        try {

            URI requestURI = new URIBuilder(uri).setPath(uri.getPath() + keyId + "/" + chatId).build();

            CloseableHttpClient httpClient = HttpClient.createSSLClient();
            HttpDelete httpDelete = new HttpDelete(requestURI);

            httpDelete.setHeader("userId", Long.toString(userId));
            httpDelete.setHeader("Authorization", accessToken);

            HttpResponse httpResponse = httpClient.execute(httpDelete);

            switch (httpResponse.getStatusLine().getStatusCode()) {

                case 200:
                    return true;
                case 401:
                    throw new RestServiceException(Error.UNAUTHORIZED);
                case 404:
                    throw new RestServiceException(Error.ERROR);
                case 405:
                    throw new RestServiceException(Error.ERROR);
                default:
                    throw new RestServiceException(Error.ERROR);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}