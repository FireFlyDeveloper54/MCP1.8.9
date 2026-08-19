package net.minecraft.client.gui;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.boss.BossStatus;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.src.Config;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.FoodStats;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import net.minecraft.world.border.WorldBorder;
import net.optifine.CustomColors;

public class GuiIngame extends Gui
{
    private static final ResourceLocation vignetteTexPath = new ResourceLocation("textures/misc/vignette.png");
    private static final ResourceLocation widgetsTexPath = new ResourceLocation("textures/gui/widgets.png");
    private static final ResourceLocation pumpkinBlurTexPath = new ResourceLocation("textures/misc/pumpkinblur.png");
    private final Random rand = new Random();
    private final Minecraft mc;
    private final RenderItem itemRenderer;
    private final GuiNewChat persistantChatGUI;
    private final GuiStreamIndicator streamIndicator;
    private int updateCounter;
    private String recordPlaying = "";
    private int recordPlayingUpFor;
    private boolean recordIsPlaying;
    public float prevVignetteBrightness = 1.0F;
    private int remainingHighlightTicks;
    private ItemStack highlightingItemStack;
    private final GuiOverlayDebug overlayDebug;
    private final GuiSpectator spectatorGui;
    private final GuiPlayerTabOverlay overlayPlayerList;
    private int titlesTimer;
    private String displayedTitle = "";
    private String displayedSubTitle = "";
    private int titleFadeIn;
    private int titleDisplayTime;
    private int titleFadeOut;
    private int playerHealth = 0;
    private int lastPlayerHealth = 0;
    private long lastSystemTime = 0L;
    private long healthUpdateCounter = 0L;
    private ScaledResolution scaledResolution;
    private int scaledResolutionDisplayWidth = -1;
    private int scaledResolutionDisplayHeight = -1;
    private int scaledResolutionGuiScale = -1;
    private boolean scaledResolutionUnicode;

    public GuiIngame(Minecraft mcIn)
    {
        this.mc = mcIn;
        this.itemRenderer = mcIn.getRenderItem();
        this.overlayDebug = new GuiOverlayDebug(mcIn);
        this.spectatorGui = new GuiSpectator(mcIn);
        this.persistantChatGUI = new GuiNewChat(mcIn);
        this.streamIndicator = new GuiStreamIndicator(mcIn);
        this.overlayPlayerList = new GuiPlayerTabOverlay(mcIn, this);
        this.setDefaultTitlesTimes();
    }

    public void setDefaultTitlesTimes()
    {
        this.titleFadeIn = 10;
        this.titleDisplayTime = 70;
        this.titleFadeOut = 20;
    }

