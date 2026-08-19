package net.minecraft.client.gui;

import com.google.common.base.MoreObjects;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.util.IntHashMap;

public class GuiPageButtonList extends GuiListExtended
{
    private final List<GuiPageButtonList.GuiEntry> entries = Lists.<GuiPageButtonList.GuiEntry>newArrayList();
    private final IntHashMap<Gui> controlsById = new IntHashMap();
    private final List<GuiTextField> textFields = Lists.<GuiTextField>newArrayList();
    private final GuiPageButtonList.GuiListEntry[][] pages;
    private int currentPage;
    private GuiPageButtonList.GuiResponder responder;
    private Gui focusedControl;

    public GuiPageButtonList(Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn, GuiPageButtonList.GuiResponder responderIn, GuiPageButtonList.GuiListEntry[]... pagesIn)
    {
        super(mcIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn);
        this.responder = responderIn;
        this.pages = pagesIn;
        this.canBeScrolled = false;
        this.createControls();
        this.rebuildEntriesForCurrentPage();
    }

    private void createControls()
    {
        for (GuiPageButtonList.GuiListEntry[] basicEntries : this.pages)
        {
            for (int i = 0; i < basicEntries.length; i += 2)
            {
                GuiPageButtonList.GuiListEntry leftEntry = basicEntries[i];
                GuiPageButtonList.GuiListEntry rightEntry = i < basicEntries.length - 1 ? basicEntries[i + 1] : null;
                Gui gui = this.createControl(leftEntry, 0, rightEntry == null);
                Gui secondGui = this.createControl(rightEntry, 160, leftEntry == null);
                GuiPageButtonList.GuiEntry guiEntry = new GuiPageButtonList.GuiEntry(gui, secondGui);
                this.entries.add(guiEntry);

                if (leftEntry != null && gui != null)
                {
                    this.controlsById.addKey(leftEntry.getId(), gui);

                    if (gui instanceof GuiTextField)
                    {
                        this.textFields.add((GuiTextField)gui);
                    }
                }

                if (rightEntry != null && secondGui != null)
                {
                    this.controlsById.addKey(rightEntry.getId(), secondGui);

                    if (secondGui instanceof GuiTextField)
                    {
                        this.textFields.add((GuiTextField)secondGui);
                    }
                }
            }
        }
    }

    private void rebuildEntriesForCurrentPage()
    {
        this.entries.clear();

        for (int i = 0; i < this.pages[this.currentPage].length; i += 2)
        {
            GuiPageButtonList.GuiListEntry leftEntry = this.pages[this.currentPage][i];
            GuiPageButtonList.GuiListEntry rightEntry = i < this.pages[this.currentPage].length - 1 ? this.pages[this.currentPage][i + 1] : null;
            Gui gui = (Gui)this.controlsById.lookup(leftEntry.getId());
            Gui thirdGui = rightEntry != null ? (Gui)this.controlsById.lookup(rightEntry.getId()) : null;
            GuiPageButtonList.GuiEntry guiEntry = new GuiPageButtonList.GuiEntry(gui, thirdGui);
            this.entries.add(guiEntry);
        }
    }

    public void setPage(int newPage)
    {
        if (newPage != this.currentPage)
        {
            int i = this.currentPage;
            this.currentPage = newPage;
            this.rebuildEntriesForCurrentPage();
            this.updatePageVisibility(i, newPage);
            this.amountScrolled = 0.0F;
        }
    }

    public int getCurrentPage()
    {
        return this.currentPage;
    }

    public int getPageCount()
    {
        return this.pages.length;
    }

    public Gui getFocusedControl()
    {
        return this.focusedControl;
    }

    public void previousPage()
    {
        if (this.currentPage > 0)
        {
            this.setPage(this.currentPage - 1);
        }
    }

    public void nextPage()
    {
        if (this.currentPage < this.pages.length - 1)
        {
            this.setPage(this.currentPage + 1);
        }
    }

    public Gui getControlById(int id)
    {
        return (Gui)this.controlsById.lookup(id);
    }

    private void updatePageVisibility(int oldPage, int newPage)
    {
        for (GuiPageButtonList.GuiListEntry leftEntry : this.pages[oldPage])
        {
            if (leftEntry != null)
            {
                this.setControlVisible((Gui)this.controlsById.lookup(leftEntry.getId()), false);
            }
        }

        for (GuiPageButtonList.GuiListEntry rightEntry : this.pages[newPage])
        {
            if (rightEntry != null)
            {
                this.setControlVisible((Gui)this.controlsById.lookup(rightEntry.getId()), true);
            }
        }
    }

