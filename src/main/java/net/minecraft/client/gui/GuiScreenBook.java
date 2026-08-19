package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import com.google.gson.JsonParseException;
import io.netty.buffer.Unpooled;
import java.io.IOException;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.ClickEvent;
import net.minecraft.init.Items;
import net.minecraft.item.ItemEditableBook;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

public class GuiScreenBook extends GuiScreen
{
    private static final Logger logger = LogManager.getLogger();
    private static final ResourceLocation bookGuiTextures = new ResourceLocation("textures/gui/book.png");
    private final EntityPlayer editingPlayer;
    private final ItemStack bookObj;
    private final boolean bookIsUnsigned;
    private boolean bookIsModified;
    private boolean bookGettingSigned;
    private int updateCount;
    private int bookImageWidth = 192;
    private int bookImageHeight = 192;
    private int bookTotalPages = 1;
    private int currPage;
    private NBTTagList bookPages;
    private String bookTitle = "";
    private List<IChatComponent> cachedComponents;
    private int cachedPage = -1;
    private GuiScreenBook.NextPageButton buttonNextPage;
    private GuiScreenBook.NextPageButton buttonPreviousPage;
    private GuiButton buttonDone;
    private GuiButton buttonSign;
    private GuiButton buttonFinalize;
    private GuiButton buttonCancel;

    public GuiScreenBook(EntityPlayer player, ItemStack book, boolean isUnsigned)
    {
        this.editingPlayer = player;
        this.bookObj = book;
        this.bookIsUnsigned = isUnsigned;

        if (book.hasTagCompound())
        {
            NBTTagCompound nBTTagCompound = book.getTagCompound();
            this.bookPages = nBTTagCompound.getTagList("pages", 8);

            if (this.bookPages != null)
            {
                this.bookPages = (NBTTagList)this.bookPages.copy();
                this.bookTotalPages = this.bookPages.tagCount();

                if (this.bookTotalPages < 1)
                {
                    this.bookTotalPages = 1;
                }
            }
        }

        if (this.bookPages == null && isUnsigned)
        {
            this.bookPages = new NBTTagList();
            this.bookPages.appendTag(new NBTTagString(""));
            this.bookTotalPages = 1;
        }
    }

    public void updateScreen()
    {
        super.updateScreen();
        ++this.updateCount;
    }

    public void initGui()
    {
        this.buttonList.clear();
        Keyboard.enableRepeatEvents(true);

        if (this.bookIsUnsigned)
        {
            this.buttonList.add(this.buttonSign = new GuiButton(3, this.width / 2 - 100, 4 + this.bookImageHeight, 98, 20, I18n.format("book.signButton", new Object[0])));
            this.buttonList.add(this.buttonDone = new GuiButton(0, this.width / 2 + 2, 4 + this.bookImageHeight, 98, 20, I18n.format("gui.done", new Object[0])));
            this.buttonList.add(this.buttonFinalize = new GuiButton(5, this.width / 2 - 100, 4 + this.bookImageHeight, 98, 20, I18n.format("book.finalizeButton", new Object[0])));
            this.buttonList.add(this.buttonCancel = new GuiButton(4, this.width / 2 + 2, 4 + this.bookImageHeight, 98, 20, I18n.format("gui.cancel", new Object[0])));
        }
        else
        {
            this.buttonList.add(this.buttonDone = new GuiButton(0, this.width / 2 - 100, 4 + this.bookImageHeight, 200, 20, I18n.format("gui.done", new Object[0])));
        }

        int i = (this.width - this.bookImageWidth) / 2;
        int j = 2;
        this.buttonList.add(this.buttonNextPage = new GuiScreenBook.NextPageButton(1, i + 120, j + 154, true));
        this.buttonList.add(this.buttonPreviousPage = new GuiScreenBook.NextPageButton(2, i + 38, j + 154, false));
        this.updateButtons();
    }

    public void onGuiClosed()
    {
        Keyboard.enableRepeatEvents(false);
    }

