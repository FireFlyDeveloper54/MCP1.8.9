package net.minecraft.block.state;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class BlockPistonStructureHelper
{
    private final World world;
    private final BlockPos pistonPos;
    private final BlockPos blockToMove;
    private final EnumFacing moveDirection;
    private final List<BlockPos> toMove = Lists.<BlockPos>newArrayList();
    private final List<BlockPos> toDestroy = Lists.<BlockPos>newArrayList();

    public BlockPistonStructureHelper(World worldIn, BlockPos posIn, EnumFacing pistonFacing, boolean extending)
    {
        this.world = worldIn;
        this.pistonPos = posIn;

        if (extending)
        {
            this.moveDirection = pistonFacing;
            this.blockToMove = posIn.offset(pistonFacing);
        }
        else
        {
            this.moveDirection = pistonFacing.getOpposite();
            this.blockToMove = posIn.offset(pistonFacing, 2);
        }
    }

    public boolean canMove()
    {
        this.toMove.clear();
        this.toDestroy.clear();
        Block block = this.world.getBlockState(this.blockToMove).getBlock();

        if (!BlockPistonBase.canPush(block, this.world, this.blockToMove, this.moveDirection, false))
        {
            if (block.getMobilityFlag() != 1)
            {
                return false;
            }
            else
            {
                this.toDestroy.add(this.blockToMove);
                return true;
            }
        }
        else if (!this.canMoveBlock(this.blockToMove))
        {
            return false;
        }
        else
        {
            for (int i = 0; i < this.toMove.size(); ++i)
            {
                BlockPos blockPos = (BlockPos)this.toMove.get(i);

                if (this.world.getBlockState(blockPos).getBlock() == Blocks.slime_block && !this.canPushAgainst(blockPos))
                {
                    return false;
                }
            }

            return true;
        }
    }

    private boolean canMoveBlock(BlockPos origin)
    {
        Block block = this.world.getBlockState(origin).getBlock();

        if (block.getMaterial() == Material.air)
        {
            return true;
        }
        else if (!BlockPistonBase.canPush(block, this.world, origin, this.moveDirection, false))
        {
            return true;
        }
        else if (origin.equals(this.pistonPos))
        {
            return true;
        }
        else if (this.toMove.contains(origin))
        {
            return true;
        }
        else
        {
            int i = 1;

            if (i + this.toMove.size() > 12)
            {
                return false;
            }
            else
            {
                while (block == Blocks.slime_block)
                {
                    BlockPos blockpos = origin.offset(this.moveDirection.getOpposite(), i);
                    block = this.world.getBlockState(blockpos).getBlock();

                    if (block.getMaterial() == Material.air || !BlockPistonBase.canPush(block, this.world, blockpos, this.moveDirection, false) || blockpos.equals(this.pistonPos))
                    {
                        break;
                    }

                    ++i;

                    if (i + this.toMove.size() > 12)
                    {
                        return false;
                    }
                }

                int tailCount = 0;

                for (int j = i - 1; j >= 0; --j)
                {
                    this.toMove.add(origin.offset(this.moveDirection.getOpposite(), j));
                    ++tailCount;
                }

                int intValue = 1;

                while (true)
                {
                    BlockPos blockpos1 = origin.offset(this.moveDirection, intValue);
                    int k = this.toMove.indexOf(blockpos1);

                    if (k > -1)
                    {
                        this.reorderMoveList(tailCount, k);

                        for (int l = 0; l <= k + tailCount; ++l)
                        {
                            BlockPos blockpos2 = (BlockPos)this.toMove.get(l);

                            if (this.world.getBlockState(blockpos2).getBlock() == Blocks.slime_block && !this.canPushAgainst(blockpos2))
                            {
                                return false;
                            }
                        }

                        return true;
                    }

                    block = this.world.getBlockState(blockpos1).getBlock();

                    if (block.getMaterial() == Material.air)
                    {
                        return true;
                    }

                    if (!BlockPistonBase.canPush(block, this.world, blockpos1, this.moveDirection, true) || blockpos1.equals(this.pistonPos))
                    {
                        return false;
                    }

                    if (block.getMobilityFlag() == 1)
                    {
                        this.toDestroy.add(blockpos1);
                        return true;
                    }

                    if (this.toMove.size() >= 12)
                    {
                        return false;
                    }

                    this.toMove.add(blockpos1);
                    ++tailCount;
                    ++intValue;
                }
            }
        }
    }

    private void reorderMoveList(int tailCount, int insertIndex)
    {
        List<BlockPos> list = Lists.<BlockPos>newArrayList();
        List<BlockPos> list1 = Lists.<BlockPos>newArrayList();
        List<BlockPos> list2 = Lists.<BlockPos>newArrayList();
        list.addAll(this.toMove.subList(0, insertIndex));
        list1.addAll(this.toMove.subList(this.toMove.size() - tailCount, this.toMove.size()));
        list2.addAll(this.toMove.subList(insertIndex, this.toMove.size() - tailCount));
        this.toMove.clear();
        this.toMove.addAll(list);
        this.toMove.addAll(list1);
        this.toMove.addAll(list2);
    }

    private boolean canPushAgainst(BlockPos pos)
    {
        for (EnumFacing enumfacing : EnumFacing.VALUES)
        {
            if (enumfacing.getAxis() != this.moveDirection.getAxis() && !this.canMoveBlock(pos.offset(enumfacing)))
            {
                return false;
            }
        }

        return true;
    }

    public List<BlockPos> getBlocksToMove()
    {
        return this.toMove;
    }

    public List<BlockPos> getBlocksToDestroy()
    {
        return this.toDestroy;
    }
}
