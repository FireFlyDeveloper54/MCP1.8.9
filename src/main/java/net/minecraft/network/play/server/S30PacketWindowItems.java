package net.minecraft.network.play.server;

import java.io.IOException;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

public class S30PacketWindowItems implements Packet<INetHandlerPlayClient>
{
    private int windowId;
    private ItemStack[] itemStacks;

    public S30PacketWindowItems()
    {
    }

    public S30PacketWindowItems(int windowIdIn, List<ItemStack> itemStacksIn)
    {
        this.windowId = windowIdIn;
        this.itemStacks = new ItemStack[itemStacksIn.size()];

        for (int slotIndex = 0; slotIndex < this.itemStacks.length; ++slotIndex)
        {
            ItemStack itemStack = itemStacksIn.get(slotIndex);
            this.itemStacks[slotIndex] = itemStack == null ? null : itemStack.copy();
        }
    }

    public void readPacketData(PacketBuffer buf) throws IOException
    {
        this.windowId = buf.readUnsignedByte();
        int itemStackCount = buf.readShort();
        this.itemStacks = new ItemStack[itemStackCount];

        for (int slotIndex = 0; slotIndex < itemStackCount; ++slotIndex)
        {
            this.itemStacks[slotIndex] = buf.readItemStackFromBuffer();
        }
    }

    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeByte(this.windowId);
        buf.writeShort(this.itemStacks.length);

        for (ItemStack itemStack : this.itemStacks)
        {
            buf.writeItemStackToBuffer(itemStack);
        }
    }

    public void processPacket(INetHandlerPlayClient handler)
    {
        handler.handleWindowItems(this);
    }

    public int getWindowId()
    {
        return this.windowId;
    }

    public ItemStack[] getItemStacks()
    {
        return this.itemStacks;
    }
}