    private void updateButtons()
    {
        this.buttonNextPage.visible = !this.bookGettingSigned && (this.currPage < this.bookTotalPages - 1 || this.bookIsUnsigned);
        this.buttonPreviousPage.visible = !this.bookGettingSigned && this.currPage > 0;
        this.buttonDone.visible = !this.bookIsUnsigned || !this.bookGettingSigned;

        if (this.bookIsUnsigned)
        {
            this.buttonSign.visible = !this.bookGettingSigned;
            this.buttonCancel.visible = this.bookGettingSigned;
            this.buttonFinalize.visible = this.bookGettingSigned;
            this.buttonFinalize.enabled = this.bookTitle.trim().length() > 0;
        }
    }

    private void sendBookToServer(boolean publish) throws IOException
    {
        if (this.bookIsUnsigned && this.bookIsModified)
        {
            if (this.bookPages != null)
            {
                while (this.bookPages.tagCount() > 1)
                {
                    String s = this.bookPages.getStringTagAt(this.bookPages.tagCount() - 1);

                    if (s.length() != 0)
                    {
                        break;
                    }

                    this.bookPages.removeTag(this.bookPages.tagCount() - 1);
                }

                if (this.bookObj.hasTagCompound())
                {
                    NBTTagCompound nbttagcompound = this.bookObj.getTagCompound();
                    nbttagcompound.setTag("pages", this.bookPages);
                }
                else
                {
                    this.bookObj.setTagInfo("pages", this.bookPages);
                }

                String fifthStringValue = "MC|BEdit";

                if (publish)
                {
                    fifthStringValue = "MC|BSign";
                    this.bookObj.setTagInfo("author", new NBTTagString(this.editingPlayer.getName()));
                    this.bookObj.setTagInfo("title", new NBTTagString(this.bookTitle.trim()));

                    for (int i = 0; i < this.bookPages.tagCount(); ++i)
                    {
                        String stringValue = this.bookPages.getStringTagAt(i);
                        IChatComponent ichatcomponent = new ChatComponentText(stringValue);
                        stringValue = IChatComponent.Serializer.componentToJson(ichatcomponent);
                        this.bookPages.set(i, new NBTTagString(stringValue));
                    }

                    this.bookObj.setItem(Items.written_book);
                }

                PacketBuffer packetbuffer = new PacketBuffer(Unpooled.buffer());
                packetbuffer.writeItemStackToBuffer(this.bookObj);
                this.mc.getNetHandler().addToSendQueue(new C17PacketCustomPayload(fifthStringValue, packetbuffer));
            }
        }
    }

    protected void actionPerformed(GuiButton button) throws IOException
    {
        if (button.enabled)
        {
            if (button.id == 0)
            {
                this.mc.displayGuiScreen((GuiScreen)null);
                this.sendBookToServer(false);
            }
            else if (button.id == 3 && this.bookIsUnsigned)
            {
                this.bookGettingSigned = true;
            }
            else if (button.id == 1)
            {
                if (this.currPage < this.bookTotalPages - 1)
                {
                    ++this.currPage;
                }
                else if (this.bookIsUnsigned)
                {
                    this.addNewPage();

                    if (this.currPage < this.bookTotalPages - 1)
                    {
                        ++this.currPage;
                    }
                }
            }
            else if (button.id == 2)
            {
                if (this.currPage > 0)
                {
                    --this.currPage;
                }
            }
            else if (button.id == 5 && this.bookGettingSigned)
            {
                this.sendBookToServer(true);
                this.mc.displayGuiScreen((GuiScreen)null);
            }
            else if (button.id == 4 && this.bookGettingSigned)
            {
                this.bookGettingSigned = false;
            }

            this.updateButtons();
        }
    }

    private void addNewPage()
    {
        if (this.bookPages != null && this.bookPages.tagCount() < 50)
        {
            this.bookPages.appendTag(new NBTTagString(""));
            ++this.bookTotalPages;
            this.bookIsModified = true;
        }
    }

    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        super.keyTyped(typedChar, keyCode);

