package net.optifine.http;

import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Proxy;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import net.minecraft.src.Config;

public class HttpPipelineConnection
{
    private String host;
    private int port;
    private Proxy proxy;
    private List<HttpPipelineRequest> listRequests;
    private List<HttpPipelineRequest> listRequestsSend;
    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private HttpPipelineSender httpPipelineSender;
    private HttpPipelineReceiver httpPipelineReceiver;
    private int countRequests;
    private boolean responseReceived;
    private long keepaliveTimeoutMs;
    private int keepaliveMaxCount;
    private long timeLastActivityMs;
    private boolean terminated;
    private static final String LF = "\n";
    public static final int TIMEOUT_CONNECT_MS = 5000;
    public static final int TIMEOUT_READ_MS = 5000;
    private static final Pattern patternFullUrl = Pattern.compile("^[a-zA-Z]+://.*");

    public HttpPipelineConnection(String host, int port)
    {
        this(host, port, Proxy.NO_PROXY);
    }

    public HttpPipelineConnection(String host, int port, Proxy proxy)
    {
        this.host = null;
        this.port = 0;
        this.proxy = Proxy.NO_PROXY;
        this.listRequests = new LinkedList();
        this.listRequestsSend = new LinkedList();
        this.socket = null;
        this.inputStream = null;
        this.outputStream = null;
        this.httpPipelineSender = null;
        this.httpPipelineReceiver = null;
        this.countRequests = 0;
        this.responseReceived = false;
        this.keepaliveTimeoutMs = 5000L;
        this.keepaliveMaxCount = 1000;
        this.timeLastActivityMs = System.currentTimeMillis();
        this.terminated = false;
        this.host = host;
        this.port = port;
        this.proxy = proxy;
        this.httpPipelineSender = new HttpPipelineSender(this);
        this.httpPipelineSender.start();
        this.httpPipelineReceiver = new HttpPipelineReceiver(this);
        this.httpPipelineReceiver.start();
    }

    public synchronized boolean addRequest(HttpPipelineRequest pr)
    {
        if (this.isClosed())
        {
            return false;
        }
        else
        {
            this.addRequest(pr, this.listRequests);
            this.addRequest(pr, this.listRequestsSend);
            ++this.countRequests;
            return true;
        }
    }

    private void addRequest(HttpPipelineRequest pr, List<HttpPipelineRequest> list)
    {
        list.add(pr);
        this.notifyAll();
    }

    public synchronized void setSocket(Socket s) throws IOException
    {
        if (!this.terminated)
        {
            if (this.socket != null)
            {
                throw new IllegalArgumentException("Already connected");
            }
            else
            {
                this.socket = s;
                this.socket.setTcpNoDelay(true);
                this.inputStream = this.socket.getInputStream();
                this.outputStream = new BufferedOutputStream(this.socket.getOutputStream());
                this.onActivity();
                this.notifyAll();
            }
        }
    }

    public synchronized OutputStream getOutputStream() throws IOException, InterruptedException
    {
        while (this.outputStream == null)
        {
            this.checkTimeout();
            this.wait(1000L);
        }

        return this.outputStream;
    }

    public synchronized InputStream getInputStream() throws IOException, InterruptedException
    {
        while (this.inputStream == null)
        {
            this.checkTimeout();
            this.wait(1000L);
        }

        return this.inputStream;
    }

    public synchronized HttpPipelineRequest getNextRequestSend() throws InterruptedException, IOException
    {
        if (this.listRequestsSend.size() <= 0 && this.outputStream != null)
        {
            this.outputStream.flush();
        }

        return this.getNextRequest(this.listRequestsSend, true);
    }

    public synchronized HttpPipelineRequest getNextRequestReceive() throws InterruptedException
    {
        return this.getNextRequest(this.listRequests, false);
    }

    private HttpPipelineRequest getNextRequest(List<HttpPipelineRequest> list, boolean remove) throws InterruptedException
    {
        while (list.size() <= 0)
        {
            this.checkTimeout();
            this.wait(1000L);
        }

        this.onActivity();

        if (remove)
        {
            return (HttpPipelineRequest)list.remove(0);
        }
        else
        {
            return (HttpPipelineRequest)list.get(0);
        }
    }

    private void checkTimeout()
    {
        if (this.socket != null)
        {
            long timeoutMs = this.keepaliveTimeoutMs;

            if (this.listRequests.size() > 0)
            {
                timeoutMs = 5000L;
            }

            long nowMs = System.currentTimeMillis();

            if (nowMs > this.timeLastActivityMs + timeoutMs)
            {
                this.terminate(new InterruptedException("Timeout " + timeoutMs));
            }
        }
    }

    private void onActivity()
    {
        this.timeLastActivityMs = System.currentTimeMillis();
    }

    public synchronized void onRequestSent(HttpPipelineRequest pr)
    {
        if (!this.terminated)
        {
            this.onActivity();
        }
    }

