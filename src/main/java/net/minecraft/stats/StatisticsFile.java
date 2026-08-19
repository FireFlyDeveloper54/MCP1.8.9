package net.minecraft.stats;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.S37PacketStatistics;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IJsonSerializable;
import net.minecraft.util.TupleIntJsonSerializable;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StatisticsFile extends StatFileWriter
{
    private static final Logger logger = LogManager.getLogger();
    private final MinecraftServer mcServer;
    private final File statsFile;
    private final Set<StatBase> dirtyStats = Sets.<StatBase>newHashSet();
    private int lastStatRequest = -300;
    private boolean hasUnsentAchievement = false;

    public StatisticsFile(MinecraftServer serverIn, File statsFileIn)
    {
        this.mcServer = serverIn;
        this.statsFile = statsFileIn;
    }

    public void readStatFile()
    {
        if (this.statsFile.isFile())
        {
            try
            {
                this.statsData.clear();
                this.statsData.putAll(this.parseJson(FileUtils.readFileToString(this.statsFile)));
            }
            catch (IOException iOException)
            {
                logger.error((String)("Couldn\'t read statistics file " + this.statsFile), (Throwable)iOException);
            }
            catch (JsonParseException jsonParseException)
            {
                logger.error((String)("Couldn\'t parse statistics file " + this.statsFile), (Throwable)jsonParseException);
            }
        }
    }

    public void saveStatFile()
    {
        try
        {
            FileUtils.writeStringToFile(this.statsFile, dumpJson(this.statsData));
        }
        catch (IOException iOException)
        {
            logger.error((String)"Couldn\'t save stats", (Throwable)iOException);
        }
    }

    public void unlockAchievement(EntityPlayer playerIn, StatBase statIn, int newValue)
    {
        int i = statIn.isAchievement() ? this.readStat(statIn) : 0;
        super.unlockAchievement(playerIn, statIn, newValue);
        this.dirtyStats.add(statIn);

        if (statIn.isAchievement() && i == 0 && newValue > 0)
        {
            this.hasUnsentAchievement = true;

            if (this.mcServer.isAnnouncingPlayerAchievements())
            {
                this.mcServer.getConfigurationManager().sendChatMsg(new ChatComponentTranslation("chat.type.achievement", new Object[] {playerIn.getDisplayName(), statIn.createChatComponent()}));
            }
        }

        if (statIn.isAchievement() && i > 0 && newValue == 0)
        {
            this.hasUnsentAchievement = true;

            if (this.mcServer.isAnnouncingPlayerAchievements())
            {
                this.mcServer.getConfigurationManager().sendChatMsg(new ChatComponentTranslation("chat.type.achievement.taken", new Object[] {playerIn.getDisplayName(), statIn.createChatComponent()}));
            }
        }
    }

    public Set<StatBase> getDirtyStats()
    {
        Set<StatBase> set = Sets.newHashSet(this.dirtyStats);
        this.dirtyStats.clear();
        this.hasUnsentAchievement = false;
        return set;
    }

    public Map<StatBase, TupleIntJsonSerializable> parseJson(String json)
    {
        JsonElement jsonElement = (new JsonParser()).parse(json);

        if (!jsonElement.isJsonObject())
        {
            return Maps.<StatBase, TupleIntJsonSerializable>newHashMap();
        }
        else
        {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            Map<StatBase, TupleIntJsonSerializable> map = Maps.<StatBase, TupleIntJsonSerializable>newHashMap();

            for (Entry<String, JsonElement> entry : jsonObject.entrySet())
            {
                StatBase statBase = StatList.getOneShotStat((String)entry.getKey());

                if (statBase != null)
                {
                    TupleIntJsonSerializable tupleIntJsonSerializable = new TupleIntJsonSerializable();

                    if (((JsonElement)entry.getValue()).isJsonPrimitive() && ((JsonElement)entry.getValue()).getAsJsonPrimitive().isNumber())
                    {
                        tupleIntJsonSerializable.setIntegerValue(((JsonElement)entry.getValue()).getAsInt());
                    }
                    else if (((JsonElement)entry.getValue()).isJsonObject())
                    {
                        JsonObject jsonobject1 = ((JsonElement)entry.getValue()).getAsJsonObject();

                        if (jsonobject1.has("value") && jsonobject1.get("value").isJsonPrimitive() && jsonobject1.get("value").getAsJsonPrimitive().isNumber())
                        {
                            tupleIntJsonSerializable.setIntegerValue(jsonobject1.getAsJsonPrimitive("value").getAsInt());
                        }

                        if (jsonobject1.has("progress") && statBase.getSerializableClass() != null)
                        {
                            try
                            {
                                Constructor <? extends IJsonSerializable > constructor = statBase.getSerializableClass().getConstructor(new Class[0]);
                                IJsonSerializable ijsonserializable = (IJsonSerializable)constructor.newInstance(new Object[0]);
                                ijsonserializable.fromJson(jsonobject1.get("progress"));
                                tupleIntJsonSerializable.setJsonSerializableValue(ijsonserializable);
                            }
                            catch (Throwable throwable)
                            {
                                logger.warn("Invalid statistic progress in " + this.statsFile, throwable);
                            }
                        }
                    }

                    map.put(statBase, tupleIntJsonSerializable);
                }
                else
                {
                    logger.warn("Invalid statistic in " + this.statsFile + ": Don\'t know what " + (String)entry.getKey() + " is");
                }
            }

            return map;
        }
    }

    public static String dumpJson(Map<StatBase, TupleIntJsonSerializable> stats)
    {
        JsonObject jsonobject = new JsonObject();

        for (Entry<StatBase, TupleIntJsonSerializable> entry : stats.entrySet())
        {
            if (((TupleIntJsonSerializable)entry.getValue()).getJsonSerializableValue() != null)
            {
                JsonObject jsonobject1 = new JsonObject();
                jsonobject1.addProperty("value", (Number)Integer.valueOf(((TupleIntJsonSerializable)entry.getValue()).getIntegerValue()));

                try
                {
                    jsonobject1.add("progress", ((TupleIntJsonSerializable)entry.getValue()).getJsonSerializableValue().getSerializableElement());
                }
                catch (Throwable throwable)
                {
                    logger.warn("Couldn\'t save statistic " + ((StatBase)entry.getKey()).getStatName() + ": error serializing progress", throwable);
                }

                jsonobject.add(((StatBase)entry.getKey()).statId, jsonobject1);
            }
            else
            {
                jsonobject.addProperty(((StatBase)entry.getKey()).statId, (Number)Integer.valueOf(((TupleIntJsonSerializable)entry.getValue()).getIntegerValue()));
            }
        }

        return jsonobject.toString();
    }

    public void markAllDirty()
    {
        for (StatBase statBase : this.statsData.keySet())
        {
            this.dirtyStats.add(statBase);
        }
    }

    public void sendStats(EntityPlayerMP player)
    {
        int i = this.mcServer.getTickCounter();
        Map<StatBase, Integer> map = Maps.<StatBase, Integer>newHashMap();

        if (this.hasUnsentAchievement || i - this.lastStatRequest > 300)
        {
            this.lastStatRequest = i;

            for (StatBase statBase : this.getDirtyStats())
            {
                map.put(statBase, Integer.valueOf(this.readStat(statBase)));
            }
        }

        player.playerNetServerHandler.sendPacket(new S37PacketStatistics(map));
    }

    public void sendAchievements(EntityPlayerMP player)
    {
        Map<StatBase, Integer> map = Maps.<StatBase, Integer>newHashMap();

        for (Achievement achievement : AchievementList.achievementList)
        {
            if (this.hasAchievementUnlocked(achievement))
            {
                map.put(achievement, Integer.valueOf(this.readStat(achievement)));
                this.dirtyStats.remove(achievement);
            }
        }

        player.playerNetServerHandler.sendPacket(new S37PacketStatistics(map));
    }

    public boolean hasUnsentAchievement()
    {
        return this.hasUnsentAchievement;
    }
}
