package net.minecraft.command.server;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.SyntaxErrorException;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.scoreboard.IScoreObjectiveCriteria;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;

public class CommandScoreboard extends CommandBase
{
    public String getCommandName()
    {
        return "scoreboard";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    public String getCommandUsage(ICommandSender sender)
    {
        return "commands.scoreboard.usage";
    }

    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {
        if (!this.handleWildcardTargets(sender, args))
        {
            if (args.length < 1)
            {
                throw new WrongUsageException("commands.scoreboard.usage", new Object[0]);
            }
            else
            {
                if (args[0].equalsIgnoreCase("objectives"))
                {
                    if (args.length == 1)
                    {
                        throw new WrongUsageException("commands.scoreboard.objectives.usage", new Object[0]);
                    }

                    if (args[1].equalsIgnoreCase("list"))
                    {
                        this.listObjectives(sender);
                    }
                    else if (args[1].equalsIgnoreCase("add"))
                    {
                        if (args.length < 4)
                        {
                            throw new WrongUsageException("commands.scoreboard.objectives.add.usage", new Object[0]);
                        }

                        this.addObjective(sender, args, 2);
                    }
                    else if (args[1].equalsIgnoreCase("remove"))
                    {
                        if (args.length != 3)
                        {
                            throw new WrongUsageException("commands.scoreboard.objectives.remove.usage", new Object[0]);
                        }

                        this.removeObjective(sender, args[2]);
                    }
                    else
                    {
                        if (!args[1].equalsIgnoreCase("setdisplay"))
                        {
                            throw new WrongUsageException("commands.scoreboard.objectives.usage", new Object[0]);
                        }

                        if (args.length != 3 && args.length != 4)
                        {
                            throw new WrongUsageException("commands.scoreboard.objectives.setdisplay.usage", new Object[0]);
                        }

                        this.setObjectiveDisplay(sender, args, 2);
                    }
                }
                else if (args[0].equalsIgnoreCase("players"))
                {
                    if (args.length == 1)
                    {
                        throw new WrongUsageException("commands.scoreboard.players.usage", new Object[0]);
                    }

                    if (args[1].equalsIgnoreCase("list"))
                    {
                        if (args.length > 3)
                        {
                            throw new WrongUsageException("commands.scoreboard.players.list.usage", new Object[0]);
                        }

                        this.listPlayers(sender, args, 2);
                    }
                    else if (args[1].equalsIgnoreCase("add"))
                    {
                        if (args.length < 5)
                        {
                            throw new WrongUsageException("commands.scoreboard.players.add.usage", new Object[0]);
                        }

                        this.setPlayer(sender, args, 2);
                    }
                    else if (args[1].equalsIgnoreCase("remove"))
                    {
                        if (args.length < 5)
                        {
                            throw new WrongUsageException("commands.scoreboard.players.remove.usage", new Object[0]);
                        }

                        this.setPlayer(sender, args, 2);
                    }
                    else if (args[1].equalsIgnoreCase("set"))
                    {
                        if (args.length < 5)
                        {
                            throw new WrongUsageException("commands.scoreboard.players.set.usage", new Object[0]);
                        }

                        this.setPlayer(sender, args, 2);
                    }
                    else if (args[1].equalsIgnoreCase("reset"))
                    {
                        if (args.length != 3 && args.length != 4)
                        {
                            throw new WrongUsageException("commands.scoreboard.players.reset.usage", new Object[0]);
                        }

                        this.resetPlayers(sender, args, 2);
                    }
                    else if (args[1].equalsIgnoreCase("enable"))
                    {
                        if (args.length != 4)
                        {
                            throw new WrongUsageException("commands.scoreboard.players.enable.usage", new Object[0]);
                        }

                        this.enableTrigger(sender, args, 2);
                    }
                    else if (args[1].equalsIgnoreCase("test"))
                    {
                        if (args.length != 5 && args.length != 6)
                        {
                            throw new WrongUsageException("commands.scoreboard.players.test.usage", new Object[0]);
                        }

                        this.testPlayerScore(sender, args, 2);
                    }
                    else
                    {
                        if (!args[1].equalsIgnoreCase("operation"))
                        {
                            throw new WrongUsageException("commands.scoreboard.players.usage", new Object[0]);
                        }

                        if (args.length != 7)
                        {
                            throw new WrongUsageException("commands.scoreboard.players.operation.usage", new Object[0]);
                        }

                        this.performPlayerOperation(sender, args, 2);
                    }
                }
                else
                {
                    if (!args[0].equalsIgnoreCase("teams"))
                    {
                        throw new WrongUsageException("commands.scoreboard.usage", new Object[0]);
                    }

                    if (args.length == 1)
                    {
                        throw new WrongUsageException("commands.scoreboard.teams.usage", new Object[0]);
                    }

                    if (args[1].equalsIgnoreCase("list"))
                    {
                        if (args.length > 3)
                        {
                            throw new WrongUsageException("commands.scoreboard.teams.list.usage", new Object[0]);
                        }

                        this.listTeams(sender, args, 2);
                    }
                    else if (args[1].equalsIgnoreCase("add"))
                    {
                        if (args.length < 3)
                        {
                            throw new WrongUsageException("commands.scoreboard.teams.add.usage", new Object[0]);
                        }

                        this.addTeam(sender, args, 2);
                    }
                    else if (args[1].equalsIgnoreCase("remove"))
                    {
                        if (args.length != 3)
                        {
                            throw new WrongUsageException("commands.scoreboard.teams.remove.usage", new Object[0]);
                        }

                        this.removeTeam(sender, args, 2);
                    }
                    else if (args[1].equalsIgnoreCase("empty"))
                    {
                        if (args.length != 3)
                        {
                            throw new WrongUsageException("commands.scoreboard.teams.empty.usage", new Object[0]);
                        }

                        this.emptyTeam(sender, args, 2);
                    }
                    else if (args[1].equalsIgnoreCase("join"))
                    {
                        if (args.length < 4 && (args.length != 3 || !(sender instanceof EntityPlayer)))
                        {
                            throw new WrongUsageException("commands.scoreboard.teams.join.usage", new Object[0]);
                        }

                        this.joinTeam(sender, args, 2);
                    }
                    else if (args[1].equalsIgnoreCase("leave"))
                    {
                        if (args.length < 3 && !(sender instanceof EntityPlayer))
                        {
                            throw new WrongUsageException("commands.scoreboard.teams.leave.usage", new Object[0]);
                        }

                        this.leaveTeam(sender, args, 2);
                    }
                    else
                    {
                        if (!args[1].equalsIgnoreCase("option"))
                        {
                            throw new WrongUsageException("commands.scoreboard.teams.usage", new Object[0]);
                        }

                        if (args.length != 4 && args.length != 5)
                        {
                            throw new WrongUsageException("commands.scoreboard.teams.option.usage", new Object[0]);
                        }

                        this.setTeamOption(sender, args, 2);
                    }
                }
            }
        }
    }

    private boolean handleWildcardTargets(ICommandSender sender, String[] args) throws CommandException
    {
        int wildcardIndex = -1;

        for (int argIndex = 0; argIndex < args.length; ++argIndex)
        {
            if (this.isUsernameIndex(args, argIndex) && "*".equals(args[argIndex]))
            {
                if (wildcardIndex >= 0)
                {
                    throw new CommandException("commands.scoreboard.noMultiWildcard", new Object[0]);
                }

                wildcardIndex = argIndex;
            }
        }

        if (wildcardIndex < 0)
        {
            return false;
        }
        else
        {
            List<String> objectiveNames = Lists.newArrayList(this.getScoreboard().getObjectiveNames());
            String originalArgument = args[wildcardIndex];
            List<String> successfulTargets = Lists.<String>newArrayList();

            for (String objectiveName : objectiveNames)
            {
                args[wildcardIndex] = objectiveName;

                try
                {
                    this.processCommand(sender, args);
                    successfulTargets.add(objectiveName);
                }
                catch (CommandException commandexception)
                {
                    ChatComponentTranslation chatcomponenttranslation = new ChatComponentTranslation(commandexception.getMessage(), commandexception.getErrorObjects());
                    chatcomponenttranslation.getChatStyle().setColor(EnumChatFormatting.RED);
                    sender.addChatMessage(chatcomponenttranslation);
                }
            }

            args[wildcardIndex] = originalArgument;
            sender.setCommandStat(CommandResultStats.Type.AFFECTED_ENTITIES, successfulTargets.size());

            if (successfulTargets.size() == 0)
            {
                throw new WrongUsageException("commands.scoreboard.allMatchesFailed", new Object[0]);
            }
            else
            {
                return true;
            }
        }
    }

    protected Scoreboard getScoreboard()
    {
        return MinecraftServer.getServer().worldServerForDimension(0).getScoreboard();
    }

    protected ScoreObjective getObjective(String name, boolean edit) throws CommandException
    {
        Scoreboard scoreboard = this.getScoreboard();
        ScoreObjective scoreobjective = scoreboard.getObjective(name);

        if (scoreobjective == null)
        {
            throw new CommandException("commands.scoreboard.objectiveNotFound", new Object[] {name});
        }
        else if (edit && scoreobjective.getCriteria().isReadOnly())
        {
            throw new CommandException("commands.scoreboard.objectiveReadOnly", new Object[] {name});
        }
        else
        {
            return scoreobjective;
        }
    }

    protected ScorePlayerTeam getTeam(String name) throws CommandException
    {
        Scoreboard scoreboard = this.getScoreboard();
        ScorePlayerTeam scoreplayerteam = scoreboard.getTeam(name);

        if (scoreplayerteam == null)
        {
            throw new CommandException("commands.scoreboard.teamNotFound", new Object[] {name});
        }
        else
        {
            return scoreplayerteam;
        }
    }

    protected void addObjective(ICommandSender sender, String[] args, int index) throws CommandException
    {
        String objectiveName = args[index++];
        String criteriaName = args[index++];
        Scoreboard scoreboard = this.getScoreboard();
        IScoreObjectiveCriteria criteria = (IScoreObjectiveCriteria)IScoreObjectiveCriteria.INSTANCES.get(criteriaName);

        if (criteria == null)
        {
            throw new WrongUsageException("commands.scoreboard.objectives.add.wrongType", new Object[] {criteriaName});
        }
        else if (scoreboard.getObjective(objectiveName) != null)
        {
            throw new CommandException("commands.scoreboard.objectives.add.alreadyExists", new Object[] {objectiveName});
        }
        else if (objectiveName.length() > 16)
        {
            throw new SyntaxErrorException("commands.scoreboard.objectives.add.tooLong", new Object[] {objectiveName, Integer.valueOf(16)});
        }
        else if (objectiveName.length() == 0)
        {
            throw new WrongUsageException("commands.scoreboard.objectives.add.usage", new Object[0]);
        }
        else
        {
            if (args.length > index)
            {
                String displayName = getChatComponentFromNthArg(sender, args, index).getUnformattedText();

                if (displayName.length() > 32)
                {
                    throw new SyntaxErrorException("commands.scoreboard.objectives.add.displayTooLong", new Object[] {displayName, Integer.valueOf(32)});
                }

                if (displayName.length() > 0)
                {
                    scoreboard.addScoreObjective(objectiveName, criteria).setDisplayName(displayName);
                }
                else
                {
                    scoreboard.addScoreObjective(objectiveName, criteria);
                }
            }
            else
            {
                scoreboard.addScoreObjective(objectiveName, criteria);
            }

            notifyOperators(sender, this, "commands.scoreboard.objectives.add.success", new Object[] {objectiveName});
        }
    }

    protected void addTeam(ICommandSender sender, String[] args, int index) throws CommandException
    {
        String teamName = args[index++];
        Scoreboard scoreboard = this.getScoreboard();

        if (scoreboard.getTeam(teamName) != null)
        {
            throw new CommandException("commands.scoreboard.teams.add.alreadyExists", new Object[] {teamName});
        }
        else if (teamName.length() > 16)
        {
            throw new SyntaxErrorException("commands.scoreboard.teams.add.tooLong", new Object[] {teamName, Integer.valueOf(16)});
        }
        else if (teamName.length() == 0)
        {
            throw new WrongUsageException("commands.scoreboard.teams.add.usage", new Object[0]);
        }
        else
        {
            if (args.length > index)
            {
                String displayName = getChatComponentFromNthArg(sender, args, index).getUnformattedText();

                if (displayName.length() > 32)
                {
                    throw new SyntaxErrorException("commands.scoreboard.teams.add.displayTooLong", new Object[] {displayName, Integer.valueOf(32)});
                }

                if (displayName.length() > 0)
                {
                    scoreboard.createTeam(teamName).setTeamName(displayName);
                }
                else
                {
                    scoreboard.createTeam(teamName);
                }
            }
            else
            {
                scoreboard.createTeam(teamName);
            }

            notifyOperators(sender, this, "commands.scoreboard.teams.add.success", new Object[] {teamName});
        }
    }

    protected void setTeamOption(ICommandSender sender, String[] args, int index) throws CommandException
    {
        ScorePlayerTeam team = this.getTeam(args[index++]);

        if (team != null)
        {
            String optionName = args[index++].toLowerCase(Locale.ROOT);

            if (!optionName.equalsIgnoreCase("color") && !optionName.equalsIgnoreCase("friendlyfire") && !optionName.equalsIgnoreCase("seeFriendlyInvisibles") && !optionName.equalsIgnoreCase("nametagVisibility") && !optionName.equalsIgnoreCase("deathMessageVisibility"))
            {
                throw new WrongUsageException("commands.scoreboard.teams.option.usage", new Object[0]);
            }
            else if (args.length == 4)
            {
                if (optionName.equalsIgnoreCase("color"))
                {
                    throw new WrongUsageException("commands.scoreboard.teams.option.noValue", new Object[] {optionName, joinNiceStringFromCollection(EnumChatFormatting.getValidValues(true, false))});
                }
                else if (!optionName.equalsIgnoreCase("friendlyfire") && !optionName.equalsIgnoreCase("seeFriendlyInvisibles"))
                {
                    if (!optionName.equalsIgnoreCase("nametagVisibility") && !optionName.equalsIgnoreCase("deathMessageVisibility"))
                    {
                        throw new WrongUsageException("commands.scoreboard.teams.option.usage", new Object[0]);
                    }
                    else
                    {
                        throw new WrongUsageException("commands.scoreboard.teams.option.noValue", new Object[] {optionName, joinNiceString(Team.EnumVisible.getNames())});
                    }
                }
                else
                {
                    throw new WrongUsageException("commands.scoreboard.teams.option.noValue", new Object[] {optionName, joinNiceStringFromCollection(Arrays.asList(new String[]{"true", "false"}))});
                }
            }
            else
            {
                String optionValue = args[index];

                if (optionName.equalsIgnoreCase("color"))
                {
                    EnumChatFormatting chatFormatting = EnumChatFormatting.getValueByName(optionValue);

                    if (chatFormatting == null || chatFormatting.isFancyStyling())
                    {
                        throw new WrongUsageException("commands.scoreboard.teams.option.noValue", new Object[] {optionName, joinNiceStringFromCollection(EnumChatFormatting.getValidValues(true, false))});
                    }

                    team.setChatFormat(chatFormatting);
                    team.setNamePrefix(chatFormatting.toString());
                    team.setNameSuffix(EnumChatFormatting.RESET.toString());
                }
                else if (optionName.equalsIgnoreCase("friendlyfire"))
                {
                    if (!optionValue.equalsIgnoreCase("true") && !optionValue.equalsIgnoreCase("false"))
                    {
                        throw new WrongUsageException("commands.scoreboard.teams.option.noValue", new Object[] {optionName, joinNiceStringFromCollection(Arrays.asList(new String[]{"true", "false"}))});
                    }

                    team.setAllowFriendlyFire(optionValue.equalsIgnoreCase("true"));
                }
                else if (optionName.equalsIgnoreCase("seeFriendlyInvisibles"))
                {
                    if (!optionValue.equalsIgnoreCase("true") && !optionValue.equalsIgnoreCase("false"))
                    {
                        throw new WrongUsageException("commands.scoreboard.teams.option.noValue", new Object[] {optionName, joinNiceStringFromCollection(Arrays.asList(new String[]{"true", "false"}))});
                    }

                    team.setSeeFriendlyInvisiblesEnabled(optionValue.equalsIgnoreCase("true"));
                }
                else if (optionName.equalsIgnoreCase("nametagVisibility"))
                {
                    Team.EnumVisible nameTagVisibility = Team.EnumVisible.getByName(optionValue);

                    if (nameTagVisibility == null)
                    {
                        throw new WrongUsageException("commands.scoreboard.teams.option.noValue", new Object[] {optionName, joinNiceString(Team.EnumVisible.getNames())});
                    }

                    team.setNameTagVisibility(nameTagVisibility);
                }
                else if (optionName.equalsIgnoreCase("deathMessageVisibility"))
                {
                    Team.EnumVisible deathMessageVisibility = Team.EnumVisible.getByName(optionValue);

                    if (deathMessageVisibility == null)
                    {
                        throw new WrongUsageException("commands.scoreboard.teams.option.noValue", new Object[] {optionName, joinNiceString(Team.EnumVisible.getNames())});
                    }

                    team.setDeathMessageVisibility(deathMessageVisibility);
                }

                notifyOperators(sender, this, "commands.scoreboard.teams.option.success", new Object[] {optionName, team.getRegisteredName(), optionValue});
            }
        }
    }

    protected void removeTeam(ICommandSender sender, String[] args, int startIndex) throws CommandException
    {
        Scoreboard scoreboard = this.getScoreboard();
        ScorePlayerTeam scoreplayerteam = this.getTeam(args[startIndex]);

        if (scoreplayerteam != null)
        {
            scoreboard.removeTeam(scoreplayerteam);
            notifyOperators(sender, this, "commands.scoreboard.teams.remove.success", new Object[] {scoreplayerteam.getRegisteredName()});
        }
    }

    protected void listTeams(ICommandSender sender, String[] args, int startIndex) throws CommandException
    {
        Scoreboard scoreboard = this.getScoreboard();

        if (args.length > startIndex)
        {
            ScorePlayerTeam scoreplayerteam = this.getTeam(args[startIndex]);

            if (scoreplayerteam == null)
            {
                return;
            }

            Collection<String> collection = scoreplayerteam.getMembershipCollection();
            sender.setCommandStat(CommandResultStats.Type.QUERY_RESULT, collection.size());

            if (collection.size() <= 0)
            {
                throw new CommandException("commands.scoreboard.teams.list.player.empty", new Object[] {scoreplayerteam.getRegisteredName()});
            }

            ChatComponentTranslation chatcomponenttranslation = new ChatComponentTranslation("commands.scoreboard.teams.list.player.count", new Object[] {Integer.valueOf(collection.size()), scoreplayerteam.getRegisteredName()});
            chatcomponenttranslation.getChatStyle().setColor(EnumChatFormatting.DARK_GREEN);
            sender.addChatMessage(chatcomponenttranslation);
            sender.addChatMessage(new ChatComponentText(joinNiceString(collection.toArray())));
        }
        else
        {
            Collection<ScorePlayerTeam> collection1 = scoreboard.getTeams();
            sender.setCommandStat(CommandResultStats.Type.QUERY_RESULT, collection1.size());

            if (collection1.size() <= 0)
            {
                throw new CommandException("commands.scoreboard.teams.list.empty", new Object[0]);
            }

            ChatComponentTranslation chatcomponenttranslation1 = new ChatComponentTranslation("commands.scoreboard.teams.list.count", new Object[] {Integer.valueOf(collection1.size())});
            chatcomponenttranslation1.getChatStyle().setColor(EnumChatFormatting.DARK_GREEN);
            sender.addChatMessage(chatcomponenttranslation1);

            for (ScorePlayerTeam scoreplayerteam1 : collection1)
            {
                sender.addChatMessage(new ChatComponentTranslation("commands.scoreboard.teams.list.entry", new Object[] {scoreplayerteam1.getRegisteredName(), scoreplayerteam1.getTeamName(), Integer.valueOf(scoreplayerteam1.getMembershipCollection().size())}));
            }
        }
    }

    protected void joinTeam(ICommandSender sender, String[] args, int startIndex) throws CommandException
    {
        Scoreboard scoreboard = this.getScoreboard();
        String teamName = args[startIndex++];
        Set<String> successfulPlayers = Sets.<String>newHashSet();
        Set<String> failedPlayers = Sets.<String>newHashSet();

        if (sender instanceof EntityPlayer && startIndex == args.length)
        {
            String senderName = getCommandSenderAsPlayer(sender).getName();

            if (scoreboard.addPlayerToTeam(senderName, teamName))
            {
                successfulPlayers.add(senderName);
            }
            else
            {
                failedPlayers.add(senderName);
            }
        }
        else
        {
            while (startIndex < args.length)
            {
                String targetArgument = args[startIndex++];

                if (targetArgument.startsWith("@"))
                {
                    for (Entity entity : getEntityList(sender, targetArgument))
                    {
                        String entityName = getEntityName(sender, entity.getUniqueID().toString());

                        if (scoreboard.addPlayerToTeam(entityName, teamName))
                        {
                            successfulPlayers.add(entityName);
                        }
                        else
                        {
                            failedPlayers.add(entityName);
                        }
                    }
                }
                else
                {
                    String playerName = getEntityName(sender, targetArgument);

                    if (scoreboard.addPlayerToTeam(playerName, teamName))
                    {
                        successfulPlayers.add(playerName);
                    }
                    else
                    {
                        failedPlayers.add(playerName);
                    }
                }
            }
        }

        if (!successfulPlayers.isEmpty())
        {
            sender.setCommandStat(CommandResultStats.Type.AFFECTED_ENTITIES, successfulPlayers.size());
            notifyOperators(sender, this, "commands.scoreboard.teams.join.success", new Object[] {Integer.valueOf(successfulPlayers.size()), teamName, joinNiceString(successfulPlayers.toArray(new String[successfulPlayers.size()]))});
        }

        if (!failedPlayers.isEmpty())
        {
            throw new CommandException("commands.scoreboard.teams.join.failure", new Object[] {Integer.valueOf(failedPlayers.size()), teamName, joinNiceString(failedPlayers.toArray(new String[failedPlayers.size()]))});
        }
    }

    protected void leaveTeam(ICommandSender sender, String[] args, int startIndex) throws CommandException
    {
        Scoreboard scoreboard = this.getScoreboard();
        Set<String> removedPlayers = Sets.<String>newHashSet();
        Set<String> failedPlayers = Sets.<String>newHashSet();

        if (sender instanceof EntityPlayer && startIndex == args.length)
        {
            String senderName = getCommandSenderAsPlayer(sender).getName();

            if (scoreboard.removePlayerFromTeams(senderName))
            {
                removedPlayers.add(senderName);
            }
            else
            {
                failedPlayers.add(senderName);
            }
        }
        else
        {
            while (startIndex < args.length)
            {
                String targetArgument = args[startIndex++];

                if (targetArgument.startsWith("@"))
                {
                    for (Entity entity : getEntityList(sender, targetArgument))
                    {
                        String entityName = getEntityName(sender, entity.getUniqueID().toString());

                        if (scoreboard.removePlayerFromTeams(entityName))
                        {
                            removedPlayers.add(entityName);
                        }
                        else
                        {
                            failedPlayers.add(entityName);
                        }
                    }
                }
                else
                {
                    String playerName = getEntityName(sender, targetArgument);

                    if (scoreboard.removePlayerFromTeams(playerName))
                    {
                        removedPlayers.add(playerName);
                    }
                    else
                    {
                        failedPlayers.add(playerName);
                    }
                }
            }
        }

        if (!removedPlayers.isEmpty())
        {
            sender.setCommandStat(CommandResultStats.Type.AFFECTED_ENTITIES, removedPlayers.size());
            notifyOperators(sender, this, "commands.scoreboard.teams.leave.success", new Object[] {Integer.valueOf(removedPlayers.size()), joinNiceString(removedPlayers.toArray(new String[removedPlayers.size()]))});
        }

        if (!failedPlayers.isEmpty())
        {
            throw new CommandException("commands.scoreboard.teams.leave.failure", new Object[] {Integer.valueOf(failedPlayers.size()), joinNiceString(failedPlayers.toArray(new String[failedPlayers.size()]))});
        }
    }

    protected void emptyTeam(ICommandSender sender, String[] args, int startIndex) throws CommandException
    {
        Scoreboard scoreboard = this.getScoreboard();
        ScorePlayerTeam scoreplayerteam = this.getTeam(args[startIndex]);

        if (scoreplayerteam != null)
        {
            Collection<String> members = Lists.newArrayList(scoreplayerteam.getMembershipCollection());
            sender.setCommandStat(CommandResultStats.Type.AFFECTED_ENTITIES, members.size());

            if (members.isEmpty())
            {
                throw new CommandException("commands.scoreboard.teams.empty.alreadyEmpty", new Object[] {scoreplayerteam.getRegisteredName()});
            }
            else
            {
                for (String memberName : members)
                {
                    scoreboard.removePlayerFromTeam(memberName, scoreplayerteam);
                }

                notifyOperators(sender, this, "commands.scoreboard.teams.empty.success", new Object[] {Integer.valueOf(members.size()), scoreplayerteam.getRegisteredName()});
            }
        }
    }

    protected void removeObjective(ICommandSender sender, String objectiveName) throws CommandException
    {
        Scoreboard scoreboard = this.getScoreboard();
        ScoreObjective scoreobjective = this.getObjective(objectiveName, false);
        scoreboard.removeObjective(scoreobjective);
        notifyOperators(sender, this, "commands.scoreboard.objectives.remove.success", new Object[] {objectiveName});
    }

    protected void listObjectives(ICommandSender sender) throws CommandException
    {
        Scoreboard scoreboard = this.getScoreboard();
        Collection<ScoreObjective> collection = scoreboard.getScoreObjectives();

        if (collection.size() <= 0)
        {
            throw new CommandException("commands.scoreboard.objectives.list.empty", new Object[0]);
        }
        else
        {
            ChatComponentTranslation chatcomponenttranslation = new ChatComponentTranslation("commands.scoreboard.objectives.list.count", new Object[] {Integer.valueOf(collection.size())});
            chatcomponenttranslation.getChatStyle().setColor(EnumChatFormatting.DARK_GREEN);
            sender.addChatMessage(chatcomponenttranslation);

            for (ScoreObjective scoreobjective : collection)
            {
                sender.addChatMessage(new ChatComponentTranslation("commands.scoreboard.objectives.list.entry", new Object[] {scoreobjective.getName(), scoreobjective.getDisplayName(), scoreobjective.getCriteria().getName()}));
            }
        }
    }

    protected void setObjectiveDisplay(ICommandSender sender, String[] args, int startIndex) throws CommandException
    {
        Scoreboard scoreboard = this.getScoreboard();
        String slotName = args[startIndex++];
        int displaySlot = Scoreboard.getObjectiveDisplaySlotNumber(slotName);
        ScoreObjective objective = null;

        if (args.length == 4)
        {
            objective = this.getObjective(args[startIndex], false);
        }

        if (displaySlot < 0)
        {
            throw new CommandException("commands.scoreboard.objectives.setdisplay.invalidSlot", new Object[] {slotName});
        }
        else
        {
            scoreboard.setObjectiveInDisplaySlot(displaySlot, objective);

            if (objective != null)
            {
                notifyOperators(sender, this, "commands.scoreboard.objectives.setdisplay.successSet", new Object[] {Scoreboard.getObjectiveDisplaySlot(displaySlot), objective.getName()});
            }
            else
            {
                notifyOperators(sender, this, "commands.scoreboard.objectives.setdisplay.successCleared", new Object[] {Scoreboard.getObjectiveDisplaySlot(displaySlot)});
            }
        }
    }

    protected void listPlayers(ICommandSender sender, String[] args, int startIndex) throws CommandException
    {
        Scoreboard scoreboard = this.getScoreboard();

        if (args.length > startIndex)
        {
            String playerName = getEntityName(sender, args[startIndex]);
            Map<ScoreObjective, Score> scoresByObjective = scoreboard.getObjectivesForEntity(playerName);
            sender.setCommandStat(CommandResultStats.Type.QUERY_RESULT, scoresByObjective.size());

            if (scoresByObjective.size() <= 0)
            {
                throw new CommandException("commands.scoreboard.players.list.player.empty", new Object[] {playerName});
            }

            ChatComponentTranslation chatcomponenttranslation = new ChatComponentTranslation("commands.scoreboard.players.list.player.count", new Object[] {Integer.valueOf(scoresByObjective.size()), playerName});
            chatcomponenttranslation.getChatStyle().setColor(EnumChatFormatting.DARK_GREEN);
            sender.addChatMessage(chatcomponenttranslation);

            for (Score score : scoresByObjective.values())
            {
                sender.addChatMessage(new ChatComponentTranslation("commands.scoreboard.players.list.player.entry", new Object[] {Integer.valueOf(score.getScorePoints()), score.getObjective().getDisplayName(), score.getObjective().getName()}));
            }
        }
        else
        {
            Collection<String> collection = scoreboard.getObjectiveNames();
            sender.setCommandStat(CommandResultStats.Type.QUERY_RESULT, collection.size());

            if (collection.size() <= 0)
            {
                throw new CommandException("commands.scoreboard.players.list.empty", new Object[0]);
            }

            ChatComponentTranslation chatcomponenttranslation1 = new ChatComponentTranslation("commands.scoreboard.players.list.count", new Object[] {Integer.valueOf(collection.size())});
            chatcomponenttranslation1.getChatStyle().setColor(EnumChatFormatting.DARK_GREEN);
            sender.addChatMessage(chatcomponenttranslation1);
            sender.addChatMessage(new ChatComponentText(joinNiceString(collection.toArray())));
        }
    }

    protected void setPlayer(ICommandSender sender, String[] args, int startIndex) throws CommandException
    {
        String actionName = args[startIndex - 1];
        int playerArgIndex = startIndex;
        String playerName = getEntityName(sender, args[startIndex++]);

        if (playerName.length() > 40)
        {
            throw new SyntaxErrorException("commands.scoreboard.players.name.tooLong", new Object[] {playerName, Integer.valueOf(40)});
        }
        else
        {
            ScoreObjective objective = this.getObjective(args[startIndex++], true);
            int scoreAmount = actionName.equalsIgnoreCase("set") ? parseInt(args[startIndex++]) : parseInt(args[startIndex++], 0);

            if (args.length > startIndex)
            {
                Entity entity = getEntity(sender, args[playerArgIndex]);

                try
                {
                    NBTTagCompound expectedData = JsonToNBT.getTagFromJson(buildString(args, startIndex));
                    NBTTagCompound actualData = new NBTTagCompound();
                    entity.writeToNBT(actualData);

                    if (!NBTUtil.compareTags(expectedData, actualData, true))
                    {
                        throw new CommandException("commands.scoreboard.players.set.tagMismatch", new Object[] {playerName});
                    }
                }
                catch (NBTException nbtexception)
                {
                    throw new CommandException("commands.scoreboard.players.set.tagError", new Object[] {nbtexception.getMessage()});
                }
            }

            Scoreboard scoreboard = this.getScoreboard();
            Score score = scoreboard.getValueFromObjective(playerName, objective);

            if (actionName.equalsIgnoreCase("set"))
            {
                score.setScorePoints(scoreAmount);
            }
            else if (actionName.equalsIgnoreCase("add"))
            {
                score.increseScore(scoreAmount);
            }
            else
            {
                score.decreaseScore(scoreAmount);
            }

            notifyOperators(sender, this, "commands.scoreboard.players.set.success", new Object[] {objective.getName(), playerName, Integer.valueOf(score.getScorePoints())});
        }
    }

    protected void resetPlayers(ICommandSender sender, String[] args, int startIndex) throws CommandException
    {
        Scoreboard scoreboard = this.getScoreboard();
        String playerName = getEntityName(sender, args[startIndex++]);

        if (args.length > startIndex)
        {
            ScoreObjective objective = this.getObjective(args[startIndex++], false);
            scoreboard.removeObjectiveFromEntity(playerName, objective);
            notifyOperators(sender, this, "commands.scoreboard.players.resetscore.success", new Object[] {objective.getName(), playerName});
        }
        else
        {
            scoreboard.removeObjectiveFromEntity(playerName, (ScoreObjective)null);
            notifyOperators(sender, this, "commands.scoreboard.players.reset.success", new Object[] {playerName});
        }
    }

    protected void enableTrigger(ICommandSender sender, String[] args, int startIndex) throws CommandException
    {
        Scoreboard scoreboard = this.getScoreboard();
        String playerName = getPlayerName(sender, args[startIndex++]);

        if (playerName.length() > 40)
        {
            throw new SyntaxErrorException("commands.scoreboard.players.name.tooLong", new Object[] {playerName, Integer.valueOf(40)});
        }
        else
        {
            ScoreObjective objective = this.getObjective(args[startIndex], false);

            if (objective.getCriteria() != IScoreObjectiveCriteria.TRIGGER)
            {
                throw new CommandException("commands.scoreboard.players.enable.noTrigger", new Object[] {objective.getName()});
            }
            else
            {
                Score score = scoreboard.getValueFromObjective(playerName, objective);
                score.setLocked(false);
                notifyOperators(sender, this, "commands.scoreboard.players.enable.success", new Object[] {objective.getName(), playerName});
            }
        }
    }

    protected void testPlayerScore(ICommandSender sender, String[] args, int startIndex) throws CommandException
    {
        Scoreboard scoreboard = this.getScoreboard();
        String playerName = getEntityName(sender, args[startIndex++]);

        if (playerName.length() > 40)
        {
            throw new SyntaxErrorException("commands.scoreboard.players.name.tooLong", new Object[] {playerName, Integer.valueOf(40)});
        }
        else
        {
            ScoreObjective objective = this.getObjective(args[startIndex++], false);

            if (!scoreboard.entityHasObjective(playerName, objective))
            {
                throw new CommandException("commands.scoreboard.players.test.notFound", new Object[] {objective.getName(), playerName});
            }
            else
            {
                int minScore = args[startIndex].equals("*") ? Integer.MIN_VALUE : parseInt(args[startIndex]);
                ++startIndex;
                int maxScore = startIndex < args.length && !args[startIndex].equals("*") ? parseInt(args[startIndex], minScore) : Integer.MAX_VALUE;
                Score score = scoreboard.getValueFromObjective(playerName, objective);

                if (score.getScorePoints() >= minScore && score.getScorePoints() <= maxScore)
                {
                    notifyOperators(sender, this, "commands.scoreboard.players.test.success", new Object[] {Integer.valueOf(score.getScorePoints()), Integer.valueOf(minScore), Integer.valueOf(maxScore)});
                }
                else
                {
                    throw new CommandException("commands.scoreboard.players.test.failed", new Object[] {Integer.valueOf(score.getScorePoints()), Integer.valueOf(minScore), Integer.valueOf(maxScore)});
                }
            }
        }
    }

    protected void performPlayerOperation(ICommandSender sender, String[] args, int startIndex) throws CommandException
    {
        Scoreboard scoreboard = this.getScoreboard();
        String targetPlayerName = getEntityName(sender, args[startIndex++]);
        ScoreObjective targetObjective = this.getObjective(args[startIndex++], true);
        String operation = args[startIndex++];
        String sourcePlayerName = getEntityName(sender, args[startIndex++]);
        ScoreObjective sourceObjective = this.getObjective(args[startIndex], false);

        if (targetPlayerName.length() > 40)
        {
            throw new SyntaxErrorException("commands.scoreboard.players.name.tooLong", new Object[] {targetPlayerName, Integer.valueOf(40)});
        }
        else if (sourcePlayerName.length() > 40)
        {
            throw new SyntaxErrorException("commands.scoreboard.players.name.tooLong", new Object[] {sourcePlayerName, Integer.valueOf(40)});
        }
        else
        {
            Score targetScore = scoreboard.getValueFromObjective(targetPlayerName, targetObjective);

            if (!scoreboard.entityHasObjective(sourcePlayerName, sourceObjective))
            {
                throw new CommandException("commands.scoreboard.players.operation.notFound", new Object[] {sourceObjective.getName(), sourcePlayerName});
            }
            else
            {
                Score sourceScore = scoreboard.getValueFromObjective(sourcePlayerName, sourceObjective);

                if (operation.equals("+="))
                {
                    targetScore.setScorePoints(targetScore.getScorePoints() + sourceScore.getScorePoints());
                }
                else if (operation.equals("-="))
                {
                    targetScore.setScorePoints(targetScore.getScorePoints() - sourceScore.getScorePoints());
                }
                else if (operation.equals("*="))
                {
                    targetScore.setScorePoints(targetScore.getScorePoints() * sourceScore.getScorePoints());
                }
                else if (operation.equals("/="))
                {
                    if (sourceScore.getScorePoints() != 0)
                    {
                        targetScore.setScorePoints(targetScore.getScorePoints() / sourceScore.getScorePoints());
                    }
                }
                else if (operation.equals("%="))
                {
                    if (sourceScore.getScorePoints() != 0)
                    {
                        targetScore.setScorePoints(targetScore.getScorePoints() % sourceScore.getScorePoints());
                    }
                }
                else if (operation.equals("="))
                {
                    targetScore.setScorePoints(sourceScore.getScorePoints());
                }
                else if (operation.equals("<"))
                {
                    targetScore.setScorePoints(Math.min(targetScore.getScorePoints(), sourceScore.getScorePoints()));
                }
                else if (operation.equals(">"))
                {
                    targetScore.setScorePoints(Math.max(targetScore.getScorePoints(), sourceScore.getScorePoints()));
                }
                else
                {
                    if (!operation.equals("><"))
                    {
                        throw new CommandException("commands.scoreboard.players.operation.invalidOperation", new Object[] {operation});
                    }

                    int oldTargetScore = targetScore.getScorePoints();
                    targetScore.setScorePoints(sourceScore.getScorePoints());
                    sourceScore.setScorePoints(oldTargetScore);
                }

                notifyOperators(sender, this, "commands.scoreboard.players.operation.success", new Object[0]);
            }
        }
    }

    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos)
    {
        if (args.length == 1)
        {
            return getListOfStringsMatchingLastWord(args, new String[] {"objectives", "players", "teams"});
        }
        else
        {
            if (args[0].equalsIgnoreCase("objectives"))
            {
                if (args.length == 2)
                {
                    return getListOfStringsMatchingLastWord(args, new String[] {"list", "add", "remove", "setdisplay"});
                }

                if (args[1].equalsIgnoreCase("add"))
                {
                    if (args.length == 4)
                    {
                        Set<String> criteriaNames = IScoreObjectiveCriteria.INSTANCES.keySet();
                        return getListOfStringsMatchingLastWord(args, criteriaNames);
                    }
                }
                else if (args[1].equalsIgnoreCase("remove"))
                {
                    if (args.length == 3)
                    {
                        return getListOfStringsMatchingLastWord(args, this.getObjectiveNames(false));
                    }
                }
                else if (args[1].equalsIgnoreCase("setdisplay"))
                {
                    if (args.length == 3)
                    {
                        return getListOfStringsMatchingLastWord(args, Scoreboard.getDisplaySlotStrings());
                    }

                    if (args.length == 4)
                    {
                        return getListOfStringsMatchingLastWord(args, this.getObjectiveNames(false));
                    }
                }
            }
            else if (args[0].equalsIgnoreCase("players"))
            {
                if (args.length == 2)
                {
                    return getListOfStringsMatchingLastWord(args, new String[] {"set", "add", "remove", "reset", "list", "enable", "test", "operation"});
                }

                if (!args[1].equalsIgnoreCase("set") && !args[1].equalsIgnoreCase("add") && !args[1].equalsIgnoreCase("remove") && !args[1].equalsIgnoreCase("reset"))
                {
                    if (args[1].equalsIgnoreCase("enable"))
                    {
                        if (args.length == 3)
                        {
                            return getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
                        }

                        if (args.length == 4)
                        {
                            return getListOfStringsMatchingLastWord(args, this.getTriggerObjectiveNames());
                        }
                    }
                    else if (!args[1].equalsIgnoreCase("list") && !args[1].equalsIgnoreCase("test"))
                    {
                        if (args[1].equalsIgnoreCase("operation"))
                        {
                            if (args.length == 3)
                            {
                                return getListOfStringsMatchingLastWord(args, this.getScoreboard().getObjectiveNames());
                            }

                            if (args.length == 4)
                            {
                                return getListOfStringsMatchingLastWord(args, this.getObjectiveNames(true));
                            }

                            if (args.length == 5)
                            {
                                return getListOfStringsMatchingLastWord(args, new String[] {"+=", "-=", "*=", "/=", "%=", "=", "<", ">", "><"});
                            }

                            if (args.length == 6)
                            {
                                return getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
                            }

                            if (args.length == 7)
                            {
                                return getListOfStringsMatchingLastWord(args, this.getObjectiveNames(false));
                            }
                        }
                    }
                    else
                    {
                        if (args.length == 3)
                        {
                            return getListOfStringsMatchingLastWord(args, this.getScoreboard().getObjectiveNames());
                        }

                        if (args.length == 4 && args[1].equalsIgnoreCase("test"))
                        {
                            return getListOfStringsMatchingLastWord(args, this.getObjectiveNames(false));
                        }
                    }
                }
                else
                {
                    if (args.length == 3)
                    {
                        return getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
                    }

                    if (args.length == 4)
                    {
                        return getListOfStringsMatchingLastWord(args, this.getObjectiveNames(true));
                    }
                }
            }
            else if (args[0].equalsIgnoreCase("teams"))
            {
                if (args.length == 2)
                {
                    return getListOfStringsMatchingLastWord(args, new String[] {"add", "remove", "join", "leave", "empty", "list", "option"});
                }

                if (args[1].equalsIgnoreCase("join"))
                {
                    if (args.length == 3)
                    {
                        return getListOfStringsMatchingLastWord(args, this.getScoreboard().getTeamNames());
                    }

                    if (args.length >= 4)
                    {
                        return getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
                    }
                }
                else
                {
                    if (args[1].equalsIgnoreCase("leave"))
                    {
                        return getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
                    }

                    if (!args[1].equalsIgnoreCase("empty") && !args[1].equalsIgnoreCase("list") && !args[1].equalsIgnoreCase("remove"))
                    {
                        if (args[1].equalsIgnoreCase("option"))
                        {
                            if (args.length == 3)
                            {
                                return getListOfStringsMatchingLastWord(args, this.getScoreboard().getTeamNames());
                            }

                            if (args.length == 4)
                            {
                                return getListOfStringsMatchingLastWord(args, new String[] {"color", "friendlyfire", "seeFriendlyInvisibles", "nametagVisibility", "deathMessageVisibility"});
                            }

                            if (args.length == 5)
                            {
                                if (args[3].equalsIgnoreCase("color"))
                                {
                                    return getListOfStringsMatchingLastWord(args, EnumChatFormatting.getValidValues(true, false));
                                }

                                if (args[3].equalsIgnoreCase("nametagVisibility") || args[3].equalsIgnoreCase("deathMessageVisibility"))
                                {
                                    return getListOfStringsMatchingLastWord(args, Team.EnumVisible.getNames());
                                }

                                if (args[3].equalsIgnoreCase("friendlyfire") || args[3].equalsIgnoreCase("seeFriendlyInvisibles"))
                                {
                                    return getListOfStringsMatchingLastWord(args, new String[] {"true", "false"});
                                }
                            }
                        }
                    }
                    else if (args.length == 3)
                    {
                        return getListOfStringsMatchingLastWord(args, this.getScoreboard().getTeamNames());
                    }
                }
            }

            return null;
        }
    }

