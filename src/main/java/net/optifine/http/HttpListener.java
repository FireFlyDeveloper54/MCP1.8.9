package net.optifine.http;

public interface HttpListener
{
    void finished(HttpRequest request, HttpResponse response);

    void failed(HttpRequest request, Exception exception);
}
