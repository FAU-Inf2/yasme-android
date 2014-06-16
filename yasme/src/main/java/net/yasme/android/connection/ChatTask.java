package net.yasme.android.connection;

import net.yasme.android.connection.ssl.HttpClient;
import net.yasme.android.entities.Chat;
import net.yasme.android.entities.Device;
import net.yasme.android.exception.Error;
import net.yasme.android.exception.RestServiceException;
import net.yasme.android.exception.UserError;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by florianwinklmeier on 03.06.14.
 */

public class ChatTask extends  ConnectionTask{

    private static ChatTask instance;
    private URI uri;

    public static ChatTask getInstance() {
        if (instance == null) {
            instance = new ChatTask();
        }
        return instance;
    }

    private ChatTask() {

        try {
            this.uri = new URIBuilder().setScheme(serverScheme).
                    setHost(serverHost).setPort(serverPort).setPath("/chat").build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    //TODO: ExceptionHandling verfeinern für ganze Klasse!

    public List<Chat> getAllChatsForUser(long userId, String accessToken) throws RestServiceException{

        List<Chat> chats = new ArrayList<Chat>();

        try {
            URI requestURI = uri;

            CloseableHttpClient httpClient = HttpClient.createSSLClient();
            HttpGet httpGet = new HttpGet(requestURI);

            httpGet.setHeader("Content-type", "application/json");
            httpGet.setHeader("Accept", "application/json");

            httpGet.setHeader("userId", Long.toString(userId));
            httpGet.setHeader("Authorization", accessToken);

            HttpResponse httpResponse = httpClient.execute(httpGet);

            System.out.println(httpResponse.getStatusLine().getStatusCode());
            switch (httpResponse.getStatusLine().getStatusCode()) {
                case 200:
                    JSONArray jsonArray = new JSONArray(new BufferedReader(new InputStreamReader(
                            httpResponse.getEntity().getContent())).readLine());

                    for (int i = 0; i < jsonArray.length(); i++)
                        chats.add(new ObjectMapper().readValue((jsonArray.getJSONObject(i)).
                                toString(), Chat.class));
                    break;
                case 401:
                    System.out.println("[DEBUG] Unauthorized");
                    throw new RestServiceException(Error.UNAUTHORIZED);
                case 500:
                    throw new RestServiceException(Error.ERROR);
                default:
                    throw new RestServiceException(Error.ERROR);
            }

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RestServiceException(Error.CONNECTION_ERROR);
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return chats;
    }

    public Long createChatwithPar(Chat chat, long userId, String accessToken) throws RestServiceException {

        URI requestURI = uri;

        try {
            CloseableHttpClient httpClient = HttpClient.createSSLClient();
            HttpPost httpPost = new HttpPost(requestURI);

            ObjectWriter ow = new ObjectMapper().writer()
                    .withDefaultPrettyPrinter();
            String json = ow.writeValueAsString(chat);

            StringEntity se = new StringEntity(json);
            httpPost.setEntity(se);

            httpPost.setHeader("Content-type", "application/json");
            httpPost.setHeader("Accept", "application/json");

            httpPost.setHeader("userId", Long.toString(userId));
            httpPost.setHeader("Authorization", accessToken);

            HttpResponse httpResponse = httpClient.execute(httpPost);

            switch (httpResponse.getStatusLine().getStatusCode()) {
                case 201:
                    return Long.parseLong((new BufferedReader(
                            new InputStreamReader(httpResponse.getEntity()
                                    .getContent(), "UTF-8")
                    )).readLine());
                case 401:
                    System.out.println("[DEBUG] Unauthorized");
                    throw new RestServiceException(Error.UNAUTHORIZED);
                case 403:
                    throw new RestServiceException(Error.ERROR);
                case 404:
                    throw new RestServiceException(Error.ERROR);
                case 500:
                    throw new RestServiceException(Error.ERROR);
                default:
                    throw new RestServiceException(Error.ERROR);
            }

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RestServiceException(Error.CONNECTION_ERROR);
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Chat getInfoOfChat(long chatId, long userId, String accessToken) throws RestServiceException {

        try {

            URI requestURI = new URIBuilder(uri).setPath(uri.getPath() + "/" + chatId+ "/info").build();

            CloseableHttpClient httpClient = HttpClient.createSSLClient();
            HttpGet httpGet = new HttpGet(requestURI);

            httpGet.setHeader("accept", "application/json");
            httpGet.setHeader("userId", Long.toString(userId));
            httpGet.setHeader("Authorization", accessToken);

            HttpResponse httpResponse = httpClient.execute(httpGet);

            switch (httpResponse.getStatusLine().getStatusCode()) {
                case 200:
                    return new ObjectMapper().readValue(new BufferedReader (new InputStreamReader(httpResponse.getEntity()
                            .getContent())).readLine(), Chat.class);
                case 401:
                    System.out.println("[DEBUG] Unauthorized");
                    throw new RestServiceException(Error.UNAUTHORIZED);
                case 403:
                    throw new RestServiceException(
                            Error.ERROR);
                case 404:
                    throw new RestServiceException(
                            UserError.CHAT_NOT_FOUND_EXCEPTION);
                case 500:
                    throw new RestServiceException(
                            Error.ERROR);
                default:
                    throw new RestServiceException(UserError.ERROR);
            }

        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RestServiceException(Error.CONNECTION_ERROR);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean addParticipantToChat(long participantId, long userId, long chatId, String accessToken)
            throws RestServiceException {
        try {
            URI requestURI = new URIBuilder(uri).setPath(uri.getPath() + "/par/" + participantId + "/" + chatId).build();

            CloseableHttpClient httpClient = HttpClient.createSSLClient();
            HttpPut httpPut = new HttpPut(requestURI);

            httpPut.setHeader("Content-type", "application/json");
            httpPut.setHeader("Accept", "application/json");

            httpPut.setHeader("userId", Long.toString(userId));
            httpPut.setHeader("Authorization", accessToken);

            HttpResponse httpResponse = httpClient.execute(httpPut);

            System.out.println(httpResponse.getStatusLine().getStatusCode());
            switch (httpResponse.getStatusLine().getStatusCode()) {
                case 201:
                    System.out.println("User added to Chat!");
                    return true;
                case 401:
                    System.out.println("[DEBUG] Unauthorized");
                    throw new RestServiceException(Error.UNAUTHORIZED);
                case 403:
                    throw new RestServiceException(Error.ERROR);
                case 404:
                    throw new RestServiceException(UserError.USER_NOT_FOUND);
                case 500:
                    throw new RestServiceException(Error.ERROR);
                default:
                    throw new RestServiceException(UserError.ERROR);
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RestServiceException(Error.CONNECTION_ERROR);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean changeOwnerOfChat(long chatId, long newOwnerId, long userId, String accessToken) throws RestServiceException {

        try {
            URI requestURI = new URIBuilder(uri).setPath(uri.getPath() + "/"+chatId+"/owner/"+userId).build();
            CloseableHttpClient httpClient = HttpClient.createSSLClient();
            HttpPost httpPost = new HttpPost(requestURI);

            httpPost.setHeader("Content-type", "application/json");
            httpPost.setHeader("Accept", "application/json");

            httpPost.setHeader("userId", Long.toString(userId));
            httpPost.setHeader("Authorization", accessToken);

            HttpResponse httpResponse = httpClient.execute(httpPost);

            switch (httpResponse.getStatusLine().getStatusCode()) {
                case 201:
                    System.out.println("Owner changed");
                    return true;
                case 401:
                    System.out.println("[DEBUG] Unauthorized");
                    throw new RestServiceException(Error.UNAUTHORIZED);
                case 404:
                    throw new RestServiceException(Error.ERROR);
                case 500:
                    throw new RestServiceException(Error.ERROR);
                default:
                    throw new RestServiceException(Error.ERROR);
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RestServiceException(Error.CONNECTION_ERROR);
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean removePartipantFromChat(long participantId, long chatId, long userId, String accessToken)
            throws RestServiceException {

        try {
            URI requestURI = new URIBuilder(uri).setPath(uri.getPath() +
                    "/par/" + participantId + "/" + chatId).build();

            CloseableHttpClient httpClient = HttpClient.createSSLClient();
            HttpDelete httpDelete = new HttpDelete(requestURI);

            httpDelete.setHeader("Content-type", "application/json");
            httpDelete.setHeader("Accept", "application/json");

            httpDelete.setHeader("userId", Long.toString(userId));
            httpDelete.setHeader("Authorization", accessToken);

            HttpResponse httpResponse = httpClient.execute(httpDelete);

            System.out.println(httpResponse.getStatusLine().getStatusCode());

            switch (httpResponse.getStatusLine().getStatusCode()) {
                case 201:
                    System.out.println("User removed from Chat!");
                    return true;
                case 400:
                    throw new RestServiceException(Error.ERROR);
                case 401:
                    System.out.println("[DEBUG] Unauthorized");
                    throw new RestServiceException(Error.UNAUTHORIZED);
                case 403:
                    throw new RestServiceException(
                            Error.ERROR);
                case 404:
                    throw new RestServiceException(
                            Error.NOT_FOUND_EXCEPTION);
                case 500:
                    throw new RestServiceException(Error.ERROR);
                default:
                    throw new RestServiceException(UserError.ERROR);
            }

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RestServiceException(Error.CONNECTION_ERROR);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean removeOneSelfFromChat(long chatId, long userId, String accessToken)
            throws RestServiceException {

        try {
            URI requestURI = new URIBuilder(uri).setPath(uri.getPath() +
                    "/" + chatId + "/par/self").build();

            CloseableHttpClient httpClient = HttpClient.createSSLClient();
            HttpDelete httpDelete = new HttpDelete(requestURI);

            httpDelete.setHeader("Content-type", "application/json");
            httpDelete.setHeader("Accept", "application/json");

            httpDelete.setHeader("userId", Long.toString(userId));
            httpDelete.setHeader("Authorization", accessToken);

            HttpResponse httpResponse = httpClient.execute(httpDelete);

            switch (httpResponse.getStatusLine().getStatusCode()) {
                case 201:
                    System.out.println("I´m out of Chat No. " +chatId);
                    return true;
                case 401:
                    System.out.println("[DEBUG] Unauthorized");
                    throw new RestServiceException(Error.UNAUTHORIZED);
                case 403:
                    throw new RestServiceException(
                            Error.ERROR);
                case 404:
                    throw new RestServiceException(
                            Error.NOT_FOUND_EXCEPTION);
                case 500:
                    throw new RestServiceException(Error.ERROR);
                default:
                    throw new RestServiceException(UserError.ERROR);
            }

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RestServiceException(Error.CONNECTION_ERROR);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateStatus(Chat chat, long userId, String accessToken) throws RestServiceException {

        try {
            URI requestURI = new URIBuilder(uri).setPath(uri.getPath() + "/" + chat.getId() +"/properties").build();

            CloseableHttpClient httpClient = HttpClient.createSSLClient();
            HttpPut httpPut = new HttpPut(requestURI);

            ObjectWriter ow = new ObjectMapper().writer()
                    .withDefaultPrettyPrinter();

            String json = ow.writeValueAsString(chat);

            StringEntity se = new StringEntity(json);
            httpPut.setEntity(se);

            httpPut.setHeader("Content-type", "application/json");
            httpPut.setHeader("Accept", "application/json");

            httpPut.setHeader("userId", Long.toString(userId));
            httpPut.setHeader("Authorization", accessToken);

            HttpResponse httpResponse = httpClient.execute(httpPut);

            switch (httpResponse.getStatusLine().getStatusCode()) {
                case 201:
                    System.out.println("Status of chat updated");
                    return true;
                case 401:
                    System.out.println("[DEBUG] Unauthorized");
                    throw new RestServiceException(Error.UNAUTHORIZED);
                case 403:
                    throw new RestServiceException(
                            Error.ERROR);
                case 404:
                    throw new RestServiceException(
                            UserError.CHAT_NOT_FOUND_EXCEPTION);
                case 500:
                    throw new RestServiceException(Error.STORE_FAILED_EXCEPTION);
                default:
                    throw new RestServiceException(UserError.ERROR);
            }

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RestServiceException(Error.CONNECTION_ERROR);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return false;
    }
}
