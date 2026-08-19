package net.minecraft.network.play.server;

import com.google.common.collect.Maps;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;

public class S37PacketStatistics implements Packet<INetHandlerPlayClient>
{
    private Map<StatBase, Integer> statistics;

    public S37PacketStatistics()
    {
    }

    public S37PacketStatistics(Map<StatBase, Integer> statisticsIn)
    {
        this.statistics = statisticsIn;
    }

    public void processPacket(INetHandlerPlayClient handler)
    {
        handler.handleStatistics(this);
    }

    public void readPacketData(PacketBuffer buf) throws IOException
    {
        int statisticCount = buf.readVarIntFromBuffer();
        this.statistics = Maps.<StatBase, Integer>newHashMap();

        for (int statisticIndex = 0; statisticIndex < statisticCount; ++statisticIndex)
        {
            StatBase stat = StatList.getOneShotStat(buf.readStringFromBuffer(32767));
            int statValue = buf.readVarIntFromBuffer();

            if (stat != null)
            {
                this.statistics.put(stat, Integer.valueOf(statValue));
            }
        }
    }

    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeVarIntToBuffer(this.statistics.size());

        for (Entry<StatBase, Integer> entry : this.statistics.entrySet())
        {
            buf.writeString(((StatBase)entry.getKey()).statId);
            buf.writeVarIntToBuffer(((Integer)entry.getValue()).intValue());
        }
    }

    public Map<StatBase, Integer> getStatistics()
    {
        return this.statistics;
    }
}
