package net.optifine.http;

import java.util.Map;

public class FileUploadThread extends Thread
{
    private String urlString;
    private Map headers;
    private byte[] content;
    private IFileUploadListener listener;

    public FileUploadThread(String urlString, Map headers, byte[] content, IFileUploadListener listener)
    {
        this.urlString = urlString;
        this.headers = headers;
        this.content = content;
        this.listener = listener;
    }

    public void run()
    {
        // OptiFine online uploads are disabled for this local MCP project.
        this.listener.fileUploadFinished(this.urlString, this.content, (Throwable)null);
    }

    public String getUrlString()
    {
        return this.urlString;
    }

    public byte[] getContent()
    {
        return this.content;
    }

    public IFileUploadListener getListener()
    {
        return this.listener;
    }
}
