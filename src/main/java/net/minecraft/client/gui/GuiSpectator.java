package net.minecraft.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.spectator.ISpectatorMenuObject;
import net.minecraft.client.gui.spectator.ISpectatorMenuRecipient;
import net.minecraft.client.gui.spectator.SpectatorMenu;
import net.minecraft.client.gui.spectator.categories.SpectatorDetails;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

public class GuiSpectator extends Gui implements ISpectatorMenuRecipient
{
    private static final ResourceLocation WIDGETS_TEX_PATH = new ResourceLocation("textures/gui/widgets.png");
    public static final ResourceLocation SPECTATOR_WIDGETS_TEX_PATH = new ResourceLocation("textures/gui/spectator_widgets.png");
    private final Minecraft mc;
    private long lastSelectionTime;
    private SpectatorMenu menu;

    public GuiSpectator(Minecraft mcIn)
    {
        this.mc = mcIn;
    }

    public void onHotbarSelected(int slot)
    {
        this.lastSelectionTime = Minecraft.getSystemTime();

        if (this.menu != null)
        {
            this.menu.selectSlot(slot);
        }
        else
        {
            this.menu = new SpectatorMenu(this);
        }
    }

    private float getHotbarAlpha()
    {
        long i = this.lastSelectionTime - Minecraft.getSystemTime() + 5000L;
        return MathHelper.clamp_float((float)i / 2000.0F, 0.0F, 1.0F);
    }

    public void renderTooltip(ScaledResolution scaledResolution, float partialTicks)
    {
        if (this.menu != null)
        {
            float f = this.getHotbarAlpha();

            if (f <= 0.0F)
            {
                this.menu.exit();
            }
            else
            {
                int i = scaledResolution.getScaledWidth() / 2;
                float oldZLevel = this.zLevel;
                this.zLevel = -90.0F;
                float menuY = (float)scaledResolution.getScaledHeight() - 22.0F * f;
                SpectatorDetails spectatorDetails = this.menu.getCurrentPage();
                this.renderPage(scaledResolution, f, i, menuY, spectatorDetails);
                this.zLevel = oldZLevel;
            }
        }
    }

    protected void renderPage(ScaledResolution scaledResolution, float alpha, int centerX, float y, SpectatorDetails details)
    {
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
        this.mc.getTextureManager().bindTexture(WIDGETS_TEX_PATH);
        this.drawTexturedModalRect((float)(centerX - 91), y, 0, 0, 182, 22);

        if (details.getSelectedSlot() >= 0)
        {
            this.drawTexturedModalRect((float)(centerX - 91 - 1 + details.getSelectedSlot() * 20), y - 1.0F, 0, 22, 24, 22);
        }

        RenderHelper.enableGUIStandardItemLighting();

        for (int i = 0; i < 9; ++i)
        {
            this.renderSlot(i, scaledResolution.getScaledWidth() / 2 - 90 + i * 20 + 2, y + 3.0F, alpha, details.getObject(i));
        }

        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableBlend();
    }

    private void renderSlot(int slotIndex, int x, float y, float alpha, ISpectatorMenuObject menuObject)
    {
        this.mc.getTextureManager().bindTexture(SPECTATOR_WIDGETS_TEX_PATH);

        if (menuObject != SpectatorMenu.EMPTY_SLOT)
        {
            int i = (int)(alpha * 255.0F);
            GlStateManager.pushMatrix();
            GlStateManager.translate((float)x, y, 0.0F);
            float f = menuObject.isEnabled() ? 1.0F : 0.25F;
            GlStateManager.color(f, f, f, alpha);
            menuObject.renderIcon(f, i);
            GlStateManager.popMatrix();
            String s = String.valueOf((Object)GameSettings.getKeyDisplayString(this.mc.gameSettings.keyBindsHotbar[slotIndex].getKeyCode()));

            if (i > 3 && menuObject.isEnabled())
            {
                this.mc.fontRendererObj.drawStringWithShadow(s, (float)(x + 19 - 2 - this.mc.fontRendererObj.getStringWidth(s)), y + 9.0F, 16777215 + (i << 24));
            }
        }
    }

    public void renderSelectedItem(ScaledResolution scaledResolution)
    {
        int i = (int)(this.getHotbarAlpha() * 255.0F);

        if (i > 3 && this.menu != null)
        {
            ISpectatorMenuObject ispectatormenuobject = this.menu.getSelectedItem();
        String s = ispectatormenuobject != SpectatorMenu.EMPTY_SLOT ? ispectatormenuobject.getSpectatorName().getFormattedText() : this.menu.getSelectedCategory().getPrompt().getFormattedText();

            if (s != null)
            {
                int j = (scaledResolution.getScaledWidth() - this.mc.fontRendererObj.getStringWidth(s)) / 2;
                int k = scaledResolution.getScaledHeight() - 35;
                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                this.mc.fontRendererObj.drawStringWithShadow(s, (float)j, (float)k, 16777215 + (i << 24));
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            }
        }
    }

    public void onSpectatorMenuClosed(SpectatorMenu menu)
    {
        this.menu = null;
        this.lastSelectionTime = 0L;
    }

    public boolean isMenuActive()
    {
        return this.menu != null;
    }

    public void cycleSlot(int direction)
    {
        int i;

        for (i = this.menu.getSelectedSlot() + direction; i >= 0 && i <= 8 && (this.menu.getItem(i) == SpectatorMenu.EMPTY_SLOT || !this.menu.getItem(i).isEnabled()); i += direction)
        {
            ;
        }

        if (i >= 0 && i <= 8)
        {
            this.menu.selectSlot(i);
            this.lastSelectionTime = Minecraft.getSystemTime();
        }
    }

    public void selectCurrentItem()
    {
        this.lastSelectionTime = Minecraft.getSystemTime();

        if (this.isMenuActive())
        {
            int i = this.menu.getSelectedSlot();

            if (i != -1)
            {
                this.menu.selectSlot(i);
            }
        }
        else
        {
            this.menu = new SpectatorMenu(this);
        }
    }
}
