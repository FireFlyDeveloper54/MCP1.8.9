package net.minecraft.scoreboard;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumChatFormatting;

public class Scoreboard
{
    private final Map<String, ScoreObjective> scoreObjectives = Maps.<String, ScoreObjective>newHashMap();
    private final Map<IScoreObjectiveCriteria, List<ScoreObjective>> scoreObjectiveCriterias = Maps.<IScoreObjectiveCriteria, List<ScoreObjective>>newHashMap();
    private final Map<String, Map<ScoreObjective, Score>> entitiesScoreObjectives = Maps.<String, Map<ScoreObjective, Score>>newHashMap();
    private final ScoreObjective[] objectiveDisplaySlots = new ScoreObjective[19];
    private final Map<String, ScorePlayerTeam> teams = Maps.<String, ScorePlayerTeam>newHashMap();
    private final Map<String, ScorePlayerTeam> teamMemberships = Maps.<String, ScorePlayerTeam>newHashMap();
    private static String[] displaySlotStrings = null;

    public ScoreObjective getObjective(String name)
    {
        return this.scoreObjectives.get(name);
    }

    public ScoreObjective addScoreObjective(String name, IScoreObjectiveCriteria criteria)
    {
        if (name.length() > 16)
        {
            throw new IllegalArgumentException("The objective name \'" + name + "\' is too long!");
        }
        else
        {
            ScoreObjective scoreObjective = this.getObjective(name);

            if (scoreObjective != null)
            {
                throw new IllegalArgumentException("An objective with the name \'" + name + "\' already exists!");
            }
            else
            {
                scoreObjective = new ScoreObjective(this, name, criteria);
                List<ScoreObjective> list = this.scoreObjectiveCriterias.get(criteria);

                if (list == null)
                {
                    list = Lists.<ScoreObjective>newArrayList();
                    this.scoreObjectiveCriterias.put(criteria, list);
                }

                list.add(scoreObjective);
                this.scoreObjectives.put(name, scoreObjective);
                this.onScoreObjectiveAdded(scoreObjective);
                return scoreObjective;
            }
        }
    }

    public Collection<ScoreObjective> getObjectivesFromCriteria(IScoreObjectiveCriteria criteria)
    {
        Collection<ScoreObjective> collection = this.scoreObjectiveCriterias.get(criteria);
        return collection == null ? Lists.<ScoreObjective>newArrayList() : Lists.newArrayList(collection);
    }

    public boolean entityHasObjective(String name, ScoreObjective objective)
    {
        Map<ScoreObjective, Score> map = this.entitiesScoreObjectives.get(name);

        if (map == null)
        {
            return false;
        }
        else
        {
            Score score = map.get(objective);
            return score != null;
        }
    }

    public Score getValueFromObjective(String name, ScoreObjective objective)
    {
        if (name.length() > 40)
        {
            throw new IllegalArgumentException("The player name \'" + name + "\' is too long!");
        }
        else
        {
            Map<ScoreObjective, Score> map = this.entitiesScoreObjectives.get(name);

            if (map == null)
            {
                map = Maps.<ScoreObjective, Score>newHashMap();
                this.entitiesScoreObjectives.put(name, map);
            }

            Score score = map.get(objective);

            if (score == null)
            {
                score = new Score(this, objective, name);
                map.put(objective, score);
            }

            return score;
        }
    }

    public Collection<Score> getSortedScores(ScoreObjective objective)
    {
        List<Score> list = Lists.<Score>newArrayList();

        for (Map<ScoreObjective, Score> map : this.entitiesScoreObjectives.values())
        {
            Score score = map.get(objective);

            if (score != null)
            {
                list.add(score);
            }
        }

        Collections.sort(list, Score.scoreComparator);
        return list;
    }

    public Collection<ScoreObjective> getScoreObjectives()
    {
        return this.scoreObjectives.values();
    }

    public Collection<String> getObjectiveNames()
    {
        return this.entitiesScoreObjectives.keySet();
    }