    private void setControlVisible(Gui control, boolean visibleIn)
    {
        if (control instanceof GuiButton)
        {
            ((GuiButton)control).visible = visibleIn;
        }
        else if (control instanceof GuiTextField)
        {
            ((GuiTextField)control).setVisible(visibleIn);
        }
        else if (control instanceof GuiLabel)
        {
            ((GuiLabel)control).visible = visibleIn;
        }
    }

    private Gui createControl(GuiPageButtonList.GuiListEntry entry, int xOffset, boolean fullWidth)
    {
        return (Gui)(entry instanceof GuiPageButtonList.GuiSlideEntry ? this.createSlider(this.width / 2 - 155 + xOffset, 0, (GuiPageButtonList.GuiSlideEntry)entry) : (entry instanceof GuiPageButtonList.GuiButtonEntry ? this.createButton(this.width / 2 - 155 + xOffset, 0, (GuiPageButtonList.GuiButtonEntry)entry) : (entry instanceof GuiPageButtonList.EditBoxEntry ? this.createTextField(this.width / 2 - 155 + xOffset, 0, (GuiPageButtonList.EditBoxEntry)entry) : (entry instanceof GuiPageButtonList.GuiLabelEntry ? this.createLabel(this.width / 2 - 155 + xOffset, 0, (GuiPageButtonList.GuiLabelEntry)entry, fullWidth) : null))));
    }

    public void setAllButtonsEnabled(boolean enabled)
    {
        for (GuiPageButtonList.GuiEntry guiEntry : this.entries)
        {
            if (guiEntry.leftControl instanceof GuiButton)
            {
                ((GuiButton)guiEntry.leftControl).enabled = enabled;
            }

            if (guiEntry.rightControl instanceof GuiButton)
            {
                ((GuiButton)guiEntry.rightControl).enabled = enabled;
            }
        }
    }

    public boolean mouseClicked(int mouseX, int mouseY, int mouseEvent)
    {
        boolean flag = super.mouseClicked(mouseX, mouseY, mouseEvent);
        int i = this.getSlotIndexFromScreenCoords(mouseX, mouseY);

        if (i >= 0)
        {
            GuiPageButtonList.GuiEntry guiEntry = this.getListEntry(i);

            if (this.focusedControl != guiEntry.focusedControl && this.focusedControl != null && this.focusedControl instanceof GuiTextField)
            {
                ((GuiTextField)this.focusedControl).setFocused(false);
            }

            this.focusedControl = guiEntry.focusedControl;
        }

        return flag;
    }

    private GuiSlider createSlider(int x, int y, GuiPageButtonList.GuiSlideEntry entry)
    {
        GuiSlider slider = new GuiSlider(this.responder, entry.getId(), x, y, entry.getLabel(), entry.getMinValue(), entry.getMaxValue(), entry.getSliderInitialValue(), entry.getFormatHelper());
        slider.visible = entry.isVisible();
        return slider;
    }

    private GuiListButton createButton(int x, int y, GuiPageButtonList.GuiButtonEntry entry)
    {
        GuiListButton listButton = new GuiListButton(this.responder, entry.getId(), x, y, entry.getLabel(), entry.getInitialValue());
        listButton.visible = entry.isVisible();
        return listButton;
    }

    private GuiTextField createTextField(int x, int y, GuiPageButtonList.EditBoxEntry entry)
    {
        GuiTextField textField = new GuiTextField(entry.getId(), this.mc.fontRendererObj, x, y, 150, 20);
        textField.setText(entry.getLabel());
        textField.setGuiResponder(this.responder);
        textField.setVisible(entry.isVisible());
        textField.setValidator(entry.getValidator());
        return textField;
    }

    private GuiLabel createLabel(int x, int y, GuiPageButtonList.GuiLabelEntry entry, boolean fullWidth)
    {
        GuiLabel labelControl;

        if (fullWidth)
        {
            labelControl = new GuiLabel(this.mc.fontRendererObj, entry.getId(), x, y, this.width - x * 2, 20, -1);
        }
        else
        {
            labelControl = new GuiLabel(this.mc.fontRendererObj, entry.getId(), x, y, 150, 20, -1);
        }

        labelControl.visible = entry.isVisible();
        labelControl.addLine(entry.getLabel());
        labelControl.setCentered();
        return labelControl;
    }

