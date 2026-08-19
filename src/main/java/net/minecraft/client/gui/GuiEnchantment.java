package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.model.ModelBook;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerEnchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnchantmentNameParts;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IWorldNameable;
import net.minecraft.world.World;
import org.lwjgl.util.glu.Project;

public class GuiEnchantment extends GuiContainer
{
    private static final ResourceLocation ENCHANTMENT_TABLE_GUI_TEXTURE = new ResourceLocation("textures/gui/container/enchanting_table.png");
    private static final ResourceLocation ENCHANTMENT_TABLE_BOOK_TEXTURE = new ResourceLocation("textures/entity/enchanting_table_book.png");
    private static final ModelBook MODEL_BOOK = new ModelBook();
    private final InventoryPlayer playerInventory;
    private Random random = new Random();
    private ContainerEnchantment container;
    public int tickCount;
    public float open;
    public float oOpen;
    public float flipT;
    public float flipA;
    public float flip;
    public float oFlip;
    ItemStack last;
    private final IWorldNameable nameable;

    public GuiEnchantment(InventoryPlayer inventory, World worldIn, IWorldNameable nameableIn)
    {
        super(new ContainerEnchantment(inventory, worldIn));
        this.playerInventory = inventory;
        this.container = (ContainerEnchantment)this.inventorySlots;
        this.nameable = nameableIn;
    }

    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        this.fontRendererObj.drawString(this.nameable.getDisplayName().getUnformattedText(), 12, 5, 4210752);
        this.fontRendererObj.drawString(this.playerInventory.getDisplayName().getUnformattedText(), 8, this.ySize - 96 + 2, 4210752);
    }

    public void updateScreen()
    {
        super.updateScreen();
        this.tickBook();
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;

        for (int k = 0; k < 3; ++k)
        {
            int l = mouseX - (i + 60);
            int secondIntValue = mouseY - (j + 14 + 19 * k);

            if (l >= 0 && secondIntValue >= 0 && l < 108 && secondIntValue < 19 && this.container.enchantItem(this.mc.thePlayer, k))
            {
                this.mc.playerController.sendEnchantPacket(this.container.windowId, k);
            }
        }
    }

    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
    {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(ENCHANTMENT_TABLE_GUI_TEXTURE);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
        GlStateManager.pushMatrix();
        GlStateManager.matrixMode(5889);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        ScaledResolution scaledresolution = new ScaledResolution(this.mc);
        GlStateManager.viewport((scaledresolution.getScaledWidth() - 320) / 2 * scaledresolution.getScaleFactor(), (scaledresolution.getScaledHeight() - 240) / 2 * scaledresolution.getScaleFactor(), 320 * scaledresolution.getScaleFactor(), 240 * scaledresolution.getScaleFactor());
        GlStateManager.translate(-0.34F, 0.23F, 0.0F);
        Project.gluPerspective(90.0F, 1.3333334F, 9.0F, 80.0F);
        float f = 1.0F;
        GlStateManager.matrixMode(5888);
        GlStateManager.loadIdentity();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.translate(0.0F, 3.3F, -16.0F);
        GlStateManager.scale(f, f, f);
        float secondFloatValue = 5.0F;
        GlStateManager.scale(secondFloatValue, secondFloatValue, secondFloatValue);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(ENCHANTMENT_TABLE_BOOK_TEXTURE);
        GlStateManager.rotate(20.0F, 1.0F, 0.0F, 0.0F);
        float thirdFloatValue = this.oFlip + (this.flip - this.oFlip) * partialTicks;
        GlStateManager.translate((1.0F - thirdFloatValue) * 0.2F, (1.0F - thirdFloatValue) * 0.1F, (1.0F - thirdFloatValue) * 0.25F);
        GlStateManager.rotate(-(1.0F - thirdFloatValue) * 90.0F - 90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
        float fourthFloatValue = this.oOpen + (this.open - this.oOpen) * partialTicks + 0.25F;
        float fifthFloatValue = this.oOpen + (this.open - this.oOpen) * partialTicks + 0.75F;
        fourthFloatValue = (fourthFloatValue - (float)MathHelper.truncateDoubleToInt((double)fourthFloatValue)) * 1.6F - 0.3F;
        fifthFloatValue = (fifthFloatValue - (float)MathHelper.truncateDoubleToInt((double)fifthFloatValue)) * 1.6F - 0.3F;

        if (fourthFloatValue < 0.0F)
        {
            fourthFloatValue = 0.0F;
        }

        if (fifthFloatValue < 0.0F)
        {
            fifthFloatValue = 0.0F;
        }

        if (fourthFloatValue > 1.0F)
        {
            fourthFloatValue = 1.0F;
        }

        if (fifthFloatValue > 1.0F)
        {
            fifthFloatValue = 1.0F;
        }

        GlStateManager.enableRescaleNormal();
        MODEL_BOOK.render((Entity)null, 0.0F, fourthFloatValue, fifthFloatValue, thirdFloatValue, 0.0F, 0.0625F);
        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.matrixMode(5889);
        GlStateManager.viewport(0, 0, this.mc.displayWidth, this.mc.displayHeight);
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(5888);
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        EnchantmentNameParts.getInstance().reseedRandomGenerator((long)this.container.xpSeed);
        int k = this.container.getLapisAmount();

        for (int l = 0; l < 3; ++l)
        {
            int intValue = i + 60;
            int fifthIntValue = intValue + 20;
            int seventhIntValue = 86;
            String s = EnchantmentNameParts.getInstance().generateNewRandomName();
            this.zLevel = 0.0F;
            this.mc.getTextureManager().bindTexture(ENCHANTMENT_TABLE_GUI_TEXTURE);
            int ninthIntValue = this.container.enchantLevels[l];
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            if (ninthIntValue == 0)
            {
                this.drawTexturedModalRect(intValue, j + 14 + 19 * l, 0, 185, 108, 19);
            }
            else
            {
                String stringValue = "" + ninthIntValue;
                FontRenderer fontrenderer = this.mc.standardGalacticFontRenderer;
                int fourthIntValue = 6839882;

                if ((k < l + 1 || this.mc.thePlayer.experienceLevel < ninthIntValue) && !this.mc.thePlayer.capabilities.isCreativeMode)
                {
                    this.drawTexturedModalRect(intValue, j + 14 + 19 * l, 0, 185, 108, 19);
                    this.drawTexturedModalRect(intValue + 1, j + 15 + 19 * l, 16 * l, 239, 16, 16);
                    fontrenderer.drawSplitString(s, fifthIntValue, j + 16 + 19 * l, seventhIntValue, (fourthIntValue & 16711422) >> 1);
                    fourthIntValue = 4226832;
                }
                else
                {
                    int sixthIntValue = mouseX - (i + 60);
                    int eighthIntValue = mouseY - (j + 14 + 19 * l);

                    if (sixthIntValue >= 0 && eighthIntValue >= 0 && sixthIntValue < 108 && eighthIntValue < 19)
                    {
                        this.drawTexturedModalRect(intValue, j + 14 + 19 * l, 0, 204, 108, 19);
                        fourthIntValue = 16777088;
                    }
                    else
                    {
                        this.drawTexturedModalRect(intValue, j + 14 + 19 * l, 0, 166, 108, 19);
                    }

                    this.drawTexturedModalRect(intValue + 1, j + 15 + 19 * l, 16 * l, 223, 16, 16);
                    fontrenderer.drawSplitString(s, fifthIntValue, j + 16 + 19 * l, seventhIntValue, fourthIntValue);
                    fourthIntValue = 8453920;
                }

                fontrenderer = this.mc.fontRendererObj;
                fontrenderer.drawStringWithShadow(stringValue, (float)(fifthIntValue + 86 - fontrenderer.getStringWidth(stringValue)), (float)(j + 16 + 19 * l + 7), fourthIntValue);
            }
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        super.drawScreen(mouseX, mouseY, partialTicks);
        boolean flag = this.mc.thePlayer.capabilities.isCreativeMode;
        int i = this.container.getLapisAmount();

        for (int j = 0; j < 3; ++j)
        {
            int k = this.container.enchantLevels[j];
            int l = this.container.enchantmentIds[j];
            int thirdIntValue = j + 1;

            if (this.isPointInRegion(60, 14 + 19 * j, 108, 17, mouseX, mouseY) && k > 0 && l >= 0)
            {
                List<String> list = Lists.<String>newArrayList();

                if (l >= 0 && Enchantment.getEnchantmentById(l & 255) != null)
                {
                    String s = Enchantment.getEnchantmentById(l & 255).getTranslatedName((l & 65280) >> 8);
                    list.add(EnumChatFormatting.WHITE.toString() + EnumChatFormatting.ITALIC.toString() + I18n.format("container.enchant.clue", new Object[] {s}));
                }

                if (!flag)
                {
                    if (l >= 0)
                    {
                        list.add("");
                    }

                    if (this.mc.thePlayer.experienceLevel < k)
                    {
                        list.add(EnumChatFormatting.RED.toString() + "Level Requirement: " + this.container.enchantLevels[j]);
                    }
                    else
                    {
                        String secondStringValue = "";

                        if (thirdIntValue == 1)
                        {
                            secondStringValue = I18n.format("container.enchant.lapis.one", new Object[0]);
                        }
                        else
                        {
                            secondStringValue = I18n.format("container.enchant.lapis.many", new Object[] {Integer.valueOf(thirdIntValue)});
                        }

                        if (i >= thirdIntValue)
                        {
                            list.add(EnumChatFormatting.GRAY.toString() + "" + secondStringValue);
                        }
                        else
                        {
                            list.add(EnumChatFormatting.RED.toString() + "" + secondStringValue);
                        }

                        if (thirdIntValue == 1)
                        {
                            secondStringValue = I18n.format("container.enchant.level.one", new Object[0]);
                        }
                        else
                        {
                            secondStringValue = I18n.format("container.enchant.level.many", new Object[] {Integer.valueOf(thirdIntValue)});
                        }

                        list.add(EnumChatFormatting.GRAY.toString() + "" + secondStringValue);
                    }
                }

                this.drawHoveringText(list, mouseX, mouseY);
                break;
            }
        }
    }

    public void tickBook()
    {
        ItemStack itemstack = this.inventorySlots.getSlot(0).getStack();

        if (!ItemStack.areItemStacksEqual(itemstack, this.last))
        {
            this.last = itemstack;

            while (true)
            {
                this.flipT += (float)(this.random.nextInt(4) - this.random.nextInt(4));

                if (this.open > this.flipT + 1.0F || this.open < this.flipT - 1.0F)
                {
                    break;
                }
            }
        }

        ++this.tickCount;
        this.oOpen = this.open;
        this.oFlip = this.flip;
        boolean flag = false;

        for (int i = 0; i < 3; ++i)
        {
            if (this.container.enchantLevels[i] != 0)
            {
                flag = true;
            }
        }

        if (flag)
        {
            this.flip += 0.2F;
        }
        else
        {
            this.flip -= 0.2F;
        }

        this.flip = MathHelper.clamp_float(this.flip, 0.0F, 1.0F);
        float floatValue = (this.flipT - this.open) * 0.4F;
        float f = 0.2F;
        floatValue = MathHelper.clamp_float(floatValue, -f, f);
        this.flipA += (floatValue - this.flipA) * 0.9F;
        this.open += this.flipA;
    }
}
