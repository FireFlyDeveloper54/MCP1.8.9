package net.minecraft.command;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class CommandResultStats
{
    private static final CommandResultStats.Type[] RESULT_TYPES = CommandResultStats.Type.VALUES;
    private static final int NUM_RESULT_TYPES = RESULT_TYPES.length;
    private static final String[] STRING_RESULT_TYPES = new String[NUM_RESULT_TYPES];
    private String[] entitiesID;
    private String[] objectives;

    public CommandResultStats()
    {
        this.entitiesID = STRING_RESULT_TYPES;
        this.objectives = STRING_RESULT_TYPES;
    }

    public void setCommandStatScore(final ICommandSender sender, CommandResultStats.Type resultTypeIn, int scorePoint)
    {
        String entityId = this.entitiesID[resultTypeIn.getTypeID()];

        if (entityId != null)
        {
            ICommandSender icommandsender = new ICommandSender()
            {
                public String getName()
                {
                    return sender.getName();
                }
                public IChatComponent getDisplayName()
                {
                    return sender.getDisplayName();
                }
                public void addChatMessage(IChatComponent component)
                {
                    sender.addChatMessage(component);
                }
                public boolean canCommandSenderUseCommand(int permLevel, String commandName)
                {
                    return true;
                }
                public BlockPos getPosition()
                {
                    return sender.getPosition();
                }
                public Vec3 getPositionVector()
                {
                    return sender.getPositionVector();
                }
                public World getEntityWorld()
                {
                    return sender.getEntityWorld();
                }
                public Entity getCommandSenderEntity()
                {
                    return sender.getCommandSenderEntity();
                }
                public boolean sendCommandFeedback()
                {
                    return sender.sendCommandFeedback();
                }
                public void setCommandStat(CommandResultStats.Type type, int amount)
                {
                    sender.setCommandStat(type, amount);
                }
            };
            String entityName;

            try
            {
                entityName = CommandBase.getEntityName(icommandsender, entityId);
            }
            catch (EntityNotFoundException caughtEntityNotFoundException)
            {
                return;
            }

            String objectiveName = this.objectives[resultTypeIn.getTypeID()];

            if (objectiveName != null)
            {
                Scoreboard scoreboard = sender.getEntityWorld().getScoreboard();
                ScoreObjective scoreObjective = scoreboard.getObjective(objectiveName);

                if (scoreObjective != null)
                {
                    if (scoreboard.entityHasObjective(entityName, scoreObjective))
                    {
                        Score score = scoreboard.getValueFromObjective(entityName, scoreObjective);
                        score.setScorePoints(scorePoint);
                    }
                }
            }
        }
    }

    public void readStatsFromNBT(NBTTagCompound tagcompound)
    {
        if (tagcompound.hasKey("CommandStats", 10))
        {
            NBTTagCompound nBTTagCompound = tagcompound.getCompoundTag("CommandStats");

            for (CommandResultStats.Type commandresultstats$type : RESULT_TYPES)
            {
                String entityKey = commandresultstats$type.getTypeName() + "Name";
                String objectiveKey = commandresultstats$type.getTypeName() + "Objective";

                if (nBTTagCompound.hasKey(entityKey, 8) && nBTTagCompound.hasKey(objectiveKey, 8))
                {
                    String entityId = nBTTagCompound.getString(entityKey);
                    String objectiveName = nBTTagCompound.getString(objectiveKey);
                    setScoreBoardStat(this, commandresultstats$type, entityId, objectiveName);
                }
            }
        }
    }

    public void writeStatsToNBT(NBTTagCompound tagcompound)
    {
        NBTTagCompound nBTTagCompound = new NBTTagCompound();

        for (CommandResultStats.Type commandresultstats$type : RESULT_TYPES)
        {
            String entityId = this.entitiesID[commandresultstats$type.getTypeID()];
            String objectiveName = this.objectives[commandresultstats$type.getTypeID()];

            if (entityId != null && objectiveName != null)
            {
                nBTTagCompound.setString(commandresultstats$type.getTypeName() + "Name", entityId);
                nBTTagCompound.setString(commandresultstats$type.getTypeName() + "Objective", objectiveName);
            }
        }

        if (!nBTTagCompound.hasNoTags())
        {
            tagcompound.setTag("CommandStats", nBTTagCompound);
        }
    }

    public static void setScoreBoardStat(CommandResultStats stats, CommandResultStats.Type resultType, String entityID, String objectiveName)
    {
        if (entityID != null && entityID.length() != 0 && objectiveName != null && objectiveName.length() != 0)
        {
            if (stats.entitiesID == STRING_RESULT_TYPES || stats.objectives == STRING_RESULT_TYPES)
            {
                stats.entitiesID = new String[NUM_RESULT_TYPES];
                stats.objectives = new String[NUM_RESULT_TYPES];
            }

            stats.entitiesID[resultType.getTypeID()] = entityID;
            stats.objectives[resultType.getTypeID()] = objectiveName;
        }
        else
        {
            removeScoreBoardStat(stats, resultType);
        }
    }

    private static void removeScoreBoardStat(CommandResultStats resultStatsIn, CommandResultStats.Type resultTypeIn)
    {
        if (resultStatsIn.entitiesID != STRING_RESULT_TYPES && resultStatsIn.objectives != STRING_RESULT_TYPES)
        {
            resultStatsIn.entitiesID[resultTypeIn.getTypeID()] = null;
            resultStatsIn.objectives[resultTypeIn.getTypeID()] = null;
            boolean isEmpty = true;

            for (CommandResultStats.Type commandresultstats$type : RESULT_TYPES)
            {
                if (resultStatsIn.entitiesID[commandresultstats$type.getTypeID()] != null && resultStatsIn.objectives[commandresultstats$type.getTypeID()] != null)
                {
                    isEmpty = false;
                    break;
                }
            }

            if (isEmpty)
            {
                resultStatsIn.entitiesID = STRING_RESULT_TYPES;
                resultStatsIn.objectives = STRING_RESULT_TYPES;
            }
        }
    }

    public void addAllStats(CommandResultStats resultStatsIn)
    {
        for (CommandResultStats.Type commandresultstats$type : RESULT_TYPES)
        {
            setScoreBoardStat(this, commandresultstats$type, resultStatsIn.entitiesID[commandresultstats$type.getTypeID()], resultStatsIn.objectives[commandresultstats$type.getTypeID()]);
        }
    }

    public static enum Type
    {
        SUCCESS_COUNT(0, "SuccessCount"),
        AFFECTED_BLOCKS(1, "AffectedBlocks"),
        AFFECTED_ENTITIES(2, "AffectedEntities"),
        AFFECTED_ITEMS(3, "AffectedItems"),
        QUERY_RESULT(4, "QueryResult");

        public static final CommandResultStats.Type[] VALUES = values();
        final int typeID;
        final String typeName;

        private Type(int id, String name)
        {
            this.typeID = id;
            this.typeName = name;
        }

        public int getTypeID()
        {
            return this.typeID;
        }

        public String getTypeName()
        {
            return this.typeName;
        }

        public static String[] getTypeNames()
        {
            String[] typeNames = new String[RESULT_TYPES.length];
            int typeIndex = 0;

            for (CommandResultStats.Type commandresultstats$type : RESULT_TYPES)
            {
                typeNames[typeIndex++] = commandresultstats$type.getTypeName();
            }

            return typeNames;
        }

        public static CommandResultStats.Type getTypeByName(String name)
        {
            for (CommandResultStats.Type commandresultstats$type : RESULT_TYPES)
            {
                if (commandresultstats$type.getTypeName().equals(name))
                {
                    return commandresultstats$type;
                }
            }

            return null;
        }
    }
}
