package net.minecraft.command;

import java.util.List;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.S19PacketEntityStatus;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.GameRules;

public class CommandGameRule extends CommandBase
{
    public String getCommandName()
    {
        return "gamerule";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    public String getCommandUsage(ICommandSender sender)
    {
        return "commands.gamerule.usage";
    }

    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {
        GameRules gamerules = this.getGameRules();
        String ruleName = args.length > 0 ? args[0] : "";
        String ruleValue = args.length > 1 ? buildString(args, 1) : "";

        switch (args.length)
        {
            case 0:
                sender.addChatMessage(new ChatComponentText(joinNiceString(gamerules.getRules())));
                break;

            case 1:
                if (!gamerules.hasRule(ruleName))
                {
                    throw new CommandException("commands.gamerule.norule", new Object[] {ruleName});
                }

                String currentRuleValue = gamerules.getString(ruleName);
                sender.addChatMessage((new ChatComponentText(ruleName)).appendText(" = ").appendText(currentRuleValue));
                sender.setCommandStat(CommandResultStats.Type.QUERY_RESULT, gamerules.getInt(ruleName));
                break;

            default:
                if (gamerules.areSameType(ruleName, GameRules.ValueType.BOOLEAN_VALUE) && !"true".equals(ruleValue) && !"false".equals(ruleValue))
                {
                    throw new CommandException("commands.generic.boolean.invalid", new Object[] {ruleValue});
                }

                gamerules.setOrCreateGameRule(ruleName, ruleValue);
                notifyGameRuleChange(gamerules, ruleName);
                notifyOperators(sender, this, "commands.gamerule.success", new Object[0]);
        }
    }

    public static void notifyGameRuleChange(GameRules rules, String ruleName)
    {
        if ("reducedDebugInfo".equals(ruleName))
        {
            byte byteValue = (byte)(rules.getBoolean(ruleName) ? 22 : 23);

            for (EntityPlayerMP entityPlayerMP : MinecraftServer.getServer().getConfigurationManager().getPlayerList())
            {
                entityPlayerMP.playerNetServerHandler.sendPacket(new S19PacketEntityStatus(entityPlayerMP, byteValue));
            }
        }
    }

    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos)
    {
        if (args.length == 1)
        {
            return getListOfStringsMatchingLastWord(args, this.getGameRules().getRules());
        }
        else
        {
            if (args.length == 2)
            {
                GameRules gameRules = this.getGameRules();

                if (gameRules.areSameType(args[0], GameRules.ValueType.BOOLEAN_VALUE))
                {
                    return getListOfStringsMatchingLastWord(args, new String[] {"true", "false"});
                }
            }

            return null;
        }
    }

    private GameRules getGameRules()
    {
        return MinecraftServer.getServer().worldServerForDimension(0).getGameRules();
    }
}
