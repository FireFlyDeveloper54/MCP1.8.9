package net.minecraft.command;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;

public class PlayerSelector
{
    private static final Pattern tokenPattern = Pattern.compile("^@([pare])(?:\\[([\\w=,!-]*)\\])?$");
    private static final Pattern intListPattern = Pattern.compile("\\G([-!]?[\\w-]*)(?:$|,)");
    private static final Pattern keyValueListPattern = Pattern.compile("\\G(\\w+)=([-!]?[\\w-]*)(?:$|,)");
    private static final Set<String> WORLD_BINDING_ARGS = Sets.newHashSet(new String[] {"x", "y", "z", "dx", "dy", "dz", "rm", "r"});

    public static EntityPlayerMP matchOnePlayer(ICommandSender sender, String token)
    {
        return (EntityPlayerMP)matchOneEntity(sender, token, EntityPlayerMP.class);
    }

    public static <T extends Entity> T matchOneEntity(ICommandSender sender, String token, Class <? extends T > targetClass)
    {
        List<T> list = matchEntities(sender, token, targetClass);
        return (T)(list.size() == 1 ? (Entity)list.get(0) : null);
    }

    public static IChatComponent matchEntitiesToChatComponent(ICommandSender sender, String token)
    {
        List<Entity> matchedEntities = matchEntities(sender, token, Entity.class);

        if (matchedEntities.isEmpty())
        {
            return null;
        }
        else
        {
            List<IChatComponent> displayNames = Lists.<IChatComponent>newArrayList();

            for (Entity entity : matchedEntities)
            {
                displayNames.add(entity.getDisplayName());
            }

            return CommandBase.join(displayNames);
        }
    }

    public static <T extends Entity> List<T> matchEntities(ICommandSender sender, String token, Class <? extends T > targetClass)
    {
        Matcher matcher = tokenPattern.matcher(token);

        if (matcher.matches() && sender.canCommandSenderUseCommand(1, "@"))
        {
            Map<String, String> arguments = getArgumentMap(matcher.group(2));

            if (!isEntityTypeValid(sender, arguments))
            {
                return Collections.<T>emptyList();
            }
            else
            {
                String selectorType = matcher.group(1);
                BlockPos selectorPos = getBlockPosFromArguments(arguments, sender.getPosition());
                List<World> worlds = getWorlds(sender, arguments);
                List<T> matchedEntities = Lists.<T>newArrayList();

                for (World world : worlds)
                {
                    if (world != null)
                    {
                        List<Predicate<Entity>> predicates = Lists.<Predicate<Entity>>newArrayList();
                        predicates.addAll(getEntityTypePredicates(arguments, selectorType));
                        predicates.addAll(getXpLevelPredicates(arguments));
                        predicates.addAll(getGamemodePredicates(arguments));
                        predicates.addAll(getTeamPredicates(arguments));
                        predicates.addAll(getScorePredicates(arguments));
                        predicates.addAll(getNamePredicates(arguments));
                        predicates.addAll(getDistancePredicates(arguments, selectorPos));
                        predicates.addAll(getRotationsPredicates(arguments));
                        matchedEntities.addAll(filterResults(arguments, targetClass, predicates, selectorType, world, selectorPos));
                    }
                }

                return sortAndLimitResults(matchedEntities, arguments, sender, targetClass, selectorType, selectorPos);
            }
        }
        else
        {
            return Collections.<T>emptyList();
        }
    }

    private static List<World> getWorlds(ICommandSender sender, Map<String, String> argumentMap)
    {
        List<World> worlds = Lists.<World>newArrayList();

        if (isWorldLimited(argumentMap))
        {
            worlds.add(sender.getEntityWorld());
        }
        else
        {
            Collections.addAll(worlds, MinecraftServer.getServer().worldServers);
        }

        return worlds;
    }

    private static <T extends Entity> boolean isEntityTypeValid(ICommandSender commandSender, Map<String, String> params)
    {
        String entityType = getArgument(params, "type");
        entityType = entityType != null && entityType.startsWith("!") ? entityType.substring(1) : entityType;

        if (entityType != null && !EntityList.isStringValidEntityName(entityType))
        {
            ChatComponentTranslation chatComponentTranslation = new ChatComponentTranslation("commands.generic.entity.invalidType", new Object[] {entityType});
            chatComponentTranslation.getChatStyle().setColor(EnumChatFormatting.RED);
            commandSender.addChatMessage(chatComponentTranslation);
            return false;
        }
        else
        {
            return true;
        }
    }

