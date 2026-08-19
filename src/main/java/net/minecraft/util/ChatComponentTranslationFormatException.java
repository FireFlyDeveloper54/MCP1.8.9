package net.minecraft.util;

public class ChatComponentTranslationFormatException extends IllegalArgumentException
{
    public ChatComponentTranslationFormatException(ChatComponentTranslation component, String message)
    {
        super("Error parsing: " + component + ": " + message);
    }

    public ChatComponentTranslationFormatException(ChatComponentTranslation component, int index)
    {
        super("Invalid index " + index + " requested for " + component);
    }

    public ChatComponentTranslationFormatException(ChatComponentTranslation component, Throwable cause)
    {
        super("Error while parsing: " + component, cause);
    }
}