    protected List<String> getObjectiveNames(boolean writableOnly)
    {
        Collection<ScoreObjective> objectives = this.getScoreboard().getScoreObjectives();
        List<String> objectiveNames = Lists.<String>newArrayList();

        for (ScoreObjective scoreObjective : objectives)
        {
            if (!writableOnly || !scoreObjective.getCriteria().isReadOnly())
            {
                objectiveNames.add(scoreObjective.getName());
            }
        }

        return objectiveNames;
    }

    protected List<String> getTriggerObjectiveNames()
    {
        Collection<ScoreObjective> objectives = this.getScoreboard().getScoreObjectives();
        List<String> objectiveNames = Lists.<String>newArrayList();

        for (ScoreObjective scoreObjective : objectives)
        {
            if (scoreObjective.getCriteria() == IScoreObjectiveCriteria.TRIGGER)
            {
                objectiveNames.add(scoreObjective.getName());
            }
        }

        return objectiveNames;
    }

    public boolean isUsernameIndex(String[] args, int index)
    {
        return !args[0].equalsIgnoreCase("players") ? (args[0].equalsIgnoreCase("teams") ? index == 2 : false) : (args.length > 1 && args[1].equalsIgnoreCase("operation") ? index == 2 || index == 5 : index == 2);
    }
}