        if (this.bookIsUnsigned)
        {
            if (this.bookGettingSigned)
            {
                this.keyTypedInTitle(typedChar, keyCode);
            }
            else
            {
                this.keyTypedInBook(typedChar, keyCode);
            }
        }
    }

    private void keyTypedInBook(char typedChar, int keyCode)
    {
        if (GuiScreen.isKeyComboCtrlV(keyCode))
        {
            this.insertIntoCurrentPage(GuiScreen.getClipboardString());
        }
        else
        {
            switch (keyCode)
            {
                case 14:
                    String s = this.pageGetCurrent();

                    if (s.length() > 0)
                    {
                        this.setCurrentPageText(removeLastCharacter(s));
                    }

                    return;

                case 28:
                case 156:
                    this.insertIntoCurrentPage("\n");
                    return;

                default:
                    if (ChatAllowedCharacters.isAllowedCharacter(typedChar))
                    {
                        this.insertIntoCurrentPage(Character.toString(typedChar));
                    }
            }
        }
    }

    private void keyTypedInTitle(char typedChar, int keyCode) throws IOException
    {
        if (GuiScreen.isKeyComboCtrlV(keyCode))
        {
            this.appendTextToTitle(GuiScreen.getClipboardString());
        }
        else
        {
            switch (keyCode)
            {
                case 14:
                    if (!this.bookTitle.isEmpty())
                    {
                        this.bookTitle = removeLastCharacter(this.bookTitle);
                        this.updateButtons();
                    }

                    return;

                case 28:
                case 156:
                    if (!this.bookTitle.isEmpty())
                    {
                        this.sendBookToServer(true);
                        this.mc.displayGuiScreen((GuiScreen)null);
                    }

                    return;

                default:
                    if (ChatAllowedCharacters.isAllowedCharacter(typedChar))
                    {
                        this.appendTextToTitle(Character.toString(typedChar));
                    }
            }
        }
    }

    private String pageGetCurrent()
    {
        return this.bookPages != null && this.currPage >= 0 && this.currPage < this.bookPages.tagCount() ? this.bookPages.getStringTagAt(this.currPage) : "";
    }

    private void setCurrentPageText(String pageText)
    {
        if (this.bookPages != null && this.currPage >= 0 && this.currPage < this.bookPages.tagCount())
        {
            this.bookPages.set(this.currPage, new NBTTagString(pageText));
            this.bookIsModified = true;
        }
    }

    private void insertIntoCurrentPage(String text)
    {
        text = filterAllowedPageText(text);

        if (text.isEmpty())
        {
            return;
        }

        String s = this.pageGetCurrent();
        StringBuilder pageText = new StringBuilder(s);
        boolean changed = false;

        for (int i = 0; i < text.length();)
        {
            int codePoint = text.codePointAt(i);
            String candidatePageText = pageText.toString() + new String(Character.toChars(codePoint));

            if (candidatePageText.length() >= 256 || this.fontRendererObj.splitStringWidth(candidatePageText + "" + EnumChatFormatting.BLACK + "_", 118) > 128)
            {
                break;
            }

            pageText.appendCodePoint(codePoint);
            changed = true;
            i += Character.charCount(codePoint);
        }

        if (changed)
        {
            this.setCurrentPageText(pageText.toString());
        }
    }

    private static String removeLastCharacter(String text)
    {
        return text.substring(0, text.offsetByCodePoints(text.length(), -1));
    }

    private static String filterAllowedPageText(String text)
    {
        StringBuilder stringBuilder = new StringBuilder(text.length());

        for (int i = 0; i < text.length();)
        {
            int codePoint = text.codePointAt(i);

            if (codePoint == '\n' || ChatAllowedCharacters.isAllowedCodePoint(codePoint))
            {
                stringBuilder.appendCodePoint(codePoint);
            }

            i += Character.charCount(codePoint);
        }

        return stringBuilder.toString();
    }

    private void appendTextToTitle(String text)
    {
        text = ChatAllowedCharacters.filterAllowedCharacters(text);

        if (text.isEmpty())
        {
            return;
        }

        int remainingUnits = 16 - this.bookTitle.length();

        if (remainingUnits <= 0)
        {
            return;
        }

        StringBuilder titleBuilder = new StringBuilder(this.bookTitle);

        for (int i = 0; i < text.length();)
        {
            int codePoint = text.codePointAt(i);
            int codePointLength = Character.charCount(codePoint);

            if (codePointLength > remainingUnits)
            {
                break;
            }

            titleBuilder.appendCodePoint(codePoint);
            i += codePointLength;
            remainingUnits -= codePointLength;
        }

        this.bookTitle = titleBuilder.toString();
        this.updateButtons();
        this.bookIsModified = true;
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(bookGuiTextures);
        int i = (this.width - this.bookImageWidth) / 2;
        int j = 2;
        this.drawTexturedModalRect(i, j, 0, 0, this.bookImageWidth, this.bookImageHeight);

        if (this.bookGettingSigned)
        {
            String s = this.bookTitle;

            if (this.bookIsUnsigned)
            {
                if (this.updateCount / 6 % 2 == 0)
                {
                    s = s + "" + EnumChatFormatting.BLACK + "_";
                }
                else
                {
                    s = s + "" + EnumChatFormatting.GRAY + "_";
                }
            }

            String thirdStringValue = I18n.format("book.editTitle", new Object[0]);
            int k = this.fontRendererObj.getStringWidth(thirdStringValue);
            this.fontRendererObj.drawString(thirdStringValue, i + 36 + (116 - k) / 2, j + 16 + 16, 0);
            int l = this.fontRendererObj.getStringWidth(s);
            this.fontRendererObj.drawString(s, (float)(i + 36 + (116 - l) / 2), (float)(j + 48), 0, false, 1.0F);
            String fourthStringValue = I18n.format("book.byAuthor", new Object[] {this.editingPlayer.getName()});
            int intValue2 = this.fontRendererObj.getStringWidth(fourthStringValue);
            this.fontRendererObj.drawString(EnumChatFormatting.DARK_GRAY + fourthStringValue, i + 36 + (116 - intValue2) / 2, j + 48 + 10, 0);
            String text3 = I18n.format("book.finalizeWarning", new Object[0]);
            this.fontRendererObj.drawSplitString(text3, i + 36, j + 80, 116, 0);
        }
        else
        {
            String text4 = I18n.format("book.pageIndicator", new Object[] {Integer.valueOf(this.currPage + 1), Integer.valueOf(this.bookTotalPages)});
            String message = "";

            if (this.bookPages != null && this.currPage >= 0 && this.currPage < this.bookPages.tagCount())
            {
                message = this.bookPages.getStringTagAt(this.currPage);
            }

            if (this.bookIsUnsigned)
            {
                if (this.fontRendererObj.getBidiFlag())
                {
                    message = message + "_";
                }
                else if (this.updateCount / 6 % 2 == 0)
                {
                    message = message + "" + EnumChatFormatting.BLACK + "_";
                }
                else
                {
                    message = message + "" + EnumChatFormatting.GRAY + "_";
                }
            }
            else if (this.cachedPage != this.currPage)
            {
                if (ItemEditableBook.validBookTagContents(this.bookObj.getTagCompound()))
                {
                    try
                    {
                        IChatComponent ichatcomponent = IChatComponent.Serializer.jsonToComponent(message);
                        this.cachedComponents = ichatcomponent != null ? GuiUtilRenderComponents.splitText(ichatcomponent, 116, this.fontRendererObj, true, true) : null;
                    }
                    catch (JsonParseException caughtJsonParseException)
                    {
                        this.cachedComponents = null;
                    }
                }
                else
                {
                    ChatComponentText chatComponentText = new ChatComponentText(EnumChatFormatting.DARK_RED.toString() + "* Invalid book tag *");
                    this.cachedComponents = Lists.newArrayList(chatComponentText);
                }

                this.cachedPage = this.currPage;
            }

            int secondIntValue = this.fontRendererObj.getStringWidth(text4);
            this.fontRendererObj.drawString(text4, i - secondIntValue + this.bookImageWidth - 44, j + 16, 0);

            if (this.cachedComponents == null)
            {
                this.fontRendererObj.drawSplitString(message, i + 36, j + 16 + 16, 116, 0, this.bookIsUnsigned ? 1.0F : 0.0F);
            }
            else
            {
                int count = Math.min(128 / this.fontRendererObj.FONT_HEIGHT, this.cachedComponents.size());

                for (int outerIndex = 0; outerIndex < count; ++outerIndex)
                {
                    IChatComponent ichatcomponent2 = this.cachedComponents.get(outerIndex);
                    this.fontRendererObj.drawString(ichatcomponent2.getUnformattedText(), i + 36, j + 16 + 16 + outerIndex * this.fontRendererObj.FONT_HEIGHT, 0);
                }

                IChatComponent ichatcomponent1 = this.getClickedComponent(mouseX, mouseY);

                if (ichatcomponent1 != null)
                {
                    this.handleComponentHover(ichatcomponent1, mouseX, mouseY);
                }
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        if (mouseButton == 0)
        {
            IChatComponent ichatcomponent = this.getClickedComponent(mouseX, mouseY);

            if (this.handleComponentClick(ichatcomponent))
            {
                return;
            }
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    protected boolean handleComponentClick(IChatComponent component)
    {
        ClickEvent clickEvent = component == null ? null : component.getChatStyle().getChatClickEvent();

        if (clickEvent == null)
        {
            return false;
        }
        else if (clickEvent.getAction() == ClickEvent.Action.CHANGE_PAGE)
        {
            String s = clickEvent.getValue();

            try
            {
                int i = Integer.parseInt(s) - 1;

                if (i >= 0 && i < this.bookTotalPages && i != this.currPage)
                {
                    this.currPage = i;
                    this.updateButtons();
                    return true;
                }
            }
            catch (Throwable caughtThrowable)
            {
                ;
            }

            return false;
        }
        else
        {
            boolean flag = super.handleComponentClick(component);

            if (flag && clickEvent.getAction() == ClickEvent.Action.RUN_COMMAND)
            {
                this.mc.displayGuiScreen((GuiScreen)null);
            }

            return flag;
        }
    }

    public IChatComponent getClickedComponent(int mouseX, int mouseY)
    {
        if (this.cachedComponents == null)
        {
            return null;
        }
        else
        {
            int i = mouseX - (this.width - this.bookImageWidth) / 2 - 36;
            int j = mouseY - 2 - 16 - 16;

            if (i >= 0 && j >= 0)
            {
                int k = Math.min(128 / this.fontRendererObj.FONT_HEIGHT, this.cachedComponents.size());

                if (i <= 116 && j < this.mc.fontRendererObj.FONT_HEIGHT * k + k)
                {
                    int l = j / this.mc.fontRendererObj.FONT_HEIGHT;

                    if (l >= 0 && l < this.cachedComponents.size())
                    {
                        IChatComponent ichatcomponent = this.cachedComponents.get(l);
                        int intValue = 0;

                        for (IChatComponent ichatcomponent1 : ichatcomponent)
                        {
                            if (ichatcomponent1 instanceof ChatComponentText)
                            {
                                intValue += this.mc.fontRendererObj.getStringWidth(((ChatComponentText)ichatcomponent1).getChatComponentText_TextValue());

                                if (intValue > i)
                                {
                                    return ichatcomponent1;
                                }
                            }
                        }
                    }

                    return null;
                }
                else
                {
                    return null;
                }
            }
            else
            {
                return null;
            }
        }
    }

    static class NextPageButton extends GuiButton
    {
        private final boolean isForward;

        public NextPageButton(int id, int x, int y, boolean isForward)
        {
            super(id, x, y, 23, 13, "");
            this.isForward = isForward;
        }

        public void drawButton(Minecraft mc, int mouseX, int mouseY)
        {
            if (this.visible)
            {
                boolean flag = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                mc.getTextureManager().bindTexture(GuiScreenBook.bookGuiTextures);
                int i = 0;
                int j = 192;

                if (flag)
                {
                    i += 23;
                }

                if (!this.isForward)
                {
                    j += 13;
                }

                this.drawTexturedModalRect(this.xPosition, this.yPosition, i, j, 23, 13);
            }
        }
    }
}
