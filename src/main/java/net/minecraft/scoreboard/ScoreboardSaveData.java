package net.minecraft.scoreboard;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.WorldSavedData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ScoreboardSaveData extends WorldSavedData
{
    private static final Logger logger = LogManager.getLogger();
    private Scoreboard theScoreboard;
    private NBTTagCompound delayedInitNbt;

    public ScoreboardSaveData()
    {
        this("scoreboard");
    }

    public ScoreboardSaveData(String name)
    {
        super(name);
    }

    public void setScoreboard(Scoreboard scoreboardIn)
    {
        this.theScoreboard = scoreboardIn;

        if (this.delayedInitNbt != null)
        {
            this.readFromNBT(this.delayedInitNbt);
        }
    }

    public void readFromNBT(NBTTagCompound nbt)
    {
        if (this.theScoreboard == null)
        {
            this.delayedInitNbt = nbt;
        }
        else
        {
            this.readObjectives(nbt.getTagList("Objectives", 10));
            this.readScores(nbt.getTagList("PlayerScores", 10));

            if (nbt.hasKey("DisplaySlots", 10))
            {
                this.readDisplayConfig(nbt.getCompoundTag("DisplaySlots"));
            }

            if (nbt.hasKey("Teams", 9))
            {
                this.readTeams(nbt.getTagList("Teams", 10));
            }
        }
    }

    protected void readTeams(NBTTagList teamsNbt)
    {
        for (int teamIndex = 0; teamIndex < teamsNbt.tagCount(); ++teamIndex)
        {
            NBTTagCompound teamNbt = teamsNbt.getCompoundTagAt(teamIndex);
            String teamName = teamNbt.getString("Name");

            if (teamName.length() > 16)
            {
                teamName = teamName.substring(0, 16);
            }

            ScorePlayerTeam scorePlayerTeam = this.theScoreboard.createTeam(teamName);
            String teamDisplayName = teamNbt.getString("DisplayName");

            if (teamDisplayName.length() > 32)
            {
                teamDisplayName = teamDisplayName.substring(0, 32);
            }

            scorePlayerTeam.setTeamName(teamDisplayName);

            if (teamNbt.hasKey("TeamColor", 8))
            {
                scorePlayerTeam.setChatFormat(EnumChatFormatting.getValueByName(teamNbt.getString("TeamColor")));
            }

            scorePlayerTeam.setNamePrefix(teamNbt.getString("Prefix"));
            scorePlayerTeam.setNameSuffix(teamNbt.getString("Suffix"));

            if (teamNbt.hasKey("AllowFriendlyFire", 99))
            {
                scorePlayerTeam.setAllowFriendlyFire(teamNbt.getBoolean("AllowFriendlyFire"));
            }

            if (teamNbt.hasKey("SeeFriendlyInvisibles", 99))
            {
                scorePlayerTeam.setSeeFriendlyInvisiblesEnabled(teamNbt.getBoolean("SeeFriendlyInvisibles"));
            }

            if (teamNbt.hasKey("NameTagVisibility", 8))
            {
                Team.EnumVisible nameTagVisibility = Team.EnumVisible.getByName(teamNbt.getString("NameTagVisibility"));

                if (nameTagVisibility != null)
                {
                    scorePlayerTeam.setNameTagVisibility(nameTagVisibility);
                }
            }

            if (teamNbt.hasKey("DeathMessageVisibility", 8))
            {
                Team.EnumVisible deathMessageVisibility = Team.EnumVisible.getByName(teamNbt.getString("DeathMessageVisibility"));

                if (deathMessageVisibility != null)
                {
                    scorePlayerTeam.setDeathMessageVisibility(deathMessageVisibility);
                }
            }

            this.readTeamPlayers(scorePlayerTeam, teamNbt.getTagList("Players", 8));
        }
    }

    protected void readTeamPlayers(ScorePlayerTeam team, NBTTagList playersNbt)
    {
        for (int playerIndex = 0; playerIndex < playersNbt.tagCount(); ++playerIndex)
        {
            this.theScoreboard.addPlayerToTeam(playersNbt.getStringTagAt(playerIndex), team.getRegisteredName());
        }
    }

    protected void readDisplayConfig(NBTTagCompound displayConfigNbt)
    {
        for (int displaySlot = 0; displaySlot < 19; ++displaySlot)
        {
            if (displayConfigNbt.hasKey("slot_" + displaySlot, 8))
            {
                String objectiveName = displayConfigNbt.getString("slot_" + displaySlot);
                ScoreObjective scoreObjective = this.theScoreboard.getObjective(objectiveName);
                this.theScoreboard.setObjectiveInDisplaySlot(displaySlot, scoreObjective);
            }
        }
    }

    protected void readObjectives(NBTTagList nbt)
    {
        for (int objectiveIndex = 0; objectiveIndex < nbt.tagCount(); ++objectiveIndex)
        {
            NBTTagCompound objectiveNbt = nbt.getCompoundTagAt(objectiveIndex);
            IScoreObjectiveCriteria criteria = (IScoreObjectiveCriteria)IScoreObjectiveCriteria.INSTANCES.get(objectiveNbt.getString("CriteriaName"));

            if (criteria != null)
            {
                String objectiveName = objectiveNbt.getString("Name");

                if (objectiveName.length() > 16)
                {
                    objectiveName = objectiveName.substring(0, 16);
                }

                ScoreObjective scoreObjective = this.theScoreboard.addScoreObjective(objectiveName, criteria);
                scoreObjective.setDisplayName(objectiveNbt.getString("DisplayName"));
                scoreObjective.setRenderType(IScoreObjectiveCriteria.EnumRenderType.byName(objectiveNbt.getString("RenderType")));
            }
        }
    }

    protected void readScores(NBTTagList nbt)
    {
        for (int scoreIndex = 0; scoreIndex < nbt.tagCount(); ++scoreIndex)
        {
            NBTTagCompound scoreNbt = nbt.getCompoundTagAt(scoreIndex);
            ScoreObjective scoreObjective = this.theScoreboard.getObjective(scoreNbt.getString("Objective"));
            String playerName = scoreNbt.getString("Name");

            if (playerName.length() > 40)
            {
                playerName = playerName.substring(0, 40);
            }

            Score score = this.theScoreboard.getValueFromObjective(playerName, scoreObjective);
            score.setScorePoints(scoreNbt.getInteger("Score"));

            if (scoreNbt.hasKey("Locked"))
            {
                score.setLocked(scoreNbt.getBoolean("Locked"));
            }
        }
    }

    public void writeToNBT(NBTTagCompound nbt)
    {
        if (this.theScoreboard == null)
        {
            logger.warn("Tried to save scoreboard without having a scoreboard...");
        }
        else
        {
            nbt.setTag("Objectives", this.objectivesToNbt());
            nbt.setTag("PlayerScores", this.scoresToNbt());
            nbt.setTag("Teams", this.teamsToNbt());
            this.writeDisplayConfig(nbt);
        }
    }

    protected NBTTagList teamsToNbt()
    {
        NBTTagList teamsNbt = new NBTTagList();

        for (ScorePlayerTeam scorePlayerTeam : this.theScoreboard.getTeams())
        {
            NBTTagCompound teamNbt = new NBTTagCompound();
            teamNbt.setString("Name", scorePlayerTeam.getRegisteredName());
            teamNbt.setString("DisplayName", scorePlayerTeam.getTeamName());

            if (scorePlayerTeam.getChatFormat().getColorIndex() >= 0)
            {
                teamNbt.setString("TeamColor", scorePlayerTeam.getChatFormat().getFriendlyName());
            }

            teamNbt.setString("Prefix", scorePlayerTeam.getColorPrefix());
            teamNbt.setString("Suffix", scorePlayerTeam.getColorSuffix());
            teamNbt.setBoolean("AllowFriendlyFire", scorePlayerTeam.getAllowFriendlyFire());
            teamNbt.setBoolean("SeeFriendlyInvisibles", scorePlayerTeam.getSeeFriendlyInvisiblesEnabled());
            teamNbt.setString("NameTagVisibility", scorePlayerTeam.getNameTagVisibility().internalName);
            teamNbt.setString("DeathMessageVisibility", scorePlayerTeam.getDeathMessageVisibility().internalName);
            NBTTagList playersNbt = new NBTTagList();

            for (String memberName : scorePlayerTeam.getMembershipCollection())
            {
                playersNbt.appendTag(new NBTTagString(memberName));
            }

            teamNbt.setTag("Players", playersNbt);
            teamsNbt.appendTag(teamNbt);
        }

        return teamsNbt;
    }

    protected void writeDisplayConfig(NBTTagCompound nbt)
    {
        NBTTagCompound displaySlotsNbt = new NBTTagCompound();
        boolean hasDisplaySlots = false;

        for (int displaySlot = 0; displaySlot < 19; ++displaySlot)
        {
            ScoreObjective scoreObjective = this.theScoreboard.getObjectiveInDisplaySlot(displaySlot);

            if (scoreObjective != null)
            {
                displaySlotsNbt.setString("slot_" + displaySlot, scoreObjective.getName());
                hasDisplaySlots = true;
            }
        }

        if (hasDisplaySlots)
        {
            nbt.setTag("DisplaySlots", displaySlotsNbt);
        }
    }

    protected NBTTagList objectivesToNbt()
    {
        NBTTagList nBTTagList = new NBTTagList();

        for (ScoreObjective scoreObjective : this.theScoreboard.getScoreObjectives())
        {
            if (scoreObjective.getCriteria() != null)
            {
                NBTTagCompound nBTTagCompound = new NBTTagCompound();
                nBTTagCompound.setString("Name", scoreObjective.getName());
                nBTTagCompound.setString("CriteriaName", scoreObjective.getCriteria().getName());
                nBTTagCompound.setString("DisplayName", scoreObjective.getDisplayName());
                nBTTagCompound.setString("RenderType", scoreObjective.getRenderType().getName());
                nBTTagList.appendTag(nBTTagCompound);
            }
        }

        return nBTTagList;
    }

    protected NBTTagList scoresToNbt()
    {
        NBTTagList nBTTagList = new NBTTagList();

        for (Score score : this.theScoreboard.getScores())
        {
            if (score.getObjective() != null)
            {
                NBTTagCompound nBTTagCompound = new NBTTagCompound();
                nBTTagCompound.setString("Name", score.getPlayerName());
                nBTTagCompound.setString("Objective", score.getObjective().getName());
                nBTTagCompound.setInteger("Score", score.getScorePoints());
                nBTTagCompound.setBoolean("Locked", score.isLocked());
                nBTTagList.appendTag(nBTTagCompound);
            }
        }

        return nBTTagList;
    }
}
