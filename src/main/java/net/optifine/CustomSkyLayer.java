package net.optifine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.src.Config;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.optifine.config.ConnectedParser;
import net.optifine.config.Matches;
import net.optifine.config.RangeListInt;
import net.optifine.render.Blender;
import net.optifine.util.NumUtils;
import net.optifine.util.SmoothFloat;
import net.optifine.util.TextureUtils;

public class CustomSkyLayer
{
    public String source = null;
    private int startFadeIn = -1;
    private int endFadeIn = -1;
    private int startFadeOut = -1;
    private int endFadeOut = -1;
    private int blend = 1;
    private boolean rotate = false;
    private float speed = 1.0F;
    private float[] axis;
    private RangeListInt days;
    private int daysLoop;
    private boolean weatherClear;
    private boolean weatherRain;
    private boolean weatherThunder;
    public BiomeGenBase[] biomes;
    public RangeListInt heights;
    private float transition;
    private SmoothFloat smoothPositionBrightness;
    public int textureId;
    private World lastWorld;
    public static final float[] DEFAULT_AXIS = new float[] {1.0F, 0.0F, 0.0F};
    private static final String WEATHER_CLEAR = "clear";
    private static final String WEATHER_RAIN = "rain";
    private static final String WEATHER_THUNDER = "thunder";

    public CustomSkyLayer(Properties props, String defSource)
    {
        this.axis = DEFAULT_AXIS;
        this.days = null;
        this.daysLoop = 8;
        this.weatherClear = true;
        this.weatherRain = false;
        this.weatherThunder = false;
        this.biomes = null;
        this.heights = null;
        this.transition = 1.0F;
        this.smoothPositionBrightness = null;
        this.textureId = -1;
        this.lastWorld = null;
        ConnectedParser connectedParser = new ConnectedParser("CustomSky");
        this.source = props.getProperty("source", defSource);
        this.startFadeIn = this.parseTime(props.getProperty("startFadeIn"));
        this.endFadeIn = this.parseTime(props.getProperty("endFadeIn"));
        this.startFadeOut = this.parseTime(props.getProperty("startFadeOut"));
        this.endFadeOut = this.parseTime(props.getProperty("endFadeOut"));
        this.blend = Blender.parseBlend(props.getProperty("blend"));
        this.rotate = this.parseBoolean(props.getProperty("rotate"), true);
        this.speed = this.parseFloat(props.getProperty("speed"), 1.0F);
        this.axis = this.parseAxis(props.getProperty("axis"), DEFAULT_AXIS);
        this.days = connectedParser.parseRangeListInt(props.getProperty("days"));
        this.daysLoop = connectedParser.parseInt(props.getProperty("daysLoop"), 8);
        List<String> weatherList = this.parseWeatherList(props.getProperty("weather", "clear"));
        this.weatherClear = weatherList.contains("clear");
        this.weatherRain = weatherList.contains("rain");
        this.weatherThunder = weatherList.contains("thunder");
        this.biomes = connectedParser.parseBiomes(props.getProperty("biomes"));
        this.heights = connectedParser.parseRangeListInt(props.getProperty("heights"));
        this.transition = this.parseFloat(props.getProperty("transition"), 1.0F);
    }

    private List<String> parseWeatherList(String str)
    {
        List<String> validWeather = Arrays.<String>asList(new String[] {"clear", "rain", "thunder"});
        List<String> weatherList = new ArrayList();
        String[] tokens = Config.tokenize(str, " ");

        for (int tokenIndex = 0; tokenIndex < tokens.length; ++tokenIndex)
        {
            String token = tokens[tokenIndex];

            if (!validWeather.contains(token))
            {
                Config.warn("Unknown weather: " + token);
            }
            else
            {
                weatherList.add(token);
            }
        }

        return weatherList;
    }

