package net.minecraft.command.server;

import java.util.List;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class CommandSummon extends CommandBase
{
    public String getCommandName()
    {
        return "summon";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    public String getCommandUsage(ICommandSender sender)
    {
        return "commands.summon.usage";
    }

    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length < 1)
        {
            throw new WrongUsageException("commands.summon.usage", new Object[0]);
        }
        else
        {
            String entityId = args[0];
            BlockPos spawnPos = sender.getPosition();
            Vec3 senderPosition = sender.getPositionVector();
            double spawnX = senderPosition.xCoord;
            double spawnY = senderPosition.yCoord;
            double spawnZ = senderPosition.zCoord;

            if (args.length >= 4)
            {
                spawnX = parseDouble(spawnX, args[1], true);
                spawnY = parseDouble(spawnY, args[2], false);
                spawnZ = parseDouble(spawnZ, args[3], true);
                spawnPos = new BlockPos(spawnX, spawnY, spawnZ);
            }

            World world = sender.getEntityWorld();

            if (!world.isBlockLoaded(spawnPos))
            {
                throw new CommandException("commands.summon.outOfWorld", new Object[0]);
            }
            else if ("LightningBolt".equals(entityId))
            {
                world.addWeatherEffect(new EntityLightningBolt(world, spawnX, spawnY, spawnZ));
                notifyOperators(sender, this, "commands.summon.success", new Object[0]);
            }
            else
            {
                NBTTagCompound entityTag = new NBTTagCompound();
                boolean hasCustomNbt = false;

                if (args.length >= 5)
                {
                    IChatComponent nbtText = getChatComponentFromNthArg(sender, args, 4);

                    try
                    {
                        entityTag = JsonToNBT.getTagFromJson(nbtText.getUnformattedText());
                        hasCustomNbt = true;
                    }
                    catch (NBTException nbtException)
                    {
                        throw new CommandException("commands.summon.tagError", new Object[] {nbtException.getMessage()});
                    }
                }

                entityTag.setString("id", entityId);
                Entity summonedEntity;

                try
                {
                    summonedEntity = EntityList.createEntityFromNBT(entityTag, world);
                }
                catch (RuntimeException caughtRuntimeException)
                {
                    throw new CommandException("commands.summon.failed", new Object[0]);
                }

                if (summonedEntity == null)
                {
                    throw new CommandException("commands.summon.failed", new Object[0]);
                }
                else
                {
                    summonedEntity.setLocationAndAngles(spawnX, spawnY, spawnZ, summonedEntity.rotationYaw, summonedEntity.rotationPitch);

                    if (!hasCustomNbt && summonedEntity instanceof EntityLiving)
                    {
                        ((EntityLiving)summonedEntity).onInitialSpawn(world.getDifficultyForLocation(new BlockPos(summonedEntity)), (IEntityLivingData)null);
                    }

                    world.spawnEntityInWorld(summonedEntity);
                    Entity rider = summonedEntity;

                    for (NBTTagCompound ridingTag = entityTag; rider != null && ridingTag.hasKey("Riding", 10); ridingTag = ridingTag.getCompoundTag("Riding"))
                    {
                        Entity mount = EntityList.createEntityFromNBT(ridingTag.getCompoundTag("Riding"), world);

                        if (mount != null)
                        {
                            mount.setLocationAndAngles(spawnX, spawnY, spawnZ, mount.rotationYaw, mount.rotationPitch);
                            world.spawnEntityInWorld(mount);
                            rider.mountEntity(mount);
                        }

                        rider = mount;
                    }

                    notifyOperators(sender, this, "commands.summon.success", new Object[0]);
                }
            }
        }
    }

    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos)
    {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, EntityList.getEntityNameList()) : (args.length > 1 && args.length <= 4 ? getTabCompletionCoordinate(args, 1, pos) : null);
    }
}
