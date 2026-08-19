package net.minecraft.client.util;

import com.google.common.collect.Lists;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public class JsonException extends IOException
{
    private final List<JsonException.Entry> pathStack = Lists.<JsonException.Entry>newArrayList();
    private final String exceptionMessage;

    public JsonException(String message)
    {
        this.pathStack.add(new JsonException.Entry());
        this.exceptionMessage = message;
    }

    public JsonException(String message, Throwable cause)
    {
        super(cause);
        this.pathStack.add(new JsonException.Entry());
        this.exceptionMessage = message;
    }

    public void addSection(String sectionName)
    {
        ((JsonException.Entry)this.pathStack.get(0)).addSection(sectionName);
    }

    public void setFilename(String filename)
    {
        ((JsonException.Entry)this.pathStack.get(0)).filename = filename;
        this.pathStack.add(0, new JsonException.Entry());
    }

    public String getMessage()
    {
        return "Invalid " + ((JsonException.Entry)this.pathStack.get(this.pathStack.size() - 1)).toString() + ": " + this.exceptionMessage;
    }

    public static JsonException wrap(Exception exception)
    {
        if (exception instanceof JsonException)
        {
            return (JsonException)exception;
        }
        else
        {
            String message = exception.getMessage();

            if (exception instanceof FileNotFoundException)
            {
                message = "File not found";
            }

            return new JsonException(message, exception);
        }
    }

    public static class Entry
    {
        private String filename;
        private final List<String> pathElements;

        private Entry()
        {
            this.filename = null;
            this.pathElements = Lists.<String>newArrayList();
        }

        private void addSection(String sectionName)
        {
            this.pathElements.add(0, sectionName);
        }

        public String getPath()
        {
            return StringUtils.join((Iterable)this.pathElements, "->");
        }

        public String toString()
        {
            return this.filename != null ? (!this.pathElements.isEmpty() ? this.filename + " " + this.getPath() : this.filename) : (!this.pathElements.isEmpty() ? "(Unknown file) " + this.getPath() : "(Unknown file)");
        }
    }
}
