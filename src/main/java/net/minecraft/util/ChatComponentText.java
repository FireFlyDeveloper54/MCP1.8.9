package net.minecraft.util;

public class ChatComponentText extends ChatComponentStyle
{
    private final String text;

    public ChatComponentText(String msg)
    {
        this.text = msg;
    }

    public String getChatComponentText_TextValue()
    {
        return this.text;
    }

    public String getUnformattedTextForChat()
    {
        return this.text;
    }

    public ChatComponentText createCopy()
    {
        ChatComponentText copy = new ChatComponentText(this.text);
        copy.setChatStyle(this.getChatStyle().createShallowCopy());

        for (IChatComponent childComponent : this.getSiblings())
        {
            copy.appendSibling(childComponent.createCopy());
        }

        return copy;
    }

    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }
        else if (!(other instanceof ChatComponentText))
        {
            return false;
        }
        else
        {
            ChatComponentText chatComponentText = (ChatComponentText)other;
            return this.text.equals(chatComponentText.getChatComponentText_TextValue()) && super.equals(other);
        }
    }

    public String toString()
    {
        return "TextComponent{text=\'" + this.text + '\'' + ", siblings=" + this.siblings + ", style=" + this.getChatStyle() + '}';
    }
}
