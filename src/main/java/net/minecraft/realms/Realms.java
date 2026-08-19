package net.minecraft.realms;

import com.google.common.util.concurrent.ListenableFuture;
import com.mojang.authlib.GameProfile;
import com.mojang.util.UUIDTypeAdapter;
import java.net.Proxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.Session;
import net.minecraft.world.WorldSettings;

public class Realms
{
    public static boolean isTouchScreen()
    {
        return Minecraft.getMinecraft().gameSettings.touchscreen;
    }

    public static Proxy getProxy()
    {
        return Minecraft.getMinecraft().getProxy();
    }

    public static String sessionId()
    {
        Session session = Minecraft.getMinecraft().getSession();
        return session == null ? null : session.getSessionID();
    }

    public static String userName()
    {
        Session session = Minecraft.getMinecraft().getSession();
        return session == null ? null : session.getUsername();
    }

    public static long currentTimeMillis()
    {
        return Minecraft.getSystemTime();
    }

    public static String getSessionId()
    {
        return Minecraft.getMinecraft().getSession().getSessionID();
    }

    public static String getUUID()
    {
        return Minecraft.getMinecraft().getSession().getPlayerID();
    }

    public static String getName()
    {
        return Minecraft.getMinecraft().getSession().getUsername();
    }

    public static String uuidToName(String uuid)
    {
        return Minecraft.getMinecraft().getSessionService().fillProfileProperties(new GameProfile(UUIDTypeAdapter.fromString(uuid), (String)null), false).getName();
    }

    public static void setScreen(RealmsScreen screen)
    {
        Minecraft.getMinecraft().displayGuiScreen(screen.getProxy());
    }

    public static String getGameDirectoryPath()
    {
        return Minecraft.getMinecraft().mcDataDir.getAbsolutePath();
    }

    public static int survivalId()
    {
        return WorldSettings.GameType.SURVIVAL.getID();
    }

    public static int creativeId()
    {
        return WorldSettings.GameType.CREATIVE.getID();
    }

    public static int adventureId()
    {
        return WorldSettings.GameType.ADVENTURE.getID();
    }

    public static int spectatorId()
    {
        return WorldSettings.GameType.SPECTATOR.getID();
    }

    public static void setConnectedToRealms(boolean connectedToRealms)
    {
        Minecraft.getMinecraft().setConnectedToRealms(connectedToRealms);
    }

    public static ListenableFuture<Object> downloadResourcePack(String url, String hash)
    {
        ListenableFuture<Object> listenablefuture = Minecraft.getMinecraft().getResourcePackRepository().downloadResourcePack(url, hash);
        return listenablefuture;
    }

    public static void clearResourcePack()
    {
        Minecraft.getMinecraft().getResourcePackRepository().clearResourcePack();
    }

    public static boolean getRealmsNotificationsEnabled()
    {
        return Minecraft.getMinecraft().gameSettings.getOptionOrdinalValue(GameSettings.Options.REALMS_NOTIFICATIONS);
    }

    public static boolean inTitleScreen()
    {
        return Minecraft.getMinecraft().currentScreen != null && Minecraft.getMinecraft().currentScreen instanceof GuiMainMenu;
    }
}
