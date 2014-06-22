package net.yasme.android.connection;

import net.yasme.android.connection.ssl.HttpClient;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
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
    protected long userId;
    protected String accessToken;

    public static void initParams(String serverScheme, String serverHost, String serverPort) {

        ConnectionTask.serverScheme = serverScheme;
        ConnectionTask.serverHost = serverHost;
        ConnectionTask.serverPort = Integer.parseInt(serverPort);
        ConnectionTask.initialized = true;
        buildURI();

        ConnectionTask.httpClient = HttpClient.createSSLClient();
        ConnectionTask.objectWriter = new ObjectMapper().writer()
                .withDefaultPrettyPrinter();
    }

    public static boolean isInitialized() {
        return initialized;
    }

    private static void buildURI() {
        try {
            ConnectionTask.baseURI = new URIBuilder().setScheme(ConnectionTask.serverScheme).
                    setHost(ConnectionTask.serverHost).setPort(ConnectionTask.serverPort).build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public HttpResponse executeRequest(Request request, String path, Map<String, String> header,
                                       Object contentValue) throws IOException {

        URI requestURI = null;

        try {
            requestURI = new URIBuilder(uri).setPath(uri.getPath() + "/" + path).build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        System.out.println(requestURI);


        //TODO: switch/case über Request Methoden
        /*
        switch (request) {
            case Request.POST.:
                requestBase = new HttpPost();
                break;
            default:
                break;
        }
        */

        HttpEntityEnclosingRequestBase requestBase = new HttpPost(requestURI);

        if (contentValue != null)
            requestBase.setEntity(new StringEntity(objectToJsonMapper(contentValue)));

        requestBase.setHeader("Content-type", "application/json");
        requestBase.setHeader("Accept", "application/json");

        return httpClient.execute(requestBase);
    }

    static <T> String objectToJsonMapper(T object) throws IOException {
        return objectWriter.writeValueAsString(object);
    }
}
