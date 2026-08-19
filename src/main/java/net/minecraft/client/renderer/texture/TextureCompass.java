package net.minecraft.client.renderer.texture;

import net.minecraft.client.Minecraft;
import optimization.FastTrig;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class TextureCompass extends TextureAtlasSprite
{
    public double currentAngle;
    public double angleDelta;
    public static String locationSprite;

    public TextureCompass(String iconName)
    {
        super(iconName);
        locationSprite = iconName;
    }

    public void updateAnimation()
    {
        Minecraft minecraft = Minecraft.getMinecraft();

        if (minecraft.theWorld != null && minecraft.thePlayer != null)
        {
            this.updateCompass(minecraft.theWorld, minecraft.thePlayer.posX, minecraft.thePlayer.posZ, (double)minecraft.thePlayer.rotationYaw, false, false);
        }
        else
        {
            this.updateCompass((World)null, 0.0D, 0.0D, 0.0D, true, false);
        }
    }

    public void updateCompass(World worldIn, double x, double z, double rotation, boolean skipWorldCalculation, boolean instantUpdate)
    {
        if (!this.framesTextureData.isEmpty())
        {
            double targetAngle = 0.0D;

            if (worldIn != null && !skipWorldCalculation)
            {
                BlockPos blockPos = worldIn.getSpawnPoint();
                double xCoordinate = (double)blockPos.getX() - x;
                double zCoordinate = (double)blockPos.getZ() - z;
                rotation = rotation % 360.0D;
                targetAngle = -((rotation - 90.0D) * Math.PI / 180.0D - FastTrig.atan2(zCoordinate, xCoordinate));

                if (!worldIn.provider.isSurfaceWorld())
                {
                    targetAngle = Math.random() * Math.PI * 2.0D;
                }
            }

            if (instantUpdate)
            {
                this.currentAngle = targetAngle;
            }
            else
            {
                double angleDifference;

                for (angleDifference = targetAngle - this.currentAngle; angleDifference < -Math.PI; angleDifference += (Math.PI * 2D))
                {
                    ;
                }

                while (angleDifference >= Math.PI)
                {
                    angleDifference -= (Math.PI * 2D);
                }

                angleDifference = MathHelper.clamp_double(angleDifference, -1.0D, 1.0D);
                this.angleDelta += angleDifference * 0.1D;
                this.angleDelta *= 0.8D;
                this.currentAngle += this.angleDelta;
            }

            int frameIndex;

            for (frameIndex = (int)((this.currentAngle / (Math.PI * 2D) + 1.0D) * (double)this.framesTextureData.size()) % this.framesTextureData.size(); frameIndex < 0; frameIndex = (frameIndex + this.framesTextureData.size()) % this.framesTextureData.size())
            {
                ;
            }

            if (frameIndex != this.frameCounter)
            {
                this.frameCounter = frameIndex;
                TextureUtil.uploadTextureMipmap(this.framesTextureData.get(this.frameCounter), this.width, this.height, this.originX, this.originY, false, false);
            }
        }
    }
}
