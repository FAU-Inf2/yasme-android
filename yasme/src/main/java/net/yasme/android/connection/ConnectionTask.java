package net.yasme.android.connection;

/**
 * Created by martin on 14.06.2014.
 */
public class ConnectionTask {
    protected static String serverScheme;
    protected static String serverHost;
    protected static int serverPort;
    protected static boolean initialized = false;

    public static void initParams(String serverScheme, String serverHost, String serverPort) {
            ConnectionTask.serverScheme = serverScheme;
            ConnectionTask.serverHost = serverHost;
            ConnectionTask.serverPort = Integer.parseInt(serverPort);
            ConnectionTask.initialized = true;
    }

    public static boolean isInitialized() {
        return initialized;
    }
}
