package net.minecraft.client.gui;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import com.mojang.authlib.GameProfile;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.scoreboard.IScoreObjectiveCriteria;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.world.WorldSettings;

public class GuiPlayerTabOverlay extends Gui
{
    private static final Ordering<NetworkPlayerInfo> PLAYER_ORDERING = Ordering.from(new GuiPlayerTabOverlay.PlayerComparator());
    private final Minecraft mc;
    private final GuiIngame guiIngame;
    private IChatComponent footer;
    private IChatComponent header;
    private long lastTimeOpened;
    private boolean isBeingRendered;

    public GuiPlayerTabOverlay(Minecraft mcIn, GuiIngame guiIngameIn)
    {
        this.mc = mcIn;
        this.guiIngame = guiIngameIn;
    }

    public String getPlayerName(NetworkPlayerInfo networkPlayerInfoIn)
    {
        return networkPlayerInfoIn.getDisplayName() != null ? networkPlayerInfoIn.getDisplayName().getFormattedText() : ScorePlayerTeam.formatPlayerName(networkPlayerInfoIn.getPlayerTeam(), networkPlayerInfoIn.getGameProfile().getName());
    }

    public void updatePlayerList(boolean willBeRendered)
    {
        if (willBeRendered && !this.isBeingRendered)
        {
            this.lastTimeOpened = Minecraft.getSystemTime();
        }

        this.isBeingRendered = willBeRendered;
    }

