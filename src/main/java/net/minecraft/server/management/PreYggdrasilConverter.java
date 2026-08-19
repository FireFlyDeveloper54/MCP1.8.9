package net.minecraft.server.management;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.ProfileLookupCallback;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PreYggdrasilConverter
{
    private static final Logger LOGGER = LogManager.getLogger();
    public static final File OLD_IPBAN_FILE = new File("banned-ips.txt");
    public static final File OLD_PLAYERBAN_FILE = new File("banned-players.txt");
    public static final File OLD_OPS_FILE = new File("ops.txt");
    public static final File OLD_WHITELIST_FILE = new File("white-list.txt");

    private static void lookupNames(MinecraftServer server, Collection<String> names, ProfileLookupCallback callback)
    {
        String[] validNames = (String[])Iterators.toArray(Iterators.filter(names.iterator(), new Predicate<String>()
        {
            public boolean apply(String name)
            {
                return !StringUtils.isNullOrEmpty(name);
            }
        }), String.class);

        if (server.isServerInOnlineMode())
        {
            server.getGameProfileRepository().findProfilesByNames(validNames, Agent.MINECRAFT, callback);
        }
        else
        {
            for (String name : validNames)
            {
                UUID uuid = EntityPlayer.getUUID(new GameProfile((UUID)null, name));
                GameProfile gameProfile = new GameProfile(uuid, name);
                callback.onProfileLookupSucceeded(gameProfile);
            }
        }
    }

    public static String getStringUUIDFromName(String username)
    {
        if (!StringUtils.isNullOrEmpty(username) && username.length() <= 16)
        {
            final MinecraftServer minecraftServer = MinecraftServer.getServer();
            GameProfile cachedProfile = minecraftServer.getPlayerProfileCache().getGameProfileForUsername(username);

            if (cachedProfile != null && cachedProfile.getId() != null)
            {
                return cachedProfile.getId().toString();
            }
            else if (!minecraftServer.isSinglePlayer() && minecraftServer.isServerInOnlineMode())
            {
                final List<GameProfile> lookedUpProfiles = Lists.<GameProfile>newArrayList();
                ProfileLookupCallback lookupCallback = new ProfileLookupCallback()
                {
                    public void onProfileLookupSucceeded(GameProfile gameProfile)
                    {
                        minecraftServer.getPlayerProfileCache().addEntry(gameProfile);
                        lookedUpProfiles.add(gameProfile);
                    }
                    public void onProfileLookupFailed(GameProfile gameProfile, Exception exception)
                    {
                        PreYggdrasilConverter.LOGGER.warn((String)("Could not lookup user whitelist entry for " + gameProfile.getName()), (Throwable)exception);
                    }
                };
                lookupNames(minecraftServer, Lists.newArrayList(new String[] {username}), lookupCallback);
                return lookedUpProfiles.size() > 0 && ((GameProfile)lookedUpProfiles.get(0)).getId() != null ? ((GameProfile)lookedUpProfiles.get(0)).getId().toString() : "";
            }
            else
            {
                return EntityPlayer.getUUID(new GameProfile((UUID)null, username)).toString();
            }
        }
        else
        {
            return username;
        }
    }
}
