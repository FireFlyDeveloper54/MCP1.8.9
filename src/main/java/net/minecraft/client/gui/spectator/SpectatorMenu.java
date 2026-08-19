package net.minecraft.client.gui.spectator;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiSpectator;
import net.minecraft.client.gui.spectator.categories.SpectatorDetails;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;

public class SpectatorMenu
{
    private static final ISpectatorMenuObject CLOSE_ITEM = new SpectatorMenu.EndSpectatorObject();
    private static final ISpectatorMenuObject SCROLL_LEFT = new SpectatorMenu.MoveMenuObject(-1, true);
    private static final ISpectatorMenuObject SCROLL_RIGHT_ENABLED = new SpectatorMenu.MoveMenuObject(1, true);
    private static final ISpectatorMenuObject SCROLL_RIGHT_DISABLED = new SpectatorMenu.MoveMenuObject(1, false);
    public static final ISpectatorMenuObject EMPTY_SLOT = new ISpectatorMenuObject()
    {
        public void selectItem(SpectatorMenu menu)
        {
        }
        public IChatComponent getSpectatorName()
        {
            return new ChatComponentText("");
        }
        public void renderIcon(float brightness, int alpha)
        {
        }
        public boolean isEnabled()
        {
            return false;
        }
    };
    private final ISpectatorMenuRecipient recipient;
    private final List<SpectatorDetails> previousCategories = Lists.<SpectatorDetails>newArrayList();
    private ISpectatorMenuView category = new BaseSpectatorGroup();
    private int selectedSlot = -1;
    private int page;

    public SpectatorMenu(ISpectatorMenuRecipient recipientIn)
    {
        this.recipient = recipientIn;
    }

    public ISpectatorMenuObject getItem(int slot)
    {
        int i = slot + this.page * 6;
        return this.page > 0 && slot == 0 ? SCROLL_LEFT : (slot == 7 ? (i < this.category.getItems().size() ? SCROLL_RIGHT_ENABLED : SCROLL_RIGHT_DISABLED) : (slot == 8 ? CLOSE_ITEM : (i >= 0 && i < this.category.getItems().size() ? (ISpectatorMenuObject)MoreObjects.firstNonNull(this.category.getItems().get(i), EMPTY_SLOT) : EMPTY_SLOT)));
    }

    public List<ISpectatorMenuObject> getItems()
    {
        List<ISpectatorMenuObject> list = Lists.<ISpectatorMenuObject>newArrayList();

        for (int i = 0; i <= 8; ++i)
        {
            list.add(this.getItem(i));
        }

        return list;
    }

    public ISpectatorMenuObject getSelectedItem()
    {
        return this.getItem(this.selectedSlot);
    }

    public ISpectatorMenuView getSelectedCategory()
    {
        return this.category;
    }

    public void selectSlot(int slot)
    {
        ISpectatorMenuObject ispectatormenuobject = this.getItem(slot);

        if (ispectatormenuobject != EMPTY_SLOT)
        {
            if (this.selectedSlot == slot && ispectatormenuobject.isEnabled())
            {
                ispectatormenuobject.selectItem(this);
            }
            else
            {
                this.selectedSlot = slot;
            }
        }
    }

    public void exit()
    {
        this.recipient.onSpectatorMenuClosed(this);
    }

    public int getSelectedSlot()
    {
        return this.selectedSlot;
    }

    public void selectCategory(ISpectatorMenuView categoryIn)
    {
        this.previousCategories.add(this.getCurrentPage());
        this.category = categoryIn;
        this.selectedSlot = -1;
        this.page = 0;
    }

    public SpectatorDetails getCurrentPage()
    {
        return new SpectatorDetails(this.category, this.getItems(), this.selectedSlot);
    }

    static class EndSpectatorObject implements ISpectatorMenuObject
    {
        private EndSpectatorObject()
        {
        }

        public void selectItem(SpectatorMenu menu)
        {
            menu.exit();
        }

        public IChatComponent getSpectatorName()
        {
            return new ChatComponentText("Close menu");
        }

        public void renderIcon(float brightness, int alpha)
        {
            Minecraft.getMinecraft().getTextureManager().bindTexture(GuiSpectator.SPECTATOR_WIDGETS_TEX_PATH);
            Gui.drawModalRectWithCustomSizedTexture(0, 0, 128.0F, 0.0F, 16, 16, 256.0F, 256.0F);
        }

        public boolean isEnabled()
        {
            return true;
        }
    }

    static class MoveMenuObject implements ISpectatorMenuObject
    {
        private final int direction;
        private final boolean enabled;

        public MoveMenuObject(int directionIn, boolean enabledIn)
        {
            this.direction = directionIn;
            this.enabled = enabledIn;
        }

        public void selectItem(SpectatorMenu menu)
        {
            menu.page = this.direction;
        }

        public IChatComponent getSpectatorName()
        {
            return this.direction < 0 ? new ChatComponentText("Previous Page") : new ChatComponentText("Next Page");
        }

        public void renderIcon(float brightness, int alpha)
        {
            Minecraft.getMinecraft().getTextureManager().bindTexture(GuiSpectator.SPECTATOR_WIDGETS_TEX_PATH);

            if (this.direction < 0)
            {
                Gui.drawModalRectWithCustomSizedTexture(0, 0, 144.0F, 0.0F, 16, 16, 256.0F, 256.0F);
            }
            else
            {
                Gui.drawModalRectWithCustomSizedTexture(0, 0, 160.0F, 0.0F, 16, 16, 256.0F, 256.0F);
            }
        }

        public boolean isEnabled()
        {
            return this.enabled;
        }
    }
}