    public void keyTyped(char typedChar, int keyCode)
    {
        if (this.focusedControl instanceof GuiTextField)
        {
            GuiTextField textField = (GuiTextField)this.focusedControl;

            if (!GuiScreen.isKeyComboCtrlV(keyCode))
            {
                if (keyCode == 15)
                {
                    textField.setFocused(false);
                    int k = this.textFields.indexOf(this.focusedControl);

                    if (GuiScreen.isShiftKeyDown())
                    {
                        if (k == 0)
                        {
                            k = this.textFields.size() - 1;
                        }
                        else
                        {
                            --k;
                        }
                    }
                    else if (k == this.textFields.size() - 1)
                    {
                        k = 0;
                    }
                    else
                    {
                        ++k;
                    }

                    this.focusedControl = this.textFields.get(k);
                    textField = (GuiTextField)this.focusedControl;
                    textField.setFocused(true);
                    int l = textField.yPosition + this.slotHeight;
                    int intValue = textField.yPosition;

                    if (l > this.bottom)
                    {
                        this.amountScrolled += (float)(l - this.bottom);
                    }
                    else if (intValue < this.top)
                    {
                        this.amountScrolled = (float)intValue;
                    }
                }
                else
                {
                    textField.textboxKeyTyped(typedChar, keyCode);
                }
            }
            else
            {
                String s = GuiScreen.getClipboardString();
                String[] astring = s.split(";");
                int i = this.textFields.indexOf(this.focusedControl);
                int j = i;

                for (String stringValue : astring)
                {
                    this.textFields.get(j).setText(stringValue);

                    if (j == this.textFields.size() - 1)
                    {
                        j = 0;
                    }
                    else
                    {
                        ++j;
                    }

                    if (j == i)
                    {
                        break;
                    }
                }
            }
        }
    }

    public GuiPageButtonList.GuiEntry getListEntry(int index)
    {
        return (GuiPageButtonList.GuiEntry)this.entries.get(index);
    }

    public int getSize()
    {
        return this.entries.size();
    }

    public int getListWidth()
    {
        return 400;
    }

    protected int getScrollBarX()
    {
        return super.getScrollBarX() + 32;
    }

    public static class EditBoxEntry extends GuiPageButtonList.GuiListEntry
    {
        private final Predicate<String> validator;

        public EditBoxEntry(int id, String label, boolean visible, Predicate<String> validatorIn)
        {
            super(id, label, visible);
            this.validator = (Predicate)MoreObjects.firstNonNull(validatorIn, Predicates.alwaysTrue());
        }

        public Predicate<String> getValidator()
        {
            return this.validator;
        }
    }

    public static class GuiButtonEntry extends GuiPageButtonList.GuiListEntry
    {
        private final boolean initialValue;

        public GuiButtonEntry(int id, String label, boolean visible, boolean initialValueIn)
        {
            super(id, label, visible);
            this.initialValue = initialValueIn;
        }

        public boolean getInitialValue()
        {
            return this.initialValue;
        }
    }

    public static class GuiEntry implements GuiListExtended.IGuiListEntry
    {
        private final Minecraft minecraft = Minecraft.getMinecraft();
        private final Gui leftControl;
        private final Gui rightControl;
        private Gui focusedControl;

        public GuiEntry(Gui leftControlIn, Gui rightControlIn)
        {
            this.leftControl = leftControlIn;
            this.rightControl = rightControlIn;
        }

        public Gui getLeftControl()
        {
            return this.leftControl;
        }

        public Gui getRightControl()
        {
            return this.rightControl;
        }

        public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected)
        {
            this.drawControl(this.leftControl, y, mouseX, mouseY, false);
            this.drawControl(this.rightControl, y, mouseX, mouseY, false);
        }

        private void drawControl(Gui control, int y, int mouseX, int mouseY, boolean selected)
        {
            if (control != null)
            {
                if (control instanceof GuiButton)
                {
                    this.drawButton((GuiButton)control, y, mouseX, mouseY, selected);
                }
                else if (control instanceof GuiTextField)
                {
                    this.drawTextField((GuiTextField)control, y, selected);
                }
                else if (control instanceof GuiLabel)
                {
                    this.drawLabel((GuiLabel)control, y, mouseX, mouseY, selected);
                }
            }
        }

        private void drawButton(GuiButton button, int y, int mouseX, int mouseY, boolean selected)
        {
            button.yPosition = y;

            if (!selected)
            {
                button.drawButton(this.minecraft, mouseX, mouseY);
            }
        }

