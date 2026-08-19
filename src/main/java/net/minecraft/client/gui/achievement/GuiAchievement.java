package net.minecraft.client.gui.achievement;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.resources.I18n;
import net.minecraft.stats.Achievement;
import net.minecraft.util.ResourceLocation;

public class GuiAchievement extends Gui
{
    private static final ResourceLocation achievementBg = new ResourceLocation("textures/gui/achievement/achievement_background.png");
    private Minecraft mc;
    private int width;
    private int height;
    private String achievementTitle;
    private String achievementDescription;
    private Achievement theAchievement;
    private long notificationTime;
    private RenderItem renderItem;
    private boolean permanentNotification;

    public GuiAchievement(Minecraft mc)
    {
        this.mc = mc;
        this.renderItem = mc.getRenderItem();
    }

    public void displayAchievement(Achievement ach)
    {
        this.achievementTitle = I18n.format("achievement.get", new Object[0]);
        this.achievementDescription = ach.getStatName().getUnformattedText();
        this.notificationTime = Minecraft.getSystemTime();
        this.theAchievement = ach;
        this.permanentNotification = false;
    }

    public void displayUnformattedAchievement(Achievement achievementIn)
    {
        this.achievementTitle = achievementIn.getStatName().getUnformattedText();
        this.achievementDescription = achievementIn.getDescription();
        this.notificationTime = Minecraft.getSystemTime() + 2500L;
        this.theAchievement = achievementIn;
        this.permanentNotification = true;
    }

    private void updateAchievementWindowScale()
    {
        GlStateManager.viewport(0, 0, this.mc.displayWidth, this.mc.displayHeight);
        GlStateManager.matrixMode(5889);
        GlStateManager.loadIdentity();
        GlStateManager.matrixMode(5888);
        GlStateManager.loadIdentity();
        this.width = this.mc.displayWidth;
        this.height = this.mc.displayHeight;
        ScaledResolution scaledResolution = new ScaledResolution(this.mc);
        this.width = scaledResolution.getScaledWidth();
        this.height = scaledResolution.getScaledHeight();
        GlStateManager.clear(256);
        GlStateManager.matrixMode(5889);
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0.0D, (double)this.width, (double)this.height, 0.0D, 1000.0D, 3000.0D);
        GlStateManager.matrixMode(5888);
        GlStateManager.loadIdentity();
        GlStateManager.translate(0.0F, 0.0F, -2000.0F);
    }

    public void updateAchievementWindow()
    {
        if (this.theAchievement != null && this.notificationTime != 0L && Minecraft.getMinecraft().thePlayer != null)
        {
            double notificationProgress = (double)(Minecraft.getSystemTime() - this.notificationTime) / 3000.0D;

            if (!this.permanentNotification)
            {
                if (notificationProgress < 0.0D || notificationProgress > 1.0D)
                {
                    this.notificationTime = 0L;
                    return;
                }
            }
            else if (notificationProgress > 0.5D)
            {
                notificationProgress = 0.5D;
            }

            this.updateAchievementWindowScale();
            GlStateManager.disableDepth();
            GlStateManager.depthMask(false);
            double verticalOffsetFactor = notificationProgress * 2.0D;

            if (verticalOffsetFactor > 1.0D)
            {
                verticalOffsetFactor = 2.0D - verticalOffsetFactor;
            }

            verticalOffsetFactor = verticalOffsetFactor * 4.0D;
            verticalOffsetFactor = 1.0D - verticalOffsetFactor;

            if (verticalOffsetFactor < 0.0D)
            {
                verticalOffsetFactor = 0.0D;
            }

            verticalOffsetFactor = verticalOffsetFactor * verticalOffsetFactor;
            verticalOffsetFactor = verticalOffsetFactor * verticalOffsetFactor;
            int notificationX = this.width - 160;
            int notificationY = 0 - (int)(verticalOffsetFactor * 36.0D);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableTexture2D();
            this.mc.getTextureManager().bindTexture(achievementBg);
            GlStateManager.disableLighting();
            this.drawTexturedModalRect(notificationX, notificationY, 96, 202, 160, 32);

            if (this.permanentNotification)
            {
                this.mc.fontRendererObj.drawSplitString(this.achievementDescription, notificationX + 30, notificationY + 7, 120, -1);
            }
            else
            {
                this.mc.fontRendererObj.drawString(this.achievementTitle, notificationX + 30, notificationY + 7, -256);
                this.mc.fontRendererObj.drawString(this.achievementDescription, notificationX + 30, notificationY + 18, -1);
            }

            RenderHelper.enableGUIStandardItemLighting();
            GlStateManager.disableLighting();
            GlStateManager.enableRescaleNormal();
            GlStateManager.enableColorMaterial();
            GlStateManager.enableLighting();
            this.renderItem.renderItemAndEffectIntoGUI(this.theAchievement.theItemStack, notificationX + 8, notificationY + 8);
            GlStateManager.disableLighting();
            GlStateManager.depthMask(true);
            GlStateManager.enableDepth();
        }
    }

    public void clearAchievements()
    {
        this.theAchievement = null;
        this.notificationTime = 0L;
    }
}
