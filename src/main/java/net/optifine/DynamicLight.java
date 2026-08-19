package net.optifine;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.src.Config;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class DynamicLight
{
    private Entity entity = null;
    private double offsetY = 0.0D;
    private double lastPosX = -2.147483648E9D;
    private double lastPosY = -2.147483648E9D;
    private double lastPosZ = -2.147483648E9D;
    private int lastLightLevel = 0;
    private boolean underwater = false;
    private long timeCheckMs = 0L;
    private Set<BlockPos> setLitChunkPos = new HashSet(8);
    private Set<BlockPos> setLitChunkPosUpdate = new HashSet(8);
    private BlockPos.MutableBlockPos blockPosMutable = new BlockPos.MutableBlockPos();

    public DynamicLight(Entity entity)
    {
        this.entity = entity;
        this.offsetY = (double)entity.getEyeHeight();
    }

    public void update(RenderGlobal renderGlobal)
    {
        if (Config.isDynamicLightsFast())
        {
            long currentTimeMs = System.currentTimeMillis();

            if (currentTimeMs < this.timeCheckMs + 500L)
            {
                return;
            }

            this.timeCheckMs = currentTimeMs;
        }

        double x = this.entity.posX - 0.5D;
        double y = this.entity.posY - 0.5D + this.offsetY;
        double z = this.entity.posZ - 0.5D;
        int lightLevel = DynamicLights.getLightLevel(this.entity);
        double deltaX = x - this.lastPosX;
        double deltaY = y - this.lastPosY;
        double deltaZ = z - this.lastPosZ;
        double movementThreshold = 0.1D;

        if (Math.abs(deltaX) > movementThreshold || Math.abs(deltaY) > movementThreshold || Math.abs(deltaZ) > movementThreshold || this.lastLightLevel != lightLevel)
        {
            this.lastPosX = x;
            this.lastPosY = y;
            this.lastPosZ = z;
            this.lastLightLevel = lightLevel;
            this.underwater = false;
            World world = renderGlobal.getWorld();

            int floorX = MathHelper.floor_double(x);
            int floorY = MathHelper.floor_double(y);
            int floorZ = MathHelper.floor_double(z);
            this.blockPosMutable.set(floorX, floorY, floorZ);

            if (world != null)
            {
                IBlockState blockState = world.getBlockState(this.blockPosMutable);
                Block block = blockState.getBlock();
                this.underwater = block == Blocks.water;
            }

            Set<BlockPos> newLitChunkPos = this.setLitChunkPosUpdate;
            newLitChunkPos.clear();

            if (lightLevel > 0)
            {
                EnumFacing xFacing = (floorX & 15) >= 8 ? EnumFacing.EAST : EnumFacing.WEST;
                EnumFacing yFacing = (floorY & 15) >= 8 ? EnumFacing.UP : EnumFacing.DOWN;
                EnumFacing zFacing = (floorZ & 15) >= 8 ? EnumFacing.SOUTH : EnumFacing.NORTH;
                BlockPos lightPos = this.blockPosMutable;
                RenderChunk centerChunk = renderGlobal.getRenderChunk(lightPos);
                BlockPos xChunkPos = this.getChunkPos(centerChunk, lightPos, xFacing);
                RenderChunk xChunk = renderGlobal.getRenderChunk(xChunkPos);
                BlockPos zChunkPos = this.getChunkPos(centerChunk, lightPos, zFacing);
                RenderChunk zChunk = renderGlobal.getRenderChunk(zChunkPos);
                BlockPos xzChunkPos = this.getChunkPos(xChunk, xChunkPos, zFacing);
                RenderChunk xzChunk = renderGlobal.getRenderChunk(xzChunkPos);
                BlockPos yChunkPos = this.getChunkPos(centerChunk, lightPos, yFacing);
                RenderChunk yChunk = renderGlobal.getRenderChunk(yChunkPos);
                BlockPos xyChunkPos = this.getChunkPos(yChunk, yChunkPos, xFacing);
                RenderChunk xyChunk = renderGlobal.getRenderChunk(xyChunkPos);
                BlockPos yzChunkPos = this.getChunkPos(yChunk, yChunkPos, zFacing);
                RenderChunk yzChunk = renderGlobal.getRenderChunk(yzChunkPos);
                BlockPos xyzChunkPos = this.getChunkPos(xyChunk, xyChunkPos, zFacing);
                RenderChunk xyzChunk = renderGlobal.getRenderChunk(xyzChunkPos);
                this.updateChunkLight(centerChunk, this.setLitChunkPos, newLitChunkPos);
                this.updateChunkLight(xChunk, this.setLitChunkPos, newLitChunkPos);
                this.updateChunkLight(zChunk, this.setLitChunkPos, newLitChunkPos);
                this.updateChunkLight(xzChunk, this.setLitChunkPos, newLitChunkPos);
                this.updateChunkLight(yChunk, this.setLitChunkPos, newLitChunkPos);
                this.updateChunkLight(xyChunk, this.setLitChunkPos, newLitChunkPos);
                this.updateChunkLight(yzChunk, this.setLitChunkPos, newLitChunkPos);
                this.updateChunkLight(xyzChunk, this.setLitChunkPos, newLitChunkPos);
            }

            this.updateLitChunks(renderGlobal);
            this.setLitChunkPosUpdate = this.setLitChunkPos;
            this.setLitChunkPos = newLitChunkPos;
        }
    }

    private BlockPos getChunkPos(RenderChunk renderChunk, BlockPos pos, EnumFacing facing)
    {
        return renderChunk != null ? renderChunk.getBlockPosOffset16(facing) : pos.offset(facing, 16);
    }

    private void updateChunkLight(RenderChunk renderChunk, Set<BlockPos> setPrevPos, Set<BlockPos> setNewPos)
    {
        if (renderChunk != null)
        {
            CompiledChunk compiledChunk = renderChunk.getCompiledChunk();

            if (compiledChunk != null && !compiledChunk.isEmpty())
            {
                renderChunk.setNeedsUpdate(true);
            }

            BlockPos chunkPos = renderChunk.getPosition();

            if (setPrevPos != null)
            {
                setPrevPos.remove(chunkPos);
            }

            if (setNewPos != null)
            {
                setNewPos.add(chunkPos);
            }
        }
    }

    public void updateLitChunks(RenderGlobal renderGlobal)
    {
        for (BlockPos chunkPos : this.setLitChunkPos)
        {
            RenderChunk renderChunk = renderGlobal.getRenderChunk(chunkPos);
            this.updateChunkLight(renderChunk, (Set<BlockPos>)null, (Set<BlockPos>)null);
        }
    }

    public Entity getEntity()
    {
        return this.entity;
    }

    public double getLastPosX()
    {
        return this.lastPosX;
    }

    public double getLastPosY()
    {
        return this.lastPosY;
    }

    public double getLastPosZ()
    {
        return this.lastPosZ;
    }

    public int getLastLightLevel()
    {
        return this.lastLightLevel;
    }

    public boolean isUnderwater()
    {
        return this.underwater;
    }

    public double getOffsetY()
    {
        return this.offsetY;
    }

    public String toString()
    {
        return "Entity: " + this.entity + ", offsetY: " + this.offsetY;
    }
}
