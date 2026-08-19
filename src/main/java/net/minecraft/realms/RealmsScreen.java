package net.minecraft.realms;

import com.mojang.util.UUIDTypeAdapter;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreenRealmsProxy;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class RealmsScreen
{
    public static final int SKIN_HEAD_U = 8;
    public static final int SKIN_HEAD_V = 8;
    public static final int SKIN_HEAD_WIDTH = 8;
    public static final int SKIN_HEAD_HEIGHT = 8;
    public static final int SKIN_HAT_U = 40;
    public static final int SKIN_HAT_V = 8;
    public static final int SKIN_HAT_WIDTH = 8;
    public static final int SKIN_HAT_HEIGHT = 8;
    public static final int SKIN_TEX_WIDTH = 64;
    public static final int SKIN_TEX_HEIGHT = 64;
    protected Minecraft minecraft;
    public int width;
    public int height;
    private GuiScreenRealmsProxy proxy = new GuiScreenRealmsProxy(this);

    public GuiScreenRealmsProxy getProxy()
    {
        return this.proxy;
    }

    public void init()
    {
    }

    public void init(Minecraft minecraft, int width, int height)
    {
    }

    public void drawCenteredString(String text, int x, int y, int color)
    {
        this.proxy.drawCenteredString(text, x, y, color);
    }

    public void drawString(String text, int x, int y, int color)
    {
        this.drawString(text, x, y, color, true);
    }

    public void drawString(String text, int x, int y, int color, boolean shadow)
    {
        this.proxy.drawString(text, x, y, color, shadow);
    }

    public void blit(int x, int y, int textureX, int textureY, int width, int height)
    {
        this.proxy.drawTexturedModalRect(x, y, textureX, textureY, width, height);
    }

    public static void blit(int x, int y, float textureX, float textureY, int textureWidth, int textureHeight, int width, int height, float atlasWidth, float atlasHeight)
    {
        Gui.drawScaledCustomSizeModalRect(x, y, textureX, textureY, textureWidth, textureHeight, width, height, atlasWidth, atlasHeight);
    }

    public static void blit(int x, int y, float textureX, float textureY, int width, int height, float textureWidth, float textureHeight)
    {
        Gui.drawModalRectWithCustomSizedTexture(x, y, textureX, textureY, width, height, textureWidth, textureHeight);
    }

    public void fillGradient(int left, int top, int right, int bottom, int startColor, int endColor)
    {
        this.proxy.drawGradientRect(left, top, right, bottom, startColor, endColor);
    }

    public void renderBackground()
    {
        this.proxy.drawDefaultBackground();
    }

    public boolean isPauseScreen()
    {
        return this.proxy.doesGuiPauseGame();
    }

    public void renderBackground(int tint)
    {
        this.proxy.drawWorldBackground(tint);
    }

    public void render(int mouseX, int mouseY, float partialTicks)
    {
        for (int i = 0; i < this.proxy.getButtonList().size(); ++i)
            {
                ((RealmsButton)this.proxy.getButtonList().get(i)).render(mouseX, mouseY);
            }
    }

    public void renderTooltip(ItemStack itemStack, int x, int y)
    {
        this.proxy.renderToolTip(itemStack, x, y);
    }

    public void renderTooltip(String text, int x, int y)
    {
        this.proxy.drawCreativeTabHoveringText(text, x, y);
    }

    public void renderTooltip(List<String> lines, int x, int y)
    {
        this.proxy.drawHoveringText(lines, x, y);
    }

    public static void bindFace(String uuid, String username)
    {
        ResourceLocation resourcelocation = AbstractClientPlayer.getLocationSkin(username);

        if (resourcelocation == null)
        {
            resourcelocation = DefaultPlayerSkin.getDefaultSkin(UUIDTypeAdapter.fromString(uuid));
        }

        AbstractClientPlayer.getDownloadImageSkin(resourcelocation, username);
        Minecraft.getMinecraft().getTextureManager().bindTexture(resourcelocation);
    }

    public static void bind(String resourcePath)
    {
        ResourceLocation resourcelocation = new ResourceLocation(resourcePath);
        Minecraft.getMinecraft().getTextureManager().bindTexture(resourcelocation);
    }

    public void tick()
    {
    }

    public int width()
    {
        return this.proxy.width;
    }

    public int height()
    {
        return this.proxy.height;
    }

    public int fontLineHeight()
    {
        return this.proxy.getFontHeight();
    }

    public int fontWidth(String text)
    {
        return this.proxy.getStringWidth(text);
    }

    public void fontDrawShadow(String text, int x, int y, int color)
    {
        this.proxy.drawStringWithShadow(text, x, y, color);
    }

    public List<String> fontSplit(String text, int width)
    {
        return this.proxy.listFormattedStringToWidth(text, width);
    }

    public void buttonClicked(RealmsButton button)
    {
    }

    public static RealmsButton newButton(int id, int x, int y, String text)
    {
        return new RealmsButton(id, x, y, text);
    }

    public static RealmsButton newButton(int id, int x, int y, int width, int height, String text)
    {
        return new RealmsButton(id, x, y, width, height, text);
    }

    public void buttonsClear()
    {
        this.proxy.clearButtons();
    }

    public void buttonsAdd(RealmsButton button)
    {
        this.proxy.addButton(button);
    }

    public List<RealmsButton> buttons()
    {
        return this.proxy.getButtonList();
    }

    public void buttonsRemove(RealmsButton button)
    {
        this.proxy.removeButton(button);
    }

    public RealmsEditBox newEditBox(int id, int x, int y, int width, int height)
    {
        return new RealmsEditBox(id, x, y, width, height);
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton)
    {
    }

    public void mouseEvent()
    {
    }

    public void keyboardEvent()
    {
    }

    public void mouseReleased(int mouseX, int mouseY, int mouseButton)
    {
    }

    public void mouseDragged(int mouseX, int mouseY, int mouseButton, long dragTime)
    {
    }

    public void keyPressed(char typedChar, int keyCode)
    {
    }

    public void confirmResult(boolean result, int id)
    {
    }

    public static String getLocalizedString(String key)
    {
        return I18n.format(key, new Object[0]);
    }

    public static String getLocalizedString(String key, Object... args)
    {
        return I18n.format(key, args);
    }

    public RealmsAnvilLevelStorageSource getLevelStorageSource()
    {
        return new RealmsAnvilLevelStorageSource(Minecraft.getMinecraft().getSaveLoader());
    }

    public void removed()
    {
    }
}