    private int parseTime(String str)
    {
        if (str == null)
        {
            return -1;
        }
        else
        {
            String[] timeParts = Config.tokenize(str, ":");

            if (timeParts.length != 2)
            {
                Config.warn("Invalid time: " + str);
                return -1;
            }
            else
            {
                String hourText = timeParts[0];
                String minuteText = timeParts[1];
                int hour = Config.parseInt(hourText, -1);
                int minute = Config.parseInt(minuteText, -1);

                if (hour >= 0 && hour <= 23 && minute >= 0 && minute <= 59)
                {
                    hour = hour - 6;

                    if (hour < 0)
                    {
                        hour += 24;
                    }

                    int time = hour * 1000 + (int)((double)minute / 60.0D * 1000.0D);
                    return time;
                }
                else
                {
                    Config.warn("Invalid time: " + str);
                    return -1;
                }
            }
        }
    }

    private boolean parseBoolean(String str, boolean defVal)
    {
        if (str == null)
        {
            return defVal;
        }
        else if (str.toLowerCase().equals("true"))
        {
            return true;
        }
        else if (str.toLowerCase().equals("false"))
        {
            return false;
        }
        else
        {
            Config.warn("Unknown boolean: " + str);
            return defVal;
        }
    }

    private float parseFloat(String str, float defVal)
    {
        if (str == null)
        {
            return defVal;
        }
        else
        {
            float value = Config.parseFloat(str, Float.MIN_VALUE);

            if (value == Float.MIN_VALUE)
            {
                Config.warn("Invalid value: " + str);
                return defVal;
            }
            else
            {
                return value;
            }
        }
    }

    private float[] parseAxis(String str, float[] defVal)
    {
        if (str == null)
        {
            return defVal;
        }
        else
        {
            String[] axisParts = Config.tokenize(str, " ");

            if (axisParts.length != 3)
            {
                Config.warn("Invalid axis: " + str);
                return defVal;
            }
            else
            {
                float[] axis = new float[3];

                for (int axisIndex = 0; axisIndex < axisParts.length; ++axisIndex)
                {
                    axis[axisIndex] = Config.parseFloat(axisParts[axisIndex], Float.MIN_VALUE);

                    if (axis[axisIndex] == Float.MIN_VALUE)
                    {
                        Config.warn("Invalid axis: " + str);
                        return defVal;
                    }

                    if (axis[axisIndex] < -1.0F || axis[axisIndex] > 1.0F)
                    {
                        Config.warn("Invalid axis values: " + str);
                        return defVal;
                    }
                }

                float axisX = axis[0];
                float axisY = axis[1];
                float axisZ = axis[2];

                if (axisX * axisX + axisY * axisY + axisZ * axisZ < 1.0E-5F)
                {
                    Config.warn("Invalid axis values: " + str);
                    return defVal;
                }
                else
                {
                    float[] transformedAxis = new float[] {axisZ, axisY, -axisX};
                    return transformedAxis;
                }
            }
        }
    }

    public boolean isValid(String path)
    {
        if (this.source == null)
        {
            Config.warn("No source texture: " + path);
            return false;
        }
        else
        {
            this.source = TextureUtils.fixResourcePath(this.source, TextureUtils.getBasePath(path));

            if (this.startFadeIn >= 0 && this.endFadeIn >= 0 && this.endFadeOut >= 0)
            {
                int fadeInDuration = this.normalizeTime(this.endFadeIn - this.startFadeIn);

                if (this.startFadeOut < 0)
                {
                    this.startFadeOut = this.normalizeTime(this.endFadeOut - fadeInDuration);

                    if (this.timeBetween(this.startFadeOut, this.startFadeIn, this.endFadeIn))
                    {
                        this.startFadeOut = this.endFadeIn;
                    }
                }

                int steadyDuration = this.normalizeTime(this.startFadeOut - this.endFadeIn);
                int fadeOutDuration = this.normalizeTime(this.endFadeOut - this.startFadeOut);
                int hiddenDuration = this.normalizeTime(this.startFadeIn - this.endFadeOut);
                int totalDuration = fadeInDuration + steadyDuration + fadeOutDuration + hiddenDuration;

                if (totalDuration != 24000)
                {
                    Config.warn("Invalid fadeIn/fadeOut times, sum is not 24h: " + totalDuration);
                    return false;
                }
                else if (this.speed < 0.0F)
                {
                    Config.warn("Invalid speed: " + this.speed);
                    return false;
                }
                else if (this.daysLoop <= 0)
                {
                    Config.warn("Invalid daysLoop: " + this.daysLoop);
                    return false;
                }
                else
                {
                    return true;
                }
            }
            else
            {
                Config.warn("Invalid times, required are: startFadeIn, endFadeIn and endFadeOut.");
                return false;
            }
        }
    }

