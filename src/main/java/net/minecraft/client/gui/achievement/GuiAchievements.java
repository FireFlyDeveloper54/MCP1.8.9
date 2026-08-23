package net.minecraft.client.gui.achievement;

import java.io.IOException;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiOptionButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.IProgressMeter;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.stats.Achievement;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatFileWriter;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.GameWindow;

public class GuiAchievements extends GuiScreen implements IProgressMeter
{
    private static final int MIN_PAN_X = AchievementList.minDisplayColumn * 24 - 112;
    private static final int MIN_PAN_Y = AchievementList.minDisplayRow * 24 - 112;
    private static final int MAX_PAN_X = AchievementList.maxDisplayColumn * 24 - 77;
    private static final int MAX_PAN_Y = AchievementList.maxDisplayRow * 24 - 77;
    private static final ResourceLocation ACHIEVEMENT_BACKGROUND = new ResourceLocation("textures/gui/achievement/achievement_background.png");
    protected GuiScreen parentScreen;
    protected int paneWidth = 256;
    protected int paneHeight = 202;
    protected int lastMouseX;
    protected int lastMouseY;
    protected float zoom = 1.0F;
    protected double prevScrollX;
    protected double prevScrollY;
    protected double scrollX;
    protected double scrollY;
    protected double targetScrollX;
    protected double targetScrollY;
    private int dragState;
    private StatFileWriter statFileWriter;
    private boolean loadingAchievements = true;

    public GuiAchievements(GuiScreen parentScreenIn, StatFileWriter statFileWriterIn)
    {
        this.parentScreen = parentScreenIn;
        this.statFileWriter = statFileWriterIn;
        int i = 141;
        int j = 141;
        this.prevScrollX = this.scrollX = this.targetScrollX = (double)(AchievementList.openInventory.displayColumn * 24 - i / 2 - 12);
        this.prevScrollY = this.scrollY = this.targetScrollY = (double)(AchievementList.openInventory.displayRow * 24 - j / 2);
    }

