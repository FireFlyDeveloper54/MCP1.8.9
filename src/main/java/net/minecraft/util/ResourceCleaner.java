package net.minecraft.util;

import java.lang.reflect.Method;

/**
 * JDK 8 keeps {@code finalize}; JDK 9+ uses {@code java.lang.ref.Cleaner} via reflection
 * so this class still compiles and runs from Java 8 through 25.
 */
public final class ResourceCleaner
{
    private static final Object CLEANER;
    private static final Method REGISTER;

    static
    {
        Object cleaner = null;
        Method register = null;

        try
        {
            Class<?> cleanerClass = Class.forName("java.lang.ref.Cleaner");
            cleaner = cleanerClass.getMethod("create").invoke((Object)null);
            register = cleanerClass.getMethod("register", new Class[] {Object.class, Runnable.class});
        }
        catch (Throwable ignored)
        {
        }

        CLEANER = cleaner;
        REGISTER = register;
    }

    private ResourceCleaner()
    {
    }

    public static boolean hasCleaner()
    {
        return CLEANER != null && REGISTER != null;
    }

    public static Object register(Object owner, Runnable action)
    {
        if (!hasCleaner() || owner == null || action == null)
        {
            return null;
        }

        try
        {
            return REGISTER.invoke(CLEANER, new Object[] {owner, action});
        }
        catch (Throwable ignored)
        {
            return null;
        }
    }
}
