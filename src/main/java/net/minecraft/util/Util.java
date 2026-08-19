package net.minecraft.util;

import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import org.apache.logging.log4j.Logger;

public class Util
{
    public static Util.EnumOS getOSType()
    {
        String osName = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        return osName.contains("win") ? Util.EnumOS.WINDOWS : (osName.contains("mac") ? Util.EnumOS.OSX : (osName.contains("solaris") ? Util.EnumOS.SOLARIS : (osName.contains("sunos") ? Util.EnumOS.SOLARIS : (osName.contains("linux") ? Util.EnumOS.LINUX : (osName.contains("unix") ? Util.EnumOS.LINUX : Util.EnumOS.UNKNOWN)))));
    }

    public static <V> V runTask(FutureTask<V> task, Logger logger)
    {
        try
        {
            task.run();
            return task.get();
        }
        catch (ExecutionException executionexception)
        {
            logger.fatal((String)"Error executing task", (Throwable)executionexception);

            if (executionexception.getCause() instanceof OutOfMemoryError)
            {
                OutOfMemoryError outofmemoryerror = (OutOfMemoryError)executionexception.getCause();
                throw outofmemoryerror;
            }
        }
        catch (InterruptedException interruptedexception)
        {
            logger.fatal((String)"Error executing task", (Throwable)interruptedexception);
        }

        return (V)((Object)null);
    }

    public static enum EnumOS
    {
        LINUX,
        SOLARIS,
        WINDOWS,
        OSX,
        UNKNOWN;
    }
}
