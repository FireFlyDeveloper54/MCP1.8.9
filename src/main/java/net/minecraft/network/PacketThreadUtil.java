package net.minecraft.network;

import net.minecraft.network.play.server.S01PacketJoinGame;
import net.minecraft.network.play.server.S07PacketRespawn;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.src.Config;
import net.minecraft.util.IThreadListener;

public class PacketThreadUtil
{
    public static int lastDimensionId = Integer.MIN_VALUE;

    public static <T extends INetHandler> void checkThreadAndEnqueue(final Packet<T> packetIn, final T handler, IThreadListener threadListener) throws ThreadQuickExitException
    {
        if (!threadListener.isCallingFromMinecraftThread())
        {
            threadListener.addScheduledTask(new Runnable()
            {
                public void run()
                {
                    PacketThreadUtil.clientPreProcessPacket(packetIn);
                    packetIn.processPacket(handler);
                }
            });
            throw ThreadQuickExitException.INSTANCE;
        }
        else
        {
            clientPreProcessPacket(packetIn);
        }
    }

    protected static void clientPreProcessPacket(Packet packetIn)
    {
        if (packetIn instanceof S08PacketPlayerPosLook)
        {
            Config.getRenderGlobal().onPlayerPositionSet();
        }

        if (packetIn instanceof S07PacketRespawn)
        {
            S07PacketRespawn s07packetrespawn = (S07PacketRespawn)packetIn;
            lastDimensionId = s07packetrespawn.getDimensionID();
        }
        else if (packetIn instanceof S01PacketJoinGame)
        {
            S01PacketJoinGame s01packetjoingame = (S01PacketJoinGame)packetIn;
            lastDimensionId = s01packetjoingame.getDimension();
        }
        else
        {
            lastDimensionId = Integer.MIN_VALUE;
        }
    }
}
