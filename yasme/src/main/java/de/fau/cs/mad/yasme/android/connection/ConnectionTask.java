package de.fau.cs.mad.yasme.android.connection;

import android.util.Log;
import android.widget.Toast;

import de.fau.cs.mad.yasme.android.R;
import de.fau.cs.mad.yasme.android.connection.ssl.HttpClient;
import de.fau.cs.mad.yasme.android.controller.Toaster;
import de.fau.cs.mad.yasme.android.exception.RestServiceException;

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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.json.JSONException;
import org.json.JSONObject;

import de.fau.cs.mad.yasme.android.exception.Error;
import de.fau.cs.mad.yasme.android.storage.DatabaseManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

public abstract class ConnectionTask {

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
    protected static String language;
    protected static int versionCode;

    protected static boolean initialized = false;

//    /*
//     * Session Params
//     */
//    protected static String userId;
//    protected static String deviceId;
//    protected static String accessToken;

//    protected static boolean initializedSession = false;

    /*
     * Connection Objects
     */
    protected static ObjectWriter objectWriter;

    /*
     * Ressource URI - individual for the single Tasks
     */
    protected URI uri;


    public static void initParams(String serverScheme, String serverHost, String serverPort, String language, int versionCode) {

        ConnectionTask.serverScheme = serverScheme;
        ConnectionTask.serverHost = serverHost;
        ConnectionTask.serverPort = Integer.parseInt(serverPort);
        ConnectionTask.language = language;
        ConnectionTask.versionCode = versionCode;


        //ConnectionTask.initializedSession = false;
        if (!buildBaseURI()) {
            Log.e(ConnectionTask.class.getSimpleName(), "Failed to build URL! May case several other issues");
            throw new ExceptionInInitializerError("Failed to build base URL! This may case several other issues");
        }

        ConnectionTask.initialized = true;
        ConnectionTask.objectWriter = new ObjectMapper().writer()
                .withDefaultPrettyPrinter();
    }

//    public static void initSession(long userId, String accessToken) {
//
//        if (!initialized) {
//            Log.e(ConnectionTask.class.getSimpleName(), "Server Params not initialized");
//        }
//        ConnectionTask.userId = Long.toString(userId);
//        ConnectionTask.accessToken = accessToken;
//        ConnectionTask.deviceId = "-1";
//        Log.i(ConnectionTask.class.getSimpleName(), "Bear in mind that deviceId has not been set yet.");
//        initializedSession = true;
//    }
//
//    public static void initSession(long userId, long deviceId, String accessToken) {
//        if (!initialized) {
//            Log.e(ConnectionTask.class.getSimpleName(), "Server Params not initialized");
//        }
//
//        ConnectionTask.userId = Long.toString(userId);
//        ConnectionTask.deviceId = Long.toString(deviceId);
//        ConnectionTask.accessToken = accessToken;
//        ConnectionTask.initializedSession = true;
//    }



    public static boolean isInitialized() {
        return initialized;
    }

    //public static boolean isInitializedSession() {
    //    return initializedSession;
    //}

    private static boolean buildBaseURI() {
        try {
            ConnectionTask.baseURI = new URIBuilder().setScheme(ConnectionTask.serverScheme).
                    setHost(ConnectionTask.serverHost).setPort(ConnectionTask.serverPort).build();
            return true;
        } catch (URISyntaxException e) {
            e.printStackTrace();
            Log.e(ConnectionTask.class.getSimpleName(), e.getMessage());
        }
        return false;
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
                Log.d(this.getClass().getSimpleName(),"Request not supported!");
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

        long userId = DatabaseManager.INSTANCE.getUserId();
        long deviceId = DatabaseManager.INSTANCE.getDeviceId();
        String accessToken = DatabaseManager.INSTANCE.getAccessToken();

        requestBase.setHeader("userId", Long.toString(userId));
        requestBase.setHeader("Authorization", accessToken);
        requestBase.setHeader("deviceId", Long.toString(deviceId));
        requestBase.setHeader("version", Integer.toString(versionCode));


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
                Log.d(this.getClass().getSimpleName(),"Request not supported!");
                return null;
        }

        requestBase.setURI(buildRequestURI(path));
        addRequestHeader(requestBase, additionalHeaders);

        if (contentValue != null) {
            try {
                StringEntity entity = new StringEntity(objectToJsonMapper(contentValue));
                entity.setContentEncoding("UTF-8");
                entity.setContentType("application/json");
                requestBase.setEntity(entity);
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
                Log.d(this.getClass().getSimpleName(),"Request not supported!");
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
            Log.d(getClass().getSimpleName(),"Generated JSON: " + result);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private URI buildRequestURI(String path) {
				Log.i(this.getClass().getSimpleName(),"PATH IS: "+path); //TODO RM
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

        requestBase.setHeader("Content-Type", "application/json");
        requestBase.setHeader("Accept", "application/json");

        // Copy additional header properties. Content-Type and Accept may be overriden
        if (null != additionalHeaders && additionalHeaders.size() != 0) {
            for (Map.Entry<String, String> header : additionalHeaders.entrySet()) {
                requestBase.setHeader(header.getKey(), header.getValue());
            }
        }

        // Get userId, deviceId and accessToken from central DatabaseManager
        long userId = DatabaseManager.INSTANCE.getUserId();
        long deviceId = DatabaseManager.INSTANCE.getDeviceId();
        String accessToken = DatabaseManager.INSTANCE.getAccessToken();

        requestBase.setHeader("userId", Long.toString(userId));
        requestBase.setHeader("Authorization", accessToken);
        requestBase.setHeader("deviceId", Long.toString(deviceId));
        requestBase.setHeader("version", Integer.toString(versionCode));
    }

    private HttpResponse executeRequest(HttpRequestBase requestBase) throws RestServiceException {
        try {
            HttpResponse httpResponse = HttpClient.createSSLClient().execute(requestBase);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            Log.d(this.getClass().getSimpleName(),"StatusCode: " + statusCode);
            if (statusCode == 200 || statusCode == 201 || statusCode == 204)
                return httpResponse;
            else{
                JSONObject json = new JSONObject(new BufferedReader(new InputStreamReader(httpResponse.getEntity()
                        .getContent())).readLine());
                throw new RestServiceException((String)json.get("message"),Integer.parseInt(json.getString("code")));
            }
        } catch (IOException e) {
            Toaster.getInstance().toast(R.string.connection_error, Toast.LENGTH_LONG);
            throw new RestServiceException(Error.CONNECTION_ERROR);
        } catch (JSONException e) {
            throw new RestServiceException(Error.ERROR);
        }
    }
}