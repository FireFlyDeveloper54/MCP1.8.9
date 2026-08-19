package net.minecraft.crash;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;

public class CrashReportCategory
{
    private final CrashReport crashReport;
    private final String name;
    private final List<CrashReportCategory.Entry> children = Lists.<CrashReportCategory.Entry>newArrayList();
    private StackTraceElement[] stackTrace = new StackTraceElement[0];

    public CrashReportCategory(CrashReport report, String name)
    {
        this.crashReport = report;
        this.name = name;
    }

    public static String getCoordinateInfo(double x, double y, double z)
    {
        return String.format(Locale.ROOT, "%.2f,%.2f,%.2f - %s", x, y, z, getCoordinateInfo(new BlockPos(x, y, z)));
    }

    public static String getCoordinateInfo(BlockPos pos)
    {
        int blockX = pos.getX();
        int blockY = pos.getY();
        int blockZ = pos.getZ();
        StringBuilder stringBuilder = new StringBuilder();

        try
        {
            stringBuilder.append("World: (").append(blockX).append(',').append(blockY).append(',').append(blockZ).append(')');
        }
        catch (Throwable caughtThrowable)
        {
            stringBuilder.append("(Error finding world loc)");
        }

        stringBuilder.append(", ");

        try
        {
            int chunkX = blockX >> 4;
            int chunkZ = blockZ >> 4;
            int chunkLocalX = blockX & 15;
            int chunkSectionY = blockY >> 4;
            int chunkLocalZ = blockZ & 15;
            int chunkMinBlockX = chunkX << 4;
            int chunkMinBlockZ = chunkZ << 4;
            int chunkMaxBlockX = (chunkX + 1 << 4) - 1;
            int chunkMaxBlockZ = (chunkZ + 1 << 4) - 1;
            stringBuilder.append("Chunk: (at ").append(chunkLocalX).append(',').append(chunkSectionY).append(',').append(chunkLocalZ).append(" in ").append(chunkX).append(',').append(chunkZ).append("; contains blocks ").append(chunkMinBlockX).append(",0,").append(chunkMinBlockZ).append(" to ").append(chunkMaxBlockX).append(",255,").append(chunkMaxBlockZ).append(')');
        }
        catch (Throwable caughtThrowable)
        {
            stringBuilder.append("(Error finding chunk loc)");
        }

        stringBuilder.append(", ");

        try
        {
            int regionX = blockX >> 9;
            int regionZ = blockZ >> 9;
            int regionMinChunkX = regionX << 5;
            int regionMinChunkZ = regionZ << 5;
            int regionMaxChunkX = (regionX + 1 << 5) - 1;
            int regionMaxChunkZ = (regionZ + 1 << 5) - 1;
            int regionMinBlockX = regionX << 9;
            int regionMinBlockZ = regionZ << 9;
            int regionMaxBlockX = (regionX + 1 << 9) - 1;
            int regionMaxBlockZ = (regionZ + 1 << 9) - 1;
            stringBuilder.append("Region: (").append(regionX).append(',').append(regionZ).append("; contains chunks ").append(regionMinChunkX).append(',').append(regionMinChunkZ).append(" to ").append(regionMaxChunkX).append(',').append(regionMaxChunkZ).append(", blocks ").append(regionMinBlockX).append(",0,").append(regionMinBlockZ).append(" to ").append(regionMaxBlockX).append(",255,").append(regionMaxBlockZ).append(')');
        }
        catch (Throwable caughtThrowable)
        {
            stringBuilder.append("(Error finding world loc)");
        }

        return stringBuilder.toString();
    }

    public void addCrashSectionCallable(String sectionName, Callable<String> callable)
    {
        try
        {
            this.addCrashSection(sectionName, callable.call());
        }
        catch (Throwable throwable)
        {
            this.addCrashSectionThrowable(sectionName, throwable);
        }
    }

    public void addCrashSection(String sectionName, Object value)
    {
        this.children.add(new CrashReportCategory.Entry(sectionName, value));
    }

    public void addCrashSectionThrowable(String sectionName, Throwable throwable)
    {
        this.addCrashSection(sectionName, throwable);
    }

    public int getPrunedStackTrace(int size)
    {
        StackTraceElement[] astacktraceelement = Thread.currentThread().getStackTrace();

        if (astacktraceelement.length <= 0)
        {
            return 0;
        }
        else
        {
            this.stackTrace = new StackTraceElement[astacktraceelement.length - 3 - size];
            System.arraycopy(astacktraceelement, 3 + size, this.stackTrace, 0, this.stackTrace.length);
            return this.stackTrace.length;
        }
    }

