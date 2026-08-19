package net.optifine;

import net.minecraft.src.Config;
import net.minecraft.world.World;

public class LightMap
{
    private CustomColormap lightMapRgb = null;
    private float[][] sunRgbs = new float[16][3];
    private float[][] torchRgbs = new float[16][3];

    public LightMap(CustomColormap lightMapRgb)
    {
        this.lightMapRgb = lightMapRgb;
    }

    public CustomColormap getColormap()
    {
        return this.lightMapRgb;
    }

    public boolean updateLightmap(World world, float torchFlickerX, int[] lmColors, boolean nightvision)
    {
        if (this.lightMapRgb == null)
        {
            return false;
        }
        else
        {
            int height = this.lightMapRgb.getHeight();

            if (nightvision && height < 64)
            {
                return false;
            }
            else
            {
                int width = this.lightMapRgb.getWidth();

                if (width < 16)
                {
                    warn("Invalid lightmap width: " + width);
                    this.lightMapRgb = null;
                    return false;
                }
                else
                {
                    int sourceOffset = 0;

                    if (nightvision)
                    {
                        sourceOffset = width * 16 * 2;
                    }

                    float sunBrightness = 1.1666666F * (world.getSunBrightness(1.0F) - 0.2F);

                    if (world.getLastLightningBolt() > 0)
                    {
                        sunBrightness = 1.0F;
                    }

                    sunBrightness = Config.limitTo1(sunBrightness);
                    float sunColumn = sunBrightness * (float)(width - 1);
                    float torchColumn = Config.limitTo1(torchFlickerX + 0.5F) * (float)(width - 1);
                    float gamma = Config.limitTo1(Config.getGameSettings().gammaSetting);
                    boolean hasGamma = gamma > 1.0E-4F;
                    float[][] mapRgbs = this.lightMapRgb.getColorsRgb();
                    this.getLightMapColumn(mapRgbs, sunColumn, sourceOffset, width, this.sunRgbs);
                    this.getLightMapColumn(mapRgbs, torchColumn, sourceOffset + 16 * width, width, this.torchRgbs);
                    float[] combinedRgb = new float[3];

                    for (int sunIndex = 0; sunIndex < 16; ++sunIndex)
                    {
                        for (int torchIndex = 0; torchIndex < 16; ++torchIndex)
                        {
                            for (int channelIndex = 0; channelIndex < 3; ++channelIndex)
                            {
                                float channelValue = Config.limitTo1(this.sunRgbs[sunIndex][channelIndex] + this.torchRgbs[torchIndex][channelIndex]);

                                if (hasGamma)
                                {
                                    float gammaValue = 1.0F - channelValue;
                                    gammaValue = 1.0F - gammaValue * gammaValue * gammaValue * gammaValue;
                                    channelValue = gamma * gammaValue + (1.0F - gamma) * channelValue;
                                }

                                combinedRgb[channelIndex] = channelValue;
                            }

                            int red = (int)(combinedRgb[0] * 255.0F);
                            int green = (int)(combinedRgb[1] * 255.0F);
                            int blue = (int)(combinedRgb[2] * 255.0F);
                            lmColors[sunIndex * 16 + torchIndex] = -16777216 | red << 16 | green << 8 | blue;
                        }
                    }

                    return true;
                }
            }
        }
    }

    private void getLightMapColumn(float[][] origMap, float x, int offset, int width, float[][] colRgb)
    {
        int floorX = (int)Math.floor((double)x);
        int ceilX = (int)Math.ceil((double)x);

        if (floorX == ceilX)
        {
            for (int lightIndex = 0; lightIndex < 16; ++lightIndex)
            {
                float[] sourceRgb = origMap[offset + lightIndex * width + floorX];
                float[] targetRgb = colRgb[lightIndex];
                targetRgb[0] = sourceRgb[0];
                targetRgb[1] = sourceRgb[1];
                targetRgb[2] = sourceRgb[2];
            }
        }
        else
        {
            float floorWeight = 1.0F - (x - (float)floorX);
            float ceilWeight = 1.0F - ((float)ceilX - x);

            for (int lightIndex = 0; lightIndex < 16; ++lightIndex)
            {
                float[] floorRgb = origMap[offset + lightIndex * width + floorX];
                float[] ceilRgb = origMap[offset + lightIndex * width + ceilX];
                float[] targetRgb = colRgb[lightIndex];

                for (int channelIndex = 0; channelIndex < 3; ++channelIndex)
                {
                    targetRgb[channelIndex] = floorRgb[channelIndex] * floorWeight + ceilRgb[channelIndex] * ceilWeight;
                }
            }
        }
    }

    private static void dbg(String str)
    {
        Config.dbg("CustomColors: " + str);
    }

    private static void warn(String str)
    {
        Config.warn("CustomColors: " + str);
    }
}
