package net.minecraft.command;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommandHandler implements ICommandManager
{
    private static final Logger logger = LogManager.getLogger();
    private final Map<String, ICommand> commandMap = Maps.<String, ICommand>newHashMap();
    private final Set<ICommand> commandSet = Sets.<ICommand>newHashSet();

    public int executeCommand(ICommandSender sender, String rawCommand)
    {
        rawCommand = rawCommand.trim();

        if (rawCommand.startsWith("/"))
        {
            rawCommand = rawCommand.substring(1);
        }

        String[] astring = rawCommand.split(" ");
        String commandName = astring[0];
        astring = dropFirstString(astring);
        ICommand icommand = (ICommand)this.commandMap.get(commandName);
        int usernameIndex = this.getUsernameIndex(icommand, astring);
        int successCount = 0;

        if (icommand == null)
        {
            ChatComponentTranslation chatComponentTranslation = new ChatComponentTranslation("commands.generic.notFound", new Object[0]);
            chatComponentTranslation.getChatStyle().setColor(EnumChatFormatting.RED);
            sender.addChatMessage(chatComponentTranslation);
        }
        else if (icommand.canCommandSenderUseCommand(sender))
        {
            if (usernameIndex > -1)
            {
                List<Entity> list = PlayerSelector.<Entity>matchEntities(sender, astring[usernameIndex], Entity.class);
                String originalSelectorArgument = astring[usernameIndex];
                sender.setCommandStat(CommandResultStats.Type.AFFECTED_ENTITIES, list.size());

                for (Entity entity : list)
                {
                    astring[usernameIndex] = entity.getUniqueID().toString();

                    if (this.tryExecute(sender, astring, icommand, rawCommand))
                    {
                        ++successCount;
                    }
                }

                astring[usernameIndex] = originalSelectorArgument;
            }
            else
            {
                sender.setCommandStat(CommandResultStats.Type.AFFECTED_ENTITIES, 1);

                if (this.tryExecute(sender, astring, icommand, rawCommand))
                {
                    ++successCount;
                }
            }
        }
        else
        {
            ChatComponentTranslation chatcomponenttranslation1 = new ChatComponentTranslation("commands.generic.permission", new Object[0]);
            chatcomponenttranslation1.getChatStyle().setColor(EnumChatFormatting.RED);
            sender.addChatMessage(chatcomponenttranslation1);
        }

        sender.setCommandStat(CommandResultStats.Type.SUCCESS_COUNT, successCount);
        return successCount;
    }

    protected boolean tryExecute(ICommandSender sender, String[] args, ICommand command, String input)
    {
        try
        {
            command.processCommand(sender, args);
            return true;
        }
        catch (WrongUsageException wrongUsageException)
        {
            ChatComponentTranslation chatcomponenttranslation2 = new ChatComponentTranslation("commands.generic.usage", new Object[] {new ChatComponentTranslation(wrongUsageException.getMessage(), wrongUsageException.getErrorObjects())});
            chatcomponenttranslation2.getChatStyle().setColor(EnumChatFormatting.RED);
            sender.addChatMessage(chatcomponenttranslation2);
        }
        catch (CommandException commandException)
        {
            ChatComponentTranslation chatcomponenttranslation1 = new ChatComponentTranslation(commandException.getMessage(), commandException.getErrorObjects());
            chatcomponenttranslation1.getChatStyle().setColor(EnumChatFormatting.RED);
            sender.addChatMessage(chatcomponenttranslation1);
        }
        catch (Throwable caughtThrowable)
        {
            ChatComponentTranslation chatcomponenttranslation = new ChatComponentTranslation("commands.generic.exception", new Object[0]);
            chatcomponenttranslation.getChatStyle().setColor(EnumChatFormatting.RED);
            sender.addChatMessage(chatcomponenttranslation);
            logger.warn("Couldn\'t process command: \'" + input + "\'");
        }

        return false;
    }

    public ICommand registerCommand(ICommand command)
    {
        this.commandMap.put(command.getCommandName(), command);
        this.commandSet.add(command);

        for (String alias : command.getCommandAliases())
        {
            ICommand icommand = (ICommand)this.commandMap.get(alias);

            if (icommand == null || !icommand.getCommandName().equals(alias))
            {
                this.commandMap.put(alias, command);
            }
        }

        return command;
    }

    private static String[] dropFirstString(String[] input)
    {
        String[] astring = new String[input.length - 1];
        System.arraycopy(input, 1, astring, 0, input.length - 1);
        return astring;
    }

    public List<String> getTabCompletionOptions(ICommandSender sender, String input, BlockPos pos)
    {
        String[] astring = input.split(" ", -1);
        String commandName = astring[0];

        if (astring.length == 1)
        {
            List<String> list = Lists.<String>newArrayList();

            for (Entry<String, ICommand> entry : this.commandMap.entrySet())
            {
                if (CommandBase.doesStringStartWith(commandName, (String)entry.getKey()) && ((ICommand)entry.getValue()).canCommandSenderUseCommand(sender))
                {
                    list.add(entry.getKey());
                }
            }

            return list;
        }
        else
        {
            if (astring.length > 1)
            {
                ICommand icommand = (ICommand)this.commandMap.get(commandName);

                if (icommand != null && icommand.canCommandSenderUseCommand(sender))
                {
                    return icommand.addTabCompletionOptions(sender, dropFirstString(astring), pos);
                }
            }

            return null;
        }
    }

    public List<ICommand> getPossibleCommands(ICommandSender sender)
    {
        List<ICommand> list = Lists.<ICommand>newArrayList();

        for (ICommand icommand : this.commandSet)
        {
            if (icommand.canCommandSenderUseCommand(sender))
            {
                list.add(icommand);
            }
        }

        return list;
    }

    public Map<String, ICommand> getCommands()
    {
        return this.commandMap;
    }

    private int getUsernameIndex(ICommand command, String[] args)
    {
        if (command == null)
        {
            return -1;
        }
        else
        {
            for (int argIndex = 0; argIndex < args.length; ++argIndex)
            {
                if (command.isUsernameIndex(args, argIndex) && PlayerSelector.matchesMultiplePlayers(args[argIndex]))
                {
                    return argIndex;
                }
            }

            return -1;
        }
    }
}
