package net.optifine;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Log
{
    private static final Logger LOGGER = LogManager.getLogger();
    public static final boolean logDetail = System.getProperty("log.detail", "false").equals("true");

    public static void detail(String message)
    {
        if (logDetail)
        {
            LOGGER.info("[OptiFine] " + message);
        }
    }

    public static void dbg(String message)
    {
        LOGGER.info("[OptiFine] " + message);
    }

    public static void warn(String message)
    {
        LOGGER.warn("[OptiFine] " + message);
    }

    public static void warn(String message, Throwable throwable)
    {
        LOGGER.warn("[OptiFine] " + message, throwable);
    }

    public static void error(String message)
    {
        LOGGER.error("[OptiFine] " + message);
    }

    public static void error(String message, Throwable throwable)
    {
        LOGGER.error("[OptiFine] " + message, throwable);
    }

    public static void log(String message)
    {
        dbg(message);
    }
}
