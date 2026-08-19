package net.minecraft.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemDye;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityFirework
{
    public static class Factory implements IParticleFactory
    {
        public EntityFX getEntityFX(int particleID, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, int... parameters)
        {
            EntityFirework.SparkFX spark = new EntityFirework.SparkFX(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn, Minecraft.getMinecraft().effectRenderer);
            spark.setAlphaF(0.99F);
            return spark;
        }
    }

    public static class OverlayFX extends EntityFX
    {
        protected OverlayFX(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn)
        {
            super(worldIn, xCoordIn, yCoordIn, zCoordIn);
            this.particleMaxAge = 4;
        }

        public void renderParticle(WorldRenderer worldRendererIn, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ)
        {
            float flashScale = 7.1F * MathHelper.sin(((float)this.particleAge + partialTicks - 1.0F) * 0.25F * (float)Math.PI);
            this.particleAlpha = 0.6F - ((float)this.particleAge + partialTicks - 1.0F) * 0.25F * 0.5F;
            float renderX = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)partialTicks - interpPosX);
            float renderY = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)partialTicks - interpPosY);
            float renderZ = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)partialTicks - interpPosZ);
            int packedLight = this.getBrightnessForRender(partialTicks);
            int lightU = packedLight >> 16 & 65535;
            int lightV = packedLight & 65535;
            worldRendererIn.pos((double)(renderX - rotationX * flashScale - rotationXY * flashScale), (double)(renderY - rotationZ * flashScale), (double)(renderZ - rotationYZ * flashScale - rotationXZ * flashScale)).tex(0.5D, 0.375D).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(lightU, lightV).endVertex();
            worldRendererIn.pos((double)(renderX - rotationX * flashScale + rotationXY * flashScale), (double)(renderY + rotationZ * flashScale), (double)(renderZ - rotationYZ * flashScale + rotationXZ * flashScale)).tex(0.5D, 0.125D).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(lightU, lightV).endVertex();
            worldRendererIn.pos((double)(renderX + rotationX * flashScale + rotationXY * flashScale), (double)(renderY + rotationZ * flashScale), (double)(renderZ + rotationYZ * flashScale + rotationXZ * flashScale)).tex(0.25D, 0.125D).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(lightU, lightV).endVertex();
            worldRendererIn.pos((double)(renderX + rotationX * flashScale - rotationXY * flashScale), (double)(renderY - rotationZ * flashScale), (double)(renderZ + rotationYZ * flashScale - rotationXZ * flashScale)).tex(0.25D, 0.375D).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(lightU, lightV).endVertex();
        }
    }

    public static class SparkFX extends EntityFX
    {
        private int baseTextureIndex = 160;
        private boolean trail;
        private boolean twinkle;
        private final EffectRenderer effectRenderer;
        private float fadeColourRed;
        private float fadeColourGreen;
        private float fadeColourBlue;
        private boolean hasFadeColour;

        public SparkFX(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, EffectRenderer effectRendererIn)
        {
            super(worldIn, xCoordIn, yCoordIn, zCoordIn);
            this.motionX = xSpeedIn;
            this.motionY = ySpeedIn;
            this.motionZ = zSpeedIn;
            this.effectRenderer = effectRendererIn;
            this.particleScale *= 0.75F;
            this.particleMaxAge = 48 + this.rand.nextInt(12);
            this.noClip = false;
        }

        public void setTrail(boolean trailIn)
        {
            this.trail = trailIn;
        }

        public void setTwinkle(boolean twinkleIn)
        {
            this.twinkle = twinkleIn;
        }

        public void setColour(int colour)
        {
            float red = (float)((colour & 16711680) >> 16) / 255.0F;
            float green = (float)((colour & 65280) >> 8) / 255.0F;
            float blue = (float)((colour & 255) >> 0) / 255.0F;
            this.setRBGColorF(red, green, blue);
        }

        public void setFadeColour(int faceColour)
        {
            this.fadeColourRed = (float)((faceColour & 16711680) >> 16) / 255.0F;
            this.fadeColourGreen = (float)((faceColour & 65280) >> 8) / 255.0F;
            this.fadeColourBlue = (float)((faceColour & 255) >> 0) / 255.0F;
            this.hasFadeColour = true;
        }

        public AxisAlignedBB getCollisionBoundingBox()
        {
            return null;
        }

        public boolean canBePushed()
        {
            return false;
        }

        public void renderParticle(WorldRenderer worldRendererIn, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ)
        {
            if (!this.twinkle || this.particleAge < this.particleMaxAge / 3 || (this.particleAge + this.particleMaxAge) / 3 % 2 == 0)
            {
                super.renderParticle(worldRendererIn, entityIn, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
            }
        }

        public void onUpdate()
        {
            this.prevPosX = this.posX;
            this.prevPosY = this.posY;
            this.prevPosZ = this.posZ;

            if (this.particleAge++ >= this.particleMaxAge)
            {
                this.setDead();
            }

            if (this.particleAge > this.particleMaxAge / 2)
            {
                this.setAlphaF(1.0F - ((float)this.particleAge - (float)(this.particleMaxAge / 2)) / (float)this.particleMaxAge);

                if (this.hasFadeColour)
                {
                    this.particleRed += (this.fadeColourRed - this.particleRed) * 0.2F;
                    this.particleGreen += (this.fadeColourGreen - this.particleGreen) * 0.2F;
                    this.particleBlue += (this.fadeColourBlue - this.particleBlue) * 0.2F;
                }
            }

            this.setParticleTextureIndex(this.baseTextureIndex + (7 - this.particleAge * 8 / this.particleMaxAge));
            this.motionY -= 0.004D;
            this.moveEntity(this.motionX, this.motionY, this.motionZ);
            this.motionX *= 0.9100000262260437D;
            this.motionY *= 0.9100000262260437D;
            this.motionZ *= 0.9100000262260437D;

            if (this.onGround)
            {
                this.motionX *= 0.699999988079071D;
                this.motionZ *= 0.699999988079071D;
            }

            if (this.trail && this.particleAge < this.particleMaxAge / 2 && (this.particleAge + this.particleMaxAge) % 2 == 0)
            {
                EntityFirework.SparkFX trailSpark = new EntityFirework.SparkFX(this.worldObj, this.posX, this.posY, this.posZ, 0.0D, 0.0D, 0.0D, this.effectRenderer);
                trailSpark.setAlphaF(0.99F);
                trailSpark.setRBGColorF(this.particleRed, this.particleGreen, this.particleBlue);
                trailSpark.particleAge = trailSpark.particleMaxAge / 2;

                if (this.hasFadeColour)
                {
                    trailSpark.hasFadeColour = true;
                    trailSpark.fadeColourRed = this.fadeColourRed;
                    trailSpark.fadeColourGreen = this.fadeColourGreen;
                    trailSpark.fadeColourBlue = this.fadeColourBlue;
                }

                trailSpark.twinkle = this.twinkle;
                this.effectRenderer.addEffect(trailSpark);
            }
        }

        public int getBrightnessForRender(float partialTicks)
        {
            return 15728880;
        }

        public float getBrightness(float partialTicks)
        {
            return 1.0F;
        }
    }

    public static class StarterFX extends EntityFX
    {
        private int fireworkAge;
        private final EffectRenderer theEffectRenderer;
        private NBTTagList fireworkExplosions;
        boolean twinkle;

        public StarterFX(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, EffectRenderer effectRendererIn, NBTTagCompound fireworkTag)
        {
            super(worldIn, xCoordIn, yCoordIn, zCoordIn, 0.0D, 0.0D, 0.0D);
            this.motionX = xSpeedIn;
            this.motionY = ySpeedIn;
            this.motionZ = zSpeedIn;
            this.theEffectRenderer = effectRendererIn;
            this.particleMaxAge = 8;

            if (fireworkTag != null)
            {
                this.fireworkExplosions = fireworkTag.getTagList("Explosions", 10);

                if (this.fireworkExplosions.tagCount() == 0)
                {
                    this.fireworkExplosions = null;
                }
                else
                {
                    this.particleMaxAge = this.fireworkExplosions.tagCount() * 2 - 1;

                    for (int explosionIndex = 0; explosionIndex < this.fireworkExplosions.tagCount(); ++explosionIndex)
                    {
                        NBTTagCompound explosionTag = this.fireworkExplosions.getCompoundTagAt(explosionIndex);

                        if (explosionTag.getBoolean("Flicker"))
                        {
                            this.twinkle = true;
                            this.particleMaxAge += 15;
                            break;
                        }
                    }
                }
            }
        }

        public void renderParticle(WorldRenderer worldRendererIn, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ)
        {
        }

        public void onUpdate()
        {
            if (this.fireworkAge == 0 && this.fireworkExplosions != null)
            {
                boolean farFromCamera = this.isFarFromCamera();
                boolean hasLargeBlast = false;

                if (this.fireworkExplosions.tagCount() >= 3)
                {
                    hasLargeBlast = true;
                }
                else
                {
                    for (int explosionIndex = 0; explosionIndex < this.fireworkExplosions.tagCount(); ++explosionIndex)
                    {
                        NBTTagCompound explosionTag = this.fireworkExplosions.getCompoundTagAt(explosionIndex);

                        if (explosionTag.getByte("Type") == 1)
                        {
                            hasLargeBlast = true;
                            break;
                        }
                    }
                }

                String blastSound = "fireworks." + (hasLargeBlast ? "largeBlast" : "blast") + (farFromCamera ? "_far" : "");
                this.worldObj.playSound(this.posX, this.posY, this.posZ, blastSound, 20.0F, 0.95F + this.rand.nextFloat() * 0.1F, true);
            }

            if (this.fireworkAge % 2 == 0 && this.fireworkExplosions != null && this.fireworkAge / 2 < this.fireworkExplosions.tagCount())
            {
                int explosionIndex = this.fireworkAge / 2;
                NBTTagCompound explosionTag = this.fireworkExplosions.getCompoundTagAt(explosionIndex);
                int explosionType = explosionTag.getByte("Type");
                boolean hasTrail = explosionTag.getBoolean("Trail");
                boolean hasTwinkle = explosionTag.getBoolean("Flicker");
                int[] colors = explosionTag.getIntArray("Colors");
                int[] fadeColors = explosionTag.getIntArray("FadeColors");

                if (colors.length == 0)
                {
                    colors = new int[] {ItemDye.dyeColors[0]};
                }

                if (explosionType == 1)
                {
                    this.createBall(0.5D, 4, colors, fadeColors, hasTrail, hasTwinkle);
                }
                else if (explosionType == 2)
                {
                    this.createShaped(0.5D, new double[][] {{0.0D, 1.0D}, {0.3455D, 0.309D}, {0.9511D, 0.309D}, {0.3795918367346939D, -0.12653061224489795D}, {0.6122448979591837D, -0.8040816326530612D}, {0.0D, -0.35918367346938773D}}, colors, fadeColors, hasTrail, hasTwinkle, false);
                }
                else if (explosionType == 3)
                {
                    this.createShaped(0.5D, new double[][] {{0.0D, 0.2D}, {0.2D, 0.2D}, {0.2D, 0.6D}, {0.6D, 0.6D}, {0.6D, 0.2D}, {0.2D, 0.2D}, {0.2D, 0.0D}, {0.4D, 0.0D}, {0.4D, -0.6D}, {0.2D, -0.6D}, {0.2D, -0.4D}, {0.0D, -0.4D}}, colors, fadeColors, hasTrail, hasTwinkle, true);
                }
                else if (explosionType == 4)
                {
                    this.createBurst(colors, fadeColors, hasTrail, hasTwinkle);
                }
                else
                {
                    this.createBall(0.25D, 2, colors, fadeColors, hasTrail, hasTwinkle);
                }

                int primaryColor = colors[0];
                float red = (float)((primaryColor & 16711680) >> 16) / 255.0F;
                float green = (float)((primaryColor & 65280) >> 8) / 255.0F;
                float blue = (float)((primaryColor & 255) >> 0) / 255.0F;
                EntityFirework.OverlayFX overlay = new EntityFirework.OverlayFX(this.worldObj, this.posX, this.posY, this.posZ);
                overlay.setRBGColorF(red, green, blue);
                this.theEffectRenderer.addEffect(overlay);
            }

            ++this.fireworkAge;

            if (this.fireworkAge > this.particleMaxAge)
            {
                if (this.twinkle)
                {
                    boolean farFromCamera = this.isFarFromCamera();
                    String twinkleSound = "fireworks." + (farFromCamera ? "twinkle_far" : "twinkle");
                    this.worldObj.playSound(this.posX, this.posY, this.posZ, twinkleSound, 20.0F, 0.9F + this.rand.nextFloat() * 0.15F, true);
                }

                this.setDead();
            }
        }

        private boolean isFarFromCamera()
        {
            Minecraft minecraft = Minecraft.getMinecraft();
            return minecraft == null || minecraft.getRenderViewEntity() == null || minecraft.getRenderViewEntity().getDistanceSq(this.posX, this.posY, this.posZ) >= 256.0D;
        }

        private void createParticle(double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, int[] colours, int[] fadeColours, boolean trail, boolean twinkleIn)
        {
            EntityFirework.SparkFX spark = new EntityFirework.SparkFX(this.worldObj, x, y, z, xSpeed, ySpeed, zSpeed, this.theEffectRenderer);
            spark.setAlphaF(0.99F);
            spark.setTrail(trail);
            spark.setTwinkle(twinkleIn);
            int colorIndex = this.rand.nextInt(colours.length);
            spark.setColour(colours[colorIndex]);

            if (fadeColours != null && fadeColours.length > 0)
            {
                spark.setFadeColour(fadeColours[this.rand.nextInt(fadeColours.length)]);
            }

            this.theEffectRenderer.addEffect(spark);
        }

        private void createBall(double speed, int size, int[] colours, int[] fadeColours, boolean trail, boolean twinkleIn)
        {
            double xCoordinate = this.posX;
            double yCoordinate = this.posY;
            double zCoordinate = this.posZ;

            for (int offsetY = -size; offsetY <= size; ++offsetY)
            {
                for (int offsetX = -size; offsetX <= size; ++offsetX)
                {
                    for (int offsetZ = -size; offsetZ <= size; ++offsetZ)
                    {
                        double velocityX = (double)offsetX + (this.rand.nextDouble() - this.rand.nextDouble()) * 0.5D;
                        double velocityY = (double)offsetY + (this.rand.nextDouble() - this.rand.nextDouble()) * 0.5D;
                        double velocityZ = (double)offsetZ + (this.rand.nextDouble() - this.rand.nextDouble()) * 0.5D;
                        double velocityScale = (double)MathHelper.sqrt_double(velocityX * velocityX + velocityY * velocityY + velocityZ * velocityZ) / speed + this.rand.nextGaussian() * 0.05D;
                        this.createParticle(xCoordinate, yCoordinate, zCoordinate, velocityX / velocityScale, velocityY / velocityScale, velocityZ / velocityScale, colours, fadeColours, trail, twinkleIn);

                        if (offsetY != -size && offsetY != size && offsetX != -size && offsetX != size)
                        {
                            offsetZ += size * 2 - 1;
                        }
                    }
                }
            }
        }

        private void createShaped(double speed, double[][] shape, int[] colours, int[] fadeColours, boolean trail, boolean twinkleIn, boolean mirror)
        {
            double firstPointX = shape[0][0];
            double firstPointY = shape[0][1];
            this.createParticle(this.posX, this.posY, this.posZ, firstPointX * speed, firstPointY * speed, 0.0D, colours, fadeColours, trail, twinkleIn);
            float baseAngle = this.rand.nextFloat() * (float)Math.PI;
            double rotationStepScale = mirror ? 0.034D : 0.34D;

            for (int rotationIndex = 0; rotationIndex < 3; ++rotationIndex)
            {
                double rotationAngle = (double)baseAngle + (double)((float)rotationIndex * (float)Math.PI) * rotationStepScale;
                double previousPointX = firstPointX;
                double previousPointY = firstPointY;

                for (int pointIndex = 1; pointIndex < shape.length; ++pointIndex)
                {
                    double nextPointX = shape[pointIndex][0];
                    double nextPointY = shape[pointIndex][1];

                    for (double interpolation = 0.25D; interpolation <= 1.0D; interpolation += 0.25D)
                    {
                        double rotatedX = (previousPointX + (nextPointX - previousPointX) * interpolation) * speed;
                        double rotatedY = (previousPointY + (nextPointY - previousPointY) * interpolation) * speed;
                        double rotatedZ = rotatedX * Math.sin(rotationAngle);
                        rotatedX = rotatedX * Math.cos(rotationAngle);

                        for (double mirrorSign = -1.0D; mirrorSign <= 1.0D; mirrorSign += 2.0D)
                        {
                            this.createParticle(this.posX, this.posY, this.posZ, rotatedX * mirrorSign, rotatedY, rotatedZ * mirrorSign, colours, fadeColours, trail, twinkleIn);
                        }
                    }

                    previousPointX = nextPointX;
                    previousPointY = nextPointY;
                }
            }
        }

        private void createBurst(int[] colours, int[] fadeColours, boolean trail, boolean twinkleIn)
        {
            double randomOffsetX = this.rand.nextGaussian() * 0.05D;
            double randomOffsetZ = this.rand.nextGaussian() * 0.05D;

            for (int particleIndex = 0; particleIndex < 70; ++particleIndex)
            {
                double particleSpeedX = this.motionX * 0.5D + this.rand.nextGaussian() * 0.15D + randomOffsetX;
                double particleSpeedZ = this.motionZ * 0.5D + this.rand.nextGaussian() * 0.15D + randomOffsetZ;
                double particleSpeedY = this.motionY * 0.5D + this.rand.nextDouble() * 0.5D;
                this.createParticle(this.posX, this.posY, this.posZ, particleSpeedX, particleSpeedY, particleSpeedZ, colours, fadeColours, trail, twinkleIn);
            }
        }

        public int getFXLayer()
        {
            return 0;
        }
    }
}
