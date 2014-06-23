package net.yasme.android.connection;

import android.content.Context;

import net.yasme.android.connection.ssl.HttpClient;
import net.yasme.android.encryption.MessageSignatur;
import net.yasme.android.entities.Chat;
import net.yasme.android.entities.Device;
import net.yasme.android.entities.MessageKey;
import net.yasme.android.exception.RestServiceException;
import net.yasme.android.exception.Error;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.PublicKey;
import java.util.ArrayList;

public class KeyTask extends ConnectionTask {

    private static KeyTask instance;
    private URI uri;
    private String accessToken;
    private Context context; //necessary for getting Key from Local Storage


    public static KeyTask getInstance(String accessToken, Context context) {
        if (instance == null) {
            instance = new KeyTask(accessToken, context);
        }
        return instance;
    }

    private KeyTask(String accessToken, Context context) {

        try {
            this.uri = new URIBuilder().setScheme(serverScheme).
                    setHost(serverHost).setPort(serverPort).setPath("/msgkey").build();
            this.accessToken = accessToken;
            this.context = context;
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }


    public MessageKey saveKey(long creatorDevice, ArrayList<Long> recipients,Chat chat, String key, byte encType, String sign ) throws RestServiceException {

        URI requestURI = uri;
        try {
            ObjectWriter ow = new ObjectMapper().writer()
                    .withDefaultPrettyPrinter();

            System.out.println("[???] Key wird an Server gesendet für insgesamt " + recipients.size() + " Users");

            int i = 0;

            //messageKey Array
            MessageKey[] messageKeys = new MessageKey[recipients.size()];

            for (long recipient: recipients){

                //encrypt the key with RSA
                MessageSignatur rsa = new MessageSignatur(context, creatorDevice);
                PublicKey pubKey = rsa.getPubKeyFromUser(recipient);
                String keyEncrypted = rsa.encrypt(key, pubKey);

                //TODO: Dummy_IV
                messageKeys[i++] = new MessageKey(0, new Device(creatorDevice),
                        new Device(recipient), chat, keyEncrypted, "DummyIV", encType,sign);

                System.out.println("[???] Key gesendet für User " + recipient);
            }

            CloseableHttpClient httpClient = HttpClient.createSSLClient();
            HttpPost httpPost = new HttpPost(requestURI);

            //komplettes Array serialisieren
            StringEntity se = new StringEntity(ow.writeValueAsString(messageKeys));
            httpPost.setEntity(se);

            httpPost.setHeader("Content-type", "application/json");
            httpPost.setHeader("Accept", "application/json");

            //TODO: userId != creatorDevice ???
            httpPost.setHeader("userId", Long.toString(creatorDevice));

            httpPost.setHeader("deviceId", Long.toString(creatorDevice));
            httpPost.setHeader("Authorization", accessToken);

            HttpResponse httpResponse = httpClient.execute(httpPost);

            /**DEBUG**/
           // System.out.println("[???]"+httpResponse.getStatusLine().getStatusCode());
            /**DEBUG**/

            switch (httpResponse.getStatusLine().getStatusCode()) {

                case 200:
                    String json = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent())).readLine();
                    /**DEBUG**/
                    //System.out.println("[DEBUG] getKeyRequest successful: " + json);
                    /**DEBUG**/

                    JSONObject obj = new JSONObject(json);

                    long keyId = obj.getLong("id");
                    long timestamp = obj.getLong("timestamp");

                    //return keyId and timestamp from serverresponse
                    //TODO: Dummy_IV
                    MessageKey result = new MessageKey(keyId, new Device(creatorDevice), new Device(0), chat, key, "DummyIV", encType,sign);
                    result.setTimestamp(timestamp);

                    return result;

                case 400:
                    throw new RestServiceException(Error.BAD_REQUEST);
                case 401:
                    throw new RestServiceException(Error.UNAUTHORIZED);
                case 403:
                    throw new RestServiceException(Error.FORBIDDEN);
                case 406:
                    throw new RestServiceException(Error.NOT_ACCEPTABLE);
                default:
                    throw new RestServiceException(Error.ERROR);
            }

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean deleteKey(long chatId, long keyId, long DeviceId) throws RestServiceException {

        try {

            URI requestURI = new URIBuilder(uri).setPath(uri.getPath() + keyId + "/" + chatId).build();

            CloseableHttpClient httpClient = HttpClient.createSSLClient();
            HttpDelete httpDelete = new HttpDelete(requestURI);

            //TODO: userId != deviceId !?
            httpDelete.setHeader("userId", Long.toString(DeviceId));

            httpDelete.setHeader("deviceId", Long.toString(DeviceId));
            httpDelete.setHeader("Authorization", accessToken);

            HttpResponse httpResponse = httpClient.execute(httpDelete);

            switch (httpResponse.getStatusLine().getStatusCode()) {

                case 200:
                    return true;
                case 401:
                    throw new RestServiceException(Error.UNAUTHORIZED);
                case 404:
                    throw new RestServiceException(Error.NOT_FOUND_EXCEPTION);
                case 405:
                    throw new RestServiceException(Error.UNAUTHORIZED);
                default:
                    throw new RestServiceException(Error.ERROR);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return false;
    }
}