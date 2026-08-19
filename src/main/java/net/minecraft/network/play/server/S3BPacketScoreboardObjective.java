package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.scoreboard.IScoreObjectiveCriteria;
import net.minecraft.scoreboard.ScoreObjective;

public class S3BPacketScoreboardObjective implements Packet<INetHandlerPlayClient>
{
    private String objectiveName;
    private String objectiveValue;
    private IScoreObjectiveCriteria.EnumRenderType type;
    private int mode;

    public S3BPacketScoreboardObjective()
    {
    }

    public S3BPacketScoreboardObjective(ScoreObjective objective, int modeIn)
    {
        this.objectiveName = objective.getName();
        this.objectiveValue = objective.getDisplayName();
        this.type = objective.getCriteria().getRenderType();
        this.mode = modeIn;
    }

    public void readPacketData(PacketBuffer buf) throws IOException
    {
        this.objectiveName = buf.readStringFromBuffer(16);
        this.mode = buf.readByte();

        if (this.mode == 0 || this.mode == 2)
        {
            this.objectiveValue = buf.readStringFromBuffer(32);
            this.type = IScoreObjectiveCriteria.EnumRenderType.byName(buf.readStringFromBuffer(16));
        }
    }

    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeString(this.objectiveName);
        buf.writeByte(this.mode);

        if (this.mode == 0 || this.mode == 2)
        {
            buf.writeString(this.objectiveValue);
            buf.writeString(this.type.getName());
        }
    }

    public void processPacket(INetHandlerPlayClient handler)
    {
        handler.handleScoreboardObjective(this);
    }

    public String getObjectiveName()
    {
        return this.objectiveName;
    }

    public String getObjectiveValue()
    {
        return this.objectiveValue;
    }

    public int getMode()
    {
        return this.mode;
    }

    public IScoreObjectiveCriteria.EnumRenderType getType()
    {
        return this.type;
    }
}
