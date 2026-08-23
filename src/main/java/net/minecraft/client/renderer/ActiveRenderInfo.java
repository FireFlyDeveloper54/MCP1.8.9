package net.minecraft.client.renderer;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.GlUtil;

public class ActiveRenderInfo
{
    private static final IntBuffer VIEWPORT = GLAllocation.createDirectIntBuffer(16);
    private static final FloatBuffer MODELVIEW = GLAllocation.createDirectFloatBuffer(16);
    private static final FloatBuffer PROJECTION = GLAllocation.createDirectFloatBuffer(16);
    private static final FloatBuffer OBJECTCOORDS = GLAllocation.createDirectFloatBuffer(3);
    private static final BlockPos.MutableBlockPos VIEW_BLOCK_POS = new BlockPos.MutableBlockPos();
    private static Vec3 position = new Vec3(0.0D, 0.0D, 0.0D);
    private static float rotationX;
    private static float rotationXZ;
    private static float rotationZ;
    private static float rotationYZ;
    private static float rotationXY;

    public static void updateRenderInfo(EntityPlayer entityplayerIn, boolean thirdPerson)
    {
        GlStateManager.getFloat(2982, MODELVIEW);
        GlStateManager.getFloat(2983, PROJECTION);
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, VIEWPORT);
        float viewportCenterX = (float)((VIEWPORT.get(0) + VIEWPORT.get(2)) / 2);
        float viewportCenterY = (float)((VIEWPORT.get(1) + VIEWPORT.get(3)) / 2);
        GlUtil.gluUnProject(viewportCenterX, viewportCenterY, 0.0F, MODELVIEW, PROJECTION, VIEWPORT, OBJECTCOORDS);
        position = new Vec3((double)OBJECTCOORDS.get(0), (double)OBJECTCOORDS.get(1), (double)OBJECTCOORDS.get(2));
        float directionMultiplier = thirdPerson ? -1.0F : 1.0F;
        float pitch = entityplayerIn.rotationPitch;
        float yaw = entityplayerIn.rotationYaw;
        float[] yawSC = new float[2];
        float[] pitchSC = new float[2];
        MathHelper.sinCosDeg(yaw, yawSC);
        MathHelper.sinCosDeg(pitch, pitchSC);
        rotationX = yawSC[1] * directionMultiplier;
        rotationZ = yawSC[0] * directionMultiplier;
        rotationYZ = -rotationZ * pitchSC[0] * directionMultiplier;
        rotationXY = rotationX * pitchSC[0] * directionMultiplier;
        rotationXZ = pitchSC[1];
    }

    public static Vec3 projectViewFromEntity(Entity entity, double partialTicks)
    {
        double xCoordinate = entity.prevPosX + (entity.posX - entity.prevPosX) * partialTicks;
        double yCoordinate = entity.prevPosY + (entity.posY - entity.prevPosY) * partialTicks;
        double zCoordinate = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * partialTicks;
        double projectedX = xCoordinate + position.xCoord;
        double projectedY = yCoordinate + position.yCoord;
        double projectedZ = zCoordinate + position.zCoord;
        return new Vec3(projectedX, projectedY, projectedZ);
    }

    public static Block getBlockAtEntityViewpoint(World worldIn, Entity entity, float partialTicks)
    {
        double entityX = entity.prevPosX + (entity.posX - entity.prevPosX) * (double)partialTicks;
        double entityY = entity.prevPosY + (entity.posY - entity.prevPosY) * (double)partialTicks;
        double entityZ = entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double)partialTicks;
        double viewX = entityX + position.xCoord;
        double viewY = entityY + position.yCoord;
        double viewZ = entityZ + position.zCoord;
        BlockPos.MutableBlockPos blockPos = VIEW_BLOCK_POS.set(MathHelper.floor_double(viewX), MathHelper.floor_double(viewY), MathHelper.floor_double(viewZ));
        IBlockState blockState = worldIn.getBlockState(blockPos);
        Block block = blockState.getBlock();

        if (block.getMaterial().isLiquid())
        {
            float liquidHeight = 0.0F;

            if (blockState.getBlock() instanceof BlockLiquid)
            {
                liquidHeight = BlockLiquid.getLiquidHeightPercent((Integer)blockState.getValue(BlockLiquid.LEVEL)) - 0.11111111F;
            }

            float liquidSurfaceY = (float)(blockPos.getY() + 1) - liquidHeight;

            if (viewY >= (double)liquidSurfaceY)
            {
                block = worldIn.getBlockState(blockPos.set(blockPos.getX(), blockPos.getY() + 1, blockPos.getZ())).getBlock();
            }
        }

        return block;
    }

    public static Vec3 getPosition()
    {
        return position;
    }

    public static float getRotationX()
    {
        return rotationX;
    }

    public static float getRotationXZ()
    {
        return rotationXZ;
    }

    public static float getRotationZ()
    {
        return rotationZ;
    }

    public static float getRotationYZ()
    {
        return rotationYZ;
    }

    public static float getRotationXY()
    {
        return rotationXY;
    }
}
