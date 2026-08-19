package net.minecraft.client.gui;

import net.minecraft.util.IChatComponent;

public class ChatLine
{
    private final int updateCounterCreated;
    private final IChatComponent lineString;
    private final int chatLineID;

    public ChatLine(int updateCounterCreatedIn, IChatComponent lineStringIn, int chatLineIdIn)
    {
        this.lineString = lineStringIn;
        this.updateCounterCreated = updateCounterCreatedIn;
        this.chatLineID = chatLineIdIn;
    }

    public IChatComponent getChatComponent()
    {
        return this.lineString;
    }

    public int getUpdatedCounter()
    {
        return this.updateCounterCreated;
    }

    public int getChatLineID()
    {
        return this.chatLineID;
    }
}
