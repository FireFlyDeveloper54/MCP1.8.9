package net.minecraft.util;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;

public abstract class ChatComponentStyle implements IChatComponent
{
    protected List<IChatComponent> siblings = Lists.<IChatComponent>newArrayList();
    private ChatStyle style;

    public IChatComponent appendSibling(IChatComponent component)
    {
        component.getChatStyle().setParentStyle(this.getChatStyle());
        this.siblings.add(component);
        return this;
    }

    public List<IChatComponent> getSiblings()
    {
        return this.siblings;
    }

    public IChatComponent appendText(String text)
    {
        return this.appendSibling(new ChatComponentText(text));
    }

    public IChatComponent setChatStyle(ChatStyle style)
    {
        this.style = style;

        for (IChatComponent childComponent : this.siblings)
        {
            childComponent.getChatStyle().setParentStyle(this.getChatStyle());
        }

        return this;
    }

    public ChatStyle getChatStyle()
    {
        if (this.style == null)
        {
            this.style = new ChatStyle();

            for (IChatComponent childComponent : this.siblings)
            {
                childComponent.getChatStyle().setParentStyle(this.style);
            }
        }

        return this.style;
    }

    public Iterator<IChatComponent> iterator()
    {
        return Iterators.<IChatComponent>concat(Iterators.<IChatComponent>forArray(new ChatComponentStyle[] {this}), createDeepCopyIterator(this.siblings));
    }

    public final String getUnformattedText()
    {
        StringBuilder stringBuilder = new StringBuilder();

        for (IChatComponent component : this)
        {
            stringBuilder.append(component.getUnformattedTextForChat());
        }

        return stringBuilder.toString();
    }

    public final String getFormattedText()
    {
        StringBuilder stringBuilder = new StringBuilder();

        for (IChatComponent component : this)
        {
            stringBuilder.append(component.getChatStyle().getFormattingCode());
            stringBuilder.append(component.getUnformattedTextForChat());
            stringBuilder.append((Object)EnumChatFormatting.RESET);
        }

        return stringBuilder.toString();
    }

    public static Iterator<IChatComponent> createDeepCopyIterator(Iterable<IChatComponent> components)
    {
        Iterator<IChatComponent> iterator = Iterators.concat(Iterators.transform(components.iterator(), new Function<IChatComponent, Iterator<IChatComponent>>()
        {
            public Iterator<IChatComponent> apply(IChatComponent component)
            {
                return component.iterator();
            }
        }));
        iterator = Iterators.transform(iterator, new Function<IChatComponent, IChatComponent>()
        {
            public IChatComponent apply(IChatComponent component)
            {
                IChatComponent copiedComponent = component.createCopy();
                copiedComponent.setChatStyle(copiedComponent.getChatStyle().createDeepCopy());
                return copiedComponent;
            }
        });
        return iterator;
    }

    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }
        else if (!(other instanceof ChatComponentStyle))
        {
            return false;
        }
        else
        {
            ChatComponentStyle chatComponentStyle = (ChatComponentStyle)other;
            return this.siblings.equals(chatComponentStyle.siblings) && this.getChatStyle().equals(chatComponentStyle.getChatStyle());
        }
    }

    public int hashCode()
    {
        return 31 * this.style.hashCode() + this.siblings.hashCode();
    }

    public String toString()
    {
        return "BaseComponent{style=" + this.style + ", siblings=" + this.siblings + '}';
    }
}
