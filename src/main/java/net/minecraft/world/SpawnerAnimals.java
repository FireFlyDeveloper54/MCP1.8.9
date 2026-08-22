package net.minecraft.world;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.WeightedRandom;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.optifine.BlockPosM;

public final class SpawnerAnimals
{
    private static final int MOB_COUNT_DIV = 289;
    private final Set<ChunkCoordIntPair> eligibleChunksForSpawning = Sets.<ChunkCoordIntPair>newHashSet();
    private Map<Class, EntityLiving> mapSampleEntitiesByClass = new HashMap();
    private int lastPlayerChunkX = Integer.MAX_VALUE;
    private int lastPlayerChunkZ = Integer.MAX_VALUE;
    private int countChunkPos;

    public int findChunksForSpawning(WorldServer worldServerIn, boolean spawnHostileMobs, boolean spawnPeacefulMobs, boolean spawnAnimals)
    {
        if (!spawnHostileMobs && !spawnPeacefulMobs)
        {
            return 0;
        }
        else
        {
            boolean flag = true;
            EntityPlayer entityplayer = null;

            if (worldServerIn.playerEntities.size() == 1)
            {
                entityplayer = (EntityPlayer)worldServerIn.playerEntities.get(0);

                if (this.eligibleChunksForSpawning.size() > 0 && entityplayer != null && entityplayer.chunkCoordX == this.lastPlayerChunkX && entityplayer.chunkCoordZ == this.lastPlayerChunkZ)
                {
                    flag = false;
                }
            }

            if (flag)
            {
                this.eligibleChunksForSpawning.clear();
                int i = 0;

                for (EntityPlayer entityplayer1 : worldServerIn.playerEntities)
                {
                    if (!entityplayer1.isSpectator())
                    {
                        int j = MathHelper.floor_double(entityplayer1.posX / 16.0D);
                        int k = MathHelper.floor_double(entityplayer1.posZ / 16.0D);
                        int l = 8;

                        for (int intValue = -l; intValue <= l; ++intValue)
                        {
                            for (int secondIntValue = -l; secondIntValue <= l; ++secondIntValue)
                            {
                                boolean flag1 = intValue == -l || intValue == l || secondIntValue == -l || secondIntValue == l;
                                ChunkCoordIntPair chunkcoordintpair = new ChunkCoordIntPair(intValue + j, secondIntValue + k);

                                if (!this.eligibleChunksForSpawning.contains(chunkcoordintpair))
                                {
                                    ++i;

                                    if (!flag1 && worldServerIn.getWorldBorder().contains(chunkcoordintpair))
                                    {
                                        this.eligibleChunksForSpawning.add(chunkcoordintpair);
                                    }
                                }
                            }
                        }
                    }
                }

                this.countChunkPos = i;

                if (entityplayer != null)
                {
                    this.lastPlayerChunkX = entityplayer.chunkCoordX;
                    this.lastPlayerChunkZ = entityplayer.chunkCoordZ;
                }
            }

            int sixteenthIntValue = 0;
            BlockPos blockpos2 = worldServerIn.getSpawnPoint();
            BlockPosM blockposm = new BlockPosM(0, 0, 0);

            for (EnumCreatureType enumcreaturetype : EnumCreatureType.VALUES)
            {
                if ((!enumcreaturetype.getPeacefulCreature() || spawnPeacefulMobs) && (enumcreaturetype.getPeacefulCreature() || spawnHostileMobs) && (!enumcreaturetype.getAnimal() || spawnAnimals))
                {
                    int thirdIntValue = worldServerIn.countEntities(enumcreaturetype.getCreatureClass());
                    int fourthIntValue = enumcreaturetype.getMaxNumberOfCreature() * this.countChunkPos / MOB_COUNT_DIV;

                    if (thirdIntValue <= fourthIntValue)
                    {
                        Collection<ChunkCoordIntPair> collection = this.eligibleChunksForSpawning;

                        
                        label561:

                        for (ChunkCoordIntPair chunkcoordintpair1 : collection)
                        {
                            BlockPos blockpos = getRandomChunkPosition(worldServerIn, chunkcoordintpair1.chunkXPos, chunkcoordintpair1.chunkZPos, blockposm);
                            int fifthIntValue = blockpos.getX();
                            int sixthIntValue = blockpos.getY();
                            int seventhIntValue = blockpos.getZ();
                            Block block = worldServerIn.getBlockState(blockpos).getBlock();

                            if (!block.isNormalCube())
                            {
                                int eighthIntValue = 0;

                                for (int ninthIntValue = 0; ninthIntValue < 3; ++ninthIntValue)
                                {
                                    int tenthIntValue = fifthIntValue;
                                    int eleventhIntValue = sixthIntValue;
                                    int twelfthIntValue = seventhIntValue;
                                    int thirteenthIntValue = 6;
                                    BiomeGenBase.SpawnListEntry biomegenbase$spawnlistentry = null;
                                    IEntityLivingData ientitylivingdata = null;

                                    for (int fourteenthIntValue = 0; fourteenthIntValue < 4; ++fourteenthIntValue)
                                    {
                                        tenthIntValue += worldServerIn.rand.nextInt(thirteenthIntValue) - worldServerIn.rand.nextInt(thirteenthIntValue);
                                        eleventhIntValue += worldServerIn.rand.nextInt(1) - worldServerIn.rand.nextInt(1);
                                        twelfthIntValue += worldServerIn.rand.nextInt(thirteenthIntValue) - worldServerIn.rand.nextInt(thirteenthIntValue);
                                        float f = (float)tenthIntValue + 0.5F;
                                        float floatValue = (float)twelfthIntValue + 0.5F;

                                        if (!worldServerIn.isAnyPlayerWithinRangeAt((double)f, (double)eleventhIntValue, (double)floatValue, 24.0D) && blockpos2.distanceSq((double)f, (double)eleventhIntValue, (double)floatValue) >= 576.0D)
                                        {
                                            BlockPos blockpos1 = new BlockPos(tenthIntValue, eleventhIntValue, twelfthIntValue);

                                            if (biomegenbase$spawnlistentry == null)
                                            {
                                                biomegenbase$spawnlistentry = worldServerIn.getSpawnListEntryForTypeAt(enumcreaturetype, blockpos1);

                                                if (biomegenbase$spawnlistentry == null)
                                                {
                                                    break;
                                                }
                                            }

                                            if (worldServerIn.canCreatureTypeSpawnHere(enumcreaturetype, biomegenbase$spawnlistentry, blockpos1) && canCreatureTypeSpawnAtLocation(EntitySpawnPlacementRegistry.getPlacementForEntity(biomegenbase$spawnlistentry.entityClass), worldServerIn, blockpos1))
                                            {
                                                EntityLiving entityliving;

                                                try
                                                {
                                                    entityliving = (EntityLiving)this.mapSampleEntitiesByClass.get(biomegenbase$spawnlistentry.entityClass);

                                                    if (entityliving == null)
                                                    {
                                                        entityliving = (EntityLiving)biomegenbase$spawnlistentry.entityClass.getConstructor(new Class[] {World.class}).newInstance(new Object[] {worldServerIn});
                                                        this.mapSampleEntitiesByClass.put(biomegenbase$spawnlistentry.entityClass, entityliving);
                                                    }
                                                }
                                                catch (Exception exception1)
                                                {
                                                    net.minecraft.src.Config.warn(exception1.getClass().getName() + ": " + exception1.getMessage(), exception1);
                                                    return sixteenthIntValue;
                                                }

                                                entityliving.setLocationAndAngles((double)f, (double)eleventhIntValue, (double)floatValue, worldServerIn.rand.nextFloat() * 360.0F, 0.0F);
                                                boolean flag2 = entityliving.getCanSpawnHere() && entityliving.isNotColliding();

                                                if (flag2)
                                                {
                                                    this.mapSampleEntitiesByClass.remove(biomegenbase$spawnlistentry.entityClass);
                                                    ientitylivingdata = entityliving.onInitialSpawn(worldServerIn.getDifficultyForLocation(new BlockPos(entityliving)), ientitylivingdata);

                                                    if (entityliving.isNotColliding())
                                                    {
                                                        ++eighthIntValue;
                                                        worldServerIn.spawnEntityInWorld(entityliving);
                                                    }

                                                    int fifteenthIntValue = entityliving.getMaxSpawnedInChunk();

                                                    if (eighthIntValue >= fifteenthIntValue)
                                                    {
                                                        continue label561;
                                                    }
                                                }

                                                sixteenthIntValue += eighthIntValue;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            return sixteenthIntValue;
        }
    }

    protected static BlockPos getRandomChunkPosition(World worldIn, int x, int z)
    {
        Chunk chunk = worldIn.getChunkFromChunkCoords(x, z);
        int i = x * 16 + worldIn.rand.nextInt(16);
        int j = z * 16 + worldIn.rand.nextInt(16);
        int k = MathHelper.roundUp(chunk.getHeightValue(i & 15, j & 15) + 1, 16);
        int l = worldIn.rand.nextInt(k > 0 ? k : chunk.getTopFilledSegment() + 16 - 1);
        return new BlockPos(i, l, j);
    }

    private static BlockPosM getRandomChunkPosition(World world, int chunkX, int chunkZ, BlockPosM mutablePos)
    {
        Chunk chunk = world.getChunkFromChunkCoords(chunkX, chunkZ);
        int i = chunkX * 16 + world.rand.nextInt(16);
        int j = chunkZ * 16 + world.rand.nextInt(16);
        int k = MathHelper.roundUp(chunk.getHeightValue(i & 15, j & 15) + 1, 16);
        int l = world.rand.nextInt(k > 0 ? k : chunk.getTopFilledSegment() + 16 - 1);
        mutablePos.setXyz(i, l, j);
        return mutablePos;
    }

    public static boolean canCreatureTypeSpawnAtLocation(EntityLiving.SpawnPlacementType spawnPlacementTypeIn, World worldIn, BlockPos pos)
    {
        if (!worldIn.getWorldBorder().contains(pos))
        {
            return false;
        }
        else if (spawnPlacementTypeIn == null)
        {
            return false;
        }
        else
        {
            Block block = worldIn.getBlockState(pos).getBlock();

            if (spawnPlacementTypeIn == EntityLiving.SpawnPlacementType.IN_WATER)
            {
                return block.getMaterial().isLiquid() && worldIn.getBlockState(pos.down()).getBlock().getMaterial().isLiquid() && !worldIn.getBlockState(pos.up()).getBlock().isNormalCube();
            }
            else
            {
                BlockPos blockpos = pos.down();
                IBlockState iblockstate = worldIn.getBlockState(blockpos);
                boolean flag = World.doesBlockHaveSolidTopSurface(worldIn, blockpos);

                if (!flag)
                {
                    return false;
                }
                else
                {
                    Block block1 = worldIn.getBlockState(blockpos).getBlock();
                    boolean flag1 = block1 != Blocks.bedrock && block1 != Blocks.barrier;
                    return flag1 && !block.isNormalCube() && !block.getMaterial().isLiquid() && !worldIn.getBlockState(pos.up()).getBlock().isNormalCube();
                }
            }
        }
    }

    public static void performWorldGenSpawning(World worldIn, BiomeGenBase biomeIn, int startX, int startZ, int sizeX, int sizeZ, Random randomIn)
    {
        List<BiomeGenBase.SpawnListEntry> list = biomeIn.getSpawnableList(EnumCreatureType.CREATURE);

        if (!list.isEmpty())
        {
            while (randomIn.nextFloat() < biomeIn.getSpawningChance())
            {
                BiomeGenBase.SpawnListEntry biomegenbase$spawnlistentry = (BiomeGenBase.SpawnListEntry)WeightedRandom.getRandomItem(worldIn.rand, list);
                int i = biomegenbase$spawnlistentry.minGroupCount + randomIn.nextInt(1 + biomegenbase$spawnlistentry.maxGroupCount - biomegenbase$spawnlistentry.minGroupCount);
                IEntityLivingData ientitylivingdata = null;
                int j = startX + randomIn.nextInt(sizeX);
                int k = startZ + randomIn.nextInt(sizeZ);
                int l = j;
                int intValue = k;
                BlockPos.MutableBlockPos heightSamplePos = new BlockPos.MutableBlockPos();

                for (int secondIntValue = 0; secondIntValue < i; ++secondIntValue)
                {
                    boolean flag = false;

                    for (int thirdIntValue = 0; !flag && thirdIntValue < 4; ++thirdIntValue)
                    {
                        BlockPos blockpos = worldIn.getTopSolidOrLiquidBlock(heightSamplePos.set(j, 0, k));

                        if (canCreatureTypeSpawnAtLocation(EntityLiving.SpawnPlacementType.ON_GROUND, worldIn, blockpos))
                        {
                            EntityLiving entityliving;

                            try
                            {
                                entityliving = (EntityLiving)biomegenbase$spawnlistentry.entityClass.getConstructor(new Class[] {World.class}).newInstance(new Object[] {worldIn});
                            }
                            catch (Exception exception1)
                            {
                                net.minecraft.src.Config.warn(exception1.getClass().getName() + ": " + exception1.getMessage(), exception1);
                                continue;
                            }

                            
                            entityliving.setLocationAndAngles((double)((float)j + 0.5F), (double)blockpos.getY(), (double)((float)k + 0.5F), randomIn.nextFloat() * 360.0F, 0.0F);
                            worldIn.spawnEntityInWorld(entityliving);
                            ientitylivingdata = entityliving.onInitialSpawn(worldIn.getDifficultyForLocation(new BlockPos(entityliving)), ientitylivingdata);
                            flag = true;
                        }

                        j += randomIn.nextInt(5) - randomIn.nextInt(5);

                        for (k += randomIn.nextInt(5) - randomIn.nextInt(5); j < startX || j >= startX + sizeX || k < startZ || k >= startZ + sizeX; k = intValue + randomIn.nextInt(5) - randomIn.nextInt(5))
                        {
                            j = l + randomIn.nextInt(5) - randomIn.nextInt(5);
                        }
                    }
                }
            }
        }
    }
}