    public boolean firstTwoElementsOfStackTraceMatch(StackTraceElement firstElement, StackTraceElement secondElement)
    {
        if (this.stackTrace.length != 0 && firstElement != null)
        {
            StackTraceElement stacktraceelement = this.stackTrace[0];

            if (stacktraceelement.isNativeMethod() == firstElement.isNativeMethod() && stacktraceelement.getClassName().equals(firstElement.getClassName()) && stacktraceelement.getFileName().equals(firstElement.getFileName()) && stacktraceelement.getMethodName().equals(firstElement.getMethodName()))
            {
                if (secondElement != null != this.stackTrace.length > 1)
                {
                    return false;
                }
                else if (secondElement != null && !this.stackTrace[1].equals(secondElement))
                {
                    return false;
                }
                else
                {
                    this.stackTrace[0] = firstElement;
                    return true;
                }
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }

    public void trimStackTraceEntriesFromBottom(int amount)
    {
        StackTraceElement[] astacktraceelement = new StackTraceElement[this.stackTrace.length - amount];
        System.arraycopy(this.stackTrace, 0, astacktraceelement, 0, astacktraceelement.length);
        this.stackTrace = astacktraceelement;
    }

    public void appendToStringBuilder(StringBuilder builder)
    {
        builder.append("-- ").append(this.name).append(" --\n");
        builder.append("Details:");

        for (CrashReportCategory.Entry crashreportcategory$entry : this.children)
        {
            builder.append("\n\t");
            builder.append(crashreportcategory$entry.getKey());
            builder.append(": ");
            builder.append(crashreportcategory$entry.getValue());
        }

        if (this.stackTrace != null && this.stackTrace.length > 0)
        {
            builder.append("\nStacktrace:");

            for (StackTraceElement stackTraceElement : this.stackTrace)
            {
                builder.append("\n\tat ");
                builder.append(stackTraceElement.toString());
            }
        }
    }

    public StackTraceElement[] getStackTrace()
    {
        return this.stackTrace;
    }

    public static void addBlockInfo(CrashReportCategory category, final BlockPos pos, final Block blockIn, final int blockData)
    {
        final int i = Block.getIdFromBlock(blockIn);
        category.addCrashSectionCallable("Block type", new Callable<String>()
        {
            public String call() throws Exception
            {
                try
                {
                    return "ID #" + i + " (" + blockIn.getUnlocalizedName() + " // " + blockIn.getClass().getCanonicalName() + ")";
                }
                catch (Throwable caughtThrowable)
                {
                    return "ID #" + i;
                }
            }
        });
        category.addCrashSectionCallable("Block data value", new Callable<String>()
        {
            public String call() throws Exception
            {
                if (blockData < 0)
                {
                    return "Unknown? (Got " + blockData + ")";
                }
                else
                {
                    String binaryData = Integer.toBinaryString(blockData);

                    while (binaryData.length() < 4)
                    {
                        binaryData = "0" + binaryData;
                    }

                    return blockData + " / 0x" + Integer.toHexString(blockData).toUpperCase(Locale.ROOT) + " / 0b" + binaryData;
                }
            }
        });
        category.addCrashSectionCallable("Block location", new Callable<String>()
        {
            public String call() throws Exception
            {
                return CrashReportCategory.getCoordinateInfo(pos);
            }
        });
    }

    public static void addBlockInfo(CrashReportCategory category, final BlockPos pos, final IBlockState state)
    {
        category.addCrashSectionCallable("Block", new Callable<String>()
        {
            public String call() throws Exception
            {
                return state.toString();
            }
        });
        category.addCrashSectionCallable("Block location", new Callable<String>()
        {
            public String call() throws Exception
            {
                return CrashReportCategory.getCoordinateInfo(pos);
            }
        });
    }

    static class Entry
    {
        private final String key;
        private final String value;

        public Entry(String key, Object value)
        {
            this.key = key;

            if (value == null)
            {
                this.value = "~~NULL~~";
            }
            else if (value instanceof Throwable)
            {
                Throwable throwable = (Throwable)value;
                this.value = "~~ERROR~~ " + throwable.getClass().getSimpleName() + ": " + throwable.getMessage();
            }
            else
            {
                this.value = value.toString();
            }
        }

        public String getKey()
        {
            return this.key;
        }

        public String getValue()
        {
            return this.value;
        }
    }
}
