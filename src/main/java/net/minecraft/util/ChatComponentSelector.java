package net.minecraft.util;

public class ChatComponentSelector extends ChatComponentStyle
{
    private final String selector;

    public ChatComponentSelector(String selectorIn)
    {
        this.selector = selectorIn;
    }

    public String getSelector()
    {
        return this.selector;
    }

    public String getUnformattedTextForChat()
    {
        return this.selector;
    }

    public ChatComponentSelector createCopy()
    {
        ChatComponentSelector copy = new ChatComponentSelector(this.selector);
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
        else if (!(other instanceof ChatComponentSelector))
        {
            return false;
        }
        else
        {
            ChatComponentSelector chatComponentSelector = (ChatComponentSelector)other;
            return this.selector.equals(chatComponentSelector.selector) && super.equals(other);
        }
    }

    public String toString()
    {
        return "SelectorComponent{pattern=\'" + this.selector + '\'' + ", siblings=" + this.siblings + ", style=" + this.getChatStyle() + '}';
    }
}
