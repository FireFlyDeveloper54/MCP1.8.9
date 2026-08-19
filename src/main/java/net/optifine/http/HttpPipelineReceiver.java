package net.optifine.http;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.src.Config;

public class HttpPipelineReceiver extends Thread
{
    private HttpPipelineConnection httpPipelineConnection = null;
    private static final Charset ASCII = Charset.forName("ASCII");
    private static final String HEADER_CONTENT_LENGTH = "Content-Length";
    private static final char CR = '\r';
    private static final char LF = '\n';

    public HttpPipelineReceiver(HttpPipelineConnection httpPipelineConnection)
    {
        super("HttpPipelineReceiver");
        this.httpPipelineConnection = httpPipelineConnection;
    }

    public void run()
    {
        while (!Thread.interrupted())
        {
            HttpPipelineRequest pipelineRequest = null;

            try
            {
                pipelineRequest = this.httpPipelineConnection.getNextRequestReceive();
                InputStream inputStream = this.httpPipelineConnection.getInputStream();
                HttpResponse httpResponse = this.readResponse(inputStream);
                this.httpPipelineConnection.onResponseReceived(pipelineRequest, httpResponse);
            }
            catch (InterruptedException caughtInterruptedException)
            {
                return;
            }
            catch (Exception exception)
            {
                this.httpPipelineConnection.onExceptionReceive(pipelineRequest, exception);
            }
        }
    }

    private HttpResponse readResponse(InputStream in) throws IOException
    {
        String statusLine = this.readLine(in);
        String[] statusParts = Config.tokenize(statusLine, " ");

        if (statusParts.length < 3)
        {
            throw new IOException("Invalid status line: " + statusLine);
        }
        else
        {
            String protocol = statusParts[0];
            int status = Config.parseInt(statusParts[1], 0);
            String statusText = statusParts[2];
            Map<String, String> headers = new LinkedHashMap();

            while (true)
            {
                String headerLine = this.readLine(in);

                if (headerLine.length() <= 0)
                {
                    byte[] responseBody = null;
                    String contentLengthHeader = (String)headers.get("Content-Length");

                    if (contentLengthHeader != null)
                    {
                        int contentLength = Config.parseInt(contentLengthHeader, -1);

                        if (contentLength > 0)
                        {
                            responseBody = new byte[contentLength];
                            this.readFull(responseBody, in);
                        }
                    }
                    else
                    {
                        String transferEncoding = (String)headers.get("Transfer-Encoding");

                        if (Config.equals(transferEncoding, "chunked"))
                        {
                            responseBody = this.readContentChunked(in);
                        }
                    }

                    return new HttpResponse(status, statusLine, headers, responseBody);
                }

                int separatorIndex = headerLine.indexOf(":");

                if (separatorIndex > 0)
                {
                    String headerName = headerLine.substring(0, separatorIndex).trim();
                    String headerValue = headerLine.substring(separatorIndex + 1).trim();
                    headers.put(headerName, headerValue);
                }
            }
        }
    }

    private byte[] readContentChunked(InputStream in) throws IOException
    {
        ByteArrayOutputStream contentBuffer = new ByteArrayOutputStream();

        while (true)
        {
            String chunkHeaderLine = this.readLine(in);
            String[] chunkHeaderParts = Config.tokenize(chunkHeaderLine, "; ");
            int chunkSize = Integer.parseInt(chunkHeaderParts[0], 16);
            byte[] chunkData = new byte[chunkSize];
            this.readFull(chunkData, in);
            contentBuffer.write(chunkData);
            this.readLine(in);

            if (chunkSize == 0)
            {
                break;
            }
        }

        return contentBuffer.toByteArray();
    }

    private void readFull(byte[] buf, InputStream in) throws IOException
    {
        int bytesRead;

        for (int offset = 0; offset < buf.length; offset += bytesRead)
        {
            bytesRead = in.read(buf, offset, buf.length - offset);

            if (bytesRead < 0)
            {
                throw new EOFException();
            }
        }
    }

    private String readLine(InputStream in) throws IOException
    {
        ByteArrayOutputStream lineBuffer = new ByteArrayOutputStream();
        int previousChar = -1;
        boolean hasCrlf = false;

        while (true)
        {
            int currentChar = in.read();

            if (currentChar < 0)
            {
                break;
            }

            lineBuffer.write(currentChar);

            if (previousChar == CR && currentChar == LF)
            {
                hasCrlf = true;
                break;
            }

            previousChar = currentChar;
        }

        byte[] lineBytes = lineBuffer.toByteArray();
        String line = new String(lineBytes, ASCII);

        if (hasCrlf)
        {
            line = line.substring(0, line.length() - 2);
        }

        return line;
    }
}
