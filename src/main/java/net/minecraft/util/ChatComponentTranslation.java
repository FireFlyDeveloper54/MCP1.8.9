package net.minecraft.util;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.IllegalFormatException;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatComponentTranslation extends ChatComponentStyle
{
    private final String key;
    private final Object[] formatArgs;
    private final Object syncLock = new Object();
    private long lastTranslationUpdateTimeInMilliseconds = -1L;
    List<IChatComponent> children = Lists.<IChatComponent>newArrayList();
    public static final Pattern stringVariablePattern = Pattern.compile("%(?:(\\d+)\\$)?([A-Za-z%]|$)");

    public ChatComponentTranslation(String translationKey, Object... args)
    {
        this.key = translationKey;
        this.formatArgs = args;

        for (Object formatArgument : args)
        {
            if (formatArgument instanceof IChatComponent)
            {
                ((IChatComponent)formatArgument).getChatStyle().setParentStyle(this.getChatStyle());
            }
        }
    }

    synchronized void ensureInitialized()
    {
        synchronized (this.syncLock)
        {
            long translationUpdateTime = StatCollector.getLastTranslationUpdateTimeInMilliseconds();

            if (translationUpdateTime == this.lastTranslationUpdateTimeInMilliseconds)
            {
                return;
            }

            this.lastTranslationUpdateTimeInMilliseconds = translationUpdateTime;
            this.children.clear();
        }

        try
        {
            this.initializeFromFormat(StatCollector.translateToLocal(this.key));
        }
        catch (ChatComponentTranslationFormatException chatComponentTranslationFormatException)
        {
            this.children.clear();

            try
            {
                this.initializeFromFormat(StatCollector.translateToFallback(this.key));
            }
            catch (ChatComponentTranslationFormatException caughtChatComponentTranslationFormatException)
            {
                throw chatComponentTranslationFormatException;
            }
        }
    }

    protected void initializeFromFormat(String format)
    {
        Matcher matcher = stringVariablePattern.matcher(format);
        int nextArgumentIndex = 0;
        int searchStart = 0;

        try
        {
            int matchEnd;

            for (; matcher.find(searchStart); searchStart = matchEnd)
            {
                int matchStart = matcher.start();
                matchEnd = matcher.end();

                if (matchStart > searchStart)
                {
                    ChatComponentText literalComponent = new ChatComponentText(String.format(format.substring(searchStart, matchStart), new Object[0]));
                    literalComponent.getChatStyle().setParentStyle(this.getChatStyle());
                    this.children.add(literalComponent);
                }

                String conversion = matcher.group(2);
                String formatToken = format.substring(matchStart, matchEnd);

                if ("%".equals(conversion) && "%%".equals(formatToken))
                {
                    ChatComponentText percentComponent = new ChatComponentText("%");
                    percentComponent.getChatStyle().setParentStyle(this.getChatStyle());
                    this.children.add(percentComponent);
                }
                else
                {
                    if (!"s".equals(conversion))
                    {
                        throw new ChatComponentTranslationFormatException(this, "Unsupported format: \'" + formatToken + "\'");
                    }

                    String explicitIndex = matcher.group(1);
                    int argumentIndex = explicitIndex != null ? Integer.parseInt(explicitIndex) - 1 : nextArgumentIndex++;

                    if (argumentIndex < this.formatArgs.length)
                    {
                        this.children.add(this.getFormatArgumentAsComponent(argumentIndex));
                    }
                }
            }

            if (searchStart < format.length())
            {
                ChatComponentText trailingComponent = new ChatComponentText(String.format(format.substring(searchStart), new Object[0]));
                trailingComponent.getChatStyle().setParentStyle(this.getChatStyle());
                this.children.add(trailingComponent);
            }
        }
        catch (IllegalFormatException formatException)
        {
            throw new ChatComponentTranslationFormatException(this, formatException);
        }
    }

    private IChatComponent getFormatArgumentAsComponent(int index)
    {
        if (index >= this.formatArgs.length)
        {
            throw new ChatComponentTranslationFormatException(this, index);
        }
        else
        {
            Object formatArgument = this.formatArgs[index];
            IChatComponent component;

            if (formatArgument instanceof IChatComponent)
            {
                component = (IChatComponent)formatArgument;
            }
            else
            {
                component = new ChatComponentText(formatArgument == null ? "null" : formatArgument.toString());
                component.getChatStyle().setParentStyle(this.getChatStyle());
            }

            return component;
        }
    }

    public IChatComponent setChatStyle(ChatStyle style)
    {
        super.setChatStyle(style);

        for (Object formatArgument : this.formatArgs)
        {
            if (formatArgument instanceof IChatComponent)
            {
                ((IChatComponent)formatArgument).getChatStyle().setParentStyle(this.getChatStyle());
            }
        }

        if (this.lastTranslationUpdateTimeInMilliseconds > -1L)
        {
            for (IChatComponent childComponent : this.children)
            {
                childComponent.getChatStyle().setParentStyle(style);
            }
        }

        return this;
    }

    public Iterator<IChatComponent> iterator()
    {
        this.ensureInitialized();
        return Iterators.<IChatComponent>concat(createDeepCopyIterator(this.children), createDeepCopyIterator(this.siblings));
    }

    public String getUnformattedTextForChat()
    {
        this.ensureInitialized();
        StringBuilder stringBuilder = new StringBuilder();

        for (IChatComponent childComponent : this.children)
        {
            stringBuilder.append(childComponent.getUnformattedTextForChat());
        }

        return stringBuilder.toString();
    }

    public ChatComponentTranslation createCopy()
    {
        Object[] copiedArgs = new Object[this.formatArgs.length];

        for (int index = 0; index < this.formatArgs.length; ++index)
        {
            if (this.formatArgs[index] instanceof IChatComponent)
            {
                copiedArgs[index] = ((IChatComponent)this.formatArgs[index]).createCopy();
            }
            else
            {
                copiedArgs[index] = this.formatArgs[index];
            }
        }

        ChatComponentTranslation copy = new ChatComponentTranslation(this.key, copiedArgs);
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
        else if (!(other instanceof ChatComponentTranslation))
        {
            return false;
        }
        else
        {
            ChatComponentTranslation chatComponentTranslation = (ChatComponentTranslation)other;
            return Arrays.equals(this.formatArgs, chatComponentTranslation.formatArgs) && this.key.equals(chatComponentTranslation.key) && super.equals(other);
        }
    }

    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + this.key.hashCode();
        result = 31 * result + Arrays.hashCode(this.formatArgs);
        return result;
    }

    public String toString()
    {
        return "TranslatableComponent{key=\'" + this.key + '\'' + ", args=" + Arrays.toString(this.formatArgs) + ", siblings=" + this.siblings + ", style=" + this.getChatStyle() + '}';
    }

    public String getKey()
    {
        return this.key;
    }

    public Object[] getFormatArgs()
    {
        return this.formatArgs;
    }
}
