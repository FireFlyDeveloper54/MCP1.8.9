package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.CombatTracker;

public class S42PacketCombatEvent implements Packet<INetHandlerPlayClient>
{
    public S42PacketCombatEvent.Event eventType;
    public int fighterEntityId;
    public int attackerEntityId;
    public int combatDuration;
    public String deathMessage;

    public S42PacketCombatEvent()
    {
    }

    @SuppressWarnings("incomplete-switch")
    public S42PacketCombatEvent(CombatTracker combatTrackerIn, S42PacketCombatEvent.Event combatEventType)
    {
        this.eventType = combatEventType;
        EntityLivingBase entityLivingBase = combatTrackerIn.getBestAttacker();

        switch (combatEventType)
        {
            case END_COMBAT:
                this.combatDuration = combatTrackerIn.getCombatDuration();
                this.attackerEntityId = entityLivingBase == null ? -1 : entityLivingBase.getEntityId();
                break;

            case ENTITY_DIED:
                this.fighterEntityId = combatTrackerIn.getFighter().getEntityId();
                this.attackerEntityId = entityLivingBase == null ? -1 : entityLivingBase.getEntityId();
                this.deathMessage = combatTrackerIn.getDeathMessage().getUnformattedText();
        }
    }

    public void readPacketData(PacketBuffer buf) throws IOException
    {
        this.eventType = (S42PacketCombatEvent.Event)buf.readEnumValue(S42PacketCombatEvent.Event.class);

        if (this.eventType == S42PacketCombatEvent.Event.END_COMBAT)
        {
            this.combatDuration = buf.readVarIntFromBuffer();
            this.attackerEntityId = buf.readInt();
        }
        else if (this.eventType == S42PacketCombatEvent.Event.ENTITY_DIED)
        {
            this.fighterEntityId = buf.readVarIntFromBuffer();
            this.attackerEntityId = buf.readInt();
            this.deathMessage = buf.readStringFromBuffer(32767);
        }
    }

    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeEnumValue(this.eventType);

        if (this.eventType == S42PacketCombatEvent.Event.END_COMBAT)
        {
            buf.writeVarIntToBuffer(this.combatDuration);
            buf.writeInt(this.attackerEntityId);
        }
        else if (this.eventType == S42PacketCombatEvent.Event.ENTITY_DIED)
        {
            buf.writeVarIntToBuffer(this.fighterEntityId);
            buf.writeInt(this.attackerEntityId);
            buf.writeString(this.deathMessage);
        }
    }

    public void processPacket(INetHandlerPlayClient handler)
    {
        handler.handleCombatEvent(this);
    }

    public static enum Event
    {
        ENTER_COMBAT,
        END_COMBAT,
        ENTITY_DIED;
    }
}
