package net.minecraft.scoreboard;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S3BPacketScoreboardObjective;
import net.minecraft.network.play.server.S3CPacketUpdateScore;
import net.minecraft.network.play.server.S3DPacketDisplayScoreboard;
import net.minecraft.network.play.server.S3EPacketTeams;
import net.minecraft.server.MinecraftServer;

public class ServerScoreboard extends Scoreboard
{
    private final MinecraftServer scoreboardMCServer;
    private final Set<ScoreObjective> trackedObjectives = Sets.<ScoreObjective>newHashSet();
    private ScoreboardSaveData scoreboardSaveData;

    public ServerScoreboard(MinecraftServer mcServer)
    {
        this.scoreboardMCServer = mcServer;
    }

    public void onScoreUpdated(Score score)
    {
        super.onScoreUpdated(score);

        if (this.trackedObjectives.contains(score.getObjective()))
        {
            this.scoreboardMCServer.getConfigurationManager().sendPacketToAllPlayers(new S3CPacketUpdateScore(score));
        }

        this.markSaveDataDirty();
    }

    public void onPlayerRemoved(String playerName)
    {
        super.onPlayerRemoved(playerName);
        this.scoreboardMCServer.getConfigurationManager().sendPacketToAllPlayers(new S3CPacketUpdateScore(playerName));
        this.markSaveDataDirty();
    }

    public void onPlayerScoreRemoved(String playerName, ScoreObjective objective)
    {
        super.onPlayerScoreRemoved(playerName, objective);
        this.scoreboardMCServer.getConfigurationManager().sendPacketToAllPlayers(new S3CPacketUpdateScore(playerName, objective));
        this.markSaveDataDirty();
    }

    public void setObjectiveInDisplaySlot(int slot, ScoreObjective objective)
    {
        ScoreObjective scoreObjective = this.getObjectiveInDisplaySlot(slot);
        super.setObjectiveInDisplaySlot(slot, objective);

        if (scoreObjective != objective && scoreObjective != null)
        {
            if (this.getObjectiveDisplaySlotCount(scoreObjective) > 0)
            {
                this.scoreboardMCServer.getConfigurationManager().sendPacketToAllPlayers(new S3DPacketDisplayScoreboard(slot, objective));
            }
            else
            {
                this.sendDisplaySlotRemovalPackets(scoreObjective);
            }
        }

        if (objective != null)
        {
            if (this.trackedObjectives.contains(objective))
            {
                this.scoreboardMCServer.getConfigurationManager().sendPacketToAllPlayers(new S3DPacketDisplayScoreboard(slot, objective));
            }
            else
            {
                this.sendObjectiveToPlayers(objective);
            }
        }

        this.markSaveDataDirty();
    }

    public boolean addPlayerToTeam(String player, String newTeam)
    {
        if (super.addPlayerToTeam(player, newTeam))
        {
            ScorePlayerTeam scorePlayerTeam = this.getTeam(newTeam);
            this.scoreboardMCServer.getConfigurationManager().sendPacketToAllPlayers(new S3EPacketTeams(scorePlayerTeam, Arrays.asList(new String[] {player}), 3));
            this.markSaveDataDirty();
            return true;
        }
        else
        {
            return false;
        }
    }

    public void removePlayerFromTeam(String playerName, ScorePlayerTeam team)
    {
        super.removePlayerFromTeam(playerName, team);
        this.scoreboardMCServer.getConfigurationManager().sendPacketToAllPlayers(new S3EPacketTeams(team, Arrays.asList(new String[] {playerName}), 4));
        this.markSaveDataDirty();
    }

    public void onScoreObjectiveAdded(ScoreObjective scoreObjectiveIn)
    {
        super.onScoreObjectiveAdded(scoreObjectiveIn);
        this.markSaveDataDirty();
    }

    public void onObjectiveDisplayNameChanged(ScoreObjective objective)
    {
        super.onObjectiveDisplayNameChanged(objective);

        if (this.trackedObjectives.contains(objective))
        {
            this.scoreboardMCServer.getConfigurationManager().sendPacketToAllPlayers(new S3BPacketScoreboardObjective(objective, 2));
        }

        this.markSaveDataDirty();
    }

