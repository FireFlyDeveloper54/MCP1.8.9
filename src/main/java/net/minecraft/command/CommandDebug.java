package net.minecraft.command;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import net.minecraft.profiler.Profiler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommandDebug extends CommandBase
{
    private static final Logger logger = LogManager.getLogger();
    private long profileStartTime;
    private int profileStartTick;

    public String getCommandName()
    {
        return "debug";
    }

    public int getRequiredPermissionLevel()
    {
        return 3;
    }

    public String getCommandUsage(ICommandSender sender)
    {
        return "commands.debug.usage";
    }

    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length < 1)
        {
            throw new WrongUsageException("commands.debug.usage", new Object[0]);
        }
        else
        {
            if (args[0].equals("start"))
            {
                if (args.length != 1)
                {
                    throw new WrongUsageException("commands.debug.usage", new Object[0]);
                }

                notifyOperators(sender, this, "commands.debug.start", new Object[0]);
                MinecraftServer.getServer().enableProfiling();
                this.profileStartTime = MinecraftServer.getCurrentTimeMillis();
                this.profileStartTick = MinecraftServer.getServer().getTickCounter();
            }
            else
            {
                if (!args[0].equals("stop"))
                {
                    throw new WrongUsageException("commands.debug.usage", new Object[0]);
                }

                if (args.length != 1)
                {
                    throw new WrongUsageException("commands.debug.usage", new Object[0]);
                }

                if (!MinecraftServer.getServer().theProfiler.profilingEnabled)
                {
                    throw new CommandException("commands.debug.notStarted", new Object[0]);
                }

                long currentTimeMillis = MinecraftServer.getCurrentTimeMillis();
                int currentTick = MinecraftServer.getServer().getTickCounter();
                long elapsedMillis = currentTimeMillis - this.profileStartTime;
                int elapsedTicks = currentTick - this.profileStartTick;
                this.saveProfileResults(elapsedMillis, elapsedTicks);
                MinecraftServer.getServer().theProfiler.profilingEnabled = false;
                notifyOperators(sender, this, "commands.debug.stop", new Object[] {Float.valueOf((float)elapsedMillis / 1000.0F), Integer.valueOf(elapsedTicks)});
            }
        }
    }

    private void saveProfileResults(long timeSpan, int tickSpan)
    {
        File file1 = new File(MinecraftServer.getServer().getFile("debug"), "profile-results-" + (new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss")).format(new Date()) + ".txt");
        file1.getParentFile().mkdirs();

        try
        {
            FileWriter fileWriter = new FileWriter(file1);
            fileWriter.write(this.getProfileResults(timeSpan, tickSpan));
            fileWriter.close();
        }
        catch (Throwable throwable)
        {
            logger.error("Could not save profiler results to " + file1, throwable);
        }
    }

    private String getProfileResults(long timeSpan, int tickSpan)
    {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("---- Minecraft Profiler Results ----\n");
        stringBuilder.append("// ");
        stringBuilder.append(getWittyComment());
        stringBuilder.append("\n\n");
        stringBuilder.append("Time span: ").append(timeSpan).append(" ms\n");
        stringBuilder.append("Tick span: ").append(tickSpan).append(" ticks\n");
        stringBuilder.append("// This is approximately ").append(String.format(Locale.ROOT, "%.2f", new Object[] {Float.valueOf((float)tickSpan / ((float)timeSpan / 1000.0F))})).append(" ticks per second. It should be ").append((int)20).append(" ticks per second\n\n");
        stringBuilder.append("--- BEGIN PROFILE DUMP ---\n\n");
        this.appendProfilerResults(0, "root", stringBuilder);
        stringBuilder.append("--- END PROFILE DUMP ---\n\n");
        return stringBuilder.toString();
    }

    private void appendProfilerResults(int depth, String sectionName, StringBuilder stringBuilder)
    {
        List<Profiler.Result> list = MinecraftServer.getServer().theProfiler.getProfilingData(sectionName);

        if (list != null && list.size() >= 3)
        {
            for (int resultIndex = 1; resultIndex < list.size(); ++resultIndex)
            {
                Profiler.Result profiler$result = (Profiler.Result)list.get(resultIndex);
                stringBuilder.append(String.format(Locale.ROOT, "[%02d] ", new Object[] {Integer.valueOf(depth)}));

                for (int indent = 0; indent < depth; ++indent)
                {
                    stringBuilder.append(" ");
                }

                stringBuilder.append(profiler$result.profilerName).append(" - ").append(String.format(Locale.ROOT, "%.2f", new Object[] {Double.valueOf(profiler$result.usePercentage)})).append("%/").append(String.format(Locale.ROOT, "%.2f", new Object[] {Double.valueOf(profiler$result.totalUsePercentage)})).append("%\n");

                if (!profiler$result.profilerName.equals("unspecified"))
                {
                    try
                    {
                        this.appendProfilerResults(depth + 1, sectionName + "." + profiler$result.profilerName, stringBuilder);
                    }
                    catch (Exception exception)
                    {
                        stringBuilder.append("[[ EXCEPTION ").append((Object)exception).append(" ]]");
                    }
                }
            }
        }
    }

    private static String getWittyComment()
    {
        String[] astring = new String[] {"Shiny numbers!", "Am I not running fast enough? :(", "I\'m working as hard as I can!", "Will I ever be good enough for you? :(", "Speedy. Zoooooom!", "Hello world", "40% better than a crash report.", "Now with extra numbers", "Now with less numbers", "Now with the same numbers", "You should add flames to things, it makes them go faster!", "Do you feel the need for... optimization?", "*cracks redstone whip*", "Maybe if you treated it better then it\'ll have more motivation to work faster! Poor server."};

        try
        {
            return astring[(int)(System.nanoTime() % (long)astring.length)];
        }
        catch (Throwable caughtThrowable)
        {
            return "Witty comment unavailable :(";
        }
    }

    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos)
    {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, new String[] {"start", "stop"}): null;
    }
}