    public void renderPlayerlist(int width, Scoreboard scoreboardIn, ScoreObjective scoreObjectiveIn)
    {
        NetHandlerPlayClient nethandlerplayclient = this.mc.thePlayer.sendQueue;
        List<NetworkPlayerInfo> list = PLAYER_ORDERING.<NetworkPlayerInfo>sortedCopy(nethandlerplayclient.getPlayerInfoMap());
        int i = 0;
        int j = 0;

        for (NetworkPlayerInfo networkplayerinfo : list)
        {
            int k = this.mc.fontRendererObj.getStringWidth(this.getPlayerName(networkplayerinfo));
            i = Math.max(i, k);

            if (scoreObjectiveIn != null && scoreObjectiveIn.getRenderType() != IScoreObjectiveCriteria.EnumRenderType.HEARTS)
            {
                k = this.mc.fontRendererObj.getStringWidth(" " + scoreboardIn.getValueFromObjective(networkplayerinfo.getGameProfile().getName(), scoreObjectiveIn).getScorePoints());
                j = Math.max(j, k);
            }
        }

        list = list.subList(0, Math.min(list.size(), 80));
        int intValue = list.size();
        int secondIntValue = intValue;
        int thirdIntValue;

        for (thirdIntValue = 1; secondIntValue > 20; secondIntValue = (intValue + thirdIntValue - 1) / thirdIntValue)
        {
            ++thirdIntValue;
        }

        boolean flag = this.mc.isIntegratedServerRunning() || this.mc.getNetHandler().getNetworkManager().getIsencrypted();
        int l;

        if (scoreObjectiveIn != null)
        {
            if (scoreObjectiveIn.getRenderType() == IScoreObjectiveCriteria.EnumRenderType.HEARTS)
            {
                l = 90;
            }
            else
            {
                l = j;
            }
        }
        else
        {
            l = 0;
        }

        int twentiethIntValue = Math.min(thirdIntValue * ((flag ? 9 : 0) + i + l + 13), width - 50) / thirdIntValue;
        int fourthIntValue = width / 2 - (twentiethIntValue * thirdIntValue + (thirdIntValue - 1) * 5) / 2;
        int fifthIntValue = 10;
        int sixthIntValue = twentiethIntValue * thirdIntValue + (thirdIntValue - 1) * 5;
        List<String> list1 = null;
        List<String> list2 = null;

        if (this.header != null)
        {
            list1 = this.mc.fontRendererObj.listFormattedStringToWidth(this.header.getFormattedText(), width - 50);

            for (String s : list1)
            {
                sixthIntValue = Math.max(sixthIntValue, this.mc.fontRendererObj.getStringWidth(s));
            }
        }

        if (this.footer != null)
        {
            list2 = this.mc.fontRendererObj.listFormattedStringToWidth(this.footer.getFormattedText(), width - 50);

            for (String stringValue : list2)
            {
                sixthIntValue = Math.max(sixthIntValue, this.mc.fontRendererObj.getStringWidth(stringValue));
            }
        }

        if (list1 != null)
        {
            drawRect(width / 2 - sixthIntValue / 2 - 1, fifthIntValue - 1, width / 2 + sixthIntValue / 2 + 1, fifthIntValue + list1.size() * this.mc.fontRendererObj.FONT_HEIGHT, Integer.MIN_VALUE);

            for (String secondStringValue : list1)
            {
                int seventhIntValue = this.mc.fontRendererObj.getStringWidth(secondStringValue);
                this.mc.fontRendererObj.drawStringWithShadow(secondStringValue, (float)(width / 2 - seventhIntValue / 2), (float)fifthIntValue, -1);
                fifthIntValue += this.mc.fontRendererObj.FONT_HEIGHT;
            }

            ++fifthIntValue;
        }

        drawRect(width / 2 - sixthIntValue / 2 - 1, fifthIntValue - 1, width / 2 + sixthIntValue / 2 + 1, fifthIntValue + secondIntValue * 9, Integer.MIN_VALUE);

        for (int eighthIntValue = 0; eighthIntValue < intValue; ++eighthIntValue)
        {
            int ninthIntValue = eighthIntValue / secondIntValue;
            int tenthIntValue = eighthIntValue % secondIntValue;
            int eleventhIntValue = fourthIntValue + ninthIntValue * twentiethIntValue + ninthIntValue * 5;
            int twelfthIntValue = fifthIntValue + tenthIntValue * 9;
            drawRect(eleventhIntValue, twelfthIntValue, eleventhIntValue + twentiethIntValue, twelfthIntValue + 8, 553648127);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableAlpha();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

            if (eighthIntValue < list.size())
            {
                NetworkPlayerInfo networkplayerinfo1 = list.get(eighthIntValue);
                String thirdStringValue = this.getPlayerName(networkplayerinfo1);
                GameProfile gameprofile = networkplayerinfo1.getGameProfile();

                if (flag)
                {
                    EntityPlayer entityplayer = this.mc.theWorld.getPlayerEntityByUUID(gameprofile.getId());
                    boolean flag1 = entityplayer != null && entityplayer.isWearing(EnumPlayerModelParts.CAPE) && (gameprofile.getName().equals("Dinnerbone") || gameprofile.getName().equals("Grumm"));
                    this.mc.getTextureManager().bindTexture(networkplayerinfo1.getLocationSkin());
                    int thirteenthIntValue = 8 + (flag1 ? 8 : 0);
                    int fourteenthIntValue = 8 * (flag1 ? -1 : 1);
                    Gui.drawScaledCustomSizeModalRect(eleventhIntValue, twelfthIntValue, 8.0F, (float)thirteenthIntValue, 8, fourteenthIntValue, 8, 8, 64.0F, 64.0F);

                    if (entityplayer != null && entityplayer.isWearing(EnumPlayerModelParts.HAT))
                    {
                        int fifteenthIntValue = 8 + (flag1 ? 8 : 0);
                        int sixteenthIntValue = 8 * (flag1 ? -1 : 1);
                        Gui.drawScaledCustomSizeModalRect(eleventhIntValue, twelfthIntValue, 40.0F, (float)fifteenthIntValue, 8, sixteenthIntValue, 8, 8, 64.0F, 64.0F);
                    }

                    eleventhIntValue += 9;
                }

                if (networkplayerinfo1.getGameType() == WorldSettings.GameType.SPECTATOR)
                {
                    thirdStringValue = EnumChatFormatting.ITALIC + thirdStringValue;
                    this.mc.fontRendererObj.drawStringWithShadow(thirdStringValue, (float)eleventhIntValue, (float)twelfthIntValue, -1862270977);
                }
                else
                {
                    this.mc.fontRendererObj.drawStringWithShadow(thirdStringValue, (float)eleventhIntValue, (float)twelfthIntValue, -1);
                }

                if (scoreObjectiveIn != null && networkplayerinfo1.getGameType() != WorldSettings.GameType.SPECTATOR)
                {
                    int seventeenthIntValue = eleventhIntValue + i + 1;
                    int eighteenthIntValue = seventeenthIntValue + l;

                    if (eighteenthIntValue - seventeenthIntValue > 5)
                    {
                        this.drawScoreboardValues(scoreObjectiveIn, twelfthIntValue, gameprofile.getName(), seventeenthIntValue, eighteenthIntValue, networkplayerinfo1);
                    }
                }

                this.drawPing(twentiethIntValue, eleventhIntValue - (flag ? 9 : 0), twelfthIntValue, networkplayerinfo1);
            }
        }

        if (list2 != null)
        {
            fifthIntValue = fifthIntValue + secondIntValue * 9 + 1;
            drawRect(width / 2 - sixthIntValue / 2 - 1, fifthIntValue - 1, width / 2 + sixthIntValue / 2 + 1, fifthIntValue + list2.size() * this.mc.fontRendererObj.FONT_HEIGHT, Integer.MIN_VALUE);

            for (String fourthStringValue : list2)
            {
                int nineteenthIntValue = this.mc.fontRendererObj.getStringWidth(fourthStringValue);
                this.mc.fontRendererObj.drawStringWithShadow(fourthStringValue, (float)(width / 2 - nineteenthIntValue / 2), (float)fifthIntValue, -1);
                fifthIntValue += this.mc.fontRendererObj.FONT_HEIGHT;
            }
        }
    }