    public void onScoreObjectiveRemoved(ScoreObjective objective)
    {
        super.onScoreObjectiveRemoved(objective);

        if (this.trackedObjectives.contains(objective))
        {
            this.sendDisplaySlotRemovalPackets(objective);
        }

        this.markSaveDataDirty();
    }

    public void broadcastTeamCreated(ScorePlayerTeam playerTeam)
    {
        super.broadcastTeamCreated(playerTeam);
        this.scoreboardMCServer.getConfigurationManager().sendPacketToAllPlayers(new S3EPacketTeams(playerTeam, 0));
        this.markSaveDataDirty();
    }

    public void sendTeamUpdate(ScorePlayerTeam playerTeam)
    {
        super.sendTeamUpdate(playerTeam);
        this.scoreboardMCServer.getConfigurationManager().sendPacketToAllPlayers(new S3EPacketTeams(playerTeam, 2));
        this.markSaveDataDirty();
    }

    public void broadcastTeamRemoved(ScorePlayerTeam playerTeam)
    {
        super.broadcastTeamRemoved(playerTeam);
        this.scoreboardMCServer.getConfigurationManager().sendPacketToAllPlayers(new S3EPacketTeams(playerTeam, 1));
        this.markSaveDataDirty();
    }

    public void setScoreboardSaveData(ScoreboardSaveData scoreboardSaveData)
    {
        this.scoreboardSaveData = scoreboardSaveData;
    }

    protected void markSaveDataDirty()
    {
        if (this.scoreboardSaveData != null)
        {
            this.scoreboardSaveData.markDirty();
        }
    }

    public List<Packet> getCreatePackets(ScoreObjective objective)
    {
        List<Packet> packets = Lists.<Packet>newArrayList();
        packets.add(new S3BPacketScoreboardObjective(objective, 0));

        for (int displaySlot = 0; displaySlot < 19; ++displaySlot)
        {
            if (this.getObjectiveInDisplaySlot(displaySlot) == objective)
            {
                packets.add(new S3DPacketDisplayScoreboard(displaySlot, objective));
            }
        }

        for (Score score : this.getSortedScores(objective))
        {
            packets.add(new S3CPacketUpdateScore(score));
        }

        return packets;
    }

    public void sendObjectiveToPlayers(ScoreObjective objective)
    {
        List<Packet> packets = this.getCreatePackets(objective);

        for (EntityPlayerMP player : this.scoreboardMCServer.getConfigurationManager().getPlayerList())
        {
            for (Packet packet : packets)
            {
                player.playerNetServerHandler.sendPacket(packet);
            }
        }

        this.trackedObjectives.add(objective);
    }

    public List<Packet> getRemovePackets(ScoreObjective objective)
    {
        List<Packet> packets = Lists.<Packet>newArrayList();
        packets.add(new S3BPacketScoreboardObjective(objective, 1));

        for (int displaySlot = 0; displaySlot < 19; ++displaySlot)
        {
            if (this.getObjectiveInDisplaySlot(displaySlot) == objective)
            {
                packets.add(new S3DPacketDisplayScoreboard(displaySlot, objective));
            }
        }

        return packets;
    }

    public void sendDisplaySlotRemovalPackets(ScoreObjective objective)
    {
        List<Packet> packets = this.getRemovePackets(objective);

        for (EntityPlayerMP entityPlayerMP : this.scoreboardMCServer.getConfigurationManager().getPlayerList())
        {
            for (Packet packet : packets)
            {
                entityPlayerMP.playerNetServerHandler.sendPacket(packet);
            }
        }

        this.trackedObjectives.remove(objective);
    }

    public int getObjectiveDisplaySlotCount(ScoreObjective objective)
    {
        int displaySlotCount = 0;

        for (int displaySlot = 0; displaySlot < 19; ++displaySlot)
        {
            if (this.getObjectiveInDisplaySlot(displaySlot) == objective)
            {
                ++displaySlotCount;
            }
        }

        return displaySlotCount;
    }
}
