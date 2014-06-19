package net.yasme.android.connection;

import net.yasme.android.connection.ssl.HttpClient;
import net.yasme.android.entities.DiffieHellmanPart;
import net.yasme.android.exception.*;
import net.yasme.android.exception.Error;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by florianwinklmeier on 16.06.14.
 */
public class DiffieHellmanPartTask extends ConnectionTask{

    private static DiffieHellmanPartTask instance;
    private URI uri;

    public static DiffieHellmanPartTask getInstance() {
        if (instance == null) {
            instance = new DiffieHellmanPartTask();
        }
        return instance;
    }

    private DiffieHellmanPartTask() {

        try {
            this.uri = new URIBuilder().setScheme(serverScheme).
                    setHost(serverHost).setPort(serverPort).setPath("/dh").build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public boolean storeDHPart(DiffieHellmanPart dh, long userId, String accessToken) throws RestServiceException{

        URI requestURI = uri;

        try {

            CloseableHttpClient httpClient = HttpClient.createSSLClient();
            HttpPost httpPost = new HttpPost(requestURI);

            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(dh);

            httpPost.setEntity(new StringEntity(json));

            httpPost.setHeader("Content-type", "application/json");
            httpPost.setHeader("Accept", "application/json");

            httpPost.setHeader("userId", Long.toString(userId));
            httpPost.setHeader("deviceId", Long.toString(userId));
            httpPost.setHeader("Authorization", accessToken);

            HttpResponse httpResponse = httpClient.execute(httpPost);

            switch (httpResponse.getStatusLine().getStatusCode()) {

                case 201:
                    System.out.println("[DEBUG] DH received");
                    return true;
                case 401:
                    System.out.println("Unauthorized");
                    throw new RestServiceException(net.yasme.android.exception.Error.UNAUTHORIZED);
                case 405:
                    throw new RestServiceException(Error.ERROR);
                default:
                    throw new RestServiceException(Error.ERROR);
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RestServiceException(Error.CONNECTION_ERROR);
        }
        return false;
    }

    public DiffieHellmanPart getNextKey(long devId, long userId, String accessToken) throws RestServiceException{

        DiffieHellmanPart dh = null;

        try {
            URI requestURI = new URIBuilder(uri).setPath(uri.getPath()+"/"+devId).build();

            CloseableHttpClient httpClient = HttpClient.createSSLClient();
            HttpGet httpGet = new HttpGet(requestURI);

            httpGet.setHeader("accept", "application/json");

            httpGet.setHeader("userId", Long.toString(userId));
            httpGet.setHeader("deviceId", Long.toString(userId));
            httpGet.setHeader("Authorization", accessToken);

            HttpResponse httpResponse = httpClient.execute(httpGet);

            switch (httpResponse.getStatusLine().getStatusCode()) {

                case 200:
                    dh = new ObjectMapper().readValue(new BufferedReader(new InputStreamReader(
                            httpResponse.getEntity().getContent())).readLine(), DiffieHellmanPart.class);
                    break;
                case 401:
                    System.out.println("Unauthorized");
                    throw new RestServiceException(Error.UNAUTHORIZED);
                case 404:
                    throw new RestServiceException(Error.NOT_FOUND_EXCEPTION);
                default:
                    throw new RestServiceException(Error.ERROR);
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }  catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RestServiceException(Error.CONNECTION_ERROR);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        return dh;
    }
}