    private static List<Predicate<Entity>> getEntityTypePredicates(Map<String, String> params, String selectorType)
    {
        List<Predicate<Entity>> predicates = Lists.<Predicate<Entity>>newArrayList();
        String entityType = getArgument(params, "type");
        final boolean isInverted = entityType != null && entityType.startsWith("!");

        if (isInverted)
        {
            entityType = entityType.substring(1);
        }

        boolean restrictToPlayers = !selectorType.equals("e");
        boolean randomSelectorWithExplicitType = selectorType.equals("r") && entityType != null;

        if ((entityType == null || !selectorType.equals("e")) && !randomSelectorWithExplicitType)
        {
            if (restrictToPlayers)
            {
                predicates.add(new Predicate<Entity>()
                {
                    public boolean apply(Entity entity)
                    {
                        return entity instanceof EntityPlayer;
                    }
                });
            }
        }
        else
        {
            final String requestedEntityType = entityType;
            predicates.add(new Predicate<Entity>()
            {
                public boolean apply(Entity entity)
                {
                    return EntityList.isStringEntityName(entity, requestedEntityType) != isInverted;
                }
            });
        }

        return predicates;
    }

    private static List<Predicate<Entity>> getXpLevelPredicates(Map<String, String> params)
    {
        List<Predicate<Entity>> list = Lists.<Predicate<Entity>>newArrayList();
        final int minLevel = parseIntWithDefault(params, "lm", -1);
        final int maxLevel = parseIntWithDefault(params, "l", -1);

        if (minLevel > -1 || maxLevel > -1)
        {
            list.add(new Predicate<Entity>()
            {
                public boolean apply(Entity entity)
                {
                    if (!(entity instanceof EntityPlayerMP))
                    {
                        return false;
                    }
                    else
                    {
                        EntityPlayerMP entityPlayerMP = (EntityPlayerMP)entity;
                        return (minLevel <= -1 || entityPlayerMP.experienceLevel >= minLevel) && (maxLevel <= -1 || entityPlayerMP.experienceLevel <= maxLevel);
                    }
                }
            });
        }

        return list;
    }

    private static List<Predicate<Entity>> getGamemodePredicates(Map<String, String> params)
    {
        List<Predicate<Entity>> list = Lists.<Predicate<Entity>>newArrayList();
        final int gameModeId = parseIntWithDefault(params, "m", WorldSettings.GameType.NOT_SET.getID());

        if (gameModeId != WorldSettings.GameType.NOT_SET.getID())
        {
            list.add(new Predicate<Entity>()
            {
                public boolean apply(Entity entity)
                {
                    if (!(entity instanceof EntityPlayerMP))
                    {
                        return false;
                    }
                    else
                    {
                        EntityPlayerMP entityPlayerMP = (EntityPlayerMP)entity;
                        return entityPlayerMP.theItemInWorldManager.getGameType().getID() == gameModeId;
                    }
                }
            });
        }

        return list;
    }

    private static List<Predicate<Entity>> getTeamPredicates(Map<String, String> params)
    {
        List<Predicate<Entity>> list = Lists.<Predicate<Entity>>newArrayList();
        String teamName = getArgument(params, "team");
        final boolean isInverted = teamName != null && teamName.startsWith("!");

        if (isInverted)
        {
            teamName = teamName.substring(1);
        }

        if (teamName != null)
        {
            final String targetTeamName = teamName;
            list.add(new Predicate<Entity>()
            {
                public boolean apply(Entity entity)
                {
                    if (!(entity instanceof EntityLivingBase))
                    {
                        return false;
                    }
                    else
                    {
                        EntityLivingBase entityLivingBase = (EntityLivingBase)entity;
                        Team team = entityLivingBase.getTeam();
                        String currentTeamName = team == null ? "" : team.getRegisteredName();
                        return currentTeamName.equals(targetTeamName) != isInverted;
                    }
                }
            });
        }

        return list;
    }

