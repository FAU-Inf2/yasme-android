package net.yasme.android.connection;

import net.yasme.android.entities.Chat;
import net.yasme.android.exception.Error;
import net.yasme.android.exception.RestServiceException;
import net.yasme.android.exception.UserError;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by florianwinklmeier on 03.06.14.
 */

public class ChatTask {

    private String url;

    public ChatTask(String url) {
        this.url = url.concat("/chat");
    }

    public Long createChatwithPar(Chat chat) throws RestServiceException {

        String requestURL = url.concat("/new");

        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(requestURL);

            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String json = ow.writeValueAsString(chat);

            StringEntity se = new StringEntity(json);
            httpPost.setEntity(se);

            httpPost.setHeader("Content-type", "application/json");
            httpPost.setHeader("Accept", "application/json");

            HttpResponse httpResponse = httpClient.execute(httpPost);

            switch (httpResponse.getStatusLine().getStatusCode()) {
                case 201:
                    return Long.parseLong((new BufferedReader(new InputStreamReader(httpResponse
                            .getEntity().getContent(), "UTF-8"))).readLine());
                case 404:
                    throw new RestServiceException(UserError.USER_NOT_FOUND);
                default:
                    throw new RestServiceException(UserError.ERROR);
            }

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RestServiceException(Error.CONNECTION_ERROR);
        }
        return null;
    }

    public Chat getInfoOfChat(long chatId) throws RestServiceException {

        String requestURL = url.concat("/info/" + chatId);

        try {

            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(requestURL);
            httpGet.addHeader("accept", "application/json");

            HttpResponse httpResponse = httpClient.execute(httpGet);

            switch (httpResponse.getStatusLine().getStatusCode()) {
                case 200:
                    System.out.println("true");
                    //TODO: JSON Object to ChatObj
                    //@return Chat
                case 404:
                    throw new RestServiceException(UserError.CHAT_NOT_FOUND_EXCEPTION);
                default:
                    throw new RestServiceException(UserError.ERROR);
            }

        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RestServiceException(Error.CONNECTION_ERROR);
        }
        return null;
    }

    public boolean addParticipantToChat(long userId, long chatId) throws RestServiceException {

        String requestURL = url.concat("/addParToChat/" + userId + "/" + chatId);

        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(requestURL);

            httpPost.setHeader("Content-type", "application/json");
            httpPost.setHeader("Accept", "application/json");

            HttpResponse httpResponse = httpClient.execute(httpPost);

            switch (httpResponse.getStatusLine().getStatusCode()) {
                case 201:
                    System.out.println("User added to Chat!");
                    return true;
                case 404:
                    throw new RestServiceException(UserError.USER_NOT_FOUND);
                default:
                    throw new RestServiceException(UserError.ERROR);
            }

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RestServiceException(Error.CONNECTION_ERROR);
        }
        return false;
    }

    public boolean removePartipantFromChat(long userId, long chatId) throws RestServiceException {

        String requestURL = url.concat("/removeParFromChat/" + userId + "/" + chatId);

        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(requestURL);

            httpPost.setHeader("Content-type", "application/json");
            httpPost.setHeader("Accept", "application/json");

            HttpResponse httpResponse = httpClient.execute(httpPost);

            switch (httpResponse.getStatusLine().getStatusCode()) {
                case 201:
                    System.out.println("User removed from Chat!");
                    return true;
                case 500:
                    throw new RestServiceException(UserError.USER_NOT_FOUND);
                case 404:
                    throw new RestServiceException(UserError.CHAT_NOT_FOUND_EXCEPTION);
                default:
                    throw new RestServiceException(UserError.ERROR);
            }

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RestServiceException(Error.CONNECTION_ERROR);
        }
        return false;
    }

    public boolean updateStatus(Chat chat) throws RestServiceException {

        String requestURL = url.concat("/update");

        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(requestURL);

            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String json = ow.writeValueAsString(chat);
            httpPost.setEntity(new StringEntity(json));

            httpPost.setHeader("Content-type", "application/json");
            httpPost.setHeader("Accept", "application/json");

            HttpResponse httpResponse = httpClient.execute(httpPost);

            switch (httpResponse.getStatusLine().getStatusCode()) {
                case 201:
                    System.out.println("Status of chat updated");
                    return true;
                case 404:
                    throw new RestServiceException(UserError.CHAT_NOT_FOUND_EXCEPTION);
                case 500:
                    throw new RestServiceException(Error.STORE_FAILED_EXCEPTION);
                default:
                    throw new RestServiceException(UserError.ERROR);
            }

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RestServiceException(Error.CONNECTION_ERROR);
        }
        return false;
    }
}