    protected void drawPing(int entryWidth, int x, int y, NetworkPlayerInfo networkPlayerInfoIn)
    {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(icons);
        int i = 0;
        int j = 0;

        if (networkPlayerInfoIn.getResponseTime() < 0)
        {
            j = 5;
        }
        else if (networkPlayerInfoIn.getResponseTime() < 150)
        {
            j = 0;
        }
        else if (networkPlayerInfoIn.getResponseTime() < 300)
        {
            j = 1;
        }
        else if (networkPlayerInfoIn.getResponseTime() < 600)
        {
            j = 2;
        }
        else if (networkPlayerInfoIn.getResponseTime() < 1000)
        {
            j = 3;
        }
        else
        {
            j = 4;
        }

        this.zLevel += 100.0F;
        this.drawTexturedModalRect(x + entryWidth - 11, y, 0 + i * 10, 176 + j * 8, 10, 8);
        this.zLevel -= 100.0F;
    }

    private void drawScoreboardValues(ScoreObjective scoreObjective, int y, String playerName, int left, int right, NetworkPlayerInfo networkPlayerInfo)
    {
        int i = scoreObjective.getScoreboard().getValueFromObjective(playerName, scoreObjective).getScorePoints();

        if (scoreObjective.getRenderType() == IScoreObjectiveCriteria.EnumRenderType.HEARTS)
        {
            this.mc.getTextureManager().bindTexture(icons);

            if (this.lastTimeOpened == networkPlayerInfo.getRenderVisibilityId())
            {
                if (i < networkPlayerInfo.getDisplayHealth())
                {
                    networkPlayerInfo.setLastHealthTime(Minecraft.getSystemTime());
                    networkPlayerInfo.setHealthBlinkTime((long)(this.guiIngame.getUpdateCounter() + 20));
                }
                else if (i > networkPlayerInfo.getDisplayHealth())
                {
                    networkPlayerInfo.setLastHealthTime(Minecraft.getSystemTime());
                    networkPlayerInfo.setHealthBlinkTime((long)(this.guiIngame.getUpdateCounter() + 10));
                }
            }

            if (Minecraft.getSystemTime() - networkPlayerInfo.getLastHealthTime() > 1000L || this.lastTimeOpened != networkPlayerInfo.getRenderVisibilityId())
            {
                networkPlayerInfo.setDisplayHealth(i);
                networkPlayerInfo.setLastHealth(i);
                networkPlayerInfo.setLastHealthTime(Minecraft.getSystemTime());
            }

            networkPlayerInfo.setRenderVisibilityId(this.lastTimeOpened);
            networkPlayerInfo.setDisplayHealth(i);
            int j = MathHelper.ceiling_float_int((float)Math.max(i, networkPlayerInfo.getLastHealth()) / 2.0F);
            int k = Math.max(MathHelper.ceiling_float_int((float)(i / 2)), Math.max(MathHelper.ceiling_float_int((float)(networkPlayerInfo.getLastHealth() / 2)), 10));
            boolean flag = networkPlayerInfo.getHealthBlinkTime() > (long)this.guiIngame.getUpdateCounter() && (networkPlayerInfo.getHealthBlinkTime() - (long)this.guiIngame.getUpdateCounter()) / 3L % 2L == 1L;

            if (j > 0)
            {
                float f = Math.min((float)(right - left - 4) / (float)k, 9.0F);

                if (f > 3.0F)
                {
                    for (int l = j; l < k; ++l)
                    {
                        this.drawTexturedModalRect((float)left + (float)l * f, (float)y, flag ? 25 : 16, 0, 9, 9);
                    }

                    for (int twentySecondIntValue = 0; twentySecondIntValue < j; ++twentySecondIntValue)
                    {
                        this.drawTexturedModalRect((float)left + (float)twentySecondIntValue * f, (float)y, flag ? 25 : 16, 0, 9, 9);

                        if (flag)
                        {
                            if (twentySecondIntValue * 2 + 1 < networkPlayerInfo.getLastHealth())
                            {
                                this.drawTexturedModalRect((float)left + (float)twentySecondIntValue * f, (float)y, 70, 0, 9, 9);
                            }

                            if (twentySecondIntValue * 2 + 1 == networkPlayerInfo.getLastHealth())
                            {
                                this.drawTexturedModalRect((float)left + (float)twentySecondIntValue * f, (float)y, 79, 0, 9, 9);
                            }
                        }

                        if (twentySecondIntValue * 2 + 1 < i)
                        {
                            this.drawTexturedModalRect((float)left + (float)twentySecondIntValue * f, (float)y, twentySecondIntValue >= 10 ? 160 : 52, 0, 9, 9);
                        }

                        if (twentySecondIntValue * 2 + 1 == i)
                        {
                            this.drawTexturedModalRect((float)left + (float)twentySecondIntValue * f, (float)y, twentySecondIntValue >= 10 ? 169 : 61, 0, 9, 9);
                        }
                    }
                }
                else
                {
                    float floatValue = MathHelper.clamp_float((float)i / 20.0F, 0.0F, 1.0F);
                    int twentyFirstIntValue = (int)((1.0F - floatValue) * 255.0F) << 16 | (int)(floatValue * 255.0F) << 8;
                    String s = "" + (float)i / 2.0F;

                    if (right - this.mc.fontRendererObj.getStringWidth(s + "hp") >= left)
                    {
                        s = s + "hp";
                    }

                    this.mc.fontRendererObj.drawStringWithShadow(s, (float)((right + left) / 2 - this.mc.fontRendererObj.getStringWidth(s) / 2), (float)y, twentyFirstIntValue);
                }
            }
        }
        else
        {
            String fifthStringValue = EnumChatFormatting.YELLOW + "" + i;
            this.mc.fontRendererObj.drawStringWithShadow(fifthStringValue, (float)(right - this.mc.fontRendererObj.getStringWidth(fifthStringValue)), (float)y, 16777215);
        }
    }

    public void setFooter(IChatComponent footerIn)
    {
        this.footer = footerIn;
    }

    public void setHeader(IChatComponent headerIn)
    {
        this.header = headerIn;
    }

    public void resetFooterHeader()
    {
        this.header = null;
        this.footer = null;
    }

    static class PlayerComparator implements Comparator<NetworkPlayerInfo>
    {
        private PlayerComparator()
        {
        }

        public int compare(NetworkPlayerInfo left, NetworkPlayerInfo right)
        {
            ScorePlayerTeam leftTeam = left.getPlayerTeam();
            ScorePlayerTeam rightTeam = right.getPlayerTeam();
            return ComparisonChain.start().compareTrueFirst(left.getGameType() != WorldSettings.GameType.SPECTATOR, right.getGameType() != WorldSettings.GameType.SPECTATOR).compare(leftTeam != null ? leftTeam.getRegisteredName() : "", rightTeam != null ? rightTeam.getRegisteredName() : "").compare(left.getGameProfile().getName(), right.getGameProfile().getName()).result();
        }
    }
}
