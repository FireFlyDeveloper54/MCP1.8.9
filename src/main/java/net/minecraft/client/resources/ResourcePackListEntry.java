package net.minecraft.client.resources;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.gui.GuiScreenResourcePacks;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ResourceLocation;

public abstract class ResourcePackListEntry implements GuiListExtended.IGuiListEntry
{
    private static final ResourceLocation RESOURCE_PACKS_TEXTURE = new ResourceLocation("textures/gui/resource_packs.png");
    private static final IChatComponent INCOMPATIBLE_PACK = new ChatComponentTranslation("resourcePack.incompatible", new Object[0]);
    private static final IChatComponent INCOMPATIBLE_PACK_OLD = new ChatComponentTranslation("resourcePack.incompatible.old", new Object[0]);
    private static final IChatComponent INCOMPATIBLE_PACK_NEW = new ChatComponentTranslation("resourcePack.incompatible.new", new Object[0]);
    protected final Minecraft mc;
    protected final GuiScreenResourcePacks resourcePacksGUI;

    public ResourcePackListEntry(GuiScreenResourcePacks resourcePacksGUIIn)
    {
        this.resourcePacksGUI = resourcePacksGUIIn;
        this.mc = Minecraft.getMinecraft();
    }

    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected)
    {
        int i = this.getPackFormat();

        if (i != 1)
        {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            Gui.drawRect(x - 1, y - 1, x + listWidth - 9, y + slotHeight + 1, -8978432);
        }

        this.bindResourcePackIcon();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0F, 0.0F, 32, 32, 32.0F, 32.0F);
        String s = this.getResourcePackName();
        String text = this.getResourcePackDescription();

        if ((this.mc.gameSettings.touchscreen || isSelected) && this.canBeMoved())
        {
            this.mc.getTextureManager().bindTexture(RESOURCE_PACKS_TEXTURE);
            Gui.drawRect(x, y, x + 32, y + 32, -1601138544);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            int j = mouseX - x;
            int k = mouseY - y;

            if (i < 1)
            {
                s = INCOMPATIBLE_PACK.getFormattedText();
                text = INCOMPATIBLE_PACK_OLD.getFormattedText();
            }
            else if (i > 1)
            {
                s = INCOMPATIBLE_PACK.getFormattedText();
                text = INCOMPATIBLE_PACK_NEW.getFormattedText();
            }

            if (this.canAddToSelected())
            {
                if (j < 32)
                {
                    Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0F, 32.0F, 32, 32, 256.0F, 256.0F);
                }
                else
                {
                    Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0F, 0.0F, 32, 32, 256.0F, 256.0F);
                }
            }
            else
            {
                if (this.canRemoveFromSelected())
                {
                    if (j < 16)
                    {
                        Gui.drawModalRectWithCustomSizedTexture(x, y, 32.0F, 32.0F, 32, 32, 256.0F, 256.0F);
                    }
                    else
                    {
                        Gui.drawModalRectWithCustomSizedTexture(x, y, 32.0F, 0.0F, 32, 32, 256.0F, 256.0F);
                    }
                }

                if (this.canMoveUp())
                {
                    if (j < 32 && j > 16 && k < 16)
                    {
                        Gui.drawModalRectWithCustomSizedTexture(x, y, 96.0F, 32.0F, 32, 32, 256.0F, 256.0F);
                    }
                    else
                    {
                        Gui.drawModalRectWithCustomSizedTexture(x, y, 96.0F, 0.0F, 32, 32, 256.0F, 256.0F);
                    }
                }

                if (this.canMoveDown())
                {
                    if (j < 32 && j > 16 && k > 16)
                    {
                        Gui.drawModalRectWithCustomSizedTexture(x, y, 64.0F, 32.0F, 32, 32, 256.0F, 256.0F);
                    }
                    else
                    {
                        Gui.drawModalRectWithCustomSizedTexture(x, y, 64.0F, 0.0F, 32, 32, 256.0F, 256.0F);
                    }
                }
            }
        }

        int intValue = this.mc.fontRendererObj.getStringWidth(s);

        if (intValue > 157)
        {
            s = this.mc.fontRendererObj.trimStringToWidth(s, 157 - this.mc.fontRendererObj.getStringWidth("...")) + "...";
        }

        this.mc.fontRendererObj.drawStringWithShadow(s, (float)(x + 32 + 2), (float)(y + 1), 16777215);
        List<String> list = this.mc.fontRendererObj.listFormattedStringToWidth(text, 157);

        for (int l = 0; l < 2 && l < list.size(); ++l)
        {
            this.mc.fontRendererObj.drawStringWithShadow(list.get(l), (float)(x + 32 + 2), (float)(y + 12 + 10 * l), 8421504);
        }
    }

    protected abstract int getPackFormat();

    protected abstract String getResourcePackDescription();

    protected abstract String getResourcePackName();

    protected abstract void bindResourcePackIcon();

    protected boolean canBeMoved()
    {
        return true;
    }

    protected boolean canAddToSelected()
    {
        return !this.resourcePacksGUI.hasResourcePackEntry(this);
    }

    protected boolean canRemoveFromSelected()
    {
        return this.resourcePacksGUI.hasResourcePackEntry(this);
    }

    protected boolean canMoveUp()
    {
        List<ResourcePackListEntry> list = this.resourcePacksGUI.getListContaining(this);
        int i = list.indexOf(this);
        return i > 0 && list.get(i - 1).canBeMoved();
    }

    protected boolean canMoveDown()
    {
        List<ResourcePackListEntry> list = this.resourcePacksGUI.getListContaining(this);
        int i = list.indexOf(this);
        return i >= 0 && i < list.size() - 1 && list.get(i + 1).canBeMoved();
    }

    public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY)
    {
        if (this.canBeMoved() && relativeX <= 32)
        {
            if (this.canAddToSelected())
            {
                this.resourcePacksGUI.markChanged();
                int j = this.getPackFormat();

                if (j != 1)
                {
                    String text = I18n.format("resourcePack.incompatible.confirm.title", new Object[0]);
                    String s = I18n.format("resourcePack.incompatible.confirm." + (j > 1 ? "new" : "old"), new Object[0]);
                    this.mc.displayGuiScreen(new GuiYesNo(new GuiYesNoCallback()
                    {
                        public void confirmClicked(boolean result, int id)
                        {
                            List<ResourcePackListEntry> list2 = ResourcePackListEntry.this.resourcePacksGUI.getListContaining(ResourcePackListEntry.this);
                            ResourcePackListEntry.this.mc.displayGuiScreen(ResourcePackListEntry.this.resourcePacksGUI);

                            if (result)
                            {
                                list2.remove(ResourcePackListEntry.this);
                                ResourcePackListEntry.this.resourcePacksGUI.getSelectedResourcePacks().add(0, ResourcePackListEntry.this);
                            }
                        }
                    }, text, s, 0));
                }
                else
                {
                    this.resourcePacksGUI.getListContaining(this).remove(this);
                    this.resourcePacksGUI.getSelectedResourcePacks().add(0, this);
                }

                return true;
            }

            if (relativeX < 16 && this.canRemoveFromSelected())
            {
                this.resourcePacksGUI.getListContaining(this).remove(this);
                this.resourcePacksGUI.getAvailableResourcePacks().add(0, this);
                this.resourcePacksGUI.markChanged();
                return true;
            }

            if (relativeX > 16 && relativeY < 16 && this.canMoveUp())
            {
                List<ResourcePackListEntry> list1 = this.resourcePacksGUI.getListContaining(this);
                int k = list1.indexOf(this);
                list1.remove(this);
                list1.add(k - 1, this);
                this.resourcePacksGUI.markChanged();
                return true;
            }

            if (relativeX > 16 && relativeY > 16 && this.canMoveDown())
            {
                List<ResourcePackListEntry> list = this.resourcePacksGUI.getListContaining(this);
                int i = list.indexOf(this);
                list.remove(this);
                list.add(i + 1, this);
                this.resourcePacksGUI.markChanged();
                return true;
            }
        }

        return false;
    }

    public void setSelected(int slotIndex, int x, int y)
    {
    }

    public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY)
    {
    }
}
