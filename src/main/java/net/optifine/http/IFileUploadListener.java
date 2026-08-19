package net.optifine.http;

public interface IFileUploadListener
{
    void fileUploadFinished(String url, byte[] content, Throwable exception);
}
