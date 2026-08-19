package net.minecraft.command;

import com.google.common.base.Functions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ResourceLocation;

public abstract class CommandBase implements ICommand
{
    private static IAdminCommand theAdmin;

    public int getRequiredPermissionLevel()
    {
        return 4;
    }

    public List<String> getCommandAliases()
    {
        return Collections.<String>emptyList();
    }

    public boolean canCommandSenderUseCommand(ICommandSender sender)
    {
        return sender.canCommandSenderUseCommand(this.getRequiredPermissionLevel(), this.getCommandName());
    }

    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos)
    {
        return null;
    }

    public static int parseInt(String input) throws NumberInvalidException
    {
        try
        {
            return Integer.parseInt(input);
        }
        catch (NumberFormatException caughtNumberFormatException)
        {
            throw new NumberInvalidException("commands.generic.num.invalid", new Object[] {input});
        }
    }

    public static int parseInt(String input, int min) throws NumberInvalidException
    {
        return parseInt(input, min, Integer.MAX_VALUE);
    }

    public static int parseInt(String input, int min, int max) throws NumberInvalidException
    {
        int parsedValue = parseInt(input);

        if (parsedValue < min)
        {
            throw new NumberInvalidException("commands.generic.num.tooSmall", new Object[] {Integer.valueOf(parsedValue), Integer.valueOf(min)});
        }
        else if (parsedValue > max)
        {
            throw new NumberInvalidException("commands.generic.num.tooBig", new Object[] {Integer.valueOf(parsedValue), Integer.valueOf(max)});
        }
        else
        {
            return parsedValue;
        }
    }

    public static long parseLong(String input) throws NumberInvalidException
    {
        try
        {
            return Long.parseLong(input);
        }
        catch (NumberFormatException caughtNumberFormatException)
        {
            throw new NumberInvalidException("commands.generic.num.invalid", new Object[] {input});
        }
    }

    public static long parseLong(String input, long min, long max) throws NumberInvalidException
    {
        long parsedValue = parseLong(input);

        if (parsedValue < min)
        {
            throw new NumberInvalidException("commands.generic.num.tooSmall", new Object[] {Long.valueOf(parsedValue), Long.valueOf(min)});
        }
        else if (parsedValue > max)
        {
            throw new NumberInvalidException("commands.generic.num.tooBig", new Object[] {Long.valueOf(parsedValue), Long.valueOf(max)});
        }
        else
        {
            return parsedValue;
        }
    }

    public static BlockPos parseBlockPos(ICommandSender sender, String[] args, int startIndex, boolean centerBlock) throws NumberInvalidException
    {
        BlockPos senderPos = sender.getPosition();
        return new BlockPos(parseDouble((double)senderPos.getX(), args[startIndex], -30000000, 30000000, centerBlock), parseDouble((double)senderPos.getY(), args[startIndex + 1], 0, 256, false), parseDouble((double)senderPos.getZ(), args[startIndex + 2], -30000000, 30000000, centerBlock));
    }

    public static double parseDouble(String input) throws NumberInvalidException
    {
        try
        {
            double parsedValue = Double.parseDouble(input);

            if (!Doubles.isFinite(parsedValue))
            {
                throw new NumberInvalidException("commands.generic.num.invalid", new Object[] {input});
            }
            else
            {
                return parsedValue;
            }
        }
        catch (NumberFormatException caughtNumberFormatException)
        {
            throw new NumberInvalidException("commands.generic.num.invalid", new Object[] {input});
        }
    }

    public static double parseDouble(String input, double min) throws NumberInvalidException
    {
        return parseDouble(input, min, Double.MAX_VALUE);
    }

    public static double parseDouble(String input, double min, double max) throws NumberInvalidException
    {
        double parsedValue = parseDouble(input);

        if (parsedValue < min)
        {
            throw new NumberInvalidException("commands.generic.double.tooSmall", new Object[] {Double.valueOf(parsedValue), Double.valueOf(min)});
        }
        else if (parsedValue > max)
        {
            throw new NumberInvalidException("commands.generic.double.tooBig", new Object[] {Double.valueOf(parsedValue), Double.valueOf(max)});
        }
        else
        {
            return parsedValue;
        }
    }

    public static boolean parseBoolean(String input) throws CommandException
    {
        if (!input.equals("true") && !input.equals("1"))
        {
            if (!input.equals("false") && !input.equals("0"))
            {
                throw new CommandException("commands.generic.boolean.invalid", new Object[] {input});
            }
            else
            {
                return false;
            }
        }
        else
        {
            return true;
        }
    }

    public static EntityPlayerMP getCommandSenderAsPlayer(ICommandSender sender) throws PlayerNotFoundException
    {
        if (sender instanceof EntityPlayerMP)
        {
            return (EntityPlayerMP)sender;
        }
        else
        {
            throw new PlayerNotFoundException("You must specify which player you wish to perform this action on.", new Object[0]);
        }
    }

    public static EntityPlayerMP getPlayer(ICommandSender sender, String username) throws PlayerNotFoundException
    {
        EntityPlayerMP player = PlayerSelector.matchOnePlayer(sender, username);

        if (player == null)
        {
            try
            {
                player = MinecraftServer.getServer().getConfigurationManager().getPlayerByUUID(UUID.fromString(username));
            }
            catch (IllegalArgumentException caughtIllegalArgumentException)
            {
                ;
            }
        }

        if (player == null)
        {
            player = MinecraftServer.getServer().getConfigurationManager().getPlayerByUsername(username);
        }

        if (player == null)
        {
            throw new PlayerNotFoundException();
        }
        else
        {
            return player;
        }
    }

    public static Entity getEntity(ICommandSender sender, String query) throws EntityNotFoundException
    {
        return getEntity(sender, query, Entity.class);
    }

    public static <T extends Entity> T getEntity(ICommandSender commandSender, String query, Class <? extends T > entityClass) throws EntityNotFoundException
    {
        Entity matchedEntity = PlayerSelector.matchOneEntity(commandSender, query, entityClass);
        MinecraftServer server = MinecraftServer.getServer();

        if (matchedEntity == null)
        {
            matchedEntity = server.getConfigurationManager().getPlayerByUsername(query);
        }

        if (matchedEntity == null)
        {
            try
            {
                UUID entityUuid = UUID.fromString(query);
                matchedEntity = server.getEntityFromUuid(entityUuid);

                if (matchedEntity == null)
                {
                    matchedEntity = server.getConfigurationManager().getPlayerByUUID(entityUuid);
                }
            }
            catch (IllegalArgumentException caughtIllegalArgumentException)
            {
                throw new EntityNotFoundException("commands.generic.entity.invalidUuid", new Object[0]);
            }
        }

        if (matchedEntity != null && entityClass.isAssignableFrom(matchedEntity.getClass()))
        {
            return (T)matchedEntity;
        }
        else
        {
            throw new EntityNotFoundException();
        }
    }

    public static List<Entity> getEntityList(ICommandSender sender, String query) throws EntityNotFoundException
    {
        return (List<Entity>)(PlayerSelector.hasArguments(query) ? PlayerSelector.matchEntities(sender, query, Entity.class) : Lists.newArrayList(new Entity[] {getEntity(sender, query)}));
    }

    public static String getPlayerName(ICommandSender sender, String query) throws PlayerNotFoundException
    {
        try
        {
            return getPlayer(sender, query).getName();
        }
        catch (PlayerNotFoundException playernotfoundexception)
        {
            if (PlayerSelector.hasArguments(query))
            {
                throw playernotfoundexception;
            }
            else
            {
                return query;
            }
        }
    }

    public static String getEntityName(ICommandSender sender, String query) throws EntityNotFoundException
    {
        try
        {
            return getPlayer(sender, query).getName();
        }
        catch (PlayerNotFoundException caughtPlayerNotFoundException)
        {
            try
            {
                return getEntity(sender, query).getUniqueID().toString();
            }
            catch (EntityNotFoundException entitynotfoundexception)
            {
                if (PlayerSelector.hasArguments(query))
                {
                    throw entitynotfoundexception;
                }
                else
                {
                    return query;
                }
            }
        }
    }

    public static IChatComponent getChatComponentFromNthArg(ICommandSender sender, String[] args, int index) throws CommandException, PlayerNotFoundException
    {
        return getChatComponentFromNthArg(sender, args, index, false);
    }

    public static IChatComponent getChatComponentFromNthArg(ICommandSender sender, String[] args, int index, boolean parseSelectors) throws PlayerNotFoundException
    {
        IChatComponent chatComponent = new ChatComponentText("");

        for (int argIndex = index; argIndex < args.length; ++argIndex)
        {
            if (argIndex > index)
            {
                chatComponent.appendText(" ");
            }

            IChatComponent argumentComponent = new ChatComponentText(args[argIndex]);

            if (parseSelectors)
            {
                IChatComponent selectorComponent = PlayerSelector.matchEntitiesToChatComponent(sender, args[argIndex]);

                if (selectorComponent == null)
                {
                    if (PlayerSelector.hasArguments(args[argIndex]))
                    {
                        throw new PlayerNotFoundException();
                    }
                }
                else
                {
                    argumentComponent = selectorComponent;
                }
            }

            chatComponent.appendSibling(argumentComponent);
        }

        return chatComponent;
    }

    public static String buildString(String[] args, int startPos)
    {
        StringBuilder stringBuilder = new StringBuilder();

        for (int argIndex = startPos; argIndex < args.length; ++argIndex)
        {
            if (argIndex > startPos)
            {
                stringBuilder.append(" ");
            }

            String argument = args[argIndex];
            stringBuilder.append(argument);
        }

        return stringBuilder.toString();
    }

    public static CommandBase.CoordinateArg parseCoordinate(double base, String input, boolean centerBlock) throws NumberInvalidException
    {
        return parseCoordinate(base, input, -30000000, 30000000, centerBlock);
    }

    public static CommandBase.CoordinateArg parseCoordinate(double base, String input, int min, int max, boolean centerBlock) throws NumberInvalidException
    {
        boolean isRelative = input.startsWith("~");

        if (isRelative && Double.isNaN(base))
        {
            throw new NumberInvalidException("commands.generic.num.invalid", new Object[] {Double.valueOf(base)});
        }
        else
        {
            double coordinateOffset = 0.0D;

            if (!isRelative || input.length() > 1)
            {
                boolean hasDecimalPoint = input.contains(".");

                if (isRelative)
                {
                    input = input.substring(1);
                }

                coordinateOffset += parseDouble(input);

                if (!hasDecimalPoint && !isRelative && centerBlock)
                {
                    coordinateOffset += 0.5D;
                }
            }

            if (min != 0 || max != 0)
            {
                if (coordinateOffset < (double)min)
                {
                    throw new NumberInvalidException("commands.generic.double.tooSmall", new Object[] {Double.valueOf(coordinateOffset), Integer.valueOf(min)});
                }

                if (coordinateOffset > (double)max)
                {
                    throw new NumberInvalidException("commands.generic.double.tooBig", new Object[] {Double.valueOf(coordinateOffset), Integer.valueOf(max)});
                }
            }

            return new CommandBase.CoordinateArg(coordinateOffset + (isRelative ? base : 0.0D), coordinateOffset, isRelative);
        }
    }

    public static double parseDouble(double base, String input, boolean centerBlock) throws NumberInvalidException
    {
        return parseDouble(base, input, -30000000, 30000000, centerBlock);
    }

    public static double parseDouble(double base, String input, int min, int max, boolean centerBlock) throws NumberInvalidException
    {
        boolean isRelative = input.startsWith("~");

        if (isRelative && Double.isNaN(base))
        {
            throw new NumberInvalidException("commands.generic.num.invalid", new Object[] {Double.valueOf(base)});
        }
        else
        {
            double coordinateValue = isRelative ? base : 0.0D;

            if (!isRelative || input.length() > 1)
            {
                boolean hasDecimalPoint = input.contains(".");

                if (isRelative)
                {
                    input = input.substring(1);
                }

                coordinateValue += parseDouble(input);

                if (!hasDecimalPoint && !isRelative && centerBlock)
                {
                    coordinateValue += 0.5D;
                }
            }

            if (min != 0 || max != 0)
            {
                if (coordinateValue < (double)min)
                {
                    throw new NumberInvalidException("commands.generic.double.tooSmall", new Object[] {Double.valueOf(coordinateValue), Integer.valueOf(min)});
                }

                if (coordinateValue > (double)max)
                {
                    throw new NumberInvalidException("commands.generic.double.tooBig", new Object[] {Double.valueOf(coordinateValue), Integer.valueOf(max)});
                }
            }

            return coordinateValue;
        }
    }

    public static Item getItemByText(ICommandSender sender, String id) throws NumberInvalidException
    {
        ResourceLocation resourcelocation = new ResourceLocation(id);
        Item item = (Item)Item.itemRegistry.getObject(resourcelocation);

        if (item == null)
        {
            throw new NumberInvalidException("commands.give.item.notFound", new Object[] {resourcelocation});
        }
        else
        {
            return item;
        }
    }

    public static Block getBlockByText(ICommandSender sender, String id) throws NumberInvalidException
    {
        ResourceLocation resourcelocation = new ResourceLocation(id);

        if (!Block.blockRegistry.containsKey(resourcelocation))
        {
            throw new NumberInvalidException("commands.give.block.notFound", new Object[] {resourcelocation});
        }
        else
        {
            Block block = (Block)Block.blockRegistry.getObject(resourcelocation);

            if (block == null)
            {
                throw new NumberInvalidException("commands.give.block.notFound", new Object[] {resourcelocation});
            }
            else
            {
                return block;
            }
        }
    }

    public static String joinNiceString(Object[] elements)
    {
        StringBuilder stringBuilder = new StringBuilder();

        for (int elementIndex = 0; elementIndex < elements.length; ++elementIndex)
        {
            String elementText = elements[elementIndex].toString();

            if (elementIndex > 0)
            {
                if (elementIndex == elements.length - 1)
                {
                    stringBuilder.append(" and ");
                }
                else
                {
                    stringBuilder.append(", ");
                }
            }

            stringBuilder.append(elementText);
        }

        return stringBuilder.toString();
    }

    public static IChatComponent join(List<IChatComponent> components)
    {
        IChatComponent joinedComponent = new ChatComponentText("");

        for (int componentIndex = 0; componentIndex < components.size(); ++componentIndex)
        {
            if (componentIndex > 0)
            {
                if (componentIndex == components.size() - 1)
                {
                    joinedComponent.appendText(" and ");
                }
                else if (componentIndex > 0)
                {
                    joinedComponent.appendText(", ");
                }
            }

            joinedComponent.appendSibling((IChatComponent)components.get(componentIndex));
        }

        return joinedComponent;
    }

    public static String joinNiceStringFromCollection(Collection<String> strings)
    {
        return joinNiceString(strings.toArray(new String[strings.size()]));
    }

    public static List<String> getTabCompletionCoordinate(String[] args, int index, BlockPos pos)
    {
        if (pos == null)
        {
            return null;
        }
        else
        {
            int currentArgIndex = args.length - 1;
            String suggestion;

            if (currentArgIndex == index)
            {
                suggestion = Integer.toString(pos.getX());
            }
            else if (currentArgIndex == index + 1)
            {
                suggestion = Integer.toString(pos.getY());
            }
            else
            {
                if (currentArgIndex != index + 2)
                {
                    return null;
                }

                suggestion = Integer.toString(pos.getZ());
            }

            return Lists.newArrayList(new String[] {suggestion});
        }
    }

    public static List<String> getTabCompletionCoordinateXZ(String[] args, int index, BlockPos pos)
    {
        if (pos == null)
        {
            return null;
        }
        else
        {
            int currentArgIndex = args.length - 1;
            String suggestion;

            if (currentArgIndex == index)
            {
                suggestion = Integer.toString(pos.getX());
            }
            else
            {
                if (currentArgIndex != index + 1)
                {
                    return null;
                }

                suggestion = Integer.toString(pos.getZ());
            }

            return Lists.newArrayList(new String[] {suggestion});
        }
    }

    public static boolean doesStringStartWith(String original, String region)
    {
        return region.regionMatches(true, 0, original, 0, original.length());
    }

    public static List<String> getListOfStringsMatchingLastWord(String[] args, String... possibilities)
    {
        return getListOfStringsMatchingLastWord(args, Arrays.asList(possibilities));
    }

    public static List<String> getListOfStringsMatchingLastWord(String[] args, Collection<?> possibilities)
    {
        String lastWord = args[args.length - 1];
        List<String> matches = Lists.<String>newArrayList();

        if (!possibilities.isEmpty())
        {
            for (String candidate : Iterables.transform(possibilities, Functions.toStringFunction()))
            {
                if (doesStringStartWith(lastWord, candidate))
                {
                    matches.add(candidate);
                }
            }

            if (matches.isEmpty())
            {
                for (Object possibility : possibilities)
                {
                    if (possibility instanceof ResourceLocation && doesStringStartWith(lastWord, ((ResourceLocation)possibility).getResourcePath()))
                    {
                        matches.add(String.valueOf(possibility));
                    }
                }
            }
        }

        return matches;
    }

    public boolean isUsernameIndex(String[] args, int index)
    {
        return false;
    }

    public static void notifyOperators(ICommandSender sender, ICommand command, String msgFormat, Object... msgParams)
    {
        notifyOperators(sender, command, 0, msgFormat, msgParams);
    }

    public static void notifyOperators(ICommandSender sender, ICommand command, int flags, String msgFormat, Object... msgParams)
    {
        if (theAdmin != null)
        {
            theAdmin.notifyOperators(sender, command, flags, msgFormat, msgParams);
        }
    }

    public static void setAdminCommander(IAdminCommand command)
    {
        theAdmin = command;
    }

    public int compareTo(ICommand command)
    {
        return this.getCommandName().compareTo(command.getCommandName());
    }

    public static class CoordinateArg
    {
        private final double result;
        private final double amount;
        private final boolean isRelative;

        protected CoordinateArg(double result, double amount, boolean isRelative)
        {
            this.result = result;
            this.amount = amount;
            this.isRelative = isRelative;
        }

        public double getResult()
        {
            return this.result;
        }

        public double getAmount()
        {
            return this.amount;
        }

        public boolean isRelative()
        {
            return this.isRelative;
        }
    }
}
