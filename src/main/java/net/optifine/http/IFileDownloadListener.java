package net.optifine.http;

public interface IFileDownloadListener
{
    void fileDownloadFinished(String url, byte[] bytes, Throwable exception);
}
