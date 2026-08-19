package net.minecraft.command;

import java.util.List;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;

public class CommandEnchant extends CommandBase
{
    public String getCommandName()
    {
        return "enchant";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    public String getCommandUsage(ICommandSender sender)
    {
        return "commands.enchant.usage";
    }

    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length < 2)
        {
            throw new WrongUsageException("commands.enchant.usage", new Object[0]);
        }
        else
        {
            EntityPlayer targetPlayer = getPlayer(sender, args[0]);
            sender.setCommandStat(CommandResultStats.Type.AFFECTED_ITEMS, 0);
            int enchantmentId;

            try
            {
                enchantmentId = parseInt(args[1], 0);
            }
            catch (NumberInvalidException numberinvalidexception)
            {
                Enchantment enchantment = Enchantment.getEnchantmentByLocation(args[1]);

                if (enchantment == null)
                {
                    throw numberinvalidexception;
                }

                enchantmentId = enchantment.effectId;
            }

            int level = 1;
            ItemStack heldStack = targetPlayer.getCurrentEquippedItem();

            if (heldStack == null)
            {
                throw new CommandException("commands.enchant.noItem", new Object[0]);
            }
            else
            {
                Enchantment targetEnchantment = Enchantment.getEnchantmentById(enchantmentId);

                if (targetEnchantment == null)
                {
                    throw new NumberInvalidException("commands.enchant.notFound", new Object[] {Integer.valueOf(enchantmentId)});
                }
                else if (!targetEnchantment.canApply(heldStack))
                {
                    throw new CommandException("commands.enchant.cantEnchant", new Object[0]);
                }
                else
                {
                    if (args.length >= 3)
                    {
                        level = parseInt(args[2], targetEnchantment.getMinLevel(), targetEnchantment.getMaxLevel());
                    }

                    if (heldStack.hasTagCompound())
                    {
                        NBTTagList enchantmentTags = heldStack.getEnchantmentTagList();

                        if (enchantmentTags != null)
                        {
                            for (int tagIndex = 0; tagIndex < enchantmentTags.tagCount(); ++tagIndex)
                            {
                                int existingEnchantmentId = enchantmentTags.getCompoundTagAt(tagIndex).getShort("id");

                                if (Enchantment.getEnchantmentById(existingEnchantmentId) != null)
                                {
                                    Enchantment existingEnchantment = Enchantment.getEnchantmentById(existingEnchantmentId);

                                    if (!existingEnchantment.canApplyTogether(targetEnchantment))
                                    {
                                        throw new CommandException("commands.enchant.cantCombine", new Object[] {targetEnchantment.getTranslatedName(level), existingEnchantment.getTranslatedName(enchantmentTags.getCompoundTagAt(tagIndex).getShort("lvl"))});
                                    }
                                }
                            }
                        }
                    }

                    heldStack.addEnchantment(targetEnchantment, level);
                    notifyOperators(sender, this, "commands.enchant.success", new Object[0]);
                    sender.setCommandStat(CommandResultStats.Type.AFFECTED_ITEMS, 1);
                }
            }
        }
    }

    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos)
    {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, this.getListOfPlayers()) : (args.length == 2 ? getListOfStringsMatchingLastWord(args, Enchantment.getRegisteredResourceLocations()) : null);
    }

    protected String[] getListOfPlayers()
    {
        return MinecraftServer.getServer().getAllUsernames();
    }

    public boolean isUsernameIndex(String[] args, int index)
    {
        return index == 0;
    }
}
