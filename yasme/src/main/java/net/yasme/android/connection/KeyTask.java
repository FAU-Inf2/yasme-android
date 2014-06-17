package net.yasme.android.connection;

import net.yasme.android.connection.ssl.HttpClient;
import net.yasme.android.entities.Chat;
import net.yasme.android.entities.Device;
import net.yasme.android.entities.MessageKey;
import net.yasme.android.entities.User;
import net.yasme.android.exception.MessageError;
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
import org.json.JSONArray;
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
import java.util.ArrayList;

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

    public boolean saveKey(long keyId, long creatorDevice, ArrayList<Long> recipients,Chat chat, String key, byte encType, String sign ) throws RestServiceException {

        URI requestURI = uri;
        try {
            ObjectWriter ow = new ObjectMapper().writer()
                    .withDefaultPrettyPrinter();

            JSONArray keys= new JSONArray();
            System.out.println("[???] Key wird an Server gesendet für " + recipients.size() + " Users");

            int i = 0;

            //messageKey Array
            MessageKey[] messageKeys = new MessageKey[recipients.size()];


            //TODO: erzeuge JSON-Object-Array mit MessageKey pro Recipient
            for (long recipient: recipients){
                System.out.println("[???] 1");

                //MessageKey zu Array  hinzufügen
                messageKeys[i++] = new MessageKey(keyId, new Device(creatorDevice),
                        new Device(recipient), chat, key, encType,sign);

                System.out.println("[???] 2");
                //System.out.println("[???] "+ ow.writeValueAsString(messageKey));
                //System.out.println("[???] "+ new StringEntity(ow.writeValueAsString(messageKey)));
                System.out.println("[???] 3");

                //keys.put(new StringEntity(ow.writeValueAsString(messageKeys)));
                System.out.println("[???] User: "+ recipient);
                System.out.println("[???] Key wird für " + recipient + " Server gesendet");
            }

            CloseableHttpClient httpClient = HttpClient.createSSLClient();
            HttpPost httpPost = new HttpPost(requestURI);

            System.out.println("[???] Sending Keys to Server:"+key.toString());

            //StringEntity se = new StringEntity(keys.toString());

            //komplettes Array serialisieren
            StringEntity se = new StringEntity(ow.writeValueAsString(messageKeys));
            httpPost.setEntity(se);

            httpPost.setHeader("Content-type", "application/json");
            httpPost.setHeader("Accept", "application/json");

            httpPost.setHeader("userId", Long.toString(creatorDevice));
            httpPost.setHeader("Authorization", accessToken);

            HttpResponse httpResponse = httpClient.execute(httpPost);

            /**DEBUG**/
            System.out.println(httpResponse.getStatusLine().getStatusCode());
            System.out.println(new BufferedReader(
                    new InputStreamReader(httpResponse.getEntity()
                            .getContent())
            ).readLine());
            /**DEBUG**/

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

    public boolean deleteKey(long chatId, long keyId, long DeviceId) {

        try {

            URI requestURI = new URIBuilder(uri).setPath(uri.getPath() + keyId + "/" + chatId).build();

            CloseableHttpClient httpClient = HttpClient.createSSLClient();
            HttpDelete httpDelete = new HttpDelete(requestURI);

            httpDelete.setHeader("userId", Long.toString(DeviceId));
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