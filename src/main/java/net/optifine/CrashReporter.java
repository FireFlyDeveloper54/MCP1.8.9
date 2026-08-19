package net.optifine;

import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.src.Config;
import net.optifine.shaders.Shaders;

public class CrashReporter
{
    public static void onCrashReport(CrashReport crashReport, CrashReportCategory category)
    {
        try
        {
            Throwable throwable = crashReport.getCrashCause();

            if (throwable == null)
            {
                return;
            }

            if (throwable.getClass().getName().contains(".fml.client.SplashProgress"))
            {
                return;
            }

            if (throwable.getClass() == Throwable.class)
            {
                return;
            }

            extendCrashReport(category);
            // OptiFine online crash report uploads are disabled for this local MCP project.
        }
        catch (Exception exception)
        {
            Config.dbg(exception.getClass().getName() + ": " + exception.getMessage());
        }
    }

    private static String makeReport(CrashReport crashReport)
    {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("OptiFineVersion: " + Config.getVersion() + "\n");
        stringBuffer.append("Summary: " + makeSummary(crashReport) + "\n");
        stringBuffer.append("\n");
        stringBuffer.append(crashReport.getCompleteReport());
        stringBuffer.append("\n");
        return stringBuffer.toString();
    }

    private static String makeSummary(CrashReport crashReport)
    {
        Throwable throwable = crashReport.getCrashCause();

        if (throwable == null)
        {
            return "Unknown";
        }
        else
        {
            StackTraceElement[] astacktraceelement = throwable.getStackTrace();
            String stackTraceEntry = "unknown";

            if (astacktraceelement.length > 0)
            {
                stackTraceEntry = astacktraceelement[0].toString().trim();
            }

            String summary = throwable.getClass().getName() + ": " + throwable.getMessage() + " (" + crashReport.getDescription() + ")" + " [" + stackTraceEntry + "]";
            return summary;
        }
    }

    public static void extendCrashReport(CrashReportCategory cat)
    {
        cat.addCrashSection("OptiFine Version", Config.getVersion());
        cat.addCrashSection("OptiFine Build", Config.getBuild());

        if (Config.getGameSettings() != null)
        {
            cat.addCrashSection("Render Distance Chunks", "" + Config.getChunkViewDistance());
            cat.addCrashSection("Mipmaps", "" + Config.getMipmapLevels());
            cat.addCrashSection("Anisotropic Filtering", "" + Config.getAnisotropicFilterLevel());
            cat.addCrashSection("Antialiasing", "" + Config.getAntialiasingLevel());
            cat.addCrashSection("Multitexture", "" + Config.isMultiTexture());
        }

        cat.addCrashSection("Shaders", "" + Shaders.getShaderPackName());
        cat.addCrashSection("OpenGlVersion", "" + Config.openGlVersion);
        cat.addCrashSection("OpenGlRenderer", "" + Config.openGlRenderer);
        cat.addCrashSection("OpenGlVendor", "" + Config.openGlVendor);
        cat.addCrashSection("CpuCount", "" + Config.getAvailableProcessors());
    }
}
