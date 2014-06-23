package net.yasme.android.connection;

import net.yasme.android.connection.ssl.HttpClient;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * Created by martin on 14.06.2014.
 */
public class ConnectionTask {

    public enum Request {
        POST,
        PUT,
        DELETE,
        GET;
    }

    /*
     * Connection Params
     */
    protected static String serverScheme;
    protected static String serverHost;
    protected static int serverPort;
    protected static boolean initialized = false;
    protected static URI baseURI;

    /*
     * Connection Objects
     */
    protected static CloseableHttpClient httpClient;
    protected static ObjectWriter objectWriter;

    /*
     * Session Params
     */
    protected URI uri;
    protected static String userId;
    protected static String accessToken;
    protected static boolean initializedSession = false;


    public static void initParams(String serverScheme, String serverHost, String serverPort) {

        ConnectionTask.serverScheme = serverScheme;
        ConnectionTask.serverHost = serverHost;
        ConnectionTask.serverPort = Integer.parseInt(serverPort);
        ConnectionTask.initialized = true;
        ConnectionTask.initializedSession = false;
        buildBaseURI();

        ConnectionTask.httpClient = HttpClient.createSSLClient();
        ConnectionTask.objectWriter = new ObjectMapper().writer()
                .withDefaultPrettyPrinter();
    }

    public static void initSession(long userId, String accessToken) {
        ConnectionTask.userId = Long.toString(userId);
        ConnectionTask.accessToken = accessToken;
        ConnectionTask.initializedSession = true;
    }


    public static boolean isInitialized() {
        return initialized;
    }


    public HttpResponse executeEntityRequest(Request request, String path, Object contentValue) throws IOException {

        URI requestURI = buildRessourceURI(path);

        HttpEntityEnclosingRequestBase requestBase = null;

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

        requestBase.setURI(requestURI);

        if (contentValue != null)
            requestBase.setEntity(new StringEntity(objectToJsonMapper(contentValue)));

        requestBase.setHeader("Content-type", "application/json");
        requestBase.setHeader("Accept", "application/json");

        if (initializedSession) {
            requestBase.setHeader("userId", ConnectionTask.userId);
            requestBase.setHeader("Authorization", accessToken);
        }

        return httpClient.execute(requestBase);
    }

    public HttpResponse executeBaseRequest(Request request, String path)
            throws IOException {

        URI requestURI = buildRessourceURI(path);

        HttpRequestBase requestBase = null;

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

        requestBase.setURI(requestURI);

        requestBase.setHeader("Content-type", "application/json");
        requestBase.setHeader("Accept", "application/json");

        System.out.println(initializedSession + " " + userId + " " + accessToken);
        if (initializedSession) {
            requestBase.setHeader("userId", ConnectionTask.userId);
            requestBase.setHeader("Authorization", accessToken);
            System.out.println("[DEBUG] TRUE" + userId + " " + accessToken );
        }

        return httpClient.execute(requestBase);
    }

    private static <T> String objectToJsonMapper(T object) throws IOException {
        return objectWriter.writeValueAsString(object);
    }

    private static void buildBaseURI() {
        try {
            ConnectionTask.baseURI = new URIBuilder().setScheme(ConnectionTask.serverScheme).
                    setHost(ConnectionTask.serverHost).setPort(ConnectionTask.serverPort).build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private URI buildRessourceURI(String path) {

        URI ressourceURI = null;
        try {
            ressourceURI = new URIBuilder(uri).setPath(uri.getPath() + "/" + path).build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return ressourceURI;
    }
}
