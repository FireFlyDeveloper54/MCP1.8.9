package net.minecraft.command;

import java.util.List;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;

public class CommandEffect extends CommandBase
{
    public String getCommandName()
    {
        return "effect";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    public String getCommandUsage(ICommandSender sender)
    {
        return "commands.effect.usage";
    }

    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length < 2)
        {
            throw new WrongUsageException("commands.effect.usage", new Object[0]);
        }
        else
        {
            EntityLivingBase entitylivingbase = (EntityLivingBase)getEntity(sender, args[0], EntityLivingBase.class);

            if (args[1].equals("clear"))
            {
                if (entitylivingbase.getActivePotionEffects().isEmpty())
                {
                    throw new CommandException("commands.effect.failure.notActive.all", new Object[] {entitylivingbase.getName()});
                }
                else
                {
                    entitylivingbase.clearActivePotions();
                    notifyOperators(sender, this, "commands.effect.success.removed.all", new Object[] {entitylivingbase.getName()});
                }
            }
            else
            {
                int effectId;

                try
                {
                    effectId = parseInt(args[1], 1);
                }
                catch (NumberInvalidException numberinvalidexception)
                {
                    Potion potion = Potion.getPotionFromResourceLocation(args[1]);

                    if (potion == null)
                    {
                        throw numberinvalidexception;
                    }

                    effectId = potion.id;
                }

                int durationTicks = 600;
                int durationSeconds = 30;
                int amplifier = 0;

                if (effectId >= 0 && effectId < Potion.potionTypes.length && Potion.potionTypes[effectId] != null)
                {
                    Potion potion1 = Potion.potionTypes[effectId];

                    if (args.length >= 3)
                    {
                        durationSeconds = parseInt(args[2], 0, 1000000);

                        if (potion1.isInstant())
                        {
                            durationTicks = durationSeconds;
                        }
                        else
                        {
                            durationTicks = durationSeconds * 20;
                        }
                    }
                    else if (potion1.isInstant())
                    {
                        durationTicks = 1;
                    }

                    if (args.length >= 4)
                    {
                        amplifier = parseInt(args[3], 0, 255);
                    }

                    boolean showParticles = true;

                    if (args.length >= 5 && "true".equalsIgnoreCase(args[4]))
                    {
                        showParticles = false;
                    }

                    if (durationSeconds > 0)
                    {
                        PotionEffect potioneffect = new PotionEffect(effectId, durationTicks, amplifier, false, showParticles);
                        entitylivingbase.addPotionEffect(potioneffect);
                        notifyOperators(sender, this, "commands.effect.success", new Object[] {new ChatComponentTranslation(potioneffect.getEffectName(), new Object[0]), Integer.valueOf(effectId), Integer.valueOf(amplifier), entitylivingbase.getName(), Integer.valueOf(durationSeconds)});
                    }
                    else if (entitylivingbase.isPotionActive(effectId))
                    {
                        entitylivingbase.removePotionEffect(effectId);
                        notifyOperators(sender, this, "commands.effect.success.removed", new Object[] {new ChatComponentTranslation(potion1.getName(), new Object[0]), entitylivingbase.getName()});
                    }
                    else
                    {
                        throw new CommandException("commands.effect.failure.notActive", new Object[] {new ChatComponentTranslation(potion1.getName(), new Object[0]), entitylivingbase.getName()});
                    }
                }
                else
                {
                    throw new NumberInvalidException("commands.effect.notFound", new Object[] {Integer.valueOf(effectId)});
                }
            }
        }
    }

    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos)
    {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, this.getAllUsernames()) : (args.length == 2 ? getListOfStringsMatchingLastWord(args, Potion.getPotionLocations()) : (args.length == 5 ? getListOfStringsMatchingLastWord(args, new String[] {"true", "false"}): null));
    }

    protected String[] getAllUsernames()
    {
        return MinecraftServer.getServer().getAllUsernames();
    }

    public boolean isUsernameIndex(String[] args, int index)
    {
        return index == 0;
    }
}
