package net.minecraft.command.server;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;

public class CommandTeleport extends CommandBase
{
    public String getCommandName()
    {
        return "tp";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    public String getCommandUsage(ICommandSender sender)
    {
        return "commands.tp.usage";
    }

    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length < 1)
        {
            throw new WrongUsageException("commands.tp.usage", new Object[0]);
        }
        else
        {
            int coordinateStartIndex = 0;
            Entity entityToTeleport;

            if (args.length != 2 && args.length != 4 && args.length != 6)
            {
                entityToTeleport = getCommandSenderAsPlayer(sender);
            }
            else
            {
                entityToTeleport = getEntity(sender, args[0]);
                coordinateStartIndex = 1;
            }

            if (args.length != 1 && args.length != 2)
            {
                if (args.length < coordinateStartIndex + 3)
                {
                    throw new WrongUsageException("commands.tp.usage", new Object[0]);
                }
                else if (entityToTeleport.worldObj != null)
                {
                    int nextArgIndex = coordinateStartIndex + 1;
                    CommandBase.CoordinateArg xArg = parseCoordinate(entityToTeleport.posX, args[coordinateStartIndex], true);
                    CommandBase.CoordinateArg yArg = parseCoordinate(entityToTeleport.posY, args[nextArgIndex++], 0, 0, false);
                    CommandBase.CoordinateArg zArg = parseCoordinate(entityToTeleport.posZ, args[nextArgIndex++], true);
                    CommandBase.CoordinateArg yawArg = parseCoordinate((double)entityToTeleport.rotationYaw, args.length > nextArgIndex ? args[nextArgIndex++] : "~", false);
                    CommandBase.CoordinateArg pitchArg = parseCoordinate((double)entityToTeleport.rotationPitch, args.length > nextArgIndex ? args[nextArgIndex] : "~", false);

                    if (entityToTeleport instanceof EntityPlayerMP)
                    {
                        Set<S08PacketPlayerPosLook.EnumFlags> relativeFlags = EnumSet.<S08PacketPlayerPosLook.EnumFlags>noneOf(S08PacketPlayerPosLook.EnumFlags.class);

                        if (xArg.isRelative())
                        {
                            relativeFlags.add(S08PacketPlayerPosLook.EnumFlags.X);
                        }

                        if (yArg.isRelative())
                        {
                            relativeFlags.add(S08PacketPlayerPosLook.EnumFlags.Y);
                        }

                        if (zArg.isRelative())
                        {
                            relativeFlags.add(S08PacketPlayerPosLook.EnumFlags.Z);
                        }

                        if (pitchArg.isRelative())
                        {
                            relativeFlags.add(S08PacketPlayerPosLook.EnumFlags.X_ROT);
                        }

                        if (yawArg.isRelative())
                        {
                            relativeFlags.add(S08PacketPlayerPosLook.EnumFlags.Y_ROT);
                        }

                        float yaw = (float)yawArg.getAmount();

                        if (!yawArg.isRelative())
                        {
                            yaw = MathHelper.wrapAngleTo180_float(yaw);
                        }

                        float pitch = (float)pitchArg.getAmount();

                        if (!pitchArg.isRelative())
                        {
                            pitch = MathHelper.wrapAngleTo180_float(pitch);
                        }

                        if (pitch > 90.0F || pitch < -90.0F)
                        {
                            pitch = MathHelper.wrapAngleTo180_float(180.0F - pitch);
                            yaw = MathHelper.wrapAngleTo180_float(yaw + 180.0F);
                        }

                        entityToTeleport.mountEntity((Entity)null);
                        ((EntityPlayerMP)entityToTeleport).playerNetServerHandler.setPlayerLocation(xArg.getAmount(), yArg.getAmount(), zArg.getAmount(), yaw, pitch, relativeFlags);
                        entityToTeleport.setRotationYawHead(yaw);
                    }
                    else
                    {
                        float wrappedYaw = (float)MathHelper.wrapAngleTo180_double(yawArg.getResult());
                        float wrappedPitch = (float)MathHelper.wrapAngleTo180_double(pitchArg.getResult());

                        if (wrappedPitch > 90.0F || wrappedPitch < -90.0F)
                        {
                            wrappedPitch = MathHelper.wrapAngleTo180_float(180.0F - wrappedPitch);
                            wrappedYaw = MathHelper.wrapAngleTo180_float(wrappedYaw + 180.0F);
                        }

                        entityToTeleport.setLocationAndAngles(xArg.getResult(), yArg.getResult(), zArg.getResult(), wrappedYaw, wrappedPitch);
                        entityToTeleport.setRotationYawHead(wrappedYaw);
                    }

                    notifyOperators(sender, this, "commands.tp.success.coordinates", new Object[] {entityToTeleport.getName(), Double.valueOf(xArg.getResult()), Double.valueOf(yArg.getResult()), Double.valueOf(zArg.getResult())});
                }
            }
            else
            {
                Entity destinationEntity = getEntity(sender, args[args.length - 1]);

                if (destinationEntity.worldObj != entityToTeleport.worldObj)
                {
                    throw new CommandException("commands.tp.notSameDimension", new Object[0]);
                }
                else
                {
                    entityToTeleport.mountEntity((Entity)null);

                    if (entityToTeleport instanceof EntityPlayerMP)
                    {
                        ((EntityPlayerMP)entityToTeleport).playerNetServerHandler.setPlayerLocation(destinationEntity.posX, destinationEntity.posY, destinationEntity.posZ, destinationEntity.rotationYaw, destinationEntity.rotationPitch);
                    }
                    else
                    {
                        entityToTeleport.setLocationAndAngles(destinationEntity.posX, destinationEntity.posY, destinationEntity.posZ, destinationEntity.rotationYaw, destinationEntity.rotationPitch);
                    }

                    notifyOperators(sender, this, "commands.tp.success", new Object[] {entityToTeleport.getName(), destinationEntity.getName()});
                }
            }
        }
    }

    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos)
    {
        return args.length != 1 && args.length != 2 ? null : getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
    }

    public boolean isUsernameIndex(String[] args, int index)
    {
        return index == 0;
    }
}
