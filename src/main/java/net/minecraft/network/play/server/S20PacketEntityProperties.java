package net.minecraft.network.play.server;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

public class S20PacketEntityProperties implements Packet<INetHandlerPlayClient>
{
    private int entityId;
    private final List<S20PacketEntityProperties.Snapshot> snapshots = Lists.<S20PacketEntityProperties.Snapshot>newArrayList();

    public S20PacketEntityProperties()
    {
    }

    public S20PacketEntityProperties(int entityIdIn, Collection<IAttributeInstance> attributes)
    {
        this.entityId = entityIdIn;

        for (IAttributeInstance attributeInstance : attributes)
        {
            this.snapshots.add(new S20PacketEntityProperties.Snapshot(attributeInstance.getAttribute().getAttributeUnlocalizedName(), attributeInstance.getBaseValue(), attributeInstance.getAllModifiers()));
        }
    }

    public void readPacketData(PacketBuffer buf) throws IOException
    {
        this.entityId = buf.readVarIntFromBuffer();
        int snapshotCount = buf.readInt();

        for (int snapshotIndex = 0; snapshotIndex < snapshotCount; ++snapshotIndex)
        {
            String attributeName = buf.readStringFromBuffer(64);
            double baseValue = buf.readDouble();
            int modifierCount = buf.readVarIntFromBuffer();
            List<AttributeModifier> modifiers = modifierCount > 0 ? Lists.<AttributeModifier>newArrayListWithCapacity(modifierCount) : Lists.<AttributeModifier>newArrayList();

            for (int modifierIndex = 0; modifierIndex < modifierCount; ++modifierIndex)
            {
                UUID uuid = buf.readUuid();
                modifiers.add(new AttributeModifier(uuid, "Unknown synced attribute modifier", buf.readDouble(), buf.readByte()));
            }

            this.snapshots.add(new S20PacketEntityProperties.Snapshot(attributeName, baseValue, modifiers));
        }
    }

    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeVarIntToBuffer(this.entityId);
        buf.writeInt(this.snapshots.size());

        for (S20PacketEntityProperties.Snapshot snapshot : this.snapshots)
        {
            buf.writeString(snapshot.getAttributeName());
            buf.writeDouble(snapshot.getBaseValue());
            buf.writeVarIntToBuffer(snapshot.getModifiers().size());

            for (AttributeModifier modifier : snapshot.getModifiers())
            {
                buf.writeUuid(modifier.getID());
                buf.writeDouble(modifier.getAmount());
                buf.writeByte(modifier.getOperation());
            }
        }
    }

    public void processPacket(INetHandlerPlayClient handler)
    {
        handler.handleEntityProperties(this);
    }

    public int getEntityId()
    {
        return this.entityId;
    }

    public List<S20PacketEntityProperties.Snapshot> getSnapshots()
    {
        return this.snapshots;
    }

    public class Snapshot
    {
        private final String attributeName;
        private final double baseValue;
        private final Collection<AttributeModifier> modifiers;

        public Snapshot(String attributeName, double baseValue, Collection<AttributeModifier> modifiers)
        {
            this.attributeName = attributeName;
            this.baseValue = baseValue;
            this.modifiers = modifiers;
        }

        public String getAttributeName()
        {
            return this.attributeName;
        }

        public double getBaseValue()
        {
            return this.baseValue;
        }

        public Collection<AttributeModifier> getModifiers()
        {
            return this.modifiers;
        }
    }
}
