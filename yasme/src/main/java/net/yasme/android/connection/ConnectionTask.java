package net.yasme.android.connection;

import android.content.Context;
import android.util.Log;

import net.yasme.android.connection.ssl.HttpClient;
import net.yasme.android.exception.RestServiceException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;

import net.yasme.android.exception.Error;
import net.yasme.android.ui.AbstractYasmeActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

public class ConnectionTask {

    public enum Request {
        POST,
        PUT,
        DELETE,
        GET
    }

    /*
     * Connection Params
     */

    protected final static String APIVERSION = "/v1";

    protected static String serverScheme;
    protected static String serverHost;
    protected static int serverPort;
    protected static URI baseURI;

    protected static boolean initialized = false;

    /*
     * Session Params
     */
    protected static String userId;
    protected static String deviceId;
    protected static String accessToken;

    protected static boolean initializedSession = false;

    /*
     * Connection Objects
     */
    protected static ObjectWriter objectWriter;

    /*
     * Ressource URI - individual for the single Tasks
     */
    protected URI uri;


    public static void initParams(String serverScheme, String serverHost, String serverPort) {

        ConnectionTask.serverScheme = serverScheme;
        ConnectionTask.serverHost = serverHost;
        ConnectionTask.serverPort = Integer.parseInt(serverPort);

        ConnectionTask.initialized = true;
        ConnectionTask.initializedSession = false;

        buildBaseURI();

        ConnectionTask.objectWriter = new ObjectMapper().writer()
                .withDefaultPrettyPrinter();
    }

    public static void initSession(long userId, long deviceId, String accessToken) {

        if (!initialized) {
            System.err.println("Server Params not initialized");
            return;
        }
        ConnectionTask.userId = Long.toString(userId);

        //TODO: Change to real deviceId
        ConnectionTask.deviceId = Long.toString(deviceId);

        ConnectionTask.accessToken = accessToken;
        ConnectionTask.initializedSession = true;
    }


    public static boolean isInitialized() {
        return initialized;
    }

    private static void buildBaseURI() {
        try {
            ConnectionTask.baseURI = new URIBuilder().setScheme(ConnectionTask.serverScheme).
                    setHost(ConnectionTask.serverHost).setPort(ConnectionTask.serverPort).build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * Builds a request to upload an image given as HttpEntity. Uses specific implementation of adding headers
     * @param request to be executed
     * @param path last component of the URI
     * @param image to be uploaded
     * @param additionalHeaders as key-value-pairs
     * @return response from server
     * @throws RestServiceException
     */
    public HttpResponse executeUpload(Request request, String path, HttpEntity image, Map<String, String> additionalHeaders) throws RestServiceException {
        HttpEntityEnclosingRequestBase requestBase;

        switch (request) {
            case POST:
                requestBase = new HttpPost();
                break;
            case PUT:
                requestBase = new HttpPut();
                break;
            default:
                System.out.println("Request not supported!");
                return null;
        }

        requestBase.setURI(buildRequestURI(path));

        if (image != null) {
            requestBase.setEntity(image);
        }

        // Copy additional header properties. Content-Type and Accept may be overriden
        if (null != additionalHeaders && additionalHeaders.size() != 0) {
            for (Map.Entry<String, String> header : additionalHeaders.entrySet()) {
                requestBase.setHeader(header.getKey(), header.getValue());
            }
        }

        if (initializedSession) {
            requestBase.setHeader("userId", userId);
            requestBase.setHeader("Authorization", accessToken);
            requestBase.setHeader("deviceId", deviceId);
        }

        return executeRequest(requestBase);
    }

    public HttpResponse executeRequest(Request request, String path, Object contentValue, Map<String, String> additionalHeaders) throws RestServiceException {

        HttpEntityEnclosingRequestBase requestBase;

        switch (request) {
            case POST:
                requestBase = new HttpPost();
                break;
            case PUT:
                requestBase = new HttpPut();
                break;
            default:
                System.out.println("Request not supported!");
                return null;
        }

        requestBase.setURI(buildRequestURI(path));
        addRequestHeader(requestBase, additionalHeaders);

        if (contentValue != null) {
            try {
                requestBase.setEntity(new StringEntity(objectToJsonMapper(contentValue)));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return executeRequest(requestBase);
    }

    public HttpResponse executeRequest(Request request, String path, Object contentValue) throws RestServiceException {
        return executeRequest(request, path, contentValue, null);
    }


    public HttpResponse executeRequest(Request request, String path, Map<String, String> additionalHeaders) throws RestServiceException {
        HttpRequestBase requestBase;

        switch (request) {
            case POST:
                requestBase = new HttpPost();
                break;
            case PUT:
                requestBase = new HttpPut();
                break;
            case DELETE:
                requestBase = new HttpDelete();
                break;
            case GET:
                requestBase = new HttpGet();
                break;
            default:
                System.out.println("Request not supported!");
                return null;
        }

        requestBase.setURI(buildRequestURI(path));
        addRequestHeader(requestBase, additionalHeaders);
        return executeRequest(requestBase);
    }

    public HttpResponse executeRequest(Request request, String path) throws RestServiceException {
        return executeRequest(request, path, null);
    }

    private <T> String objectToJsonMapper(T object) {
        String result = null;
        try {
            result = objectWriter.writeValueAsString(object);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private URI buildRequestURI(String path) {

        if (path.equals(""))
            return uri;

        URI ressourceURI = null;
        try {
            ressourceURI = new URIBuilder(uri).setPath(uri.getPath() + "/" + path).build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return ressourceURI;
    }

    private void addRequestHeader(HttpRequestBase requestBase, Map<String, String> additionalHeaders) {

        requestBase.setHeader("Content-type", "application/json");
        requestBase.setHeader("Accept", "application/json");

        // Copy additional header properties. Content-Type and Accept may be overriden
        if (null != additionalHeaders && additionalHeaders.size() != 0) {
            for (Map.Entry<String, String> header : additionalHeaders.entrySet()) {
                requestBase.setHeader(header.getKey(), header.getValue());
            }
        }

        System.out.println("[DEBUG] Session initialized? " + initializedSession);

        if (initializedSession) {
            requestBase.setHeader("userId", userId);
            requestBase.setHeader("Authorization", accessToken);
            requestBase.setHeader("deviceId", deviceId);

            System.out.println("[DEBUG] userId:  " + userId + " accessToken: " + accessToken);
        }
    }

    private HttpResponse executeRequest(HttpRequestBase requestBase) throws RestServiceException {

        try {
            HttpResponse httpResponse = HttpClient.createSSLClient().execute(requestBase);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            System.out.println("[DEBUG] StatusCode: " + statusCode);
            if (statusCode == 200 || statusCode == 201 || statusCode == 204)
                return httpResponse;
            else
                throw new RestServiceException((new BufferedReader(new InputStreamReader(
                        httpResponse.getEntity().getContent(), "UTF-8"))).readLine(), statusCode);
        } catch (IOException e) {
            throw new RestServiceException(Error.CONNECTION_ERROR);
        }
    }
}
