package net.optifine.http;

import net.minecraft.client.Minecraft;

public class FileDownloadThread extends Thread
{
    private String urlString = null;
    private IFileDownloadListener listener = null;

    public FileDownloadThread(String urlString, IFileDownloadListener listener)
    {
        this.urlString = urlString;
        this.listener = listener;
    }

    public void run()
    {
        // OptiFine online downloads are disabled for this local MCP project.
        this.listener.fileDownloadFinished(this.urlString, (byte[])null, (Throwable)null);
    }

    public String getUrlString()
    {
        return this.urlString;
    }

    public IFileDownloadListener getListener()
    {
        return this.listener;
    }
}
