package net.minecraft.client.particle;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityRainFX extends EntityFX
{
    private final BlockPos.MutableBlockPos samplePos = new BlockPos.MutableBlockPos();

    protected EntityRainFX(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn)
    {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, 0.0D, 0.0D, 0.0D);
        this.motionX *= 0.30000001192092896D;
        this.motionY = this.rand.nextDouble() * 0.20000000298023224D + 0.10000000149011612D;
        this.motionZ *= 0.30000001192092896D;
        this.particleRed = 1.0F;
        this.particleGreen = 1.0F;
        this.particleBlue = 1.0F;
        this.setParticleTextureIndex(19 + this.rand.nextInt(4));
        this.setSize(0.01F, 0.01F);
        this.particleGravity = 0.06F;
        this.particleMaxAge = (int)(8.0D / (this.rand.nextDouble() * 0.8D + 0.2D));
    }

    public void onUpdate()
    {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        this.motionY -= (double)this.particleGravity;
        this.moveEntity(this.motionX, this.motionY, this.motionZ);
        this.motionX *= 0.9800000190734863D;
        this.motionY *= 0.9800000190734863D;
        this.motionZ *= 0.9800000190734863D;

        if (this.particleMaxAge-- <= 0)
        {
            this.setDead();
        }

        if (this.onGround)
        {
            if (this.rand.nextDouble() < 0.5D)
            {
                this.setDead();
            }

            this.motionX *= 0.699999988079071D;
            this.motionZ *= 0.699999988079071D;
        }

        BlockPos blockPos = this.samplePos.set(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY), MathHelper.floor_double(this.posZ));
        IBlockState blockState = this.worldObj.getBlockState(blockPos);
        Block block = blockState.getBlock();
        block.setBlockBoundsBasedOnState(this.worldObj, blockPos);
        Material material = blockState.getBlock().getMaterial();

        if (material.isLiquid() || material.isSolid())
        {
            double collisionHeight = 0.0D;

            if (blockState.getBlock() instanceof BlockLiquid)
            {
                collisionHeight = (double)(1.0F - BlockLiquid.getLiquidHeightPercent(((Integer)blockState.getValue(BlockLiquid.LEVEL)).intValue()));
            }
            else
            {
                collisionHeight = block.getBlockBoundsMaxY();
            }

            double collisionY = (double)MathHelper.floor_double(this.posY) + collisionHeight;

            if (this.posY < collisionY)
            {
                this.setDead();
            }
        }
    }

    public static class Factory implements IParticleFactory
    {
        public EntityFX getEntityFX(int particleID, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, int... parameters)
        {
            return new EntityRainFX(worldIn, xCoordIn, yCoordIn, zCoordIn);
        }
    }
}
