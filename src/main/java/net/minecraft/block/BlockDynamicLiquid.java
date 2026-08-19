package net.minecraft.block;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class BlockDynamicLiquid extends BlockLiquid
{
    int adjacentSourceBlocks;

    protected BlockDynamicLiquid(Material materialIn)
    {
        super(materialIn);
    }

    private void placeStaticBlock(World worldIn, BlockPos pos, IBlockState currentState)
    {
        worldIn.setBlockState(pos, getStaticBlock(this.blockMaterial).getDefaultState().withProperty(LEVEL, currentState.getValue(LEVEL)), 2);
    }

    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
        int currentLevel = ((Integer)state.getValue(LEVEL)).intValue();
        int levelDecreasePerBlock = 1;

        if (this.blockMaterial == Material.lava && !worldIn.provider.doesWaterVaporize())
        {
            levelDecreasePerBlock = 2;
        }

        int tickRate = this.tickRate(worldIn);

        if (currentLevel > 0)
        {
            int minimumNeighborLevel = -100;
            this.adjacentSourceBlocks = 0;

            for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL)
            {
                minimumNeighborLevel = this.checkAdjacentBlock(worldIn, pos.offset(facing), minimumNeighborLevel);
            }

            int nextLevel = minimumNeighborLevel + levelDecreasePerBlock;

            if (nextLevel >= 8 || minimumNeighborLevel < 0)
            {
                nextLevel = -1;
            }

            if (this.getLevel(worldIn, pos.up()) >= 0)
            {
                int upperLevel = this.getLevel(worldIn, pos.up());

                if (upperLevel >= 8)
                {
                    nextLevel = upperLevel;
                }
                else
                {
                    nextLevel = upperLevel + 8;
                }
            }

            if (this.adjacentSourceBlocks >= 2 && this.blockMaterial == Material.water)
            {
                IBlockState belowState = worldIn.getBlockState(pos.down());

                if (belowState.getBlock().getMaterial().isSolid())
                {
                    nextLevel = 0;
                }
                else if (belowState.getBlock().getMaterial() == this.blockMaterial && ((Integer)belowState.getValue(LEVEL)).intValue() == 0)
                {
                    nextLevel = 0;
                }
            }

            if (this.blockMaterial == Material.lava && currentLevel < 8 && nextLevel < 8 && nextLevel > currentLevel && rand.nextInt(4) != 0)
            {
                tickRate *= 4;
            }

            if (nextLevel == currentLevel)
            {
                this.placeStaticBlock(worldIn, pos, state);
            }
            else
            {
                currentLevel = nextLevel;

                if (nextLevel < 0)
                {
                    worldIn.setBlockToAir(pos);
                }
                else
                {
                    state = state.withProperty(LEVEL, Integer.valueOf(nextLevel));
                    worldIn.setBlockState(pos, state, 2);
                    worldIn.scheduleUpdate(pos, this, tickRate);
                    worldIn.notifyNeighborsOfStateChange(pos, this);
                }
            }
        }
        else
        {
            this.placeStaticBlock(worldIn, pos, state);
        }

        IBlockState downState = worldIn.getBlockState(pos.down());

        if (this.canFlowInto(worldIn, pos.down(), downState))
        {
            if (this.blockMaterial == Material.lava && worldIn.getBlockState(pos.down()).getBlock().getMaterial() == Material.water)
            {
                worldIn.setBlockState(pos.down(), Blocks.stone.getDefaultState());
                this.triggerMixEffects(worldIn, pos.down());
                return;
            }

            if (currentLevel >= 8)
            {
                this.tryFlowInto(worldIn, pos.down(), downState, currentLevel);
            }
            else
            {
                this.tryFlowInto(worldIn, pos.down(), downState, currentLevel + 8);
            }
        }
        else if (currentLevel >= 0 && (currentLevel == 0 || this.isBlocked(worldIn, pos.down(), downState)))
        {
            Set<EnumFacing> flowDirections = this.getPossibleFlowDirections(worldIn, pos);
            int flowLevel = currentLevel + levelDecreasePerBlock;

            if (currentLevel >= 8)
            {
                flowLevel = 1;
            }

            if (flowLevel >= 8)
            {
                return;
            }

            for (EnumFacing flowDirection : flowDirections)
            {
                this.tryFlowInto(worldIn, pos.offset(flowDirection), worldIn.getBlockState(pos.offset(flowDirection)), flowLevel);
            }
        }
    }

    private void tryFlowInto(World worldIn, BlockPos pos, IBlockState state, int level)
    {
        if (this.canFlowInto(worldIn, pos, state))
        {
            if (state.getBlock() != Blocks.air)
            {
                if (this.blockMaterial == Material.lava)
                {
                    this.triggerMixEffects(worldIn, pos);
                }
                else
                {
                    state.getBlock().dropBlockAsItem(worldIn, pos, state, 0);
                }
            }

            worldIn.setBlockState(pos, this.getDefaultState().withProperty(LEVEL, Integer.valueOf(level)), 3);
        }
    }

    private int calculateFlowCost(World worldIn, BlockPos pos, int distance, EnumFacing excludedDirection)
    {
        int shortestCost = 1000;

        for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL)
        {
            if (facing != excludedDirection)
            {
                BlockPos offsetPos = pos.offset(facing);
                IBlockState offsetState = worldIn.getBlockState(offsetPos);

                if (!this.isBlocked(worldIn, offsetPos, offsetState) && (offsetState.getBlock().getMaterial() != this.blockMaterial || ((Integer)offsetState.getValue(LEVEL)).intValue() > 0))
                {
                    if (!this.isBlocked(worldIn, offsetPos.down(), offsetState))
                    {
                        return distance;
                    }

                    if (distance < 4)
                    {
                        int flowCost = this.calculateFlowCost(worldIn, offsetPos, distance + 1, facing.getOpposite());

                        if (flowCost < shortestCost)
                        {
                            shortestCost = flowCost;
                        }
                    }
                }
            }
        }

        return shortestCost;
    }

    private Set<EnumFacing> getPossibleFlowDirections(World worldIn, BlockPos pos)
    {
        int bestCost = 1000;
        Set<EnumFacing> flowDirections = EnumSet.<EnumFacing>noneOf(EnumFacing.class);

        for (EnumFacing facing : EnumFacing.Plane.HORIZONTAL)
        {
            BlockPos offsetPos = pos.offset(facing);
            IBlockState offsetState = worldIn.getBlockState(offsetPos);

            if (!this.isBlocked(worldIn, offsetPos, offsetState) && (offsetState.getBlock().getMaterial() != this.blockMaterial || ((Integer)offsetState.getValue(LEVEL)).intValue() > 0))
            {
                int flowCost;

                if (this.isBlocked(worldIn, offsetPos.down(), worldIn.getBlockState(offsetPos.down())))
                {
                    flowCost = this.calculateFlowCost(worldIn, offsetPos, 1, facing.getOpposite());
                }
                else
                {
                    flowCost = 0;
                }

                if (flowCost < bestCost)
                {
                    flowDirections.clear();
                }

                if (flowCost <= bestCost)
                {
                    flowDirections.add(facing);
                    bestCost = flowCost;
                }
            }
        }

        return flowDirections;
    }

    private boolean isBlocked(World worldIn, BlockPos pos, IBlockState state)
    {
        Block blockAtPos = worldIn.getBlockState(pos).getBlock();
        return !(blockAtPos instanceof BlockDoor) && blockAtPos != Blocks.standing_sign && blockAtPos != Blocks.ladder && blockAtPos != Blocks.reeds ? (blockAtPos.blockMaterial == Material.portal ? true : blockAtPos.blockMaterial.blocksMovement()) : true;
    }

    protected int checkAdjacentBlock(World worldIn, BlockPos pos, int currentMinLevel)
    {
        int adjacentLevel = this.getLevel(worldIn, pos);

        if (adjacentLevel < 0)
        {
            return currentMinLevel;
        }
        else
        {
            if (adjacentLevel == 0)
            {
                ++this.adjacentSourceBlocks;
            }

            if (adjacentLevel >= 8)
            {
                adjacentLevel = 0;
            }

            return currentMinLevel >= 0 && adjacentLevel >= currentMinLevel ? currentMinLevel : adjacentLevel;
        }
    }

    private boolean canFlowInto(World worldIn, BlockPos pos, IBlockState state)
    {
        Material blockMaterial = state.getBlock().getMaterial();
        return blockMaterial != this.blockMaterial && blockMaterial != Material.lava && !this.isBlocked(worldIn, pos, state);
    }

    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state)
    {
        if (!this.checkForMixing(worldIn, pos, state))
        {
            worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn));
        }
    }
}