    private static List<Predicate<Entity>> getScorePredicates(Map<String, String> params)
    {
        List<Predicate<Entity>> predicates = Lists.<Predicate<Entity>>newArrayList();
        final Map<String, Integer> scoreFilters = getScoreMap(params);

        if (scoreFilters != null && scoreFilters.size() > 0)
        {
            predicates.add(new Predicate<Entity>()
            {
                public boolean apply(Entity entity)
                {
                    Scoreboard scoreboard = MinecraftServer.getServer().worldServerForDimension(0).getScoreboard();

                    for (Entry<String, Integer> entry : scoreFilters.entrySet())
                    {
                        String objectiveName = entry.getKey();
                        boolean requiresMinimum = false;

                        if (objectiveName.endsWith("_min") && objectiveName.length() > 4)
                        {
                            requiresMinimum = true;
                            objectiveName = objectiveName.substring(0, objectiveName.length() - 4);
                        }

                        ScoreObjective objective = scoreboard.getObjective(objectiveName);

                        if (objective == null)
                        {
                            return false;
                        }

                        String scoreHolderName = entity instanceof EntityPlayerMP ? entity.getName() : entity.getUniqueID().toString();

                        if (!scoreboard.entityHasObjective(scoreHolderName, objective))
                        {
                            return false;
                        }

                        Score score = scoreboard.getValueFromObjective(scoreHolderName, objective);
                        int scoreValue = score.getScorePoints();

                        if (scoreValue < entry.getValue().intValue() && requiresMinimum)
                        {
                            return false;
                        }

                        if (scoreValue > entry.getValue().intValue() && !requiresMinimum)
                        {
                            return false;
                        }
                    }

                    return true;
                }
            });
        }

        return predicates;
    }

    private static List<Predicate<Entity>> getNamePredicates(Map<String, String> params)
    {
        List<Predicate<Entity>> list = Lists.<Predicate<Entity>>newArrayList();
        String name = getArgument(params, "name");
        final boolean isInverted = name != null && name.startsWith("!");

        if (isInverted)
        {
            name = name.substring(1);
        }

        if (name != null)
        {
            final String targetName = name;
            list.add(new Predicate<Entity>()
            {
                public boolean apply(Entity entity)
                {
                    return entity.getName().equals(targetName) != isInverted;
                }
            });
        }

        return list;
    }

    private static List<Predicate<Entity>> getDistancePredicates(Map<String, String> params, final BlockPos position)
    {
        List<Predicate<Entity>> list = Lists.<Predicate<Entity>>newArrayList();
        final int minRadius = parseIntWithDefault(params, "rm", -1);
        final int maxRadius = parseIntWithDefault(params, "r", -1);

        if (position != null && (minRadius >= 0 || maxRadius >= 0))
        {
            final int minRadiusSq = minRadius * minRadius;
            final int maxRadiusSq = maxRadius * maxRadius;
            list.add(new Predicate<Entity>()
            {
                public boolean apply(Entity entity)
                {
                    int distanceSq = (int)entity.getDistanceSqToCenter(position);
                    return (minRadius < 0 || distanceSq >= minRadiusSq) && (maxRadius < 0 || distanceSq <= maxRadiusSq);
                }
            });
        }

        return list;
    }

    private static List<Predicate<Entity>> getRotationsPredicates(Map<String, String> params)
    {
        List<Predicate<Entity>> list = Lists.<Predicate<Entity>>newArrayList();

        if (params.containsKey("rym") || params.containsKey("ry"))
        {
            final int minYaw = normalizeAngle(parseIntWithDefault(params, "rym", 0));
            final int maxYaw = normalizeAngle(parseIntWithDefault(params, "ry", 359));
            list.add(new Predicate<Entity>()
            {
                public boolean apply(Entity entity)
                {
                    int yaw = PlayerSelector.normalizeAngle((int)Math.floor((double)entity.rotationYaw));
                    return minYaw > maxYaw ? yaw >= minYaw || yaw <= maxYaw : yaw >= minYaw && yaw <= maxYaw;
                }
            });
        }

        if (params.containsKey("rxm") || params.containsKey("rx"))
        {
            final int minPitch = normalizeAngle(parseIntWithDefault(params, "rxm", 0));
            final int maxPitch = normalizeAngle(parseIntWithDefault(params, "rx", 359));
            list.add(new Predicate<Entity>()
            {
                public boolean apply(Entity entity)
                {
                    int pitch = PlayerSelector.normalizeAngle((int)Math.floor((double)entity.rotationPitch));
                    return minPitch > maxPitch ? pitch >= minPitch || pitch <= maxPitch : pitch >= minPitch && pitch <= maxPitch;
                }
            });
        }

        return list;
    }

