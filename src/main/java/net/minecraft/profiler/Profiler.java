package net.minecraft.profiler;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.src.Config;
import net.optifine.Lagometer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Profiler
{
    private static final Logger logger = LogManager.getLogger();
    private final List<String> sectionList = Lists.<String>newArrayList();
    private final List<Long> timestampList = Lists.<Long>newArrayList();
    public boolean profilingEnabled;
    private String profilingSection = "";
    private final Map<String, Long> profilingMap = Maps.<String, Long>newHashMap();
    public boolean profilerGlobalEnabled = true;
    private boolean profilerLocalEnabled;
    private static final String SCHEDULED_EXECUTABLES = "scheduledExecutables";
    private static final String TICK = "tick";
    private static final String PRE_RENDER_ERRORS = "preRenderErrors";
    private static final String RENDER = "render";
    private static final String DISPLAY = "display";
    private static final int HASH_SCHEDULED_EXECUTABLES = "scheduledExecutables".hashCode();
    private static final int HASH_TICK = "tick".hashCode();
    private static final int HASH_PRE_RENDER_ERRORS = "preRenderErrors".hashCode();
    private static final int HASH_RENDER = "render".hashCode();
    private static final int HASH_DISPLAY = "display".hashCode();

    public Profiler()
    {
        this.profilerLocalEnabled = this.profilerGlobalEnabled;
    }

    public void clearProfiling()
    {
        this.profilingMap.clear();
        this.profilingSection = "";
        this.sectionList.clear();
        this.profilerLocalEnabled = this.profilerGlobalEnabled;
    }

    public void startSection(String name)
    {
        if (Lagometer.isActive())
        {
            int i = name.hashCode();

            if (i == HASH_SCHEDULED_EXECUTABLES && name.equals("scheduledExecutables"))
            {
                Lagometer.timerScheduledExecutables.start();
            }
            else if (i == HASH_TICK && name.equals("tick") && Config.isMinecraftThread())
            {
                Lagometer.timerScheduledExecutables.end();
                Lagometer.timerTick.start();
            }
            else if (i == HASH_PRE_RENDER_ERRORS && name.equals("preRenderErrors"))
            {
                Lagometer.timerTick.end();
            }
        }

        if (Config.isFastRender())
        {
            int j = name.hashCode();

            if (j == HASH_RENDER && name.equals("render"))
            {
                GlStateManager.clearEnabled = false;
            }
            else if (j == HASH_DISPLAY && name.equals("display"))
            {
                GlStateManager.clearEnabled = true;
            }
        }

        if (this.profilerLocalEnabled)
        {
            if (this.profilingEnabled)
            {
                if (this.profilingSection.length() > 0)
                {
                    this.profilingSection = this.profilingSection + ".";
                }

                this.profilingSection = this.profilingSection + name;
                this.sectionList.add(this.profilingSection);
                this.timestampList.add(Long.valueOf(System.nanoTime()));
            }
        }
    }

    public void endSection()
    {
        if (this.profilerLocalEnabled)
        {
            if (this.profilingEnabled)
            {
                long i = System.nanoTime();
                long j = this.timestampList.remove(this.timestampList.size() - 1).longValue();
                this.sectionList.remove(this.sectionList.size() - 1);
                long k = i - j;
                Long previousTime = this.profilingMap.get(this.profilingSection);

                if (previousTime != null)
                {
                    this.profilingMap.put(this.profilingSection, Long.valueOf(previousTime.longValue() + k));
                }
                else
                {
                    this.profilingMap.put(this.profilingSection, Long.valueOf(k));
                }

                if (k > 100000000L)
                {
                    logger.warn("Something\'s taking too long! \'" + this.profilingSection + "\' took aprox " + (double)k / 1000000.0D + " ms");
                }

                this.profilingSection = !this.sectionList.isEmpty() ? this.sectionList.get(this.sectionList.size() - 1) : "";
            }
        }
    }

    public List<Profiler.Result> getProfilingData(String profilerName)
    {
        if (!this.profilingEnabled)
        {
            return null;
        }
        else
        {
            Long rootTime = this.profilingMap.get("root");
            Long sectionTime = this.profilingMap.get(profilerName);
            long i = rootTime != null ? rootTime.longValue() : 0L;
            long j = sectionTime != null ? sectionTime.longValue() : -1L;
            List<Profiler.Result> list = Lists.<Profiler.Result>newArrayList();

            if (profilerName.length() > 0)
            {
                profilerName = profilerName + ".";
            }

            long k = 0L;

            for (String s : this.profilingMap.keySet())
            {
                if (s.length() > profilerName.length() && s.startsWith(profilerName) && s.indexOf(".", profilerName.length() + 1) < 0)
                {
                    k += this.profilingMap.get(s).longValue();
                }
            }

            float f = (float)k;

            if (k < j)
            {
                k = j;
            }

            if (i < k)
            {
                i = k;
            }

            for (String text2 : this.profilingMap.keySet())
            {
                if (text2.length() > profilerName.length() && text2.startsWith(profilerName) && text2.indexOf(".", profilerName.length() + 1) < 0)
                {
                    long l = this.profilingMap.get(text2).longValue();
                    double doubleValue = (double)l * 100.0D / (double)k;
                    double doubleValue2 = (double)l * 100.0D / (double)i;
                    String text3 = text2.substring(profilerName.length());
                    list.add(new Profiler.Result(text3, doubleValue, doubleValue2));
                }
            }

            for (String text4 : this.profilingMap.keySet())
            {
                this.profilingMap.put(text4, Long.valueOf(this.profilingMap.get(text4).longValue() * 950L / 1000L));
            }

            if ((float)k > f)
            {
                list.add(new Profiler.Result("unspecified", (double)((float)k - f) * 100.0D / (double)k, (double)((float)k - f) * 100.0D / (double)i));
            }

            Collections.sort(list);
            list.add(0, new Profiler.Result(profilerName, 100.0D, (double)k * 100.0D / (double)i));
            return list;
        }
    }

    public void endStartSection(String name)
    {
        if (this.profilerLocalEnabled)
        {
            this.endSection();
            this.startSection(name);
        }
    }

    public String getNameOfLastSection()
    {
        return this.sectionList.size() == 0 ? "[UNKNOWN]" : this.sectionList.get(this.sectionList.size() - 1);
    }

    public void startSection(Class<?> sectionClass)
    {
        if (this.profilingEnabled)
        {
            this.startSection(sectionClass.getSimpleName());
        }
    }

    public static final class Result implements Comparable<Profiler.Result>
    {
        public double usePercentage;
        public double totalUsePercentage;
        public String profilerName;

        public Result(String profilerName, double usePercentage, double totalUsePercentage)
        {
            this.profilerName = profilerName;
            this.usePercentage = usePercentage;
            this.totalUsePercentage = totalUsePercentage;
        }

        public int compareTo(Profiler.Result other)
        {
            return other.usePercentage < this.usePercentage ? -1 : (other.usePercentage > this.usePercentage ? 1 : other.profilerName.compareTo(this.profilerName));
        }

        public int getColor()
        {
            return (this.profilerName.hashCode() & 11184810) + 4473924;
        }
    }
}
