package net.optifine.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.src.Config;

public class HttpPipeline
{
    private static Map mapConnections = new HashMap();
    public static final String HEADER_USER_AGENT = "User-Agent";
    public static final String HEADER_HOST = "Host";
    public static final String HEADER_ACCEPT = "Accept";
    public static final String HEADER_LOCATION = "Location";
    public static final String HEADER_KEEP_ALIVE = "Keep-Alive";
    public static final String HEADER_CONNECTION = "Connection";
    public static final String HEADER_VALUE_KEEP_ALIVE = "keep-alive";
    public static final String HEADER_TRANSFER_ENCODING = "Transfer-Encoding";
    public static final String HEADER_VALUE_CHUNKED = "chunked";

    public static void addRequest(String urlStr, HttpListener listener) throws IOException
    {
        addRequest(urlStr, listener, Proxy.NO_PROXY);
    }

    public static void addRequest(String urlStr, HttpListener listener, Proxy proxy) throws IOException
    {
        throw new IOException("OptiFine online HTTP pipeline is disabled");
    }

    public static HttpRequest makeRequest(String urlStr, Proxy proxy) throws IOException
    {
        throw new IOException("OptiFine online HTTP pipeline is disabled");
    }

    public static void addRequest(HttpPipelineRequest pr)
    {
        throw new IllegalStateException("OptiFine online HTTP pipeline is disabled");
    }

    private static synchronized HttpPipelineConnection getConnection(String host, int port, Proxy proxy)
    {
        String connectionKey = makeConnectionKey(host, port, proxy);
        HttpPipelineConnection connection = (HttpPipelineConnection)mapConnections.get(connectionKey);

        if (connection == null)
        {
            connection = new HttpPipelineConnection(host, port, proxy);
            mapConnections.put(connectionKey, connection);
        }

        return connection;
    }

    private static synchronized void removeConnection(String host, int port, Proxy proxy, HttpPipelineConnection hpc)
    {
        String connectionKey = makeConnectionKey(host, port, proxy);
        HttpPipelineConnection connection = (HttpPipelineConnection)mapConnections.get(connectionKey);

        if (connection == hpc)
        {
            mapConnections.remove(connectionKey);
        }
    }

    private static String makeConnectionKey(String host, int port, Proxy proxy)
    {
        String connectionKey = host + ":" + port + "-" + proxy;
        return connectionKey;
    }

    public static byte[] get(String urlStr) throws IOException
    {
        return get(urlStr, Proxy.NO_PROXY);
    }

    public static byte[] get(String urlStr, Proxy proxy) throws IOException
    {
        if (urlStr.startsWith("file:"))
        {
            URL url = new URL(urlStr);
            InputStream inputStream = url.openStream();
            byte[] fileBytes = Config.readAll(inputStream);
            return fileBytes;
        }
        else
        {
            throw new IOException("OptiFine online HTTP pipeline is disabled");
        }
    }

    public static HttpResponse executeRequest(HttpRequest req) throws IOException
    {
        throw new IOException("OptiFine online HTTP pipeline is disabled");
    }

    public static boolean hasActiveRequests()
    {
        for (Object o : mapConnections.values())
        {
            HttpPipelineConnection connection = (HttpPipelineConnection) o;
            if (connection.hasActiveRequests())
            {
                return true;
            }
        }

        return false;
    }
}
