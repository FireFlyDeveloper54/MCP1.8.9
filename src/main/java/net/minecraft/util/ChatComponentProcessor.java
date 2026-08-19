package net.minecraft.util;

import java.util.List;
import net.minecraft.command.CommandException;
import net.minecraft.command.EntityNotFoundException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerSelector;
import net.minecraft.entity.Entity;

public class ChatComponentProcessor
{
    public static IChatComponent processComponent(ICommandSender commandSender, IChatComponent component, Entity entityIn) throws CommandException
    {
        IChatComponent processedComponent = null;

        if (component instanceof ChatComponentScore)
        {
            ChatComponentScore scoreComponent = (ChatComponentScore)component;
            String scoreName = scoreComponent.getName();

            if (PlayerSelector.hasArguments(scoreName))
            {
                List<Entity> matchingEntities = PlayerSelector.<Entity>matchEntities(commandSender, scoreName, Entity.class);

                if (matchingEntities.size() != 1)
                {
                    throw new EntityNotFoundException();
                }

                scoreName = matchingEntities.get(0).getName();
            }

            processedComponent = entityIn != null && scoreName.equals("*") ? new ChatComponentScore(entityIn.getName(), scoreComponent.getObjective()) : new ChatComponentScore(scoreName, scoreComponent.getObjective());
            ((ChatComponentScore)processedComponent).setValue(scoreComponent.getUnformattedTextForChat());
        }
        else if (component instanceof ChatComponentSelector)
        {
            String selector = ((ChatComponentSelector)component).getSelector();
            processedComponent = PlayerSelector.matchEntitiesToChatComponent(commandSender, selector);

            if (processedComponent == null)
            {
                processedComponent = new ChatComponentText("");
            }
        }
        else if (component instanceof ChatComponentText)
        {
            processedComponent = new ChatComponentText(((ChatComponentText)component).getChatComponentText_TextValue());
        }
        else
        {
            if (!(component instanceof ChatComponentTranslation))
            {
                return component;
            }

            Object[] formatArgs = ((ChatComponentTranslation)component).getFormatArgs();

            for (int argIndex = 0; argIndex < formatArgs.length; ++argIndex)
            {
                Object formatArg = formatArgs[argIndex];

                if (formatArg instanceof IChatComponent)
                {
                    formatArgs[argIndex] = processComponent(commandSender, (IChatComponent)formatArg, entityIn);
                }
            }

            processedComponent = new ChatComponentTranslation(((ChatComponentTranslation)component).getKey(), formatArgs);
        }

        ChatStyle chatStyle = component.getChatStyle();

        if (chatStyle != null)
        {
            processedComponent.setChatStyle(chatStyle.createShallowCopy());
        }

        for (IChatComponent sibling : component.getSiblings())
        {
            processedComponent.appendSibling(processComponent(commandSender, sibling, entityIn));
        }

        return processedComponent;
    }
}