    private int normalizeTime(int timeMc)
    {
        while (timeMc >= 24000)
        {
            timeMc -= 24000;
        }

        while (timeMc < 0)
        {
            timeMc += 24000;
        }

        return timeMc;
    }

    public void render(World world, int timeOfDay, float celestialAngle, float rainStrength, float thunderStrength)
    {
        float positionBrightness = this.getPositionBrightness(world);
        float weatherBrightness = this.getWeatherBrightness(rainStrength, thunderStrength);
        float fadeBrightness = this.getFadeBrightness(timeOfDay);
        float brightness = positionBrightness * weatherBrightness * fadeBrightness;
        brightness = Config.limit(brightness, 0.0F, 1.0F);

        if (brightness >= 1.0E-4F)
        {
            GlStateManager.bindTexture(this.textureId);
            Blender.setupBlend(this.blend, brightness);
            GlStateManager.pushMatrix();

            if (this.rotate)
            {
                float dayFraction = 0.0F;

                if (this.speed != (float)Math.round(this.speed))
                {
                    long day = (world.getWorldTime() + 18000L) / 24000L;
                    double speedFraction = (double)(this.speed % 1.0F);
                    double dayOffset = (double)day * speedFraction;
                    dayFraction = (float)(dayOffset % 1.0D);
                }

                GlStateManager.rotate(360.0F * (dayFraction + celestialAngle * this.speed), this.axis[0], this.axis[1], this.axis[2]);
            }

            Tessellator tessellator = Tessellator.getInstance();
            GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(-90.0F, 0.0F, 0.0F, 1.0F);
            this.renderSide(tessellator, 4);
            GlStateManager.pushMatrix();
            GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
            this.renderSide(tessellator, 1);
            GlStateManager.popMatrix();
            GlStateManager.pushMatrix();
            GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
            this.renderSide(tessellator, 0);
            GlStateManager.popMatrix();
            GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
            this.renderSide(tessellator, 5);
            GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
            this.renderSide(tessellator, 2);
            GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
            this.renderSide(tessellator, 3);
            GlStateManager.popMatrix();
        }
    }

    private float getPositionBrightness(World world)
    {
        if (this.biomes == null && this.heights == null)
        {
            return 1.0F;
        }
        else
        {
            float brightness = this.getPositionBrightnessRaw(world);

            if (this.smoothPositionBrightness == null)
            {
                this.smoothPositionBrightness = new SmoothFloat(brightness, this.transition);
            }

            brightness = this.smoothPositionBrightness.getSmoothValue(brightness);
            return brightness;
        }
    }

    private float getPositionBrightnessRaw(World world)
    {
        Entity entity = Minecraft.getMinecraft().getRenderViewEntity();

        if (entity == null)
        {
            return 0.0F;
        }
        else
        {
            BlockPos blockPos = entity.getPosition();

            if (this.biomes != null)
            {
                BiomeGenBase biome = world.getBiomeGenForCoords(blockPos);

                if (biome == null)
                {
                    return 0.0F;
                }

                if (!Matches.biome(biome, this.biomes))
                {
                    return 0.0F;
                }
            }

            return this.heights != null && !this.heights.isInRange(blockPos.getY()) ? 0.0F : 1.0F;
        }
    }