    public void renderGameOverlay(float partialTicks)
    {
        ScaledResolution scaledResolution = this.getScaledResolution();
        int i = scaledResolution.getScaledWidth();
        int j = scaledResolution.getScaledHeight();
        this.mc.entityRenderer.setupOverlayRendering();
        GlStateManager.enableBlend();

        if (Config.isVignetteEnabled())
        {
            this.renderVignette(this.mc.thePlayer.getBrightness(partialTicks), scaledResolution);
        }
        else
        {
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        }

        ItemStack itemstack = this.mc.thePlayer.inventory.armorItemInSlot(3);

        if (this.mc.gameSettings.thirdPersonView == 0 && itemstack != null && itemstack.getItem() == Item.getItemFromBlock(Blocks.pumpkin))
        {
            this.renderPumpkinOverlay(scaledResolution);
        }

        if (!this.mc.thePlayer.isPotionActive(Potion.confusion))
        {
            float f = this.mc.thePlayer.prevTimeInPortal + (this.mc.thePlayer.timeInPortal - this.mc.thePlayer.prevTimeInPortal) * partialTicks;

            if (f > 0.0F)
            {
                this.renderPortal(f, scaledResolution);
            }
        }

        if (this.mc.playerController.isSpectator())
        {
            this.spectatorGui.renderTooltip(scaledResolution, partialTicks);
        }
        else
        {
            this.renderTooltip(scaledResolution, partialTicks);
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(icons);
        GlStateManager.enableBlend();

        if (this.showCrosshair())
        {
            GlStateManager.tryBlendFuncSeparate(775, 769, 1, 0);
            GlStateManager.enableAlpha();
            this.drawTexturedModalRect(i / 2 - 7, j / 2 - 7, 0, 0, 16, 16);
        }

        GlStateManager.enableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        this.mc.mcProfiler.startSection("bossHealth");
        this.renderBossHealth();
        this.mc.mcProfiler.endSection();

        if (this.mc.playerController.shouldDrawHUD())
        {
            this.renderPlayerStats(scaledResolution);
        }

        GlStateManager.disableBlend();

        if (this.mc.thePlayer.getSleepTimer() > 0)
        {
            this.mc.mcProfiler.startSection("sleep");
            GlStateManager.disableDepth();
            GlStateManager.disableAlpha();
            int secondIntValue2 = this.mc.thePlayer.getSleepTimer();
            float floatValue2 = (float)secondIntValue2 / 100.0F;

            if (floatValue2 > 1.0F)
            {
                floatValue2 = 1.0F - (float)(secondIntValue2 - 100) / 10.0F;
            }

            int k = (int)(220.0F * floatValue2) << 24 | 1052704;
            drawRect(0, 0, i, j, k);
            GlStateManager.enableAlpha();
            GlStateManager.enableDepth();
            this.mc.mcProfiler.endSection();
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        int thirdIntValue2 = i / 2 - 91;

        if (this.mc.thePlayer.isRidingHorse())
        {
            this.renderHorseJumpBar(scaledResolution, thirdIntValue2);
        }
        else if (this.mc.playerController.gameIsSurvivalOrAdventure())
        {
            this.renderExpBar(scaledResolution, thirdIntValue2);
        }

        if (this.mc.gameSettings.heldItemTooltips && !this.mc.playerController.isSpectator())
        {
            this.renderSelectedItem(scaledResolution);
        }
        else if (this.mc.thePlayer.isSpectator())
        {
            this.spectatorGui.renderSelectedItem(scaledResolution);
        }

        if (this.mc.isDemo())
        {
            this.renderDemo(scaledResolution);
        }

        if (this.mc.gameSettings.showDebugInfo)
        {
            this.overlayDebug.renderDebugInfo(scaledResolution);
        }

        if (this.recordPlayingUpFor > 0)
        {
            this.mc.mcProfiler.startSection("overlayMessage");
            float floatValue3 = (float)this.recordPlayingUpFor - partialTicks;
            int fourthIntValue = (int)(floatValue3 * 255.0F / 20.0F);

            if (fourthIntValue > 255)
            {
                fourthIntValue = 255;
            }

            if (fourthIntValue > 8)
            {
                GlStateManager.pushMatrix();
                GlStateManager.translate((float)(i / 2), (float)(j - 68), 0.0F);
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                int l = 16777215;

                if (this.recordIsPlaying)
                {
                    l = MathHelper.hsvToRGB(floatValue3 / 50.0F, 0.7F, 0.6F) & 16777215;
                }

                this.getFontRenderer().drawString(this.recordPlaying, -this.getFontRenderer().getStringWidth(this.recordPlaying) / 2, -4, l + (fourthIntValue << 24 & -16777216));
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            }

            this.mc.mcProfiler.endSection();
        }

        if (this.titlesTimer > 0)
        {
            this.mc.mcProfiler.startSection("titleAndSubtitle");
            float floatValue4 = (float)this.titlesTimer - partialTicks;
            int intValue2 = 255;

            if (this.titlesTimer > this.titleFadeOut + this.titleDisplayTime)
            {
                float floatValue5 = (float)(this.titleFadeIn + this.titleDisplayTime + this.titleFadeOut) - floatValue4;
                intValue2 = (int)(floatValue5 * 255.0F / (float)this.titleFadeIn);
            }

            if (this.titlesTimer <= this.titleFadeOut)
            {
                intValue2 = (int)(floatValue4 * 255.0F / (float)this.titleFadeOut);
            }

            intValue2 = MathHelper.clamp_int(intValue2, 0, 255);

            if (intValue2 > 8)
            {
                GlStateManager.pushMatrix();
                GlStateManager.translate((float)(i / 2), (float)(j / 2), 0.0F);
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                GlStateManager.pushMatrix();
                GlStateManager.scale(4.0F, 4.0F, 4.0F);
                int intValue3 = intValue2 << 24 & -16777216;
                this.getFontRenderer().drawString(this.displayedTitle, (float)(-this.getFontRenderer().getStringWidth(this.displayedTitle) / 2), -10.0F, 16777215 | intValue3, true);
                GlStateManager.popMatrix();
                GlStateManager.pushMatrix();
                GlStateManager.scale(2.0F, 2.0F, 2.0F);
                this.getFontRenderer().drawString(this.displayedSubTitle, (float)(-this.getFontRenderer().getStringWidth(this.displayedSubTitle) / 2), 5.0F, 16777215 | intValue3, true);
                GlStateManager.popMatrix();
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            }

            this.mc.mcProfiler.endSection();
        }

        Scoreboard scoreboard = this.mc.theWorld.getScoreboard();
        ScoreObjective scoreObjective = null;
        ScorePlayerTeam scorePlayerTeam = scoreboard.getPlayersTeam(this.mc.thePlayer.getName());

        if (scorePlayerTeam != null)
        {
            int intValue4 = scorePlayerTeam.getChatFormat().getColorIndex();

            if (intValue4 >= 0)
            {
                scoreObjective = scoreboard.getObjectiveInDisplaySlot(3 + intValue4);
            }
        }

        ScoreObjective scoreobjective1 = scoreObjective != null ? scoreObjective : scoreboard.getObjectiveInDisplaySlot(1);

        if (scoreobjective1 != null)
        {
            this.renderScoreboard(scoreobjective1, scaledResolution);
        }

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.disableAlpha();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0F, (float)(j - 48), 0.0F);
        this.mc.mcProfiler.startSection("chat");
        this.persistantChatGUI.drawChat(this.updateCounter);
        this.mc.mcProfiler.endSection();
        GlStateManager.popMatrix();
        scoreobjective1 = scoreboard.getObjectiveInDisplaySlot(0);

        if (this.mc.gameSettings.keyBindPlayerList.isKeyDown() && (!this.mc.isIntegratedServerRunning() || this.mc.thePlayer.sendQueue.getPlayerInfoMap().size() > 1 || scoreobjective1 != null))
        {
            this.overlayPlayerList.updatePlayerList(true);
            this.overlayPlayerList.renderPlayerlist(i, scoreboard, scoreobjective1);
        }
        else
        {
            this.overlayPlayerList.updatePlayerList(false);
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableLighting();
        GlStateManager.enableAlpha();
    }

    protected void renderTooltip(ScaledResolution sr, float partialTicks)
    {
        if (this.mc.getRenderViewEntity() instanceof EntityPlayer)
        {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.mc.getTextureManager().bindTexture(widgetsTexPath);
            EntityPlayer entityPlayer = (EntityPlayer)this.mc.getRenderViewEntity();
            int i = sr.getScaledWidth() / 2;
            float f = this.zLevel;
            this.zLevel = -90.0F;
            this.drawTexturedModalRect(i - 91, sr.getScaledHeight() - 22, 0, 0, 182, 22);
            this.drawTexturedModalRect(i - 91 - 1 + entityPlayer.inventory.currentItem * 20, sr.getScaledHeight() - 22 - 1, 0, 22, 24, 22);
            this.zLevel = f;
            GlStateManager.enableRescaleNormal();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            RenderHelper.enableGUIStandardItemLighting();

            for (int j = 0; j < 9; ++j)
            {
                int k = sr.getScaledWidth() / 2 - 90 + j * 20 + 2;
                int l = sr.getScaledHeight() - 16 - 3;
                this.renderHotbarItem(j, k, l, partialTicks, entityPlayer);
            }

            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableRescaleNormal();
            GlStateManager.disableBlend();
        }
    }

    public void renderHorseJumpBar(ScaledResolution scaledRes, int x)
    {
        this.mc.mcProfiler.startSection("jumpBar");
        this.mc.getTextureManager().bindTexture(Gui.icons);
        float f = this.mc.thePlayer.getHorseJumpPower();
        int i = 182;
        int j = (int)(f * (float)(i + 1));
        int k = scaledRes.getScaledHeight() - 32 + 3;
        this.drawTexturedModalRect(x, k, 0, 84, i, 5);

        if (j > 0)
        {
            this.drawTexturedModalRect(x, k, 0, 89, j, 5);
        }

        this.mc.mcProfiler.endSection();
    }

    public void renderExpBar(ScaledResolution scaledRes, int x)
    {
        this.mc.mcProfiler.startSection("expBar");
        this.mc.getTextureManager().bindTexture(Gui.icons);
        int i = this.mc.thePlayer.xpBarCap();

        if (i > 0)
        {
            int j = 182;
            int k = (int)(this.mc.thePlayer.experience * (float)(j + 1));
            int l = scaledRes.getScaledHeight() - 32 + 3;
            this.drawTexturedModalRect(x, l, 0, 64, j, 5);

            if (k > 0)
            {
                this.drawTexturedModalRect(x, l, 0, 69, k, 5);
            }
        }

        this.mc.mcProfiler.endSection();

        if (this.mc.thePlayer.experienceLevel > 0)
        {
            this.mc.mcProfiler.startSection("expLevel");
            int tenthIntValue = 8453920;

            if (Config.isCustomColors())
            {
                tenthIntValue = CustomColors.getExpBarTextColor(tenthIntValue);
            }

            String s = "" + this.mc.thePlayer.experienceLevel;
            int thirteenthIntValue = (scaledRes.getScaledWidth() - this.getFontRenderer().getStringWidth(s)) / 2;
            int fifthIntValue = scaledRes.getScaledHeight() - 31 - 4;
            int eighthIntValue = 0;
            this.getFontRenderer().drawString(s, thirteenthIntValue + 1, fifthIntValue, 0);
            this.getFontRenderer().drawString(s, thirteenthIntValue - 1, fifthIntValue, 0);
            this.getFontRenderer().drawString(s, thirteenthIntValue, fifthIntValue + 1, 0);
            this.getFontRenderer().drawString(s, thirteenthIntValue, fifthIntValue - 1, 0);
            this.getFontRenderer().drawString(s, thirteenthIntValue, fifthIntValue, tenthIntValue);
            this.mc.mcProfiler.endSection();
        }
    }

    public void renderSelectedItem(ScaledResolution scaledRes)
    {
        this.mc.mcProfiler.startSection("selectedItemName");

        if (this.remainingHighlightTicks > 0 && this.highlightingItemStack != null)
        {
            String s = this.highlightingItemStack.getDisplayName();

            if (this.highlightingItemStack.hasDisplayName())
            {
                s = EnumChatFormatting.ITALIC + s;
            }

            int i = (scaledRes.getScaledWidth() - this.getFontRenderer().getStringWidth(s)) / 2;
            int j = scaledRes.getScaledHeight() - 59;

            if (!this.mc.playerController.shouldDrawHUD())
            {
                j += 14;
            }

            int k = (int)((float)this.remainingHighlightTicks * 256.0F / 10.0F);

            if (k > 255)
            {
                k = 255;
            }

            if (k > 0)
            {
                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                this.getFontRenderer().drawStringWithShadow(s, (float)i, (float)j, 16777215 + (k << 24));
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            }
        }

        this.mc.mcProfiler.endSection();
    }

    public void renderDemo(ScaledResolution scaledRes)
    {
        this.mc.mcProfiler.startSection("demo");
        String s = "";

        if (this.mc.theWorld.getTotalWorldTime() >= 120500L)
        {
            s = I18n.format("demo.demoExpired", new Object[0]);
        }
        else
        {
            s = I18n.format("demo.remainingTime", new Object[] {StringUtils.ticksToElapsedTime((int)(120500L - this.mc.theWorld.getTotalWorldTime()))});
        }

        int i = this.getFontRenderer().getStringWidth(s);
        this.getFontRenderer().drawStringWithShadow(s, (float)(scaledRes.getScaledWidth() - i - 10), 5.0F, 16777215);
        this.mc.mcProfiler.endSection();
    }

    protected boolean showCrosshair()
    {
        if (this.mc.gameSettings.showDebugInfo && !this.mc.thePlayer.hasReducedDebug() && !this.mc.gameSettings.reducedDebugInfo)
        {
            return false;
        }
        else if (this.mc.playerController.isSpectator())
        {
            if (this.mc.pointedEntity != null)
            {
                return true;
            }
            else
            {
                if (this.mc.objectMouseOver != null && this.mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK)
                {
                    BlockPos blockPos = this.mc.objectMouseOver.getBlockPos();

                    if (this.mc.theWorld.getTileEntity(blockPos) instanceof IInventory)
                    {
                        return true;
                    }
                }

                return false;
            }
        }
        else
        {
            return true;
        }
    }

    public void renderStreamIndicator(ScaledResolution scaledRes)
    {
        this.streamIndicator.render(scaledRes.getScaledWidth() - 10, 10);
    }

    private void renderScoreboard(ScoreObjective objective, ScaledResolution scaledRes)
    {
        Scoreboard scoreboard = objective.getScoreboard();
        Collection<Score> collection = scoreboard.getSortedScores(objective);
        List<Score> list = Lists.newArrayList(Iterables.filter(collection, new Predicate<Score>()
        {
            public boolean apply(Score score)
            {
                return score.getPlayerName() != null && !score.getPlayerName().startsWith("#");
            }
        }));

        if (list.size() > 15)
        {
            collection = Lists.newArrayList(Iterables.skip(list, collection.size() - 15));
        }
        else
        {
            collection = list;
        }

        int i = this.getFontRenderer().getStringWidth(objective.getDisplayName());

        for (Score score : collection)
        {
            ScorePlayerTeam scoreplayerteam = scoreboard.getPlayersTeam(score.getPlayerName());
            String s = ScorePlayerTeam.formatPlayerName(scoreplayerteam, score.getPlayerName()) + ": " + EnumChatFormatting.RED + score.getScorePoints();
            i = Math.max(i, this.getFontRenderer().getStringWidth(s));
        }

        int sixthIntValue = collection.size() * this.getFontRenderer().FONT_HEIGHT;
        int intValue = scaledRes.getScaledHeight() / 2 + sixthIntValue / 3;
        int secondIntValue = 3;
        int thirdIntValue = scaledRes.getScaledWidth() - i - secondIntValue;
        int j = 0;

        for (Score score1 : collection)
        {
            ++j;
            ScorePlayerTeam scoreplayerteam1 = scoreboard.getPlayersTeam(score1.getPlayerName());
            String stringValue = ScorePlayerTeam.formatPlayerName(scoreplayerteam1, score1.getPlayerName());
            String secondStringValue = EnumChatFormatting.RED + "" + score1.getScorePoints();
            int k = intValue - j * this.getFontRenderer().FONT_HEIGHT;
            int l = scaledRes.getScaledWidth() - secondIntValue + 2;
            drawRect(thirdIntValue - 2, k, l, k + this.getFontRenderer().FONT_HEIGHT, 1342177280);
            this.getFontRenderer().drawString(stringValue, thirdIntValue, k, 553648127);
            this.getFontRenderer().drawString(secondStringValue, l - this.getFontRenderer().getStringWidth(secondStringValue), k, 553648127);

            if (j == collection.size())
            {
                String thirdStringValue = objective.getDisplayName();
                drawRect(thirdIntValue - 2, k - this.getFontRenderer().FONT_HEIGHT - 1, l, k - 1, 1610612736);
                drawRect(thirdIntValue - 2, k - 1, l, k, 1342177280);
                this.getFontRenderer().drawString(thirdStringValue, thirdIntValue + i / 2 - this.getFontRenderer().getStringWidth(thirdStringValue) / 2, k - this.getFontRenderer().FONT_HEIGHT, 553648127);
            }
        }
    }

    private void renderPlayerStats(ScaledResolution scaledRes)
    {
        if (this.mc.getRenderViewEntity() instanceof EntityPlayer)
        {
            EntityPlayer entityPlayer = (EntityPlayer)this.mc.getRenderViewEntity();
            int i = MathHelper.ceiling_float_int(entityPlayer.getHealth());
            boolean flag = this.healthUpdateCounter > (long)this.updateCounter && (this.healthUpdateCounter - (long)this.updateCounter) / 3L % 2L == 1L;

            if (i < this.playerHealth && entityPlayer.hurtResistantTime > 0)
            {
                this.lastSystemTime = Minecraft.getSystemTime();
                this.healthUpdateCounter = (long)(this.updateCounter + 20);
            }
            else if (i > this.playerHealth && entityPlayer.hurtResistantTime > 0)
            {
                this.lastSystemTime = Minecraft.getSystemTime();
                this.healthUpdateCounter = (long)(this.updateCounter + 10);
            }

            if (Minecraft.getSystemTime() - this.lastSystemTime > 1000L)
            {
                this.playerHealth = i;
                this.lastPlayerHealth = i;
                this.lastSystemTime = Minecraft.getSystemTime();
            }

            this.playerHealth = i;
            int j = this.lastPlayerHealth;
            this.rand.setSeed((long)(this.updateCounter * 312871));
            boolean flag1 = false;
            FoodStats foodStats = entityPlayer.getFoodStats();
            int k = foodStats.getFoodLevel();
            int l = foodStats.getPrevFoodLevel();
            IAttributeInstance iattributeinstance = entityPlayer.getEntityAttribute(SharedMonsterAttributes.maxHealth);
            int intValue2 = scaledRes.getScaledWidth() / 2 - 91;
            int secondIntValue2 = scaledRes.getScaledWidth() / 2 + 91;
            int thirdIntValue2 = scaledRes.getScaledHeight() - 39;
            float f = (float)iattributeinstance.getAttributeValue();
            float floatValue2 = entityPlayer.getAbsorptionAmount();
            int fourthIntValue2 = MathHelper.ceiling_float_int((f + floatValue2) / 2.0F / 10.0F);
            int intValue3 = Math.max(10 - (fourthIntValue2 - 2), 3);
            int intValue4 = thirdIntValue2 - (fourthIntValue2 - 1) * intValue3 - 10;
            float floatValue3 = floatValue2;
            int intValue5 = entityPlayer.getTotalArmorValue();
            int intValue6 = -1;

            if (entityPlayer.isPotionActive(Potion.regeneration))
            {
                intValue6 = this.updateCounter % MathHelper.ceiling_float_int(f + 5.0F);
            }

            this.mc.mcProfiler.startSection("armor");

            for (int index = 0; index < 10; ++index)
            {
                if (intValue5 > 0)
                {
                    int intValue7 = intValue2 + index * 8;

                    if (index * 2 + 1 < intValue5)
                    {
                        this.drawTexturedModalRect(intValue7, intValue4, 34, 9, 9, 9);
                    }

                    if (index * 2 + 1 == intValue5)
                    {
                        this.drawTexturedModalRect(intValue7, intValue4, 25, 9, 9, 9);
                    }

                    if (index * 2 + 1 > intValue5)
                    {
                        this.drawTexturedModalRect(intValue7, intValue4, 16, 9, 9, 9);
                    }
                }
            }

            this.mc.mcProfiler.endStartSection("health");

            for (int index2 = MathHelper.ceiling_float_int((f + floatValue2) / 2.0F) - 1; index2 >= 0; --index2)
            {
                int intValue8 = 16;

                if (entityPlayer.isPotionActive(Potion.poison))
                {
                    intValue8 += 36;
                }
                else if (entityPlayer.isPotionActive(Potion.wither))
                {
                    intValue8 += 72;
                }

                int eleventhIntValue = 0;

                if (flag)
                {
                    eleventhIntValue = 1;
                }

                int fourteenthIntValue = MathHelper.ceiling_float_int((float)(index2 + 1) / 10.0F) - 1;
                int intValue11 = intValue2 + index2 % 10 * 8;
                int intValue12 = thirdIntValue2 - fourteenthIntValue * intValue3;

                if (i <= 4)
                {
                    intValue12 += this.rand.nextInt(2);
                }

                if (index2 == intValue6)
                {
                    intValue12 -= 2;
                }

                int twelfthIntValue = 0;

                if (entityPlayer.worldObj.getWorldInfo().isHardcoreModeEnabled())
                {
                    twelfthIntValue = 5;
                }

                this.drawTexturedModalRect(intValue11, intValue12, 16 + eleventhIntValue * 9, 9 * twelfthIntValue, 9, 9);

                if (flag)
                {
                    if (index2 * 2 + 1 < j)
                    {
                        this.drawTexturedModalRect(intValue11, intValue12, intValue8 + 54, 9 * twelfthIntValue, 9, 9);
                    }

                    if (index2 * 2 + 1 == j)
                    {
                        this.drawTexturedModalRect(intValue11, intValue12, intValue8 + 63, 9 * twelfthIntValue, 9, 9);
                    }
                }

                if (floatValue3 <= 0.0F)
                {
                    if (index2 * 2 + 1 < i)
                    {
                        this.drawTexturedModalRect(intValue11, intValue12, intValue8 + 36, 9 * twelfthIntValue, 9, 9);
                    }

                    if (index2 * 2 + 1 == i)
                    {
                        this.drawTexturedModalRect(intValue11, intValue12, intValue8 + 45, 9 * twelfthIntValue, 9, 9);
                    }
                }
                else
                {
                    if (floatValue3 == floatValue2 && floatValue2 % 2.0F == 1.0F)
                    {
                        this.drawTexturedModalRect(intValue11, intValue12, intValue8 + 153, 9 * twelfthIntValue, 9, 9);
                    }
                    else
                    {
                        this.drawTexturedModalRect(intValue11, intValue12, intValue8 + 144, 9 * twelfthIntValue, 9, 9);
                    }

                    floatValue3 -= 2.0F;
                }
            }

            Entity entity = entityPlayer.ridingEntity;

            if (entity == null)
            {
                this.mc.mcProfiler.endStartSection("food");

                for (int index3 = 0; index3 < 10; ++index3)
                {
                    int intValue14 = thirdIntValue2;
                    int intValue15 = 16;
                    int intValue16 = 0;

                    if (entityPlayer.isPotionActive(Potion.hunger))
                    {
                        intValue15 += 36;
                        intValue16 = 13;
                    }

                    if (entityPlayer.getFoodStats().getSaturationLevel() <= 0.0F && this.updateCounter % (k * 3 + 1) == 0)
                    {
                        intValue14 = thirdIntValue2 + (this.rand.nextInt(3) - 1);
                    }

                    if (flag1)
                    {
                        intValue16 = 1;
                    }

                    int ninthIntValue = secondIntValue2 - index3 * 8 - 9;
                    this.drawTexturedModalRect(ninthIntValue, intValue14, 16 + intValue16 * 9, 27, 9, 9);

                    if (flag1)
                    {
                        if (index3 * 2 + 1 < l)
                        {
                            this.drawTexturedModalRect(ninthIntValue, intValue14, intValue15 + 54, 27, 9, 9);
                        }

                        if (index3 * 2 + 1 == l)
                        {
                            this.drawTexturedModalRect(ninthIntValue, intValue14, intValue15 + 63, 27, 9, 9);
                        }
                    }

                    if (index3 * 2 + 1 < k)
                    {
                        this.drawTexturedModalRect(ninthIntValue, intValue14, intValue15 + 36, 27, 9, 9);
                    }

                    if (index3 * 2 + 1 == k)
                    {
                        this.drawTexturedModalRect(ninthIntValue, intValue14, intValue15 + 45, 27, 9, 9);
                    }
                }
            }
            else if (entity instanceof EntityLivingBase)
            {
                this.mc.mcProfiler.endStartSection("mountHealth");
                EntityLivingBase entityLivingBase = (EntityLivingBase)entity;
                int intValue18 = (int)Math.ceil((double)entityLivingBase.getHealth());
                float floatValue4 = entityLivingBase.getMaxHealth();
                int intValue19 = (int)(floatValue4 + 0.5F) / 2;

                if (intValue19 > 30)
                {
                    intValue19 = 30;
                }

                int seventhIntValue = thirdIntValue2;

                for (int index4 = 0; intValue19 > 0; index4 += 20)
                {
                    int intValue21 = Math.min(intValue19, 10);
                    intValue19 -= intValue21;

                    for (int index5 = 0; index5 < intValue21; ++index5)
                    {
                        int intValue22 = 52;
                        int intValue23 = 0;

                        if (flag1)
                        {
                            intValue23 = 1;
                        }

                        int fifteenthIntValue = secondIntValue2 - index5 * 8 - 9;
                        this.drawTexturedModalRect(fifteenthIntValue, seventhIntValue, intValue22 + intValue23 * 9, 9, 9, 9);

                        if (index5 * 2 + 1 + index4 < intValue18)
                        {
                            this.drawTexturedModalRect(fifteenthIntValue, seventhIntValue, intValue22 + 36, 9, 9, 9);
                        }

                        if (index5 * 2 + 1 + index4 == intValue18)
                        {
                            this.drawTexturedModalRect(fifteenthIntValue, seventhIntValue, intValue22 + 45, 9, 9, 9);
                        }
                    }

                    seventhIntValue -= 10;
                }
            }

            this.mc.mcProfiler.endStartSection("air");

            if (entityPlayer.isInsideOfMaterial(Material.water))
            {
                int intValue25 = this.mc.thePlayer.getAir();
                int intValue26 = MathHelper.ceiling_double_int((double)(intValue25 - 2) * 10.0D / 300.0D);
                int intValue27 = MathHelper.ceiling_double_int((double)intValue25 * 10.0D / 300.0D) - intValue26;

                for (int index6 = 0; index6 < intValue26 + intValue27; ++index6)
                {
                    if (index6 < intValue26)
                    {
                        this.drawTexturedModalRect(secondIntValue2 - index6 * 8 - 9, intValue4, 16, 18, 9, 9);
                    }
                    else
                    {
                        this.drawTexturedModalRect(secondIntValue2 - index6 * 8 - 9, intValue4, 25, 18, 9, 9);
                    }
                }
            }

            this.mc.mcProfiler.endSection();
        }
    }

    private void renderBossHealth()
    {
        if (BossStatus.bossName != null && BossStatus.statusBarTime > 0)
        {
            --BossStatus.statusBarTime;
            FontRenderer fontRenderer = this.mc.fontRendererObj;
            ScaledResolution scaledResolution = this.getScaledResolution();
            int i = scaledResolution.getScaledWidth();
            int j = 182;
            int k = i / 2 - j / 2;
            int l = (int)(BossStatus.healthScale * (float)(j + 1));
            int intValue2 = 12;
            this.drawTexturedModalRect(k, intValue2, 0, 74, j, 5);
            this.drawTexturedModalRect(k, intValue2, 0, 74, j, 5);

            if (l > 0)
            {
                this.drawTexturedModalRect(k, intValue2, 0, 79, l, 5);
            }

            String s = BossStatus.bossName;
            this.getFontRenderer().drawStringWithShadow(s, (float)(i / 2 - this.getFontRenderer().getStringWidth(s) / 2), (float)(intValue2 - 10), 16777215);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.mc.getTextureManager().bindTexture(icons);
        }
    }

    private ScaledResolution getScaledResolution()
    {
        boolean unicode = this.mc.isUnicode();

        if (this.scaledResolution == null || this.scaledResolutionDisplayWidth != this.mc.displayWidth || this.scaledResolutionDisplayHeight != this.mc.displayHeight || this.scaledResolutionGuiScale != this.mc.gameSettings.guiScale || this.scaledResolutionUnicode != unicode)
        {
            this.scaledResolution = new ScaledResolution(this.mc);
            this.scaledResolutionDisplayWidth = this.mc.displayWidth;
            this.scaledResolutionDisplayHeight = this.mc.displayHeight;
            this.scaledResolutionGuiScale = this.mc.gameSettings.guiScale;
            this.scaledResolutionUnicode = unicode;
        }

        return this.scaledResolution;
    }

    private void renderPumpkinOverlay(ScaledResolution scaledRes)
    {
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableAlpha();
        this.mc.getTextureManager().bindTexture(pumpkinBlurTexPath);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldRenderer.pos(0.0D, (double)scaledRes.getScaledHeight(), -90.0D).tex(0.0D, 1.0D).endVertex();
        worldRenderer.pos((double)scaledRes.getScaledWidth(), (double)scaledRes.getScaledHeight(), -90.0D).tex(1.0D, 1.0D).endVertex();
        worldRenderer.pos((double)scaledRes.getScaledWidth(), 0.0D, -90.0D).tex(1.0D, 0.0D).endVertex();
        worldRenderer.pos(0.0D, 0.0D, -90.0D).tex(0.0D, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableAlpha();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void renderVignette(float lightLevel, ScaledResolution scaledRes)
    {
        if (!Config.isVignetteEnabled())
        {
            GlStateManager.enableDepth();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        }
        else
        {
            lightLevel = 1.0F - lightLevel;
            lightLevel = MathHelper.clamp_float(lightLevel, 0.0F, 1.0F);
            WorldBorder worldborder = this.mc.theWorld.getWorldBorder();
            float f = (float)worldborder.getClosestDistance(this.mc.thePlayer);
            double doubleValue = Math.min(worldborder.getResizeSpeed() * (double)worldborder.getWarningTime() * 1000.0D, Math.abs(worldborder.getTargetSize() - worldborder.getDiameter()));
            double secondDoubleValue = Math.max((double)worldborder.getWarningDistance(), doubleValue);

            if ((double)f < secondDoubleValue)
            {
                f = 1.0F - (float)((double)f / secondDoubleValue);
            }
            else
            {
                f = 0.0F;
            }

            this.prevVignetteBrightness = (float)((double)this.prevVignetteBrightness + (double)(lightLevel - this.prevVignetteBrightness) * 0.01D);
            GlStateManager.disableDepth();
            GlStateManager.depthMask(false);
            GlStateManager.tryBlendFuncSeparate(0, 769, 1, 0);

            if (f > 0.0F)
            {
                GlStateManager.color(0.0F, f, f, 1.0F);
            }
            else
            {
                GlStateManager.color(this.prevVignetteBrightness, this.prevVignetteBrightness, this.prevVignetteBrightness, 1.0F);
            }

            this.mc.getTextureManager().bindTexture(vignetteTexPath);
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();
            worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
            worldrenderer.pos(0.0D, (double)scaledRes.getScaledHeight(), -90.0D).tex(0.0D, 1.0D).endVertex();
            worldrenderer.pos((double)scaledRes.getScaledWidth(), (double)scaledRes.getScaledHeight(), -90.0D).tex(1.0D, 1.0D).endVertex();
            worldrenderer.pos((double)scaledRes.getScaledWidth(), 0.0D, -90.0D).tex(1.0D, 0.0D).endVertex();
            worldrenderer.pos(0.0D, 0.0D, -90.0D).tex(0.0D, 0.0D).endVertex();
            tessellator.draw();
            GlStateManager.depthMask(true);
            GlStateManager.enableDepth();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        }
    }

    private void renderPortal(float timeInPortal, ScaledResolution scaledRes)
    {
        if (timeInPortal < 1.0F)
        {
            timeInPortal = timeInPortal * timeInPortal;
            timeInPortal = timeInPortal * timeInPortal;
            timeInPortal = timeInPortal * 0.8F + 0.2F;
        }

        GlStateManager.disableAlpha();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(1.0F, 1.0F, 1.0F, timeInPortal);
        this.mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
        TextureAtlasSprite textureAtlasSprite = this.mc.getBlockRendererDispatcher().getBlockModelShapes().getTexture(Blocks.portal.getDefaultState());
        float f = textureAtlasSprite.getMinU();
        float floatValue2 = textureAtlasSprite.getMinV();
        float floatValue3 = textureAtlasSprite.getMaxU();
        float floatValue4 = textureAtlasSprite.getMaxV();
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldRenderer.pos(0.0D, (double)scaledRes.getScaledHeight(), -90.0D).tex((double)f, (double)floatValue4).endVertex();
        worldRenderer.pos((double)scaledRes.getScaledWidth(), (double)scaledRes.getScaledHeight(), -90.0D).tex((double)floatValue3, (double)floatValue4).endVertex();
        worldRenderer.pos((double)scaledRes.getScaledWidth(), 0.0D, -90.0D).tex((double)floatValue3, (double)floatValue2).endVertex();
        worldRenderer.pos(0.0D, 0.0D, -90.0D).tex((double)f, (double)floatValue2).endVertex();
        tessellator.draw();
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableAlpha();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void renderHotbarItem(int index, int xPos, int yPos, float partialTicks, EntityPlayer player)
    {
        ItemStack itemstack = player.inventory.mainInventory[index];

        if (itemstack != null)
        {
            float f = (float)itemstack.animationsToGo - partialTicks;

            if (f > 0.0F)
            {
                GlStateManager.pushMatrix();
                float floatValue = 1.0F + f / 5.0F;
                GlStateManager.translate((float)(xPos + 8), (float)(yPos + 12), 0.0F);
                GlStateManager.scale(1.0F / floatValue, (floatValue + 1.0F) / 2.0F, 1.0F);
                GlStateManager.translate((float)(-(xPos + 8)), (float)(-(yPos + 12)), 0.0F);
            }

            this.itemRenderer.renderItemAndEffectIntoGUI(itemstack, xPos, yPos);

            if (f > 0.0F)
            {
                GlStateManager.popMatrix();
            }

            this.itemRenderer.renderItemOverlays(this.mc.fontRendererObj, itemstack, xPos, yPos);
        }
    }

    public void updateTick()
    {
        if (this.recordPlayingUpFor > 0)
        {
            --this.recordPlayingUpFor;
        }

        if (this.titlesTimer > 0)
        {
            --this.titlesTimer;

            if (this.titlesTimer <= 0)
            {
                this.displayedTitle = "";
                this.displayedSubTitle = "";
            }
        }

        ++this.updateCounter;
        this.streamIndicator.updateStreamAlpha();

        if (this.mc.thePlayer != null)
        {
            ItemStack itemStack = this.mc.thePlayer.inventory.getCurrentItem();

            if (itemStack == null)
            {
                this.remainingHighlightTicks = 0;
            }
            else if (this.highlightingItemStack != null && itemStack.getItem() == this.highlightingItemStack.getItem() && ItemStack.areItemStackTagsEqual(itemStack, this.highlightingItemStack) && (itemStack.isItemStackDamageable() || itemStack.getMetadata() == this.highlightingItemStack.getMetadata()))
            {
                if (this.remainingHighlightTicks > 0)
                {
                    --this.remainingHighlightTicks;
                }
            }
            else
            {
                this.remainingHighlightTicks = 40;
            }

            this.highlightingItemStack = itemStack;
        }
    }

    public void setRecordPlayingMessage(String recordName)
    {
        this.setRecordPlaying(I18n.format("record.nowPlaying", new Object[] {recordName}), true);
    }

    public void setRecordPlaying(String message, boolean isPlaying)
    {
        this.recordPlaying = message;
        this.recordPlayingUpFor = 60;
        this.recordIsPlaying = isPlaying;
    }

    public void displayTitle(String title, String subTitle, int timeFadeIn, int displayTime, int timeFadeOut)
    {
        if (title == null && subTitle == null && timeFadeIn < 0 && displayTime < 0 && timeFadeOut < 0)
        {
            this.displayedTitle = "";
            this.displayedSubTitle = "";
            this.titlesTimer = 0;
        }
        else if (title != null)
        {
            this.displayedTitle = title;
            this.titlesTimer = this.titleFadeIn + this.titleDisplayTime + this.titleFadeOut;
        }
        else if (subTitle != null)
        {
            this.displayedSubTitle = subTitle;
        }
        else
        {
            if (timeFadeIn >= 0)
            {
                this.titleFadeIn = timeFadeIn;
            }

            if (displayTime >= 0)
            {
                this.titleDisplayTime = displayTime;
            }

            if (timeFadeOut >= 0)
            {
                this.titleFadeOut = timeFadeOut;
            }

            if (this.titlesTimer > 0)
            {
                this.titlesTimer = this.titleFadeIn + this.titleDisplayTime + this.titleFadeOut;
            }
        }
    }

    public void setRecordPlaying(IChatComponent component, boolean isPlaying)
    {
        this.setRecordPlaying(component.getUnformattedText(), isPlaying);
    }

    public GuiNewChat getChatGUI()
    {
        return this.persistantChatGUI;
    }

    public int getUpdateCounter()
    {
        return this.updateCounter;
    }

    public FontRenderer getFontRenderer()
    {
        return this.mc.fontRendererObj;
    }

    public GuiSpectator getSpectatorGui()
    {
        return this.spectatorGui;
    }

    public GuiPlayerTabOverlay getTabList()
    {
        return this.overlayPlayerList;
    }

    public void resetPlayersOverlayFooterHeader()
    {
        this.overlayPlayerList.resetFooterHeader();
    }
}