    public void initGui()
    {
        this.mc.getNetHandler().addToSendQueue(new C16PacketClientStatus(C16PacketClientStatus.EnumState.REQUEST_STATS));
        this.buttonList.clear();
        this.buttonList.add(new GuiOptionButton(1, this.width / 2 + 24, this.height / 2 + 74, 80, 20, I18n.format("gui.done", new Object[0])));
    }

    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (!this.loadingAchievements)
        {
            if (button.id == 1)
            {
                this.mc.displayGuiScreen(this.parentScreen);
            }
        }
    }

    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        if (keyCode == this.mc.gameSettings.keyBindInventory.getKeyCode())
        {
            this.mc.displayGuiScreen((GuiScreen)null);
            this.mc.setIngameFocus();
        }
        else
        {
            super.keyTyped(typedChar, keyCode);
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        if (this.loadingAchievements)
        {
            this.drawDefaultBackground();
            this.drawCenteredString(this.fontRendererObj, I18n.format("multiplayer.downloadingStats", new Object[0]), this.width / 2, this.height / 2, 16777215);
            this.drawCenteredString(this.fontRendererObj, lanSearchStates[(int)(Minecraft.getSystemTime() / 150L % (long)lanSearchStates.length)], this.width / 2, this.height / 2 + this.fontRendererObj.FONT_HEIGHT * 2, 16777215);
        }
        else
        {
            if (GameWindow.isButtonDown(0))
            {
                int i = (this.width - this.paneWidth) / 2;
                int j = (this.height - this.paneHeight) / 2;
                int k = i + 8;
                int l = j + 17;

                if ((this.dragState == 0 || this.dragState == 1) && mouseX >= k && mouseX < k + 224 && mouseY >= l && mouseY < l + 155)
                {
                    if (this.dragState == 0)
                    {
                        this.dragState = 1;
                    }
                    else
                    {
                        this.scrollX -= (double)((float)(mouseX - this.lastMouseX) * this.zoom);
                        this.scrollY -= (double)((float)(mouseY - this.lastMouseY) * this.zoom);
                        this.targetScrollX = this.prevScrollX = this.scrollX;
                        this.targetScrollY = this.prevScrollY = this.scrollY;
                    }

                    this.lastMouseX = mouseX;
                    this.lastMouseY = mouseY;
                }
            }
            else
            {
                this.dragState = 0;
            }

            int secondIntValue = GameWindow.getDWheel();
            float floatValue = this.zoom;

            if (secondIntValue < 0)
            {
                this.zoom += 0.25F;
            }
            else if (secondIntValue > 0)
            {
                this.zoom -= 0.25F;
            }

            this.zoom = MathHelper.clamp_float(this.zoom, 1.0F, 2.0F);

            if (this.zoom != floatValue)
            {
                float floatValue2 = floatValue - this.zoom;
                float floatValue3 = floatValue * (float)this.paneWidth;
                float f = floatValue * (float)this.paneHeight;
                float floatValue5 = this.zoom * (float)this.paneWidth;
                float floatValue6 = this.zoom * (float)this.paneHeight;
                this.scrollX -= (double)((floatValue5 - floatValue3) * 0.5F);
                this.scrollY -= (double)((floatValue6 - f) * 0.5F);
                this.targetScrollX = this.prevScrollX = this.scrollX;
                this.targetScrollY = this.prevScrollY = this.scrollY;
            }

            if (this.targetScrollX < (double)MIN_PAN_X)
            {
                this.targetScrollX = (double)MIN_PAN_X;
            }

            if (this.targetScrollY < (double)MIN_PAN_Y)
            {
                this.targetScrollY = (double)MIN_PAN_Y;
            }

            if (this.targetScrollX >= (double)MAX_PAN_X)
            {
                this.targetScrollX = (double)(MAX_PAN_X - 1);
            }

            if (this.targetScrollY >= (double)MAX_PAN_Y)
            {
                this.targetScrollY = (double)(MAX_PAN_Y - 1);
            }

            this.drawDefaultBackground();
            this.drawAchievementScreen(mouseX, mouseY, partialTicks);
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            this.drawTitle();
            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
        }
    }

    public void doneLoading()
    {
        if (this.loadingAchievements)
        {
            this.loadingAchievements = false;
        }
    }

    public void updateScreen()
    {
        if (!this.loadingAchievements)
        {
            this.prevScrollX = this.scrollX;
            this.prevScrollY = this.scrollY;
            double doubleValue = this.targetScrollX - this.scrollX;
            double doubleValue2 = this.targetScrollY - this.scrollY;

            if (doubleValue * doubleValue + doubleValue2 * doubleValue2 < 4.0D)
            {
                this.scrollX += doubleValue;
                this.scrollY += doubleValue2;
            }
            else
            {
                this.scrollX += doubleValue * 0.85D;
                this.scrollY += doubleValue2 * 0.85D;
            }
        }
    }

    protected void drawTitle()
    {
        int i = (this.width - this.paneWidth) / 2;
        int j = (this.height - this.paneHeight) / 2;
        this.fontRendererObj.drawString(I18n.format("gui.achievements", new Object[0]), i + 15, j + 5, 4210752);
    }

    protected void drawAchievementScreen(int mouseX, int mouseY, float partialTicks)
    {
        int i = MathHelper.floor_double(this.prevScrollX + (this.scrollX - this.prevScrollX) * (double)partialTicks);
        int j = MathHelper.floor_double(this.prevScrollY + (this.scrollY - this.prevScrollY) * (double)partialTicks);

        if (i < MIN_PAN_X)
        {
            i = MIN_PAN_X;
        }

        if (j < MIN_PAN_Y)
        {
            j = MIN_PAN_Y;
        }

        if (i >= MAX_PAN_X)
        {
            i = MAX_PAN_X - 1;
        }

        if (j >= MAX_PAN_Y)
        {
            j = MAX_PAN_Y - 1;
        }

        int k = (this.width - this.paneWidth) / 2;
        int l = (this.height - this.paneHeight) / 2;
        int intValue = k + 16;
        int eleventhIntValue = l + 17;
        this.zLevel = 0.0F;
        GlStateManager.depthFunc(518);
        GlStateManager.pushMatrix();
        GlStateManager.translate((float)intValue, (float)eleventhIntValue, -200.0F);
        GlStateManager.scale(1.0F / this.zoom, 1.0F / this.zoom, 0.0F);
        GlStateManager.enableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableColorMaterial();
        int twentiethIntValue = i + 288 >> 4;
        int twentyEighthIntValue = j + 288 >> 4;
        int thirdIntValue = (i + 288) % 16;
        int twelfthIntValue = (j + 288) % 16;
        int twentyFirstIntValue = 4;
        int twentyNinthIntValue = 8;
        int fourthIntValue = 10;
        int thirteenthIntValue = 22;
        int twentySecondIntValue = 37;
        Random random = new Random();
        float f = 16.0F / this.zoom;
        float secondFloatValue = 16.0F / this.zoom;

        for (int thirtiethIntValue = 0; (float)thirtiethIntValue * f - (float)twelfthIntValue < 155.0F; ++thirtiethIntValue)
        {
            float fourthFloatValue = 0.6F - (float)(twentyEighthIntValue + thirtiethIntValue) / 25.0F * 0.3F;
            GlStateManager.color(fourthFloatValue, fourthFloatValue, fourthFloatValue, 1.0F);

            for (int fifthIntValue = 0; (float)fifthIntValue * secondFloatValue - (float)thirdIntValue < 224.0F; ++fifthIntValue)
            {
                random.setSeed((long)(this.mc.getSession().getPlayerID().hashCode() + twentiethIntValue + fifthIntValue + (twentyEighthIntValue + thirtiethIntValue) * 16));
                int fourteenthIntValue = random.nextInt(1 + twentyEighthIntValue + thirtiethIntValue) + (twentyEighthIntValue + thirtiethIntValue) / 2;
                TextureAtlasSprite textureatlassprite = this.getBlockTexture(Blocks.sand);

                if (fourteenthIntValue <= 37 && twentyEighthIntValue + thirtiethIntValue != 35)
                {
                    if (fourteenthIntValue == 22)
                    {
                        if (random.nextInt(2) == 0)
                        {
                            textureatlassprite = this.getBlockTexture(Blocks.diamond_ore);
                        }
                        else
                        {
                            textureatlassprite = this.getBlockTexture(Blocks.redstone_ore);
                        }
                    }
                    else if (fourteenthIntValue == 10)
                    {
                        textureatlassprite = this.getBlockTexture(Blocks.iron_ore);
                    }
                    else if (fourteenthIntValue == 8)
                    {
                        textureatlassprite = this.getBlockTexture(Blocks.coal_ore);
                    }
                    else if (fourteenthIntValue > 4)
                    {
                        textureatlassprite = this.getBlockTexture(Blocks.stone);
                    }
                    else if (fourteenthIntValue > 0)
                    {
                        textureatlassprite = this.getBlockTexture(Blocks.dirt);
                    }
                }
                else
                {
                    Block block = Blocks.bedrock;
                    textureatlassprite = this.getBlockTexture(block);
                }

                this.mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
                this.drawTexturedModalRect(fifthIntValue * 16 - thirdIntValue, thirtiethIntValue * 16 - twelfthIntValue, textureatlassprite, 16, 16);
            }
        }

        GlStateManager.enableDepth();
        GlStateManager.depthFunc(515);
        this.mc.getTextureManager().bindTexture(ACHIEVEMENT_BACKGROUND);

        for (int fifteenthIntValue = 0; fifteenthIntValue < AchievementList.achievementList.size(); ++fifteenthIntValue)
        {
            Achievement achievement1 = (Achievement)AchievementList.achievementList.get(fifteenthIntValue);

            if (achievement1.parentAchievement != null)
            {
                int twentyFourthIntValue = achievement1.displayColumn * 24 - i + 11;
                int number32IntValue = achievement1.displayRow * 24 - j + 11;
                int sixteenthIntValue = achievement1.parentAchievement.displayColumn * 24 - i + 11;
                int twentyFifthIntValue = achievement1.parentAchievement.displayRow * 24 - j + 11;
                boolean flag = this.statFileWriter.hasAchievementUnlocked(achievement1);
                boolean flag1 = this.statFileWriter.canUnlockAchievement(achievement1);
                int twentyThirdIntValue = this.statFileWriter.getAchievementUnlockDepth(achievement1);

                if (twentyThirdIntValue <= 4)
                {
                    int number31IntValue = -16777216;

                    if (flag)
                    {
                        number31IntValue = -6250336;
                    }
                    else if (flag1)
                    {
                        number31IntValue = -16711936;
                    }

                    this.drawHorizontalLine(twentyFourthIntValue, sixteenthIntValue, number32IntValue, number31IntValue);
                    this.drawVerticalLine(sixteenthIntValue, number32IntValue, twentyFifthIntValue, number31IntValue);

                    if (twentyFourthIntValue > sixteenthIntValue)
                    {
                        this.drawTexturedModalRect(twentyFourthIntValue - 11 - 7, number32IntValue - 5, 114, 234, 7, 11);
                    }
                    else if (twentyFourthIntValue < sixteenthIntValue)
                    {
                        this.drawTexturedModalRect(twentyFourthIntValue + 11, number32IntValue - 5, 107, 234, 7, 11);
                    }
                    else if (number32IntValue > twentyFifthIntValue)
                    {
                        this.drawTexturedModalRect(twentyFourthIntValue - 5, number32IntValue - 11 - 7, 96, 234, 11, 7);
                    }
                    else if (number32IntValue < twentyFifthIntValue)
                    {
                        this.drawTexturedModalRect(twentyFourthIntValue - 5, number32IntValue + 11, 96, 241, 11, 7);
                    }
                }
            }
        }

        Achievement achievement = null;
        float fifthFloatValue = (float)(mouseX - intValue) * this.zoom;
        float sixthFloatValue = (float)(mouseY - eleventhIntValue) * this.zoom;
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableColorMaterial();

        for (int seventhIntValue = 0; seventhIntValue < AchievementList.achievementList.size(); ++seventhIntValue)
        {
            Achievement achievement2 = (Achievement)AchievementList.achievementList.get(seventhIntValue);
            int number33IntValue = achievement2.displayColumn * 24 - i;
            int seventeenthIntValue = achievement2.displayRow * 24 - j;

            if (number33IntValue >= -24 && seventeenthIntValue >= -24 && (float)number33IntValue <= 224.0F * this.zoom && (float)seventeenthIntValue <= 155.0F * this.zoom)
            {
                int number34IntValue = this.statFileWriter.getAchievementUnlockDepth(achievement2);

                if (this.statFileWriter.hasAchievementUnlocked(achievement2))
                {
                    float seventhFloatValue = 0.75F;
                    GlStateManager.color(seventhFloatValue, seventhFloatValue, seventhFloatValue, 1.0F);
                }
                else if (this.statFileWriter.canUnlockAchievement(achievement2))
                {
                    float eighthFloatValue = 1.0F;
                    GlStateManager.color(eighthFloatValue, eighthFloatValue, eighthFloatValue, 1.0F);
                }
                else if (number34IntValue < 3)
                {
                    float ninthFloatValue = 0.3F;
                    GlStateManager.color(ninthFloatValue, ninthFloatValue, ninthFloatValue, 1.0F);
                }
                else if (number34IntValue == 3)
                {
                    float tenthFloatValue = 0.2F;
                    GlStateManager.color(tenthFloatValue, tenthFloatValue, tenthFloatValue, 1.0F);
                }
                else
                {
                    if (number34IntValue != 4)
                    {
                        continue;
                    }

                    float eleventhFloatValue = 0.1F;
                    GlStateManager.color(eleventhFloatValue, eleventhFloatValue, eleventhFloatValue, 1.0F);
                }

                this.mc.getTextureManager().bindTexture(ACHIEVEMENT_BACKGROUND);

                if (achievement2.getSpecial())
                {
                    this.drawTexturedModalRect(number33IntValue - 2, seventeenthIntValue - 2, 26, 202, 26, 26);
                }
                else
                {
                    this.drawTexturedModalRect(number33IntValue - 2, seventeenthIntValue - 2, 0, 202, 26, 26);
                }

                if (!this.statFileWriter.canUnlockAchievement(achievement2))
                {
                    float thirdFloatValue = 0.1F;
                    GlStateManager.color(thirdFloatValue, thirdFloatValue, thirdFloatValue, 1.0F);
                    this.itemRender.isNotRenderingEffectsInGUI(false);
                }

                GlStateManager.enableLighting();
                GlStateManager.enableCull();
                this.itemRender.renderItemAndEffectIntoGUI(achievement2.theItemStack, number33IntValue + 3, seventeenthIntValue + 3);
                GlStateManager.blendFunc(770, 771);
                GlStateManager.disableLighting();

                if (!this.statFileWriter.canUnlockAchievement(achievement2))
                {
                    this.itemRender.isNotRenderingEffectsInGUI(true);
                }

                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

                if (fifthFloatValue >= (float)number33IntValue && fifthFloatValue <= (float)(number33IntValue + 22) && sixthFloatValue >= (float)seventeenthIntValue && sixthFloatValue <= (float)(seventeenthIntValue + 22))
                {
                    achievement = achievement2;
                }
            }
        }

        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.popMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(ACHIEVEMENT_BACKGROUND);
        this.drawTexturedModalRect(k, l, 0, 0, this.paneWidth, this.paneHeight);
        this.zLevel = 0.0F;
        GlStateManager.depthFunc(515);
        GlStateManager.disableDepth();
        GlStateManager.enableTexture2D();
        super.drawScreen(mouseX, mouseY, partialTicks);

        if (achievement != null)
        {
            String s = achievement.getStatName().getUnformattedText();
            String stringValue = achievement.getDescription();
            int eighthIntValue = mouseX + 12;
            int twentySixthIntValue = mouseY - 4;
            int ninthIntValue = this.statFileWriter.getAchievementUnlockDepth(achievement);

            if (this.statFileWriter.canUnlockAchievement(achievement))
            {
                int eighteenthIntValue = Math.max(this.fontRendererObj.getStringWidth(s), 120);
                int tenthIntValue = this.fontRendererObj.splitStringWidth(stringValue, eighteenthIntValue);

                if (this.statFileWriter.hasAchievementUnlocked(achievement))
                {
                    tenthIntValue += 12;
                }

                this.drawGradientRect(eighthIntValue - 3, twentySixthIntValue - 3, eighthIntValue + eighteenthIntValue + 3, twentySixthIntValue + tenthIntValue + 3 + 12, -1073741824, -1073741824);
                this.fontRendererObj.drawSplitString(stringValue, eighthIntValue, twentySixthIntValue + 12, eighteenthIntValue, -6250336);

                if (this.statFileWriter.hasAchievementUnlocked(achievement))
                {
                    this.fontRendererObj.drawStringWithShadow(I18n.format("achievement.taken", new Object[0]), (float)eighthIntValue, (float)(twentySixthIntValue + tenthIntValue + 4), -7302913);
                }
            }
            else if (ninthIntValue == 3)
            {
                s = I18n.format("achievement.unknown", new Object[0]);
                int twentySeventhIntValue = Math.max(this.fontRendererObj.getStringWidth(s), 120);
                String secondStringValue = (new ChatComponentTranslation("achievement.requires", new Object[] {achievement.parentAchievement.getStatName()})).getUnformattedText();
                int sixthIntValue = this.fontRendererObj.splitStringWidth(secondStringValue, twentySeventhIntValue);
                this.drawGradientRect(eighthIntValue - 3, twentySixthIntValue - 3, eighthIntValue + twentySeventhIntValue + 3, twentySixthIntValue + sixthIntValue + 12 + 3, -1073741824, -1073741824);
                this.fontRendererObj.drawSplitString(secondStringValue, eighthIntValue, twentySixthIntValue + 12, twentySeventhIntValue, -9416624);
            }
            else if (ninthIntValue < 3)
            {
                int number35IntValue = Math.max(this.fontRendererObj.getStringWidth(s), 120);
                String thirdStringValue = (new ChatComponentTranslation("achievement.requires", new Object[] {achievement.parentAchievement.getStatName()})).getUnformattedText();
                int nineteenthIntValue = this.fontRendererObj.splitStringWidth(thirdStringValue, number35IntValue);
                this.drawGradientRect(eighthIntValue - 3, twentySixthIntValue - 3, eighthIntValue + number35IntValue + 3, twentySixthIntValue + nineteenthIntValue + 12 + 3, -1073741824, -1073741824);
                this.fontRendererObj.drawSplitString(thirdStringValue, eighthIntValue, twentySixthIntValue + 12, number35IntValue, -9416624);
            }
            else
            {
                s = null;
            }

            if (s != null)
            {
                this.fontRendererObj.drawStringWithShadow(s, (float)eighthIntValue, (float)twentySixthIntValue, this.statFileWriter.canUnlockAchievement(achievement) ? (achievement.getSpecial() ? -128 : -1) : (achievement.getSpecial() ? -8355776 : -8355712));
            }
        }

        GlStateManager.enableDepth();
        GlStateManager.enableLighting();
        RenderHelper.disableStandardItemLighting();
    }

    private TextureAtlasSprite getBlockTexture(Block block)
    {
        return Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getTexture(block.getDefaultState());
    }

    public boolean doesGuiPauseGame()
    {
        return !this.loadingAchievements;
    }
}