    private float getWeatherBrightness(float rainStrength, float thunderStrength)
    {
        float clearBrightness = 1.0F - rainStrength;
        float rainBrightness = rainStrength - thunderStrength;
        float weatherBrightness = 0.0F;

        if (this.weatherClear)
        {
            weatherBrightness += clearBrightness;
        }

        if (this.weatherRain)
        {
            weatherBrightness += rainBrightness;
        }

        if (this.weatherThunder)
        {
            weatherBrightness += thunderStrength;
        }

        weatherBrightness = NumUtils.limit(weatherBrightness, 0.0F, 1.0F);
        return weatherBrightness;
    }

    private float getFadeBrightness(int timeOfDay)
    {
        if (this.timeBetween(timeOfDay, this.startFadeIn, this.endFadeIn))
        {
            int fadeInDuration = this.normalizeTime(this.endFadeIn - this.startFadeIn);
            int fadeInTime = this.normalizeTime(timeOfDay - this.startFadeIn);
            return (float)fadeInTime / (float)fadeInDuration;
        }
        else if (this.timeBetween(timeOfDay, this.endFadeIn, this.startFadeOut))
        {
            return 1.0F;
        }
        else if (this.timeBetween(timeOfDay, this.startFadeOut, this.endFadeOut))
        {
            int fadeOutDuration = this.normalizeTime(this.endFadeOut - this.startFadeOut);
            int fadeOutTime = this.normalizeTime(timeOfDay - this.startFadeOut);
            return 1.0F - (float)fadeOutTime / (float)fadeOutDuration;
        }
        else
        {
            return 0.0F;
        }
    }

    private void renderSide(Tessellator tess, int side)
    {
        WorldRenderer worldRenderer = tess.getWorldRenderer();
        double tileU = (double)(side % 3) / 3.0D;
        double tileV = (double)(side / 3) / 2.0D;
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldRenderer.pos(-100.0D, -100.0D, -100.0D).tex(tileU, tileV).endVertex();
        worldRenderer.pos(-100.0D, -100.0D, 100.0D).tex(tileU, tileV + 0.5D).endVertex();
        worldRenderer.pos(100.0D, -100.0D, 100.0D).tex(tileU + 0.3333333333333333D, tileV + 0.5D).endVertex();
        worldRenderer.pos(100.0D, -100.0D, -100.0D).tex(tileU + 0.3333333333333333D, tileV).endVertex();
        tess.draw();
    }

    public boolean isActive(World world, int timeOfDay)
    {
        if (world != this.lastWorld)
        {
            this.lastWorld = world;
            this.smoothPositionBrightness = null;
        }

        if (this.timeBetween(timeOfDay, this.endFadeOut, this.startFadeIn))
        {
            return false;
        }
        else
        {
            if (this.days != null)
            {
                long worldTime = world.getWorldTime();
                long dayTime;

                for (dayTime = worldTime - (long)this.startFadeIn; dayTime < 0L; dayTime += (long)(24000 * this.daysLoop))
                {
                    ;
                }

                int day = (int)(dayTime / 24000L);
                int dayOfLoop = day % this.daysLoop;

                if (!this.days.isInRange(dayOfLoop))
                {
                    return false;
                }
            }

            return true;
        }
    }

    private boolean timeBetween(int timeOfDay, int timeStart, int timeEnd)
    {
        return timeStart <= timeEnd ? timeOfDay >= timeStart && timeOfDay <= timeEnd : timeOfDay >= timeStart || timeOfDay <= timeEnd;
    }

    public String toString()
    {
        return "" + this.source + ", " + this.startFadeIn + "-" + this.endFadeIn + " " + this.startFadeOut + "-" + this.endFadeOut;
    }
}
