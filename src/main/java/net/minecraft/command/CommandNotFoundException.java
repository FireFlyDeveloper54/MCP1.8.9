package net.minecraft.command;

public class CommandNotFoundException extends CommandException
{
    public CommandNotFoundException()
    {
        this("commands.generic.notFound", new Object[0]);
    }

    public CommandNotFoundException(String message, Object... formatArgs)
    {
        super(message, formatArgs);
    }
}
