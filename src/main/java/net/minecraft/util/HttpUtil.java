package net.minecraft.util;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.ServerSocket;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.server.MinecraftServer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HttpUtil
{
    public static final ListeningExecutorService DOWNLOAD_EXECUTOR = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool((new ThreadFactoryBuilder()).setDaemon(true).setNameFormat("Downloader %d").build()));
    private static final AtomicInteger downloadThreadsStarted = new AtomicInteger(0);
    private static final Logger logger = LogManager.getLogger();

    public static String buildPostString(Map<String, Object> data)
    {
        StringBuilder stringBuilder = new StringBuilder();

        for (Entry<String, Object> entry : data.entrySet())
        {
            if (stringBuilder.length() > 0)
            {
                stringBuilder.append('&');
            }

            try
            {
                stringBuilder.append(URLEncoder.encode((String)entry.getKey(), "UTF-8"));
            }
            catch (UnsupportedEncodingException unsupportedencodingexception1)
            {
                net.minecraft.src.Config.warn(unsupportedencodingexception1.getClass().getName() + ": " + unsupportedencodingexception1.getMessage(), unsupportedencodingexception1);
            }

            if (entry.getValue() != null)
            {
                stringBuilder.append('=');

                try
                {
                    stringBuilder.append(URLEncoder.encode(entry.getValue().toString(), "UTF-8"));
                }
                catch (UnsupportedEncodingException unsupportedencodingexception)
                {
                    net.minecraft.src.Config.warn(unsupportedencodingexception.getClass().getName() + ": " + unsupportedencodingexception.getMessage(), unsupportedencodingexception);
                }
            }
        }

        return stringBuilder.toString();
    }

    public static String postMap(URL url, Map<String, Object> data, boolean skipLoggingErrors)
    {
        return post(url, buildPostString(data), skipLoggingErrors);
    }

    private static String post(URL url, String content, boolean skipLoggingErrors)
    {
        try
        {
            Proxy proxy = MinecraftServer.getServer() == null ? null : MinecraftServer.getServer().getServerProxy();

            if (proxy == null)
            {
                proxy = Proxy.NO_PROXY;
            }

            HttpURLConnection httpurlconnection = (HttpURLConnection)url.openConnection(proxy);
            httpurlconnection.setRequestMethod("POST");
            httpurlconnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            httpurlconnection.setRequestProperty("Content-Length", "" + content.getBytes().length);
            httpurlconnection.setRequestProperty("Content-Language", "en-US");
            httpurlconnection.setUseCaches(false);
            httpurlconnection.setDoInput(true);
            httpurlconnection.setDoOutput(true);
            DataOutputStream dataOutputStream = new DataOutputStream(httpurlconnection.getOutputStream());
            dataOutputStream.writeBytes(content);
            dataOutputStream.flush();
            dataOutputStream.close();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpurlconnection.getInputStream()));
            StringBuffer stringBuffer = new StringBuffer();
            String line;

            while ((line = bufferedReader.readLine()) != null)
            {
                stringBuffer.append(line);
                stringBuffer.append('\r');
            }

            bufferedReader.close();
            return stringBuffer.toString();
        }
        catch (Exception exception)
        {
            if (!skipLoggingErrors)
            {
                logger.error((String)("Could not post to " + url), (Throwable)exception);
            }

            return "";
        }
    }

    public static ListenableFuture<Object> downloadResourcePack(final File saveFile, final String packUrl, final Map<String, String> requestProperties, final int maxSize, final IProgressUpdate progressUpdate, final Proxy proxy)
    {
        ListenableFuture<?> listenableFuture = DOWNLOAD_EXECUTOR.submit(new Runnable()
        {
            public void run()
            {
                HttpURLConnection httpURLConnection = null;
                InputStream inputStream = null;
                OutputStream outputStream = null;

                if (progressUpdate != null)
                {
                    progressUpdate.resetProgressAndMessage("Downloading Resource Pack");
                    progressUpdate.displayLoadingString("Making Request...");
                }

                try
                {
                    try
                    {
                        byte[] buffer = new byte[4096];
                        URL url = new URL(packUrl);
                        httpURLConnection = (HttpURLConnection)url.openConnection(proxy);
                        float downloadedSize = 0.0F;
                        float requestPropertyCount = (float)requestProperties.entrySet().size();

                        for (Entry<String, String> entry : requestProperties.entrySet())
                        {
                            httpURLConnection.setRequestProperty((String)entry.getKey(), (String)entry.getValue());

                            if (progressUpdate != null)
                            {
                                progressUpdate.setLoadingProgress((int)(++downloadedSize / requestPropertyCount * 100.0F));
                            }
                        }

                        inputStream = httpURLConnection.getInputStream();
                        requestPropertyCount = (float)httpURLConnection.getContentLength();
                        int contentLength = httpURLConnection.getContentLength();

                        if (progressUpdate != null)
                        {
                            progressUpdate.displayLoadingString(String.format(Locale.ROOT, "Downloading file (%.2f MB)...", requestPropertyCount / 1000.0F / 1000.0F));
                        }

                        if (saveFile.exists())
                        {
                            long existingFileSize = saveFile.length();

                            if (existingFileSize == (long)contentLength)
                            {
                                if (progressUpdate != null)
                                {
                                    progressUpdate.setDoneWorking();
                                }

                                return;
                            }

                            HttpUtil.logger.warn("Deleting " + saveFile + " as it does not match what we currently have (" + contentLength + " vs our " + existingFileSize + ").");
                            FileUtils.deleteQuietly(saveFile);
                        }
                        else if (saveFile.getParentFile() != null)
                        {
                            saveFile.getParentFile().mkdirs();
                        }

                        outputStream = new DataOutputStream(new FileOutputStream(saveFile));

                        if (maxSize > 0 && requestPropertyCount > (float)maxSize)
                        {
                            if (progressUpdate != null)
                            {
                                progressUpdate.setDoneWorking();
                            }

                            throw new IOException("Filesize is bigger than maximum allowed (file is " + downloadedSize + ", limit is " + maxSize + ")");
                        }

                        int bytesRead = 0;

                        while ((bytesRead = inputStream.read(buffer)) >= 0)
                        {
                            downloadedSize += (float)bytesRead;

                            if (progressUpdate != null)
                            {
                                progressUpdate.setLoadingProgress((int)(downloadedSize / requestPropertyCount * 100.0F));
                            }

                            if (maxSize > 0 && downloadedSize > (float)maxSize)
                            {
                                if (progressUpdate != null)
                                {
                                    progressUpdate.setDoneWorking();
                                }

                                throw new IOException("Filesize was bigger than maximum allowed (got >= " + downloadedSize + ", limit was " + maxSize + ")");
                            }

                            if (Thread.interrupted())
                            {
                                HttpUtil.logger.error("INTERRUPTED");

                                if (progressUpdate != null)
                                {
                                    progressUpdate.setDoneWorking();
                                }

                                return;
                            }

                            outputStream.write(buffer, 0, bytesRead);
                        }

                        if (progressUpdate != null)
                        {
                            progressUpdate.setDoneWorking();
                            return;
                        }
                    }
                    catch (Throwable throwable)
                    {
                        net.minecraft.src.Config.warn(throwable.getClass().getName() + ": " + throwable.getMessage(), throwable);

                        if (httpURLConnection != null)
                        {
                            InputStream inputstream1 = httpURLConnection.getErrorStream();

                            try
                            {
                                HttpUtil.logger.error(IOUtils.toString(inputstream1));
                            }
                            catch (IOException iOException)
                            {
                                net.minecraft.src.Config.warn(iOException.getClass().getName() + ": " + iOException.getMessage(), iOException);
                            }
                        }

                        if (progressUpdate != null)
                        {
                            progressUpdate.setDoneWorking();
                            return;
                        }
                    }
                }
                finally
                {
                    IOUtils.closeQuietly(inputStream);
                    IOUtils.closeQuietly(outputStream);
                }
            }
        });
        return (ListenableFuture<Object>) listenableFuture;
    }

    public static int getSuitableLanPort() throws IOException
    {
        ServerSocket serversocket = null;
        int port = -1;

        try
        {
            serversocket = new ServerSocket(0);
            port = serversocket.getLocalPort();
        }
        finally
        {
            try
            {
                if (serversocket != null)
                {
                    serversocket.close();
                }
            }
            catch (IOException caughtIoException)
            {
                ;
            }
        }

        return port;
    }

    public static String get(URL url) throws IOException
    {
        HttpURLConnection httpurlconnection = (HttpURLConnection)url.openConnection();
        httpurlconnection.setRequestMethod("GET");
        BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(httpurlconnection.getInputStream()));
        StringBuilder stringbuilder = new StringBuilder();
        String line;

        while ((line = bufferedreader.readLine()) != null)
        {
            stringbuilder.append(line);
            stringbuilder.append('\r');
        }

        bufferedreader.close();
        return stringbuilder.toString();
    }
}
