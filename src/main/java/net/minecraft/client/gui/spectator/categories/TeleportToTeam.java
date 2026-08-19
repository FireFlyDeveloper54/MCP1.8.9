package net.minecraft.client.gui.spectator.categories;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiSpectator;
import net.minecraft.client.gui.spectator.ISpectatorMenuObject;
import net.minecraft.client.gui.spectator.ISpectatorMenuView;
import net.minecraft.client.gui.spectator.SpectatorMenu;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

public class TeleportToTeam implements ISpectatorMenuView, ISpectatorMenuObject
{
    private static final Random RANDOM = new Random();
    private final List<ISpectatorMenuObject> items = Lists.<ISpectatorMenuObject>newArrayList();

    public TeleportToTeam()
    {
        Minecraft minecraft = Minecraft.getMinecraft();

        for (ScorePlayerTeam scorePlayerTeam : minecraft.theWorld.getScoreboard().getTeams())
        {
            this.items.add(new TeleportToTeam.TeamSelectionObject(scorePlayerTeam));
        }
    }

    public List<ISpectatorMenuObject> getItems()
    {
        return this.items;
    }

    public IChatComponent getPrompt()
    {
        return new ChatComponentText("Select a team to teleport to");
    }

    public void selectItem(SpectatorMenu menu)
    {
        menu.selectCategory(this);
    }

    public IChatComponent getSpectatorName()
    {
        return new ChatComponentText("Teleport to team member");
    }

    public void renderIcon(float brightness, int alpha)
    {
        Minecraft.getMinecraft().getTextureManager().bindTexture(GuiSpectator.SPECTATOR_WIDGETS_TEX_PATH);
        Gui.drawModalRectWithCustomSizedTexture(0, 0, 16.0F, 0.0F, 16, 16, 256.0F, 256.0F);
    }

    public boolean isEnabled()
    {
        for (ISpectatorMenuObject ispectatormenuobject : this.items)
        {
            if (ispectatormenuobject.isEnabled())
            {
                return true;
            }
        }

        return false;
    }

    class TeamSelectionObject implements ISpectatorMenuObject
    {
        private final ScorePlayerTeam team;
        private final ResourceLocation skin;
        private final List<NetworkPlayerInfo> players;

        public TeamSelectionObject(ScorePlayerTeam teamIn)
        {
            this.team = teamIn;
            this.players = Lists.<NetworkPlayerInfo>newArrayList();

            for (String s : teamIn.getMembershipCollection())
            {
                NetworkPlayerInfo networkPlayerInfo = Minecraft.getMinecraft().getNetHandler().getPlayerInfo(s);

                if (networkPlayerInfo != null)
                {
                    this.players.add(networkPlayerInfo);
                }
            }

            if (!this.players.isEmpty())
            {
                String name = ((NetworkPlayerInfo)this.players.get(RANDOM.nextInt(this.players.size()))).getGameProfile().getName();
                this.skin = AbstractClientPlayer.getLocationSkin(name);
                AbstractClientPlayer.getDownloadImageSkin(this.skin, name);
            }
            else
            {
                this.skin = DefaultPlayerSkin.getDefaultSkinLegacy();
            }
        }

        public void selectItem(SpectatorMenu menu)
        {
            menu.selectCategory(new TeleportToPlayer(this.players));
        }

        public IChatComponent getSpectatorName()
        {
            return new ChatComponentText(this.team.getTeamName());
        }

        public void renderIcon(float brightness, int alpha)
        {
            int i = -1;
            String s = FontRenderer.getFormatFromString(this.team.getColorPrefix());

            if (s.length() >= 2)
            {
                i = Minecraft.getMinecraft().fontRendererObj.getColorCode(s.charAt(1));
            }

            if (i >= 0)
            {
                float f = (float)(i >> 16 & 255) / 255.0F;
                float floatValue2 = (float)(i >> 8 & 255) / 255.0F;
                float floatValue3 = (float)(i & 255) / 255.0F;
                Gui.drawRect(1, 1, 15, 15, MathHelper.rgb(f * brightness, floatValue2 * brightness, floatValue3 * brightness) | alpha << 24);
            }

            Minecraft.getMinecraft().getTextureManager().bindTexture(this.skin);
            GlStateManager.color(brightness, brightness, brightness, (float)alpha / 255.0F);
            Gui.drawScaledCustomSizeModalRect(2, 2, 8.0F, 8.0F, 8, 8, 12, 12, 64.0F, 64.0F);
            Gui.drawScaledCustomSizeModalRect(2, 2, 40.0F, 8.0F, 8, 8, 12, 12, 64.0F, 64.0F);
        }

        public boolean isEnabled()
        {
            return !this.players.isEmpty();
        }
    }
}