    private static <T extends Entity> List<T> filterResults(Map<String, String> params, Class <? extends T > entityClass, List<Predicate<Entity>> inputList, String type, World worldIn, BlockPos position)
    {
        List<T> matches = Lists.<T>newArrayList();
        String entityType = getArgument(params, "type");
        entityType = entityType != null && entityType.startsWith("!") ? entityType.substring(1) : entityType;
        boolean selectorTargetsPlayers = !type.equals("e");
        boolean randomSelectorWithExplicitType = type.equals("r") && entityType != null;
        int deltaX = parseIntWithDefault(params, "dx", 0);
        int deltaY = parseIntWithDefault(params, "dy", 0);
        int deltaZ = parseIntWithDefault(params, "dz", 0);
        int radius = parseIntWithDefault(params, "r", -1);
        Predicate<Entity> combinedPredicate = Predicates.and(inputList);
        Predicate<Entity> selectablePredicate = Predicates.<Entity> and (EntitySelectors.selectAnything, combinedPredicate);

        if (position != null)
        {
            int playerCount = worldIn.playerEntities.size();
            int entityCount = worldIn.loadedEntityList.size();
            boolean shouldUsePlayerList = playerCount < entityCount / 16;

            if (!params.containsKey("dx") && !params.containsKey("dy") && !params.containsKey("dz"))
            {
                if (radius >= 0)
                {
                    AxisAlignedBB radiusBox = new AxisAlignedBB((double)(position.getX() - radius), (double)(position.getY() - radius), (double)(position.getZ() - radius), (double)(position.getX() + radius + 1), (double)(position.getY() + radius + 1), (double)(position.getZ() + radius + 1));

                    if (selectorTargetsPlayers && shouldUsePlayerList && !randomSelectorWithExplicitType)
                    {
                        matches.addAll(worldIn.<T>getPlayers(entityClass, selectablePredicate));
                    }
                    else
                    {
                        matches.addAll(worldIn.<T>getEntitiesWithinAABB(entityClass, radiusBox, selectablePredicate));
                    }
                }
                else if (type.equals("a"))
                {
                    matches.addAll(worldIn.<T>getPlayers(entityClass, combinedPredicate));
                }
                else if (!type.equals("p") && (!type.equals("r") || randomSelectorWithExplicitType))
                {
                    matches.addAll(worldIn.<T>getEntities(entityClass, selectablePredicate));
                }
                else
                {
                    matches.addAll(worldIn.<T>getPlayers(entityClass, selectablePredicate));
                }
            }
            else
            {
                final AxisAlignedBB searchBox = createAABB(position, deltaX, deltaY, deltaZ);

                if (selectorTargetsPlayers && shouldUsePlayerList && !randomSelectorWithExplicitType)
                {
                    Predicate<Entity> insideSearchBox = new Predicate<Entity>()
                    {
                        public boolean apply(Entity entity)
                        {
                            return entity.posX >= searchBox.minX && entity.posY >= searchBox.minY && entity.posZ >= searchBox.minZ ? entity.posX < searchBox.maxX && entity.posY < searchBox.maxY && entity.posZ < searchBox.maxZ : false;
                        }
                    };
                    matches.addAll(worldIn.<T>getPlayers(entityClass, Predicates.<T> and (selectablePredicate, insideSearchBox)));
                }
                else
                {
                    matches.addAll(worldIn.<T>getEntitiesWithinAABB(entityClass, searchBox, selectablePredicate));
                }
            }
        }
        else if (type.equals("a"))
        {
            matches.addAll(worldIn.<T>getPlayers(entityClass, combinedPredicate));
        }
        else if (!type.equals("p") && (!type.equals("r") || randomSelectorWithExplicitType))
        {
            matches.addAll(worldIn.<T>getEntities(entityClass, selectablePredicate));
        }
        else
        {
            matches.addAll(worldIn.<T>getPlayers(entityClass, selectablePredicate));
        }

        return matches;
    }

    private static <T extends Entity> List<T> sortAndLimitResults(List<T> results, Map<String, String> params, ICommandSender sender, Class <? extends T > targetClass, String selectorType, final BlockPos position)
    {
        int limit = parseIntWithDefault(params, "c", !selectorType.equals("a") && !selectorType.equals("e") ? 1 : 0);

        if (!selectorType.equals("p") && !selectorType.equals("a") && !selectorType.equals("e"))
        {
            if (selectorType.equals("r"))
            {
                Collections.shuffle(results);
            }
        }
        else if (position != null)
        {
            Collections.sort(results, new Comparator<Entity>()
            {
                public int compare(Entity first, Entity second)
                {
                    return ComparisonChain.start().compare(first.getDistanceSq(position), second.getDistanceSq(position)).result();
                }
            });
        }

        Entity entity = sender.getCommandSenderEntity();

        if (entity != null && targetClass.isAssignableFrom(entity.getClass()) && limit == 1 && results.contains(entity) && !"r".equals(selectorType))
        {
            results = Lists.newArrayList((T)entity);
        }

        if (limit != 0)
        {
            if (limit < 0)
            {
                Collections.reverse(results);
            }

            results = results.subList(0, Math.min(Math.abs(limit), results.size()));
        }

        return results;
    }