    public void removeObjectiveFromEntity(String name, ScoreObjective objective)
    {
        if (objective == null)
        {
            Map<ScoreObjective, Score> map = this.entitiesScoreObjectives.remove(name);

            if (map != null)
            {
                this.onPlayerRemoved(name);
            }
        }
        else
        {
            Map<ScoreObjective, Score> entityScores = this.entitiesScoreObjectives.get(name);

            if (entityScores != null)
            {
                Score score = entityScores.remove(objective);

                if (entityScores.size() < 1)
                {
                    Map<ScoreObjective, Score> removedScores = this.entitiesScoreObjectives.remove(name);

                    if (removedScores != null)
                    {
                        this.onPlayerRemoved(name);
                    }
                }
                else if (score != null)
                {
                    this.onPlayerScoreRemoved(name, objective);
                }
            }
        }
    }

    public Collection<Score> getScores()
    {
        Collection<Map<ScoreObjective, Score>> collection = this.entitiesScoreObjectives.values();
        List<Score> list = Lists.<Score>newArrayList();

        for (Map<ScoreObjective, Score> map : collection)
        {
            list.addAll(map.values());
        }

        return list;
    }

    public Map<ScoreObjective, Score> getObjectivesForEntity(String name)
    {
        Map<ScoreObjective, Score> map = this.entitiesScoreObjectives.get(name);

        if (map == null)
        {
            map = Maps.<ScoreObjective, Score>newHashMap();
        }

        return map;
    }

    public void removeObjective(ScoreObjective objective)
    {
        this.scoreObjectives.remove(objective.getName());

        for (int displaySlot = 0; displaySlot < 19; ++displaySlot)
        {
            if (this.getObjectiveInDisplaySlot(displaySlot) == objective)
            {
                this.setObjectiveInDisplaySlot(displaySlot, null);
            }
        }

        List<ScoreObjective> list = this.scoreObjectiveCriterias.get(objective.getCriteria());

        if (list != null)
        {
            list.remove(objective);
        }

        for (Map<ScoreObjective, Score> map : this.entitiesScoreObjectives.values())
        {
            map.remove(objective);
        }

        this.onScoreObjectiveRemoved(objective);
    }

    public void setObjectiveInDisplaySlot(int slot, ScoreObjective objective)
    {
        this.objectiveDisplaySlots[slot] = objective;
    }

    public ScoreObjective getObjectiveInDisplaySlot(int slot)
    {
        return this.objectiveDisplaySlots[slot];
    }

    public ScorePlayerTeam getTeam(String name)
    {
        return this.teams.get(name);
    }

    public ScorePlayerTeam createTeam(String name)
    {
        if (name.length() > 16)
        {
            throw new IllegalArgumentException("The team name \'" + name + "\' is too long!");
        }
        else
        {
            ScorePlayerTeam scorePlayerTeam = this.getTeam(name);

            if (scorePlayerTeam != null)
            {
                throw new IllegalArgumentException("A team with the name \'" + name + "\' already exists!");
            }
            else
            {
                scorePlayerTeam = new ScorePlayerTeam(this, name);
                this.teams.put(name, scorePlayerTeam);
                this.broadcastTeamCreated(scorePlayerTeam);
                return scorePlayerTeam;
            }
        }
    }

    public void removeTeam(ScorePlayerTeam team)
    {
        this.teams.remove(team.getRegisteredName());

        for (String memberName : team.getMembershipCollection())
        {
            this.teamMemberships.remove(memberName);
        }

        this.broadcastTeamRemoved(team);
    }

    public boolean addPlayerToTeam(String player, String newTeam)
    {
        if (player.length() > 40)
        {
            throw new IllegalArgumentException("The player name \'" + player + "\' is too long!");
        }
        else if (!this.teams.containsKey(newTeam))
        {
            return false;
        }
        else
        {
            ScorePlayerTeam scorePlayerTeam = this.getTeam(newTeam);

            if (this.getPlayersTeam(player) != null)
            {
                this.removePlayerFromTeams(player);
            }

            this.teamMemberships.put(player, scorePlayerTeam);
            scorePlayerTeam.getMembershipCollection().add(player);
            return true;
        }
    }

