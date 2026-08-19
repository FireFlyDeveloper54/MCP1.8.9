package net.minecraft.server.management;

import com.google.common.base.Charsets;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.ProfileLookupCallback;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import org.apache.commons.io.IOUtils;

public class PlayerProfileCache
{
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
    private final Map<String, PlayerProfileCache.ProfileEntry> usernameToProfileEntryMap = Maps.<String, PlayerProfileCache.ProfileEntry>newHashMap();
    private final Map<UUID, PlayerProfileCache.ProfileEntry> uuidToProfileEntryMap = Maps.<UUID, PlayerProfileCache.ProfileEntry>newHashMap();
    private final LinkedList<GameProfile> gameProfiles = Lists.<GameProfile>newLinkedList();
    private final MinecraftServer mcServer;
    protected final Gson gson;
    private final File usercacheFile;
    private static final ParameterizedType TYPE = new ParameterizedType()
    {
        public Type[] getActualTypeArguments()
        {
            return new Type[] {PlayerProfileCache.ProfileEntry.class};
        }
        public Type getRawType()
        {
            return List.class;
        }
        public Type getOwnerType()
        {
            return null;
        }
    };

    public PlayerProfileCache(MinecraftServer server, File cacheFile)
    {
        this.mcServer = server;
        this.usercacheFile = cacheFile;
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeHierarchyAdapter(PlayerProfileCache.ProfileEntry.class, new PlayerProfileCache.Serializer());
        this.gson = gsonBuilder.create();
        this.load();
    }

    private static GameProfile getGameProfile(MinecraftServer server, String username)
    {
        final GameProfile[] lookedUpProfiles = new GameProfile[1];
        ProfileLookupCallback lookupCallback = new ProfileLookupCallback()
        {
            public void onProfileLookupSucceeded(GameProfile gameProfile)
            {
                lookedUpProfiles[0] = gameProfile;
            }
            public void onProfileLookupFailed(GameProfile gameProfile, Exception exception)
            {
                lookedUpProfiles[0] = null;
            }
        };
        server.getGameProfileRepository().findProfilesByNames(new String[] {username}, Agent.MINECRAFT, lookupCallback);

        if (!server.isServerInOnlineMode() && lookedUpProfiles[0] == null)
        {
            UUID offlineUuid = EntityPlayer.getUUID(new GameProfile((UUID)null, username));
            GameProfile offlineProfile = new GameProfile(offlineUuid, username);
            lookupCallback.onProfileLookupSucceeded(offlineProfile);
        }

        return lookedUpProfiles[0];
    }

    public void addEntry(GameProfile gameProfile)
    {
        this.addEntry(gameProfile, (Date)null);
    }

    private void addEntry(GameProfile gameProfile, Date expirationDate)
    {
        UUID uuid = gameProfile.getId();

        if (expirationDate == null)
        {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(2, 1);
            expirationDate = calendar.getTime();
        }

        String lowercaseName = gameProfile.getName().toLowerCase(Locale.ROOT);
        PlayerProfileCache.ProfileEntry profileEntry = new PlayerProfileCache.ProfileEntry(gameProfile, expirationDate);

        if (this.uuidToProfileEntryMap.containsKey(uuid))
        {
            PlayerProfileCache.ProfileEntry previousEntry = (PlayerProfileCache.ProfileEntry)this.uuidToProfileEntryMap.get(uuid);
            this.usernameToProfileEntryMap.remove(previousEntry.getGameProfile().getName().toLowerCase(Locale.ROOT));
            this.gameProfiles.remove(gameProfile);
        }

        this.usernameToProfileEntryMap.put(lowercaseName, profileEntry);
        this.uuidToProfileEntryMap.put(uuid, profileEntry);
        this.gameProfiles.addFirst(gameProfile);
        this.save();
    }

    public GameProfile getGameProfileForUsername(String username)
    {
        String lowercaseUsername = username.toLowerCase(Locale.ROOT);
        PlayerProfileCache.ProfileEntry profileEntry = (PlayerProfileCache.ProfileEntry)this.usernameToProfileEntryMap.get(lowercaseUsername);

        if (profileEntry != null && (new Date()).getTime() >= profileEntry.expirationDate.getTime())
        {
            this.uuidToProfileEntryMap.remove(profileEntry.getGameProfile().getId());
            this.usernameToProfileEntryMap.remove(profileEntry.getGameProfile().getName().toLowerCase(Locale.ROOT));
            this.gameProfiles.remove(profileEntry.getGameProfile());
            profileEntry = null;
        }

        if (profileEntry != null)
        {
            GameProfile gameProfile = profileEntry.getGameProfile();
            this.gameProfiles.remove(gameProfile);
            this.gameProfiles.addFirst(gameProfile);
        }
        else
        {
            GameProfile lookedUpProfile = getGameProfile(this.mcServer, lowercaseUsername);

            if (lookedUpProfile != null)
            {
                this.addEntry(lookedUpProfile);
                profileEntry = (PlayerProfileCache.ProfileEntry)this.usernameToProfileEntryMap.get(lowercaseUsername);
            }
        }

        this.save();
        return profileEntry == null ? null : profileEntry.getGameProfile();
    }

    public String[] getUsernames()
    {
        List<String> usernames = Lists.newArrayList(this.usernameToProfileEntryMap.keySet());
        return (String[])usernames.toArray(new String[usernames.size()]);
    }