    public synchronized void onResponseReceived(HttpPipelineRequest pr, HttpResponse resp)
    {
        if (!this.terminated)
        {
            this.responseReceived = true;
            this.onActivity();

            if (this.listRequests.size() > 0 && this.listRequests.get(0) == pr)
            {
                this.listRequests.remove(0);
                pr.setClosed(true);
                String redirectLocation = resp.getHeader("Location");

                if (resp.getStatus() / 100 == 3 && redirectLocation != null && pr.getHttpRequest().getRedirects() < 5)
                {
                    try
                    {
                        redirectLocation = this.normalizeUrl(redirectLocation, pr.getHttpRequest());
                        HttpRequest redirectRequest = HttpPipeline.makeRequest(redirectLocation, pr.getHttpRequest().getProxy());
                        redirectRequest.setRedirects(pr.getHttpRequest().getRedirects() + 1);
                        HttpPipelineRequest pipelineRequest = new HttpPipelineRequest(redirectRequest, pr.getHttpListener());
                        HttpPipeline.addRequest(pipelineRequest);
                    }
                    catch (IOException ioException)
                    {
                        pr.getHttpListener().failed(pr.getHttpRequest(), ioException);
                    }
                }
                else
                {
                    HttpListener listener = pr.getHttpListener();
                    listener.finished(pr.getHttpRequest(), resp);
                }

                this.checkResponseHeader(resp);
            }
            else
            {
                throw new IllegalArgumentException("Response out of order: " + pr);
            }
        }
    }

    private String normalizeUrl(String url, HttpRequest hr)
    {
        if (patternFullUrl.matcher(url).matches())
        {
            return url;
        }
        else if (url.startsWith("//"))
        {
            return "http:" + url;
        }
        else
        {
            String hostAndPort = hr.getHost();

            if (hr.getPort() != 80)
            {
                hostAndPort = hostAndPort + ":" + hr.getPort();
            }

            if (url.startsWith("/"))
            {
                return "http://" + hostAndPort + url;
            }
            else
            {
                String requestFile = hr.getFile();
                int lastSlashIndex = requestFile.lastIndexOf("/");
                return lastSlashIndex >= 0 ? "http://" + hostAndPort + requestFile.substring(0, lastSlashIndex + 1) + url : "http://" + hostAndPort + "/" + url;
            }
        }
    }

    private void checkResponseHeader(HttpResponse resp)
    {
        String connectionHeader = resp.getHeader("Connection");

        if (connectionHeader != null && !connectionHeader.toLowerCase().equals("keep-alive"))
        {
            this.terminate(new EOFException("Connection not keep-alive"));
        }

        String keepAliveHeader = resp.getHeader("Keep-Alive");

        if (keepAliveHeader != null)
        {
            String[] keepAliveParts = Config.tokenize(keepAliveHeader, ",;");

            for (int partIndex = 0; partIndex < keepAliveParts.length; ++partIndex)
            {
                String keepAlivePart = keepAliveParts[partIndex];
                String[] keyValue = this.split(keepAlivePart, '=');

                if (keyValue.length >= 2)
                {
                    if (keyValue[0].equals("timeout"))
                    {
                        int timeoutSeconds = Config.parseInt(keyValue[1], -1);

                        if (timeoutSeconds > 0)
                        {
                            this.keepaliveTimeoutMs = (long)(timeoutSeconds * 1000);
                        }
                    }

                    if (keyValue[0].equals("max"))
                    {
                        int maxRequests = Config.parseInt(keyValue[1], -1);

                        if (maxRequests > 0)
                        {
                            this.keepaliveMaxCount = maxRequests;
                        }
                    }
                }
            }
        }
    }

    private String[] split(String str, char separator)
    {
        int separatorIndex = str.indexOf(separator);

        if (separatorIndex < 0)
        {
            return new String[] {str};
        }
        else
        {
            String key = str.substring(0, separatorIndex);
            String value = str.substring(separatorIndex + 1);
            return new String[] {key, value};
        }
    }

    public synchronized void onExceptionSend(HttpPipelineRequest pr, Exception e)
    {
        this.terminate(e);
    }

    public synchronized void onExceptionReceive(HttpPipelineRequest pr, Exception e)
    {
        this.terminate(e);
    }

    private synchronized void terminate(Exception e)
    {
        if (!this.terminated)
        {
            this.terminated = true;
            this.terminateRequests(e);

            if (this.httpPipelineSender != null)
            {
                this.httpPipelineSender.interrupt();
            }

            if (this.httpPipelineReceiver != null)
            {
                this.httpPipelineReceiver.interrupt();
            }

            try
            {
                if (this.socket != null)
                {
                    this.socket.close();
                }
            }
            catch (IOException caughtIoException)
            {
                ;
            }

            this.socket = null;
            this.inputStream = null;
            this.outputStream = null;
        }
    }

    private void terminateRequests(Exception e)
    {
        if (this.listRequests.size() > 0)
        {
            if (!this.responseReceived)
            {
                HttpPipelineRequest failedRequest = (HttpPipelineRequest)this.listRequests.remove(0);
                failedRequest.getHttpListener().failed(failedRequest.getHttpRequest(), e);
                failedRequest.setClosed(true);
            }

            while (this.listRequests.size() > 0)
            {
                HttpPipelineRequest queuedRequest = (HttpPipelineRequest)this.listRequests.remove(0);
                HttpPipeline.addRequest(queuedRequest);
            }
        }
    }

    public synchronized boolean isClosed()
    {
        return this.terminated ? true : this.countRequests >= this.keepaliveMaxCount;
    }

    public int getCountRequests()
    {
        return this.countRequests;
    }

    public synchronized boolean hasActiveRequests()
    {
        return this.listRequests.size() > 0;
    }

    public String getHost()
    {
        return this.host;
    }

    public int getPort()
    {
        return this.port;
    }

    public Proxy getProxy()
    {
        return this.proxy;
    }
}
