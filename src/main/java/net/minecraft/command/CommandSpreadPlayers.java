package net.minecraft.command;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class CommandSpreadPlayers extends CommandBase
{
    public String getCommandName()
    {
        return "spreadplayers";
    }

    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    public String getCommandUsage(ICommandSender sender)
    {
        return "commands.spreadplayers.usage";
    }

    public void processCommand(ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length < 6)
        {
            throw new WrongUsageException("commands.spreadplayers.usage", new Object[0]);
        }
        else
        {
            int argIndex = 0;
            BlockPos senderPos = sender.getPosition();
            double centerX = parseDouble((double)senderPos.getX(), args[argIndex++], true);
            double centerZ = parseDouble((double)senderPos.getZ(), args[argIndex++], true);
            double spreadDistance = parseDouble(args[argIndex++], 0.0D);
            double maxRange = parseDouble(args[argIndex++], spreadDistance + 1.0D);
            boolean respectTeams = parseBoolean(args[argIndex++]);
            List<Entity> targets = Lists.<Entity>newArrayList();

            while (argIndex < args.length)
            {
                String targetArgument = args[argIndex++];

                if (PlayerSelector.hasArguments(targetArgument))
                {
                    List<Entity> selectedEntities = PlayerSelector.<Entity>matchEntities(sender, targetArgument, Entity.class);

                    if (selectedEntities.size() == 0)
                    {
                        throw new EntityNotFoundException();
                    }

                    targets.addAll(selectedEntities);
                }
                else
                {
                    EntityPlayer player = MinecraftServer.getServer().getConfigurationManager().getPlayerByUsername(targetArgument);

                    if (player == null)
                    {
                        throw new PlayerNotFoundException();
                    }

                    targets.add(player);
                }
            }

            sender.setCommandStat(CommandResultStats.Type.AFFECTED_ENTITIES, targets.size());

            if (targets.isEmpty())
            {
                throw new EntityNotFoundException();
            }
            else
            {
                sender.addChatMessage(new ChatComponentTranslation("commands.spreadplayers.spreading." + (respectTeams ? "teams" : "players"), new Object[] {Integer.valueOf(targets.size()), Double.valueOf(maxRange), Double.valueOf(centerX), Double.valueOf(centerZ), Double.valueOf(spreadDistance)}));
                this.spreadPlayers(sender, targets, new CommandSpreadPlayers.Position(centerX, centerZ), spreadDistance, maxRange, ((Entity)targets.get(0)).worldObj, respectTeams);
            }
        }
    }

    private void spreadPlayers(ICommandSender sender, List<Entity> entities, CommandSpreadPlayers.Position center, double spreadDistance, double maxRange, World worldIn, boolean respectTeams) throws CommandException
    {
        Random random = new Random();
        double minX = center.x - maxRange;
        double minZ = center.z - maxRange;
        double maxX = center.x + maxRange;
        double maxZ = center.z + maxRange;
        CommandSpreadPlayers.Position[] positions = this.createInitialPositions(random, respectTeams ? this.getNumberOfTeams(entities) : entities.size(), minX, minZ, maxX, maxZ);
        int iterations = this.spreadPositions(center, spreadDistance, worldIn, random, minX, minZ, maxX, maxZ, positions, respectTeams);
        double averageDistance = this.setPlayerPositions(entities, worldIn, positions, respectTeams);
        notifyOperators(sender, this, "commands.spreadplayers.success." + (respectTeams ? "teams" : "players"), new Object[] {Integer.valueOf(positions.length), Double.valueOf(center.x), Double.valueOf(center.z)});

        if (positions.length > 1)
        {
            sender.addChatMessage(new ChatComponentTranslation("commands.spreadplayers.info." + (respectTeams ? "teams" : "players"), new Object[] {String.format(Locale.ROOT, "%.2f", new Object[]{Double.valueOf(averageDistance)}), Integer.valueOf(iterations)}));
        }
    }

    private int getNumberOfTeams(List<Entity> entities)
    {
        Set<Team> teams = Sets.<Team>newHashSet();

        for (Entity entity : entities)
        {
            if (entity instanceof EntityPlayer)
            {
                teams.add(((EntityPlayer)entity).getTeam());
            }
            else
            {
                teams.add((Team)null);
            }
        }

        return teams.size();
    }

    private int spreadPositions(CommandSpreadPlayers.Position center, double spreadDistance, World worldIn, Random random, double minX, double minZ, double maxX, double maxZ, CommandSpreadPlayers.Position[] positions, boolean respectTeams) throws CommandException
    {
        boolean needsAnotherPass = true;
        double closestDistance = 3.4028234663852886E38D;
        int iteration;

        for (iteration = 0; iteration < 10000 && needsAnotherPass; ++iteration)
        {
            needsAnotherPass = false;
            closestDistance = 3.4028234663852886E38D;

            for (int currentIndex = 0; currentIndex < positions.length; ++currentIndex)
            {
                CommandSpreadPlayers.Position currentPosition = positions[currentIndex];
                int closeNeighborCount = 0;
                CommandSpreadPlayers.Position separationVector = new CommandSpreadPlayers.Position();

                for (int otherIndex = 0; otherIndex < positions.length; ++otherIndex)
                {
                    if (currentIndex != otherIndex)
                    {
                        CommandSpreadPlayers.Position otherPosition = positions[otherIndex];
                        double distance = currentPosition.getDistance(otherPosition);
                        closestDistance = Math.min(distance, closestDistance);

                        if (distance < spreadDistance)
                        {
                            ++closeNeighborCount;
                            separationVector.x += otherPosition.x - currentPosition.x;
                            separationVector.z += otherPosition.z - currentPosition.z;
                        }
                    }
                }

                if (closeNeighborCount > 0)
                {
                    separationVector.x /= (double)closeNeighborCount;
                    separationVector.z /= (double)closeNeighborCount;
                    double vectorLength = (double)separationVector.getLength();

                    if (vectorLength > 0.0D)
                    {
                        separationVector.normalize();
                        currentPosition.subtract(separationVector);
                    }
                    else
                    {
                        currentPosition.randomize(random, minX, minZ, maxX, maxZ);
                    }

                    needsAnotherPass = true;
                }

                if (currentPosition.clampWithinBounds(minX, minZ, maxX, maxZ))
                {
                    needsAnotherPass = true;
                }
            }

            if (!needsAnotherPass)
            {
                for (CommandSpreadPlayers.Position position : positions)
                {
                    if (!position.isSafe(worldIn))
                    {
                        position.randomize(random, minX, minZ, maxX, maxZ);
                        needsAnotherPass = true;
                    }
                }
            }
        }

        if (iteration >= 10000)
        {
            throw new CommandException("commands.spreadplayers.failure." + (respectTeams ? "teams" : "players"), new Object[] {Integer.valueOf(positions.length), Double.valueOf(center.x), Double.valueOf(center.z), String.format(Locale.ROOT, "%.2f", new Object[]{Double.valueOf(closestDistance)})});
        }
        else
        {
            return iteration;
        }
    }

    private double setPlayerPositions(List<Entity> entities, World worldIn, CommandSpreadPlayers.Position[] positions, boolean respectTeams)
    {
        double totalClosestDistance = 0.0D;
        int positionIndex = 0;
        Map<Team, CommandSpreadPlayers.Position> teamPositions = Maps.<Team, CommandSpreadPlayers.Position>newHashMap();

        for (int entityIndex = 0; entityIndex < entities.size(); ++entityIndex)
        {
            Entity entity = (Entity)entities.get(entityIndex);
            CommandSpreadPlayers.Position assignedPosition;

            if (respectTeams)
            {
                Team team = entity instanceof EntityPlayer ? ((EntityPlayer)entity).getTeam() : null;

                if (!teamPositions.containsKey(team))
                {
                    teamPositions.put(team, positions[positionIndex++]);
                }

                assignedPosition = (CommandSpreadPlayers.Position)teamPositions.get(team);
            }
            else
            {
                assignedPosition = positions[positionIndex++];
            }

            entity.setPositionAndUpdate((double)((float)MathHelper.floor_double(assignedPosition.x) + 0.5F), (double)assignedPosition.getSpawnY(worldIn), (double)MathHelper.floor_double(assignedPosition.z) + 0.5D);
            double closestDistance = Double.MAX_VALUE;

            for (int otherIndex = 0; otherIndex < positions.length; ++otherIndex)
            {
                if (assignedPosition != positions[otherIndex])
                {
                    double distance = assignedPosition.getDistance(positions[otherIndex]);
                    closestDistance = Math.min(distance, closestDistance);
                }
            }

            totalClosestDistance += closestDistance;
        }

        totalClosestDistance = totalClosestDistance / (double)entities.size();
        return totalClosestDistance;
    }

    private CommandSpreadPlayers.Position[] createInitialPositions(Random random, int count, double minX, double minZ, double maxX, double maxZ)
    {
        CommandSpreadPlayers.Position[] positions = new CommandSpreadPlayers.Position[count];

        for (int index = 0; index < positions.length; ++index)
        {
            CommandSpreadPlayers.Position position = new CommandSpreadPlayers.Position();
            position.randomize(random, minX, minZ, maxX, maxZ);
            positions[index] = position;
        }

        return positions;
    }

    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos)
    {
        return args.length >= 1 && args.length <= 2 ? getTabCompletionCoordinateXZ(args, 0, pos) : null;
    }

    static class Position
    {
        double x;
        double z;

        Position()
        {
        }

        Position(double x, double z)
        {
            this.x = x;
            this.z = z;
        }

        double getDistance(CommandSpreadPlayers.Position other)
        {
            double deltaX = this.x - other.x;
            double deltaZ = this.z - other.z;
            return Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        }

        void normalize()
        {
            double length = (double)this.getLength();
            this.x /= length;
            this.z /= length;
        }

        float getLength()
        {
            return MathHelper.sqrt_double(this.x * this.x + this.z * this.z);
        }

        public void subtract(CommandSpreadPlayers.Position other)
        {
            this.x -= other.x;
            this.z -= other.z;
        }

        public boolean clampWithinBounds(double minX, double minZ, double maxX, double maxZ)
        {
            boolean wasClamped = false;

            if (this.x < minX)
            {
                this.x = minX;
                wasClamped = true;
            }
            else if (this.x > maxX)
            {
                this.x = maxX;
                wasClamped = true;
            }

            if (this.z < minZ)
            {
                this.z = minZ;
                wasClamped = true;
            }
            else if (this.z > maxZ)
            {
                this.z = maxZ;
                wasClamped = true;
            }

            return wasClamped;
        }

        public int getSpawnY(World worldIn)
        {
            BlockPos blockPos = new BlockPos(this.x, 256.0D, this.z);

            while (blockPos.getY() > 0)
            {
                blockPos = blockPos.down();

                if (worldIn.getBlockState(blockPos).getBlock().getMaterial() != Material.air)
                {
                    return blockPos.getY() + 1;
                }
            }

            return 257;
        }

        public boolean isSafe(World worldIn)
        {
            BlockPos blockPos = new BlockPos(this.x, 256.0D, this.z);

            while (blockPos.getY() > 0)
            {
                blockPos = blockPos.down();
                Material material = worldIn.getBlockState(blockPos).getBlock().getMaterial();

                if (material != Material.air)
                {
                    return !material.isLiquid() && material != Material.fire;
                }
            }

            return false;
        }

        public void randomize(Random random, double minX, double minZ, double maxX, double maxZ)
        {
            this.x = MathHelper.getRandomDoubleInRange(random, minX, maxX);
            this.z = MathHelper.getRandomDoubleInRange(random, minZ, maxZ);
        }
    }
}