    public GameProfile getProfileByUUID(UUID uuid)
    {
        PlayerProfileCache.ProfileEntry profileEntry = (PlayerProfileCache.ProfileEntry)this.uuidToProfileEntryMap.get(uuid);
        return profileEntry == null ? null : profileEntry.getGameProfile();
    }

    private PlayerProfileCache.ProfileEntry getByUUID(UUID uuid)
    {
        PlayerProfileCache.ProfileEntry profileEntry = (PlayerProfileCache.ProfileEntry)this.uuidToProfileEntryMap.get(uuid);

        if (profileEntry != null)
        {
            GameProfile gameProfile = profileEntry.getGameProfile();
            this.gameProfiles.remove(gameProfile);
            this.gameProfiles.addFirst(gameProfile);
        }

        return profileEntry;
    }

    public void load()
    {
        BufferedReader bufferedReader = null;

        try
        {
            bufferedReader = Files.newReader(this.usercacheFile, Charsets.UTF_8);
            List<PlayerProfileCache.ProfileEntry> cachedEntries = (List)this.gson.fromJson((Reader)bufferedReader, TYPE);
            this.usernameToProfileEntryMap.clear();
            this.uuidToProfileEntryMap.clear();
            this.gameProfiles.clear();

            for (PlayerProfileCache.ProfileEntry profileEntry : Lists.reverse(cachedEntries))
            {
                if (profileEntry != null)
                {
                    this.addEntry(profileEntry.getGameProfile(), profileEntry.getExpirationDate());
                }
            }
        }
        catch (FileNotFoundException caughtFileNotFoundException)
        {
            ;
        }
        catch (JsonParseException caughtJsonParseException)
        {
            ;
        }
        finally
        {
            IOUtils.closeQuietly((Reader)bufferedReader);
        }
    }

    public void save()
    {
        String serializedEntries = this.gson.toJson((Object)this.getEntriesWithLimit(1000));
        BufferedWriter bufferedWriter = null;

        try
        {
            bufferedWriter = Files.newWriter(this.usercacheFile, Charsets.UTF_8);
            bufferedWriter.write(serializedEntries);
            return;
        }
        catch (FileNotFoundException caughtFileNotFoundException)
        {
            ;
        }
        catch (IOException caughtIoException)
        {
            return;
        }
        finally
        {
            IOUtils.closeQuietly((Writer)bufferedWriter);
        }
    }

    private List<PlayerProfileCache.ProfileEntry> getEntriesWithLimit(int limitSize)
    {
        ArrayList<PlayerProfileCache.ProfileEntry> entries = Lists.<PlayerProfileCache.ProfileEntry>newArrayList();

        for (GameProfile gameProfile : Lists.newArrayList(Iterators.limit(this.gameProfiles.iterator(), limitSize)))
        {
            PlayerProfileCache.ProfileEntry profileEntry = this.getByUUID(gameProfile.getId());

            if (profileEntry != null)
            {
                entries.add(profileEntry);
            }
        }

        return entries;
    }

    class ProfileEntry
    {
        private final GameProfile gameProfile;
        private final Date expirationDate;

        private ProfileEntry(GameProfile gameProfileIn, Date expirationDateIn)
        {
            this.gameProfile = gameProfileIn;
            this.expirationDate = expirationDateIn;
        }

        public GameProfile getGameProfile()
        {
            return this.gameProfile;
        }

        public Date getExpirationDate()
        {
            return this.expirationDate;
        }
    }

    class Serializer implements JsonDeserializer<PlayerProfileCache.ProfileEntry>, JsonSerializer<PlayerProfileCache.ProfileEntry>
    {
        private Serializer()
        {
        }

        public JsonElement serialize(PlayerProfileCache.ProfileEntry entry, Type typeOfSrc, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("name", entry.getGameProfile().getName());
            UUID uuid = entry.getGameProfile().getId();
            jsonObject.addProperty("uuid", uuid == null ? "" : uuid.toString());
            jsonObject.addProperty("expiresOn", PlayerProfileCache.dateFormat.format(entry.getExpirationDate()));
            return jsonObject;
        }

        public PlayerProfileCache.ProfileEntry deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            if (json.isJsonObject())
            {
                JsonObject jsonObject = json.getAsJsonObject();
                JsonElement nameElement = jsonObject.get("name");
                JsonElement uuidElement = jsonObject.get("uuid");
                JsonElement expiresOnElement = jsonObject.get("expiresOn");

                if (nameElement != null && uuidElement != null)
                {
                    String uuidText = uuidElement.getAsString();
                    String profileName = nameElement.getAsString();
                    Date date = null;

                    if (expiresOnElement != null)
                    {
                        try
                        {
                            date = PlayerProfileCache.dateFormat.parse(expiresOnElement.getAsString());
                        }
                        catch (ParseException caughtParseException)
                        {
                            date = null;
                        }
                    }

                    if (profileName != null && uuidText != null)
                    {
                        UUID uuid;

                        try
                        {
                            uuid = UUID.fromString(uuidText);
                        }
                        catch (Throwable caughtThrowable)
                        {
                            return null;
                        }

                        PlayerProfileCache.ProfileEntry profileEntry = PlayerProfileCache.this.new ProfileEntry(new GameProfile(uuid, profileName), date);
                        return profileEntry;
                    }
                    else
                    {
                        return null;
                    }
                }
                else
                {
                    return null;
                }
            }
            else
            {
                return null;
            }
        }
    }
}
