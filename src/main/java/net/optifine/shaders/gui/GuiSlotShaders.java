package net.optifine.shaders.gui;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.resources.I18n;
import net.minecraft.src.Config;
import net.optifine.Lang;
import net.optifine.shaders.IShaderPack;
import net.optifine.shaders.Shaders;
import net.optifine.util.ResUtils;

class GuiSlotShaders extends GuiSlot
{
    private ArrayList shaderslist;
    private int selectedIndex;
    private long lastClickedCached = 0L;
    final GuiShaders shadersGui;

    public GuiSlotShaders(GuiShaders shadersGui, int width, int height, int top, int bottom, int slotHeight)
    {
        super(shadersGui.getMc(), width, height, top, bottom, slotHeight);
        this.shadersGui = shadersGui;
        this.updateList();
        this.amountScrolled = 0.0F;
        int selectedScroll = this.selectedIndex * slotHeight;
        int centerScroll = (bottom - top) / 2;

        if (selectedScroll > centerScroll)
        {
            this.scrollBy(selectedScroll - centerScroll);
        }
    }

    public int getListWidth()
    {
        return this.width - 20;
    }

    public void updateList()
    {
        this.shaderslist = Shaders.listOfShaders();
        this.selectedIndex = 0;
        int shaderIndex = 0;

        for (int shaderCount = this.shaderslist.size(); shaderIndex < shaderCount; ++shaderIndex)
        {
            if (((String)this.shaderslist.get(shaderIndex)).equals(Shaders.currentShaderName))
            {
                this.selectedIndex = shaderIndex;
                break;
            }
        }
    }

    protected int getSize()
    {
        return this.shaderslist.size();
    }

    protected void elementClicked(int index, boolean doubleClicked, int mouseX, int mouseY)
    {
        if (index != this.selectedIndex || this.lastClicked != this.lastClickedCached)
        {
            String shaderName = (String)this.shaderslist.get(index);
            IShaderPack shaderPack = Shaders.getShaderPack(shaderName);

            if (this.checkCompatible(shaderPack, index))
            {
                this.selectIndex(index);
            }
        }
    }

    private void selectIndex(int index)
    {
        this.selectedIndex = index;
        this.lastClickedCached = this.lastClicked;
        Shaders.setShaderPack((String)this.shaderslist.get(index));
        Shaders.uninit();
        this.shadersGui.updateButtons();
    }

    private boolean checkCompatible(IShaderPack shaderPack, final int index)
    {
        if (shaderPack == null)
        {
            return true;
        }
        else
        {
            InputStream inputStream = shaderPack.getResourceAsStream("/shaders/shaders.properties");
            Properties properties = ResUtils.readProperties(inputStream, "Shaders");

            if (properties == null)
            {
                return true;
            }
            else
            {
                String versionKey = "version.1.8.9";
                String requiredVersion = properties.getProperty(versionKey);

                if (requiredVersion == null)
                {
                    return true;
                }
                else
                {
                    requiredVersion = requiredVersion.trim();
                    String currentRelease = "M6_pre2";
                    int compareResult = Config.compareRelease(currentRelease, requiredVersion);

                    if (compareResult >= 0)
                    {
                        return true;
                    }
                    else
                    {
                        String requiredVersionName = ("HD_U_" + requiredVersion).replace('_', ' ');
                        String line1 = I18n.format("of.message.shaders.nv1", new Object[] {requiredVersionName});
                        String line2 = I18n.format("of.message.shaders.nv2", new Object[0]);
                        GuiYesNoCallback yesNoCallback = new GuiYesNoCallback()
                        {
                            public void confirmClicked(boolean result, int id)
                            {
                                if (result)
                                {
                                    GuiSlotShaders.this.selectIndex(index);
                                }

                                GuiSlotShaders.this.mc.displayGuiScreen(GuiSlotShaders.this.shadersGui);
                            }
                        };
                        GuiYesNo yesNoScreen = new GuiYesNo(yesNoCallback, line1, line2, 0);
                        this.mc.displayGuiScreen(yesNoScreen);
                        return false;
                    }
                }
            }
        }
    }

    protected boolean isSelected(int index)
    {
        return index == this.selectedIndex;
    }

    protected int getScrollBarX()
    {
        return this.width - 6;
    }

    protected int getContentHeight()
    {
        return this.getSize() * 18;
    }

    protected void drawBackground()
    {
    }

    protected void drawSlot(int index, int posX, int posY, int contentY, int mouseX, int mouseY)
    {
        String shaderName = (String)this.shaderslist.get(index);

        if (shaderName.equals("OFF"))
        {
            shaderName = Lang.get("of.options.shaders.packNone");
        }
        else if (shaderName.equals("(internal)"))
        {
            shaderName = Lang.get("of.options.shaders.packDefault");
        }

        this.shadersGui.drawCenteredString(shaderName, this.width / 2, posY + 1, 14737632);
    }

    public int getSelectedIndex()
    {
        return this.selectedIndex;
    }
}
