package net.minecraft.command;

import java.util.List;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class CommandParticle extends CommandBase
{
    public String getCommandName()
    {
        return "particle";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    public String getCommandUsage(ICommandSender sender)
    {
        return "commands.particle.usage";
    }

    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length < 8)
        {
            throw new WrongUsageException("commands.particle.usage", new Object[0]);
        }
        else
        {
            boolean foundParticleType = false;
            EnumParticleTypes particleType = null;

            for (EnumParticleTypes candidateParticleType : EnumParticleTypes.VALUES)
            {
                if (candidateParticleType.hasArguments())
                {
                    if (args[0].startsWith(candidateParticleType.getParticleName()))
                    {
                        foundParticleType = true;
                        particleType = candidateParticleType;
                        break;
                    }
                }
                else if (args[0].equals(candidateParticleType.getParticleName()))
                {
                    foundParticleType = true;
                    particleType = candidateParticleType;
                    break;
                }
            }

            if (!foundParticleType)
            {
                throw new CommandException("commands.particle.notFound", new Object[] {args[0]});
            }
            else
            {
                String particleName = args[0];
                Vec3 senderPosition = sender.getPositionVector();
                double x = (double)((float)parseDouble(senderPosition.xCoord, args[1], true));
                double y = (double)((float)parseDouble(senderPosition.yCoord, args[2], true));
                double z = (double)((float)parseDouble(senderPosition.zCoord, args[3], true));
                double xOffset = (double)((float)parseDouble(args[4]));
                double yOffset = (double)((float)parseDouble(args[5]));
                double zOffset = (double)((float)parseDouble(args[6]));
                double speed = (double)((float)parseDouble(args[7]));
                int count = 0;

                if (args.length > 8)
                {
                    count = parseInt(args[8], 0);
                }

                boolean force = false;

                if (args.length > 9 && "force".equals(args[9]))
                {
                    force = true;
                }

                World world = sender.getEntityWorld();

                if (world instanceof WorldServer)
                {
                    WorldServer worldServer = (WorldServer)world;
                    int[] particleArguments = new int[particleType.getArgumentCount()];

                    if (particleType.hasArguments())
                    {
                        String[] particleNameParts = args[0].split("_", 3);

                        for (int argumentIndex = 1; argumentIndex < particleNameParts.length; ++argumentIndex)
                        {
                            try
                            {
                                particleArguments[argumentIndex - 1] = Integer.parseInt(particleNameParts[argumentIndex]);
                            }
                            catch (NumberFormatException caughtNumberFormatException)
                            {
                                throw new CommandException("commands.particle.notFound", new Object[] {args[0]});
                            }
                        }
                    }

                    worldServer.spawnParticle(particleType, force, x, y, z, count, xOffset, yOffset, zOffset, speed, particleArguments);
                    notifyOperators(sender, this, "commands.particle.success", new Object[] {particleName, Integer.valueOf(Math.max(count, 1))});
                }
            }
        }
    }

    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos)
    {
        return args.length == 1 ? getListOfStringsMatchingLastWord(args, EnumParticleTypes.getParticleNames()) : (args.length > 1 && args.length <= 4 ? getTabCompletionCoordinate(args, 1, pos) : (args.length == 10 ? getListOfStringsMatchingLastWord(args, new String[] {"normal", "force"}): null));
    }
}