        private void drawTextField(GuiTextField textField, int y, boolean selected)
        {
            textField.yPosition = y;

            if (!selected)
            {
                textField.drawTextBox();
            }
        }

        private void drawLabel(GuiLabel labelControl, int y, int mouseX, int mouseY, boolean selected)
        {
            labelControl.yPosition = y;

            if (!selected)
            {
                labelControl.drawLabel(this.minecraft, mouseX, mouseY);
            }
        }

        public void setSelected(int slotIndex, int x, int y)
        {
            this.drawControl(this.leftControl, y, 0, 0, true);
            this.drawControl(this.rightControl, y, 0, 0, true);
        }

        public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY)
        {
            boolean flag = this.mousePressedControl(this.leftControl, mouseX, mouseY, mouseEvent);
            boolean flag1 = this.mousePressedControl(this.rightControl, mouseX, mouseY, mouseEvent);
            return flag || flag1;
        }

        private boolean mousePressedControl(Gui control, int mouseX, int mouseY, int mouseEvent)
        {
            if (control == null)
            {
                return false;
            }
            else if (control instanceof GuiButton)
            {
                return this.mousePressedButton((GuiButton)control, mouseX, mouseY, mouseEvent);
            }
            else
            {
                if (control instanceof GuiTextField)
                {
                    this.mousePressedTextField((GuiTextField)control, mouseX, mouseY, mouseEvent);
                }

                return false;
            }
        }

        private boolean mousePressedButton(GuiButton button, int mouseX, int mouseY, int mouseEvent)
        {
            boolean flag = button.mousePressed(this.minecraft, mouseX, mouseY);

            if (flag)
            {
                this.focusedControl = button;
            }

            return flag;
        }

        private void mousePressedTextField(GuiTextField textField, int mouseX, int mouseY, int mouseEvent)
        {
            textField.mouseClicked(mouseX, mouseY, mouseEvent);

            if (textField.isFocused())
            {
                this.focusedControl = textField;
            }
        }

        public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY)
        {
            this.mouseReleasedControl(this.leftControl, x, y, mouseEvent);
            this.mouseReleasedControl(this.rightControl, x, y, mouseEvent);
        }

        private void mouseReleasedControl(Gui control, int x, int y, int mouseEvent)
        {
            if (control != null)
            {
                if (control instanceof GuiButton)
                {
                    this.mouseReleasedButton((GuiButton)control, x, y, mouseEvent);
                }
            }
        }

        private void mouseReleasedButton(GuiButton button, int x, int y, int mouseEvent)
        {
            button.mouseReleased(x, y);
        }
    }

    public static class GuiLabelEntry extends GuiPageButtonList.GuiListEntry
    {
        public GuiLabelEntry(int id, String label, boolean visible)
        {
            super(id, label, visible);
        }
    }

    public static class GuiListEntry
    {
        private final int id;
        private final String label;
        private final boolean visible;

        public GuiListEntry(int id, String label, boolean visible)
        {
            this.id = id;
            this.label = label;
            this.visible = visible;
        }

        public int getId()
        {
            return this.id;
        }

        public String getLabel()
        {
            return this.label;
        }

        public boolean isVisible()
        {
            return this.visible;
        }
    }

    public interface GuiResponder
    {
        void setEntryValue(int id, boolean value);

        void onTick(int id, float value);

        void setEntryValue(int id, String text);
    }

    public static class GuiSlideEntry extends GuiPageButtonList.GuiListEntry
    {
        private final GuiSlider.FormatHelper formatHelper;
        private final float minValue;
        private final float maxValue;
        private final float sliderInitialValue;

        public GuiSlideEntry(int id, String label, boolean visible, GuiSlider.FormatHelper formatHelperIn, float minValueIn, float maxValueIn, float initialValueIn)
        {
            super(id, label, visible);
            this.formatHelper = formatHelperIn;
            this.minValue = minValueIn;
            this.maxValue = maxValueIn;
            this.sliderInitialValue = initialValueIn;
        }

        public GuiSlider.FormatHelper getFormatHelper()
        {
            return this.formatHelper;
        }

        public float getMinValue()
        {
            return this.minValue;
        }

        public float getMaxValue()
        {
            return this.maxValue;
        }

        public float getSliderInitialValue()
        {
            return this.sliderInitialValue;
        }
    }
}