    private static AxisAlignedBB createAABB(BlockPos position, int dx, int dy, int dz)
    {
        boolean negativeX = dx < 0;
        boolean negativeY = dy < 0;
        boolean negativeZ = dz < 0;
        int minX = position.getX() + (negativeX ? dx : 0);
        int minY = position.getY() + (negativeY ? dy : 0);
        int minZ = position.getZ() + (negativeZ ? dz : 0);
        int maxX = position.getX() + (negativeX ? 0 : dx) + 1;
        int maxY = position.getY() + (negativeY ? 0 : dy) + 1;
        int maxZ = position.getZ() + (negativeZ ? 0 : dz) + 1;
        return new AxisAlignedBB((double)minX, (double)minY, (double)minZ, (double)maxX, (double)maxY, (double)maxZ);
    }

    public static int normalizeAngle(int angle)
    {
        angle = angle % 360;

        if (angle >= 160)
        {
            angle -= 360;
        }

        if (angle < 0)
        {
            angle += 360;
        }

        return angle;
    }

    private static BlockPos getBlockPosFromArguments(Map<String, String> params, BlockPos fallback)
    {
        return new BlockPos(parseIntWithDefault(params, "x", fallback.getX()), parseIntWithDefault(params, "y", fallback.getY()), parseIntWithDefault(params, "z", fallback.getZ()));
    }

    private static boolean isWorldLimited(Map<String, String> params)
    {
        for (String argumentName : WORLD_BINDING_ARGS)
        {
            if (params.containsKey(argumentName))
            {
                return true;
            }
        }

        return false;
    }

    private static int parseIntWithDefault(Map<String, String> params, String key, int defaultValue)
    {
        return params.containsKey(key) ? MathHelper.parseIntWithDefault(params.get(key), defaultValue) : defaultValue;
    }

    private static String getArgument(Map<String, String> params, String key)
    {
        return params.get(key);
    }

    public static Map<String, Integer> getScoreMap(Map<String, String> params)
    {
        Map<String, Integer> scoreMap = Maps.<String, Integer>newHashMap();

        for (String argumentName : params.keySet())
        {
            if (argumentName.startsWith("score_") && argumentName.length() > "score_".length())
            {
                scoreMap.put(argumentName.substring("score_".length()), Integer.valueOf(MathHelper.parseIntWithDefault(params.get(argumentName), 1)));
            }
        }

        return scoreMap;
    }

    public static boolean matchesMultiplePlayers(String token)
    {
        Matcher matcher = tokenPattern.matcher(token);

        if (!matcher.matches())
        {
            return false;
        }
        else
        {
            Map<String, String> arguments = getArgumentMap(matcher.group(2));
            String selectorType = matcher.group(1);
            int defaultLimit = !"a".equals(selectorType) && !"e".equals(selectorType) ? 1 : 0;
            return parseIntWithDefault(arguments, "c", defaultLimit) != 1;
        }
    }

    public static boolean hasArguments(String token)
    {
        return tokenPattern.matcher(token).matches();
    }

    private static Map<String, String> getArgumentMap(String argumentString)
    {
        Map<String, String> arguments = Maps.<String, String>newHashMap();

        if (argumentString == null)
        {
            return arguments;
        }
        else
        {
            int positionalIndex = 0;
            int lastMatchEnd = -1;

            for (Matcher matcher = intListPattern.matcher(argumentString); matcher.find(); lastMatchEnd = matcher.end())
            {
                String argumentName = null;

                switch (positionalIndex++)
                {
                    case 0:
                        argumentName = "x";
                        break;

                    case 1:
                        argumentName = "y";
                        break;

                    case 2:
                        argumentName = "z";
                        break;

                    case 3:
                        argumentName = "r";
                }

                if (argumentName != null && matcher.group(1).length() > 0)
                {
                    arguments.put(argumentName, matcher.group(1));
                }
            }

            if (lastMatchEnd < argumentString.length())
            {
                Matcher keyValueMatcher = keyValueListPattern.matcher(lastMatchEnd == -1 ? argumentString : argumentString.substring(lastMatchEnd));

                while (keyValueMatcher.find())
                {
                    arguments.put(keyValueMatcher.group(1), keyValueMatcher.group(2));
                }
            }

            return arguments;
        }
    }
}
