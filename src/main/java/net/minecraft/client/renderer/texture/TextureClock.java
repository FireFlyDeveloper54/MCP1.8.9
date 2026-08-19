package net.minecraft.client.renderer.texture;

import net.minecraft.client.Minecraft;
import net.minecraft.util.MathHelper;

public class TextureClock extends TextureAtlasSprite
{
    private double currentAngle;
    private double angleDelta;

    public TextureClock(String iconName)
    {
        super(iconName);
    }

    public void updateAnimation()
    {
        if (!this.framesTextureData.isEmpty())
        {
            Minecraft minecraft = Minecraft.getMinecraft();
            double targetAngle = 0.0D;

            if (minecraft.theWorld != null && minecraft.thePlayer != null)
            {
                targetAngle = (double)minecraft.theWorld.getCelestialAngle(1.0F);

                if (!minecraft.theWorld.provider.isSurfaceWorld())
                {
                    targetAngle = Math.random();
                }
            }

            double angleDifference;

            for (angleDifference = targetAngle - this.currentAngle; angleDifference < -0.5D; ++angleDifference)
            {
                ;
            }

            while (angleDifference >= 0.5D)
            {
                --angleDifference;
            }

            angleDifference = MathHelper.clamp_double(angleDifference, -1.0D, 1.0D);
            this.angleDelta += angleDifference * 0.1D;
            this.angleDelta *= 0.8D;
            this.currentAngle += this.angleDelta;
            int frameIndex;

            for (frameIndex = (int)((this.currentAngle + 1.0D) * (double)this.framesTextureData.size()) % this.framesTextureData.size(); frameIndex < 0; frameIndex = (frameIndex + this.framesTextureData.size()) % this.framesTextureData.size())
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
