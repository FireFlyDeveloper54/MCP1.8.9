package net.optifine.http;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Map;

public class HttpPipelineSender extends Thread
{
    private HttpPipelineConnection httpPipelineConnection = null;
    private static final String CRLF = "\r\n";
    private static Charset ASCII = Charset.forName("ASCII");

    public HttpPipelineSender(HttpPipelineConnection httpPipelineConnection)
    {
        super("HttpPipelineSender");
        this.httpPipelineConnection = httpPipelineConnection;
    }

    public void run()
    {
        HttpPipelineRequest pipelineRequest = null;

        try
        {
            this.connect();

            while (!Thread.interrupted())
            {
                pipelineRequest = this.httpPipelineConnection.getNextRequestSend();
                HttpRequest httpRequest = pipelineRequest.getHttpRequest();
                OutputStream outputStream = this.httpPipelineConnection.getOutputStream();
                this.writeRequest(httpRequest, outputStream);
                this.httpPipelineConnection.onRequestSent(pipelineRequest);
            }
        }
        catch (InterruptedException caughtInterruptedException)
        {
            return;
        }
        catch (Exception exception)
        {
            this.httpPipelineConnection.onExceptionSend(pipelineRequest, exception);
        }
    }

    private void connect() throws IOException
    {
        String host = this.httpPipelineConnection.getHost();
        int port = this.httpPipelineConnection.getPort();
        Proxy proxy = this.httpPipelineConnection.getProxy();
        Socket socket = new Socket(proxy);
        socket.connect(new InetSocketAddress(host, port), 5000);
        this.httpPipelineConnection.setSocket(socket);
    }

    private void writeRequest(HttpRequest req, OutputStream out) throws IOException
    {
        this.write(out, req.getMethod() + " " + req.getFile() + " " + req.getHttp() + "\r\n");
        Map<String, String> headers = req.getHeaders();

        for (String headerName : headers.keySet())
        {
            String headerValue = (String)req.getHeaders().get(headerName);
            this.write(out, headerName + ": " + headerValue + "\r\n");
        }

        this.write(out, "\r\n");
    }

    private void write(OutputStream out, String str) throws IOException
    {
        byte[] bytes = str.getBytes(ASCII);
        out.write(bytes);
    }
}
