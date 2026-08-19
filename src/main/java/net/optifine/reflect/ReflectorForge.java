package net.optifine.reflect;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class ReflectorForge
{
    public static InputStream getOptiFineResourceStream(String path)
    {
        if (!Reflector.OptiFineClassTransformer_instance.exists())
        {
            return null;
        }

        Object optifineTransformer = Reflector.getFieldValue(Reflector.OptiFineClassTransformer_instance);

        if (optifineTransformer == null)
        {
            return null;
        }

        if (path.startsWith("/"))
        {
            path = path.substring(1);
        }

        byte[] resourceBytes = (byte[])Reflector.call(optifineTransformer, Reflector.OptiFineClassTransformer_getOptiFineResource, new Object[] {path});

        if (resourceBytes == null)
        {
            return null;
        }

        return new ByteArrayInputStream(resourceBytes);
    }
}
