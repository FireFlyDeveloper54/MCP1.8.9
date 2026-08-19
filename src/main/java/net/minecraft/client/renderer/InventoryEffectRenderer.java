package net.minecraft.client.renderer;

import java.util.Collection;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Container;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

public abstract class InventoryEffectRenderer extends GuiContainer
{
    private boolean hasActivePotionEffects;

    public InventoryEffectRenderer(Container inventorySlotsIn)
    {
        super(inventorySlotsIn);
    }

    public void initGui()
    {
        super.initGui();
        this.updateActivePotionEffects();
    }

    protected void updateActivePotionEffects()
    {
        if (!this.mc.thePlayer.getActivePotionEffects().isEmpty())
        {
            this.guiLeft = 160 + (this.width - this.xSize - 200) / 2;
            this.hasActivePotionEffects = true;
        }
        else
        {
            this.guiLeft = (this.width - this.xSize) / 2;
            this.hasActivePotionEffects = false;
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        super.drawScreen(mouseX, mouseY, partialTicks);

        if (this.hasActivePotionEffects)
        {
            this.drawActivePotionEffects();
        }
    }

    private void drawActivePotionEffects()
    {
        int effectPanelX = this.guiLeft - 124;
        int effectPanelY = this.guiTop;
        int effectPanelTextureY = 166;
        Collection<PotionEffect> activePotionEffects = this.mc.thePlayer.getActivePotionEffects();

        if (!activePotionEffects.isEmpty())
        {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.disableLighting();
            int rowHeight = 33;

            if (activePotionEffects.size() > 5)
            {
                rowHeight = 132 / (activePotionEffects.size() - 1);
            }

            for (PotionEffect potionEffect : this.mc.thePlayer.getActivePotionEffects())
            {
                Potion potion = Potion.potionTypes[potionEffect.getPotionID()];
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                this.mc.getTextureManager().bindTexture(inventoryBackground);
                this.drawTexturedModalRect(effectPanelX, effectPanelY, 0, effectPanelTextureY, 140, 32);

                if (potion.hasStatusIcon())
                {
                    int statusIconIndex = potion.getStatusIconIndex();
                    this.drawTexturedModalRect(effectPanelX + 6, effectPanelY + 7, 0 + statusIconIndex % 8 * 18, 198 + statusIconIndex / 8 * 18, 18, 18);
                }

                String potionName = I18n.format(potion.getName(), new Object[0]);

                if (potionEffect.getAmplifier() == 1)
                {
                    potionName = potionName + " " + I18n.format("enchantment.level.2", new Object[0]);
                }
                else if (potionEffect.getAmplifier() == 2)
                {
                    potionName = potionName + " " + I18n.format("enchantment.level.3", new Object[0]);
                }
                else if (potionEffect.getAmplifier() == 3)
                {
                    potionName = potionName + " " + I18n.format("enchantment.level.4", new Object[0]);
                }

                this.fontRendererObj.drawStringWithShadow(potionName, (float)(effectPanelX + 10 + 18), (float)(effectPanelY + 6), 16777215);
                String durationText = Potion.getDurationString(potionEffect);
                this.fontRendererObj.drawStringWithShadow(durationText, (float)(effectPanelX + 10 + 18), (float)(effectPanelY + 6 + 10), 8355711);
                effectPanelY += rowHeight;
            }
        }
    }
}
