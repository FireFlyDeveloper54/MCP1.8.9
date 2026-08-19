package net.minecraft.entity.ai;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import net.minecraft.block.Block;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockStateHelper;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class EntityAIEatGrass extends EntityAIBase
{
    private static final Predicate<IBlockState> TALL_GRASS_PREDICATE = BlockStateHelper.forBlock(Blocks.tallgrass).where(BlockTallGrass.TYPE, Predicates.equalTo(BlockTallGrass.EnumType.GRASS));
    private EntityLiving grassEaterEntity;
    private World entityWorld;
    int eatingGrassTimer;

    public EntityAIEatGrass(EntityLiving grassEaterEntityIn)
    {
        this.grassEaterEntity = grassEaterEntityIn;
        this.entityWorld = grassEaterEntityIn.worldObj;
        this.setMutexBits(7);
    }

    public boolean shouldExecute()
    {
        if (this.grassEaterEntity.getRNG().nextInt(this.grassEaterEntity.isChild() ? 50 : 1000) != 0)
        {
            return false;
        }
        else
        {
            BlockPos blockPos = new BlockPos(this.grassEaterEntity.posX, this.grassEaterEntity.posY, this.grassEaterEntity.posZ);
            return TALL_GRASS_PREDICATE.apply(this.entityWorld.getBlockState(blockPos)) ? true : this.entityWorld.getBlockState(blockPos.down()).getBlock() == Blocks.grass;
        }
    }

    public void startExecuting()
    {
        this.eatingGrassTimer = 40;
        this.entityWorld.setEntityState(this.grassEaterEntity, (byte)10);
        this.grassEaterEntity.getNavigator().clearPathEntity();
    }

    public void resetTask()
    {
        this.eatingGrassTimer = 0;
    }

    public boolean continueExecuting()
    {
        return this.eatingGrassTimer > 0;
    }

    public int getEatingGrassTimer()
    {
        return this.eatingGrassTimer;
    }

    public void updateTask()
    {
        this.eatingGrassTimer = Math.max(0, this.eatingGrassTimer - 1);

        if (this.eatingGrassTimer == 4)
        {
            BlockPos blockPos = new BlockPos(this.grassEaterEntity.posX, this.grassEaterEntity.posY, this.grassEaterEntity.posZ);

            if (TALL_GRASS_PREDICATE.apply(this.entityWorld.getBlockState(blockPos)))
            {
                if (this.entityWorld.getGameRules().getBoolean("mobGriefing"))
                {
                    this.entityWorld.destroyBlock(blockPos, false);
                }

                this.grassEaterEntity.eatGrassBonus();
            }
            else
            {
                BlockPos grassBlockPos = blockPos.down();

                if (this.entityWorld.getBlockState(grassBlockPos).getBlock() == Blocks.grass)
                {
                    if (this.entityWorld.getGameRules().getBoolean("mobGriefing"))
                    {
                        this.entityWorld.playAuxSFX(2001, grassBlockPos, Block.getIdFromBlock(Blocks.grass));
                        this.entityWorld.setBlockState(grassBlockPos, Blocks.dirt.getDefaultState(), 2);
                    }

                    this.grassEaterEntity.eatGrassBonus();
                }
            }
        }
    }
}
