package net.minecraft.command;

import java.util.List;
import java.util.Locale;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.MathHelper;
import net.minecraft.world.border.WorldBorder;

public class CommandWorldBorder extends CommandBase
{
    public String getCommandName()
    {
        return "worldborder";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    public String getCommandUsage(ICommandSender sender)
    {
        return "commands.worldborder.usage";
    }

    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length < 1)
        {
            throw new WrongUsageException("commands.worldborder.usage", new Object[0]);
        }
        else
        {
            WorldBorder worldBorder = this.getWorldBorder();

            if (args[0].equals("set"))
            {
                if (args.length != 2 && args.length != 3)
                {
                    throw new WrongUsageException("commands.worldborder.set.usage", new Object[0]);
                }

                double oldDiameter = worldBorder.getTargetSize();
                double newDiameter = parseDouble(args[1], 1.0D, 6.0E7D);
                long transitionTimeMs = args.length > 2 ? parseLong(args[2], 0L, 9223372036854775L) * 1000L : 0L;

                if (transitionTimeMs > 0L)
                {
                    worldBorder.setTransition(oldDiameter, newDiameter, transitionTimeMs);

                    if (oldDiameter > newDiameter)
                    {
                        notifyOperators(sender, this, "commands.worldborder.setSlowly.shrink.success", new Object[] {String.format(Locale.ROOT, "%.1f", new Object[]{Double.valueOf(newDiameter)}), String.format(Locale.ROOT, "%.1f", new Object[]{Double.valueOf(oldDiameter)}), Long.toString(transitionTimeMs / 1000L)});
                    }
                    else
                    {
                        notifyOperators(sender, this, "commands.worldborder.setSlowly.grow.success", new Object[] {String.format(Locale.ROOT, "%.1f", new Object[]{Double.valueOf(newDiameter)}), String.format(Locale.ROOT, "%.1f", new Object[]{Double.valueOf(oldDiameter)}), Long.toString(transitionTimeMs / 1000L)});
                    }
                }
                else
                {
                    worldBorder.setTransition(newDiameter);
                    notifyOperators(sender, this, "commands.worldborder.set.success", new Object[] {String.format(Locale.ROOT, "%.1f", new Object[]{Double.valueOf(newDiameter)}), String.format(Locale.ROOT, "%.1f", new Object[]{Double.valueOf(oldDiameter)})});
                }
            }
            else if (args[0].equals("add"))
            {
                if (args.length != 2 && args.length != 3)
                {
                    throw new WrongUsageException("commands.worldborder.add.usage", new Object[0]);
                }

                double oldDiameter = worldBorder.getDiameter();
                double newDiameter = oldDiameter + parseDouble(args[1], -oldDiameter, 6.0E7D - oldDiameter);
                long transitionTimeMs = worldBorder.getTimeUntilTarget() + (args.length > 2 ? parseLong(args[2], 0L, 9223372036854775L) * 1000L : 0L);

                if (transitionTimeMs > 0L)
                {
                    worldBorder.setTransition(oldDiameter, newDiameter, transitionTimeMs);

                    if (oldDiameter > newDiameter)
                    {
                        notifyOperators(sender, this, "commands.worldborder.setSlowly.shrink.success", new Object[] {String.format(Locale.ROOT, "%.1f", new Object[]{Double.valueOf(newDiameter)}), String.format(Locale.ROOT, "%.1f", new Object[]{Double.valueOf(oldDiameter)}), Long.toString(transitionTimeMs / 1000L)});
                    }
                    else
                    {
                        notifyOperators(sender, this, "commands.worldborder.setSlowly.grow.success", new Object[] {String.format(Locale.ROOT, "%.1f", new Object[]{Double.valueOf(newDiameter)}), String.format(Locale.ROOT, "%.1f", new Object[]{Double.valueOf(oldDiameter)}), Long.toString(transitionTimeMs / 1000L)});
                    }
                }
                else
                {
                    worldBorder.setTransition(newDiameter);
                    notifyOperators(sender, this, "commands.worldborder.set.success", new Object[] {String.format(Locale.ROOT, "%.1f", new Object[]{Double.valueOf(newDiameter)}), String.format(Locale.ROOT, "%.1f", new Object[]{Double.valueOf(oldDiameter)})});
                }
            }
            else if (args[0].equals("center"))
            {
                if (args.length != 3)
                {
                    throw new WrongUsageException("commands.worldborder.center.usage", new Object[0]);
                }

                BlockPos senderPos = sender.getPosition();
                double centerX = parseDouble((double)senderPos.getX() + 0.5D, args[1], true);
                double centerZ = parseDouble((double)senderPos.getZ() + 0.5D, args[2], true);
                worldBorder.setCenter(centerX, centerZ);
                notifyOperators(sender, this, "commands.worldborder.center.success", new Object[] {Double.valueOf(centerX), Double.valueOf(centerZ)});
            }
            else if (args[0].equals("damage"))
            {
                if (args.length < 2)
                {
                    throw new WrongUsageException("commands.worldborder.damage.usage", new Object[0]);
                }

                if (args[1].equals("buffer"))
                {
                    if (args.length != 3)
                    {
                        throw new WrongUsageException("commands.worldborder.damage.buffer.usage", new Object[0]);
                    }

                    double newDamageBuffer = parseDouble(args[2], 0.0D);
                    double oldDamageBuffer = worldBorder.getDamageBuffer();
                    worldBorder.setDamageBuffer(newDamageBuffer);
                    notifyOperators(sender, this, "commands.worldborder.damage.buffer.success", new Object[] {String.format(Locale.ROOT, "%.1f", new Object[]{Double.valueOf(newDamageBuffer)}), String.format(Locale.ROOT, "%.1f", new Object[]{Double.valueOf(oldDamageBuffer)})});
                }
                else if (args[1].equals("amount"))
                {
                    if (args.length != 3)
                    {
                        throw new WrongUsageException("commands.worldborder.damage.amount.usage", new Object[0]);
                    }

                    double newDamageAmount = parseDouble(args[2], 0.0D);
                    double oldDamageAmount = worldBorder.getDamageAmount();
                    worldBorder.setDamageAmount(newDamageAmount);
                    notifyOperators(sender, this, "commands.worldborder.damage.amount.success", new Object[] {String.format(Locale.ROOT, "%.2f", new Object[]{Double.valueOf(newDamageAmount)}), String.format(Locale.ROOT, "%.2f", new Object[]{Double.valueOf(oldDamageAmount)})});
                }
            }
            else if (args[0].equals("warning"))
            {
                if (args.length < 2)
                {
                    throw new WrongUsageException("commands.worldborder.warning.usage", new Object[0]);
                }

                int newWarningValue = parseInt(args[2], 0);

                if (args[1].equals("time"))
                {
                    if (args.length != 3)
                    {
                        throw new WrongUsageException("commands.worldborder.warning.time.usage", new Object[0]);
                    }

                    int oldWarningTime = worldBorder.getWarningTime();
                    worldBorder.setWarningTime(newWarningValue);
                    notifyOperators(sender, this, "commands.worldborder.warning.time.success", new Object[] {Integer.valueOf(newWarningValue), Integer.valueOf(oldWarningTime)});
                }
                else if (args[1].equals("distance"))
                {
                    if (args.length != 3)
                    {
                        throw new WrongUsageException("commands.worldborder.warning.distance.usage", new Object[0]);
                    }

                    int oldWarningDistance = worldBorder.getWarningDistance();
                    worldBorder.setWarningDistance(newWarningValue);
                    notifyOperators(sender, this, "commands.worldborder.warning.distance.success", new Object[] {Integer.valueOf(newWarningValue), Integer.valueOf(oldWarningDistance)});
                }
            }
            else
            {
                if (!args[0].equals("get"))
                {
                    throw new WrongUsageException("commands.worldborder.usage", new Object[0]);
                }

                double diameter = worldBorder.getDiameter();
                sender.setCommandStat(CommandResultStats.Type.QUERY_RESULT, MathHelper.floor_double(diameter + 0.5D));
                sender.addChatMessage(new ChatComponentTranslation("commands.worldborder.get.success", new Object[] {String.format(Locale.ROOT, "%.0f", new Object[]{Double.valueOf(diameter)})}));
            }
        }
    }

    protected WorldBorder getWorldBorder()
    {
        return MinecraftServer.getServer().worldServers[0].getWorldBorder();
    }

    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos)
    {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, new String[] {"set", "center", "damage", "warning", "add", "get"}): (args.length == 2 && args[0].equals("damage") ? getListOfStringsMatchingLastWord(args, new String[] {"buffer", "amount"}): (args.length >= 2 && args.length <= 3 && args[0].equals("center") ? getTabCompletionCoordinateXZ(args, 1, pos) : (args.length == 2 && args[0].equals("warning") ? getListOfStringsMatchingLastWord(args, new String[] {"time", "distance"}): null)));
    }
}
