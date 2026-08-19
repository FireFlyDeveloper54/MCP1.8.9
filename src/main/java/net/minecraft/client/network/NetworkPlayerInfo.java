package net.minecraft.client.network;

import com.google.common.base.MoreObjects;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.network.play.server.S38PacketPlayerListItem;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldSettings;

public class NetworkPlayerInfo
{
    private final GameProfile gameProfile;
    private WorldSettings.GameType gameType;
    private int responseTime;
    private boolean playerTexturesLoaded = false;
    private ResourceLocation locationSkin;
    private ResourceLocation locationCape;
    private String skinType;
    private IChatComponent displayName;
    private int displayHealth = 0;
    private int lastHealth = 0;
    private long lastHealthTime = 0L;
    private long healthBlinkTime = 0L;
    private long renderVisibilityId = 0L;

    public NetworkPlayerInfo(GameProfile gameProfileIn)
    {
        this.gameProfile = gameProfileIn;
    }

    public NetworkPlayerInfo(S38PacketPlayerListItem.AddPlayerData addPlayerData)
    {
        this.gameProfile = addPlayerData.getProfile();
        this.gameType = addPlayerData.getGameMode();
        this.responseTime = addPlayerData.getPing();
        this.displayName = addPlayerData.getDisplayName();
    }

    public GameProfile getGameProfile()
    {
        return this.gameProfile;
    }

    public WorldSettings.GameType getGameType()
    {
        return this.gameType;
    }

    public int getResponseTime()
    {
        return this.responseTime;
    }

    protected void setGameType(WorldSettings.GameType gameTypeIn)
    {
        this.gameType = gameTypeIn;
    }

    protected void setResponseTime(int responseTimeIn)
    {
        this.responseTime = responseTimeIn;
    }

    public boolean hasLocationSkin()
    {
        return this.locationSkin != null;
    }

    public String getSkinType()
    {
        return this.skinType == null ? DefaultPlayerSkin.getSkinType(this.gameProfile.getId()) : this.skinType;
    }

    public ResourceLocation getLocationSkin()
    {
        if (this.locationSkin == null)
        {
            this.loadPlayerTextures();
        }

        return (ResourceLocation)MoreObjects.firstNonNull(this.locationSkin, DefaultPlayerSkin.getDefaultSkin(this.gameProfile.getId()));
    }

    public ResourceLocation getLocationCape()
    {
        if (this.locationCape == null)
        {
            this.loadPlayerTextures();
        }

        return this.locationCape;
    }

    public ScorePlayerTeam getPlayerTeam()
    {
        return Minecraft.getMinecraft().theWorld.getScoreboard().getPlayersTeam(this.getGameProfile().getName());
    }

    protected void loadPlayerTextures()
    {
        synchronized (this)
        {
            if (!this.playerTexturesLoaded)
            {
                this.playerTexturesLoaded = true;
                Minecraft.getMinecraft().getSkinManager().loadProfileTextures(this.gameProfile, new SkinManager.SkinAvailableCallback()
                {
                    public void skinAvailable(Type type, ResourceLocation location, MinecraftProfileTexture profileTexture)
                    {
                        switch (type)
                        {
                            case SKIN:
                                NetworkPlayerInfo.this.locationSkin = location;
                                NetworkPlayerInfo.this.skinType = profileTexture.getMetadata("model");

                                if (NetworkPlayerInfo.this.skinType == null)
                                {
                                    NetworkPlayerInfo.this.skinType = "default";
                                }

                                break;

                            case CAPE:
                                NetworkPlayerInfo.this.locationCape = location;
                        }
                    }
                }, true);
            }
        }
    }

    public void setDisplayName(IChatComponent displayNameIn)
    {
        this.displayName = displayNameIn;
    }

    public IChatComponent getDisplayName()
    {
        return this.displayName;
    }

    public int getDisplayHealth()
    {
        return this.displayHealth;
    }

    public void setDisplayHealth(int displayHealthIn)
    {
        this.displayHealth = displayHealthIn;
    }

    public int getLastHealth()
    {
        return this.lastHealth;
    }

    public void setLastHealth(int lastHealthIn)
    {
        this.lastHealth = lastHealthIn;
    }

    public long getLastHealthTime()
    {
        return this.lastHealthTime;
    }

    public void setLastHealthTime(long lastHealthTimeIn)
    {
        this.lastHealthTime = lastHealthTimeIn;
    }

    public long getHealthBlinkTime()
    {
        return this.healthBlinkTime;
    }

    public void setHealthBlinkTime(long healthBlinkTimeIn)
    {
        this.healthBlinkTime = healthBlinkTimeIn;
    }

    public long getRenderVisibilityId()
    {
        return this.renderVisibilityId;
    }

    public void setRenderVisibilityId(long renderVisibilityIdIn)
    {
        this.renderVisibilityId = renderVisibilityIdIn;
    }
}