    public boolean removePlayerFromTeams(String playerName)
    {
        ScorePlayerTeam scorePlayerTeam = this.getPlayersTeam(playerName);

        if (scorePlayerTeam != null)
        {
            this.removePlayerFromTeam(playerName, scorePlayerTeam);
            return true;
        }
        else
        {
            return false;
        }
    }

    public void removePlayerFromTeam(String playerName, ScorePlayerTeam team)
    {
        if (this.getPlayersTeam(playerName) != team)
        {
            throw new IllegalStateException("Player is either on another team or not on any team. Cannot remove from team \'" + team.getRegisteredName() + "\'.");
        }
        else
        {
            this.teamMemberships.remove(playerName);
            team.getMembershipCollection().remove(playerName);
        }
    }

    public Collection<String> getTeamNames()
    {
        return this.teams.keySet();
    }

    public Collection<ScorePlayerTeam> getTeams()
    {
        return this.teams.values();
    }

    public ScorePlayerTeam getPlayersTeam(String playerName)
    {
        return this.teamMemberships.get(playerName);
    }

    public void onScoreObjectiveAdded(ScoreObjective scoreObjectiveIn)
    {
    }

    public void onObjectiveDisplayNameChanged(ScoreObjective objective)
    {
    }

    public void onScoreObjectiveRemoved(ScoreObjective objective)
    {
    }

    public void onScoreUpdated(Score score)
    {
    }

    public void onPlayerRemoved(String playerName)
    {
    }

    public void onPlayerScoreRemoved(String playerName, ScoreObjective objective)
    {
    }

    public void broadcastTeamCreated(ScorePlayerTeam playerTeam)
    {
    }

    public void sendTeamUpdate(ScorePlayerTeam playerTeam)
    {
    }

    public void broadcastTeamRemoved(ScorePlayerTeam playerTeam)
    {
    }

    public static String getObjectiveDisplaySlot(int slot)
    {
        switch (slot)
        {
            case 0:
                return "list";

            case 1:
                return "sidebar";

            case 2:
                return "belowName";

            default:
                if (slot >= 3 && slot <= 18)
                {
                    EnumChatFormatting teamColor = EnumChatFormatting.getByColorIndex(slot - 3);

                    if (teamColor != null && teamColor != EnumChatFormatting.RESET)
                    {
                        return "sidebar.team." + teamColor.getFriendlyName();
                    }
                }

                return null;
        }
    }

    public static int getObjectiveDisplaySlotNumber(String slotName)
    {
        if (slotName.equalsIgnoreCase("list"))
        {
            return 0;
        }
        else if (slotName.equalsIgnoreCase("sidebar"))
        {
            return 1;
        }
        else if (slotName.equalsIgnoreCase("belowName"))
        {
            return 2;
        }
        else
        {
            if (slotName.startsWith("sidebar.team."))
            {
                String teamColorName = slotName.substring("sidebar.team.".length());
                EnumChatFormatting teamColor = EnumChatFormatting.getValueByName(teamColorName);

                if (teamColor != null && teamColor.getColorIndex() >= 0)
                {
                    return teamColor.getColorIndex() + 3;
                }
            }

            return -1;
        }
    }

    public static String[] getDisplaySlotStrings()
    {
        if (displaySlotStrings == null)
        {
            displaySlotStrings = new String[19];

            for (int displaySlot = 0; displaySlot < 19; ++displaySlot)
            {
                displaySlotStrings[displaySlot] = getObjectiveDisplaySlot(displaySlot);
            }
        }

        return displaySlotStrings;
    }

    public void removeEntity(Entity entity)
    {
        if (entity != null && !(entity instanceof EntityPlayer) && !entity.isEntityAlive())
        {
            String entityId = entity.getUniqueID().toString();
            this.removeObjectiveFromEntity(entityId, null);
            this.removePlayerFromTeams(entityId);
        }
    }
}
