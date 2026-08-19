package net.minecraft.command;

import com.google.gson.JsonParseException;
import java.util.List;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.S45PacketTitle;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentProcessor;
import net.minecraft.util.IChatComponent;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommandTitle extends CommandBase
{
    private static final Logger LOGGER = LogManager.getLogger();

    public String getCommandName()
    {
        return "title";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    public String getCommandUsage(ICommandSender sender)
    {
        return "commands.title.usage";
    }

    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length < 2)
        {
            throw new WrongUsageException("commands.title.usage", new Object[0]);
        }
        else
        {
            if (args.length < 3)
            {
                if ("title".equals(args[1]) || "subtitle".equals(args[1]))
                {
                    throw new WrongUsageException("commands.title.usage.title", new Object[0]);
                }

                if ("times".equals(args[1]))
                {
                    throw new WrongUsageException("commands.title.usage.times", new Object[0]);
                }
            }

            EntityPlayerMP targetPlayer = getPlayer(sender, args[0]);
            S45PacketTitle.Type titleType = S45PacketTitle.Type.byName(args[1]);

            if (titleType != S45PacketTitle.Type.CLEAR && titleType != S45PacketTitle.Type.RESET)
            {
                if (titleType == S45PacketTitle.Type.TIMES)
                {
                    if (args.length != 5)
                    {
                        throw new WrongUsageException("commands.title.usage", new Object[0]);
                    }
                    else
                    {
                        int fadeInTicks = parseInt(args[2]);
                        int displayTicks = parseInt(args[3]);
                        int fadeOutTicks = parseInt(args[4]);
                        S45PacketTitle timesPacket = new S45PacketTitle(fadeInTicks, displayTicks, fadeOutTicks);
                        targetPlayer.playerNetServerHandler.sendPacket(timesPacket);
                        notifyOperators(sender, this, "commands.title.success", new Object[0]);
                    }
                }
                else if (args.length < 3)
                {
                    throw new WrongUsageException("commands.title.usage", new Object[0]);
                }
                else
                {
                    String componentJson = buildString(args, 2);
                    IChatComponent titleComponent;

                    try
                    {
                        titleComponent = IChatComponent.Serializer.jsonToComponent(componentJson);
                    }
                    catch (JsonParseException jsonParseException)
                    {
                        Throwable throwable = ExceptionUtils.getRootCause(jsonParseException);
                        throw new SyntaxErrorException("commands.tellraw.jsonException", new Object[] {throwable == null ? "" : throwable.getMessage()});
                    }

                    S45PacketTitle titlePacket = new S45PacketTitle(titleType, ChatComponentProcessor.processComponent(sender, titleComponent, targetPlayer));
                    targetPlayer.playerNetServerHandler.sendPacket(titlePacket);
                    notifyOperators(sender, this, "commands.title.success", new Object[0]);
                }
            }
            else if (args.length != 2)
            {
                throw new WrongUsageException("commands.title.usage", new Object[0]);
            }
            else
            {
                S45PacketTitle titlePacket = new S45PacketTitle(titleType, (IChatComponent)null);
                targetPlayer.playerNetServerHandler.sendPacket(titlePacket);
                notifyOperators(sender, this, "commands.title.success", new Object[0]);
            }
        }
    }

    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos)
    {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames()) : (args.length == 2 ? getListOfStringsMatchingLastWord(args, S45PacketTitle.Type.getNames()) : null);
    }

    public boolean isUsernameIndex(String[] args, int index)
    {
        return index == 0;
    }
}
