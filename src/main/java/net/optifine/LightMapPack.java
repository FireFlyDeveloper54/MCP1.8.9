package net.optifine;

import net.minecraft.world.World;

public class LightMapPack
{
    private LightMap lightMap;
    private LightMap lightMapRain;
    private LightMap lightMapThunder;
    private int[] colorBuffer1 = new int[0];
    private int[] colorBuffer2 = new int[0];

    public LightMapPack(LightMap lightMap, LightMap lightMapRain, LightMap lightMapThunder)
    {
        if (lightMapRain != null || lightMapThunder != null)
        {
            if (lightMapRain == null)
            {
                lightMapRain = lightMap;
            }

            if (lightMapThunder == null)
            {
                lightMapThunder = lightMapRain;
            }
        }

        this.lightMap = lightMap;
        this.lightMapRain = lightMapRain;
        this.lightMapThunder = lightMapThunder;
    }

    public boolean updateLightmap(World world, float torchFlickerX, int[] lmColors, boolean nightvision, float partialTicks)
    {
        if (this.lightMapRain == null && this.lightMapThunder == null)
        {
            return this.lightMap.updateLightmap(world, torchFlickerX, lmColors, nightvision);
        }
        else
        {
            int dimensionId = world.provider.getDimensionId();

            if (dimensionId != 1 && dimensionId != -1)
            {
                float rainStrength = world.getRainStrength(partialTicks);
                float thunderStrength = world.getThunderStrength(partialTicks);
                float minBlendWeight = 1.0E-4F;
                boolean hasRain = rainStrength > minBlendWeight;
                boolean hasThunder = thunderStrength > minBlendWeight;

                if (!hasRain && !hasThunder)
                {
                    return this.lightMap.updateLightmap(world, torchFlickerX, lmColors, nightvision);
                }
                else
                {
                    if (rainStrength > 0.0F)
                    {
                        thunderStrength /= rainStrength;
                    }

                    float clearWeight = 1.0F - rainStrength;
                    float rainWeight = rainStrength - thunderStrength;

                    if (this.colorBuffer1.length != lmColors.length)
                    {
                        this.colorBuffer1 = new int[lmColors.length];
                        this.colorBuffer2 = new int[lmColors.length];
                    }

                    int blendCount = 0;
                    int[][] colorBuffers = new int[][] {lmColors, this.colorBuffer1, this.colorBuffer2};
                    float[] blendWeights = new float[3];

                    if (clearWeight > minBlendWeight && this.lightMap.updateLightmap(world, torchFlickerX, colorBuffers[blendCount], nightvision))
                    {
                        blendWeights[blendCount] = clearWeight;
                        ++blendCount;
                    }

                    if (rainWeight > minBlendWeight && this.lightMapRain != null && this.lightMapRain.updateLightmap(world, torchFlickerX, colorBuffers[blendCount], nightvision))
                    {
                        blendWeights[blendCount] = rainWeight;
                        ++blendCount;
                    }

                    if (thunderStrength > minBlendWeight && this.lightMapThunder != null && this.lightMapThunder.updateLightmap(world, torchFlickerX, colorBuffers[blendCount], nightvision))
                    {
                        blendWeights[blendCount] = thunderStrength;
                        ++blendCount;
                    }

                    return blendCount == 2 ? this.blend(colorBuffers[0], blendWeights[0], colorBuffers[1], blendWeights[1]) : (blendCount == 3 ? this.blend(colorBuffers[0], blendWeights[0], colorBuffers[1], blendWeights[1], colorBuffers[2], blendWeights[2]) : true);
                }
            }
            else
            {
                return this.lightMap.updateLightmap(world, torchFlickerX, lmColors, nightvision);
            }
        }
    }

    private boolean blend(int[] cols0, float weight0, int[] cols1, float weight1)
    {
        if (cols1.length != cols0.length)
        {
            return false;
        }
        else
        {
            for (int pixelIndex = 0; pixelIndex < cols0.length; ++pixelIndex)
            {
                int color0 = cols0[pixelIndex];
                int red0 = color0 >> 16 & 255;
                int green0 = color0 >> 8 & 255;
                int blue0 = color0 & 255;
                int color1 = cols1[pixelIndex];
                int red1 = color1 >> 16 & 255;
                int green1 = color1 >> 8 & 255;
                int blue1 = color1 & 255;
                int blendedRed = (int)((float)red0 * weight0 + (float)red1 * weight1);
                int blendedGreen = (int)((float)green0 * weight0 + (float)green1 * weight1);
                int blendedBlue = (int)((float)blue0 * weight0 + (float)blue1 * weight1);
                cols0[pixelIndex] = -16777216 | blendedRed << 16 | blendedGreen << 8 | blendedBlue;
            }

            return true;
        }
    }

    private boolean blend(int[] cols0, float weight0, int[] cols1, float weight1, int[] cols2, float weight2)
    {
        if (cols1.length == cols0.length && cols2.length == cols0.length)
        {
            for (int pixelIndex = 0; pixelIndex < cols0.length; ++pixelIndex)
            {
                int color0 = cols0[pixelIndex];
                int red0 = color0 >> 16 & 255;
                int green0 = color0 >> 8 & 255;
                int blue0 = color0 & 255;
                int color1 = cols1[pixelIndex];
                int red1 = color1 >> 16 & 255;
                int green1 = color1 >> 8 & 255;
                int blue1 = color1 & 255;
                int color2 = cols2[pixelIndex];
                int red2 = color2 >> 16 & 255;
                int green2 = color2 >> 8 & 255;
                int blue2 = color2 & 255;
                int blendedRed = (int)((float)red0 * weight0 + (float)red1 * weight1 + (float)red2 * weight2);
                int blendedGreen = (int)((float)green0 * weight0 + (float)green1 * weight1 + (float)green2 * weight2);
                int blendedBlue = (int)((float)blue0 * weight0 + (float)blue1 * weight1 + (float)blue2 * weight2);
                cols0[pixelIndex] = -16777216 | blendedRed << 16 | blendedGreen << 8 | blendedBlue;
            }

            return true;
        }
        else
        {
            return false;
        }
    }
}
