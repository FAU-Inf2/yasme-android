package net.yasme.android.connection;

import net.yasme.android.connection.ssl.HttpClient;
import net.yasme.android.exception.RestServiceException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;

import net.yasme.android.exception.Error;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

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
    protected static String serverScheme;
    protected static String serverHost;
    protected static int serverPort;
    protected static URI baseURI;

    protected static boolean initialized = false;

    /*
     * Session Params
     */
    protected static String userId;
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

    public static void initSession(long userId, String accessToken) {

        if (!initialized) {
            System.err.println("Server Params not initialized");
            return;
        }
        ConnectionTask.userId = Long.toString(userId);
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

    public HttpResponse executeRequest(Request request, String path, Object contentValue) throws RestServiceException {

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
        addRequestHeader(requestBase);

        if (contentValue != null) {
            try {
                requestBase.setEntity(new StringEntity(objectToJsonMapper(contentValue)));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return executeRequest(requestBase);
    }

    public HttpResponse executeRequest(Request request, String path)
            throws RestServiceException {

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
        addRequestHeader(requestBase);
        return executeRequest(requestBase);
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

    private void addRequestHeader(HttpRequestBase requestBase) {

        requestBase.setHeader("Content-type", "application/json");
        requestBase.setHeader("Accept", "application/json");

        System.out.println("[DEBUG] Session initialized? " + initializedSession);

        if (initializedSession) {
            requestBase.setHeader("userId", userId);
            requestBase.setHeader("Authorization", accessToken);

            //TODO: Change with real DeviceId
            requestBase.setHeader("deviceId", ConnectionTask.userId);

            System.out.println("[DEBUG] userId:  " + userId + " accessToken: " + accessToken);
        }
    }

    private HttpResponse executeRequest(HttpRequestBase requestBase) throws RestServiceException {

        HttpResponse httpResponse;

        try {
            httpResponse = HttpClient.createSSLClient().execute(requestBase);
            int statusCode = httpResponse.getStatusLine().getStatusCode();

            System.out.println("[DEBUG] StatusCode: " + statusCode);

            if (statusCode == 200 || statusCode == 201)
                return httpResponse;
            else
                throw new RestServiceException((new BufferedReader(new InputStreamReader(
                        httpResponse.getEntity().getContent(), "UTF-8"))).readLine(), statusCode);
        } catch (IOException e) {
            throw new RestServiceException(Error.CONNECTION_ERROR);
        }
    }
}
