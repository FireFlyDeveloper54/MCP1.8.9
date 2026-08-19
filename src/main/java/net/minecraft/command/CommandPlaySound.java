package net.minecraft.command;

import java.util.List;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.S29PacketSoundEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

public class CommandPlaySound extends CommandBase
{
    public String getCommandName()
    {
        return "playsound";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    public String getCommandUsage(ICommandSender sender)
    {
        return "commands.playsound.usage";
    }

    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length < 2)
        {
            throw new WrongUsageException(this.getCommandUsage(sender), new Object[0]);
        }
        else
        {
            int argIndex = 0;
            String soundName = args[argIndex++];
            EntityPlayerMP targetPlayer = getPlayer(sender, args[argIndex++]);
            Vec3 senderPosition = sender.getPositionVector();
            double soundX = senderPosition.xCoord;

            if (args.length > argIndex)
            {
                soundX = parseDouble(soundX, args[argIndex++], true);
            }

            double soundY = senderPosition.yCoord;

            if (args.length > argIndex)
            {
                soundY = parseDouble(soundY, args[argIndex++], 0, 0, false);
            }

            double soundZ = senderPosition.zCoord;

            if (args.length > argIndex)
            {
                soundZ = parseDouble(soundZ, args[argIndex++], true);
            }

            double volume = 1.0D;

            if (args.length > argIndex)
            {
                volume = parseDouble(args[argIndex++], 0.0D, 3.4028234663852886E38D);
            }

            double pitch = 1.0D;

            if (args.length > argIndex)
            {
                pitch = parseDouble(args[argIndex++], 0.0D, 2.0D);
            }

            double minimumVolume = 0.0D;

            if (args.length > argIndex)
            {
                minimumVolume = parseDouble(args[argIndex], 0.0D, 1.0D);
            }

            double audibleRange = volume > 1.0D ? volume * 16.0D : 16.0D;
            double distanceToSound = targetPlayer.getDistance(soundX, soundY, soundZ);

            if (distanceToSound > audibleRange)
            {
                if (minimumVolume <= 0.0D)
                {
                    throw new CommandException("commands.playsound.playerTooFar", new Object[] {targetPlayer.getName()});
                }

                double deltaX = soundX - targetPlayer.posX;
                double deltaY = soundY - targetPlayer.posY;
                double deltaZ = soundZ - targetPlayer.posZ;
                double distanceVectorLength = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);

                if (distanceVectorLength > 0.0D)
                {
                    soundX = targetPlayer.posX + deltaX / distanceVectorLength * 2.0D;
                    soundY = targetPlayer.posY + deltaY / distanceVectorLength * 2.0D;
                    soundZ = targetPlayer.posZ + deltaZ / distanceVectorLength * 2.0D;
                }

                volume = minimumVolume;
            }

            targetPlayer.playerNetServerHandler.sendPacket(new S29PacketSoundEffect(soundName, soundX, soundY, soundZ, (float)volume, (float)pitch));
            notifyOperators(sender, this, "commands.playsound.success", new Object[] {soundName, targetPlayer.getName()});
        }
    }

    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos)
    {
        return args.length == 2 ? getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames()) : (args.length > 2 && args.length <= 5 ? getTabCompletionCoordinate(args, 2, pos) : null);
    }

    public boolean isUsernameIndex(String[] args, int index)
    {
        return index == 1;
    }
}
