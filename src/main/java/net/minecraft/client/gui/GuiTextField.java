package net.minecraft.client.gui;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.MathHelper;

public class GuiTextField extends Gui
{
    private final int id;
    private final FontRenderer fontRendererInstance;
    public int xPosition;
    public int yPosition;
    private final int width;
    private final int height;
    private String text = "";
    private int maxStringLength = 32;
    private int cursorCounter;
    private boolean enableBackgroundDrawing = true;
    private boolean canLoseFocus = true;
    private boolean isFocused;
    private boolean isEnabled = true;
    private int lineScrollOffset;
    private int cursorPosition;
    private int selectionEnd;
    private int enabledColor = 14737632;
    private int disabledColor = 7368816;
    private boolean visible = true;
    private GuiPageButtonList.GuiResponder guiResponder;
    private Predicate<String> validator = Predicates.<String>alwaysTrue();

    public GuiTextField(int componentId, FontRenderer fontrendererObj, int x, int y, int width, int height)
    {
        this.id = componentId;
        this.fontRendererInstance = fontrendererObj;
        this.xPosition = x;
        this.yPosition = y;
        this.width = width;
        this.height = height;
    }

    public void setGuiResponder(GuiPageButtonList.GuiResponder responder)
    {
        this.guiResponder = responder;
    }

    public void updateCursorCounter()
    {
        ++this.cursorCounter;
    }

    public void setText(String textIn)
    {
        if (this.validator.apply(textIn))
        {
            this.text = this.trimToMaxStringLength(textIn);

            this.setCursorPositionEnd();
        }
    }

    public String getText()
    {
        return this.text;
    }

    public String getSelectedText()
    {
        int i = this.cursorPosition < this.selectionEnd ? this.cursorPosition : this.selectionEnd;
        int j = this.cursorPosition < this.selectionEnd ? this.selectionEnd : this.cursorPosition;
        return this.text.substring(i, j);
    }

    public void setValidator(Predicate<String> theValidator)
    {
        this.validator = theValidator;
    }

    public void writeText(String textToWrite)
    {
        String newText = "";
        String filteredText = ChatAllowedCharacters.filterAllowedCharacters(textToWrite);
        int selectionStart = this.cursorPosition < this.selectionEnd ? this.cursorPosition : this.selectionEnd;
        int selectionEnd = this.cursorPosition < this.selectionEnd ? this.selectionEnd : this.cursorPosition;
        int remainingLength = this.maxStringLength - this.text.length() - (selectionStart - selectionEnd);
        int insertedLength = 0;

        if (this.text.length() > 0)
        {
            newText = newText + this.text.substring(0, selectionStart);
        }

        String stringToInsert = trimStringToLength(filteredText, remainingLength);
        newText = newText + stringToInsert;
        insertedLength = stringToInsert.length();

        if (this.text.length() > 0 && selectionEnd < this.text.length())
        {
            newText = newText + this.text.substring(selectionEnd);
        }

        if (this.validator.apply(newText))
        {
            this.text = newText;
            this.moveCursorBy(selectionStart - this.selectionEnd + insertedLength);

            if (this.guiResponder != null)
            {
                this.guiResponder.setEntryValue(this.id, this.text);
            }
        }
    }

    public void deleteWords(int wordOffset)
    {
        if (this.text.length() != 0)
        {
            if (this.selectionEnd != this.cursorPosition)
            {
                this.writeText("");
            }
            else
            {
                this.deleteFromCursor(this.getNthWordFromCursor(wordOffset) - this.cursorPosition);
            }
        }
    }

    public void deleteFromCursor(int characterOffset)
    {
        if (this.text.length() != 0)
        {
            if (this.selectionEnd != this.cursorPosition)
            {
                this.writeText("");
            }
            else
            {
                boolean deletingLeft = characterOffset < 0;
                int deleteStart;
                int deleteEnd;

                if (characterOffset == -1)
                {
                    deleteStart = this.getPreviousCharacterIndex(this.cursorPosition);
                    deleteEnd = this.cursorPosition;
                }
                else if (characterOffset == 1)
                {
                    deleteStart = this.cursorPosition;
                    deleteEnd = this.getNextCharacterIndex(this.cursorPosition);
                }
                else
                {
                    deleteStart = deletingLeft ? this.cursorPosition + characterOffset : this.cursorPosition;
                    deleteEnd = deletingLeft ? this.cursorPosition : this.cursorPosition + characterOffset;
                    deleteStart = this.clampToCharacterBoundary(deleteStart);
                    deleteEnd = this.clampToCharacterBoundary(deleteEnd);
                }

                String newText = "";

                if (deleteStart >= 0)
                {
                    newText = this.text.substring(0, deleteStart);
                }

                if (deleteEnd < this.text.length())
                {
                    newText = newText + this.text.substring(deleteEnd);
                }

                if (this.validator.apply(newText))
                {
                    this.text = newText;
                    this.setCursorPosition(deleteStart);

                    if (this.guiResponder != null)
                    {
                        this.guiResponder.setEntryValue(this.id, this.text);
                    }
                }
            }
        }
    }

    public int getId()
    {
        return this.id;
    }

    public int getNthWordFromCursor(int wordOffset)
    {
        return this.getNthWordFromPos(wordOffset, this.getCursorPosition());
    }

    public int getNthWordFromPos(int wordOffset, int position)
    {
        return this.getNthWordFromPosWithSpaces(wordOffset, position, true);
    }

    public int getNthWordFromPosWithSpaces(int wordOffset, int position, boolean skipSpaces)
    {
        int i = this.clampToCharacterBoundary(position);
        boolean movingLeft = wordOffset < 0;
        int steps = Math.abs(wordOffset);

        for (int step = 0; step < steps; ++step)
        {
            i = movingLeft ? this.getPreviousWordBoundary(i, skipSpaces) : this.getNextWordBoundary(i, skipSpaces);
        }

        return i;
    }

    private int getNextWordBoundary(int position, boolean skipSpaces)
    {
        int length = this.text.length();
        int i = this.clampToCharacterBoundary(position);

        if (i >= length)
        {
            return length;
        }

        if (skipSpaces && this.isWhitespaceAt(i))
        {
            return this.skipWhitespaceForward(i);
        }

        int wordType = this.getWordTypeAt(i);

        do
        {
            i = this.getNextCharacterIndex(i);
        }
        while (i < length && this.getWordTypeAt(i) == wordType);

        return skipSpaces ? this.skipWhitespaceForward(i) : i;
    }

    private int getPreviousWordBoundary(int position, boolean skipSpaces)
    {
        int i = this.clampToCharacterBoundary(position);

        if (i <= 0)
        {
            return 0;
        }

        if (!skipSpaces && this.isWhitespaceBefore(i))
        {
            return i;
        }

        if (skipSpaces)
        {
            i = this.skipWhitespaceBackward(i);

            if (i <= 0)
            {
                return 0;
            }
        }

        int wordType = this.getWordTypeBefore(i);

        do
        {
            i = this.getPreviousCharacterIndex(i);
        }
        while (i > 0 && this.getWordTypeBefore(i) == wordType);

        return i;
    }

    private int skipWhitespaceForward(int position)
    {
        int i = this.clampToCharacterBoundary(position);

        while (i < this.text.length() && this.isWhitespaceAt(i))
        {
            i = this.getNextCharacterIndex(i);
        }

        return i;
    }

    private int skipWhitespaceBackward(int position)
    {
        int i = this.clampToCharacterBoundary(position);

        while (i > 0 && this.isWhitespaceBefore(i))
        {
            i = this.getPreviousCharacterIndex(i);
        }

        return i;
    }

    private boolean isWhitespaceAt(int index)
    {
        return index < this.text.length() && isWhitespaceCodePoint(this.text.codePointAt(index));
    }

    private boolean isWhitespaceBefore(int index)
    {
        return index > 0 && isWhitespaceCodePoint(this.text.codePointBefore(index));
    }

    private int getWordTypeAt(int index)
    {
        return getWordType(this.text.codePointAt(index));
    }

    private int getWordTypeBefore(int index)
    {
        return getWordType(this.text.codePointBefore(index));
    }

    private static int getWordType(int codePoint)
    {
        if (isWhitespaceCodePoint(codePoint))
        {
            return 0;
        }

        if (isEastAsianCodePoint(codePoint))
        {
            return 1;
        }

        if (Character.isDigit(codePoint))
        {
            return 2;
        }

        if (Character.isLetter(codePoint) || codePoint == '_')
        {
            return 3;
        }

        return 4;
    }

    private static boolean isWhitespaceCodePoint(int codePoint)
    {
        return codePoint == 32 || Character.isWhitespace(codePoint);
    }

    private static boolean isEastAsianCodePoint(int codePoint)
    {
        return codePoint >= 0x2E80 && codePoint <= 0x9FFF || codePoint >= 0xAC00 && codePoint <= 0xD7AF || codePoint >= 0xF900 && codePoint <= 0xFAFF || codePoint >= 0xFE30 && codePoint <= 0xFE4F || codePoint >= 0xFF00 && codePoint <= 0xFFEF;
    }

    public void moveCursorBy(int offset)
    {
        this.setCursorPosition(this.selectionEnd + offset);
    }

    public void setCursorPosition(int position)
    {
        this.cursorPosition = this.clampToCharacterBoundary(position);
        int i = this.text.length();
        this.cursorPosition = MathHelper.clamp_int(this.cursorPosition, 0, i);
        this.setSelectionPos(this.cursorPosition);
    }

    public void setCursorPositionZero()
    {
        this.setCursorPosition(0);
    }

    public void setCursorPositionEnd()
    {
        this.setCursorPosition(this.text.length());
    }

    public boolean textboxKeyTyped(char typedChar, int keyCode)
    {
        if (!this.isFocused)
        {
            return false;
        }
        else if (GuiScreen.isKeyComboCtrlA(keyCode))
        {
            this.setCursorPositionEnd();
            this.setSelectionPos(0);
            return true;
        }
        else if (GuiScreen.isKeyComboCtrlC(keyCode))
        {
            GuiScreen.setClipboardString(this.getSelectedText());
            return true;
        }
        else if (GuiScreen.isKeyComboCtrlV(keyCode))
        {
            if (this.isEnabled)
            {
                this.writeText(GuiScreen.getClipboardString());
            }

            return true;
        }
        else if (GuiScreen.isKeyComboCtrlX(keyCode))
        {
            GuiScreen.setClipboardString(this.getSelectedText());

            if (this.isEnabled)
            {
                this.writeText("");
            }

            return true;
        }
        else if (typedChar == 8 || typedChar == 127)
        {
            if (this.isEnabled)
            {
                if (GuiScreen.isCtrlKeyDown())
                {
                    this.deleteWords(typedChar == 8 ? -1 : 1);
                }
                else
                {
                    this.deleteFromCursor(typedChar == 8 ? -1 : 1);
                }
            }

            return true;
        }
        else
        {
            switch (keyCode)
            {
                case 14:
                    if (GuiScreen.isCtrlKeyDown())
                    {
                        if (this.isEnabled)
                        {
                            this.deleteWords(-1);
                        }
                    }
                    else if (this.isEnabled)
                    {
                        this.deleteFromCursor(-1);
                    }

                    return true;

                case 199:
                    if (GuiScreen.isShiftKeyDown())
                    {
                        this.setSelectionPos(0);
                    }
                    else
                    {
                        this.setCursorPositionZero();
                    }

                    return true;

                case 203:
                    if (GuiScreen.isShiftKeyDown())
                    {
                        if (GuiScreen.isCtrlKeyDown())
                        {
                            this.setSelectionPos(this.getNthWordFromPos(-1, this.getSelectionEnd()));
                        }
                        else
                        {
                            this.setSelectionPos(this.getPreviousCharacterIndex(this.getSelectionEnd()));
                        }
                    }
                    else if (GuiScreen.isCtrlKeyDown())
                    {
                        this.setCursorPosition(this.getNthWordFromCursor(-1));
                    }
                    else
                    {
                        this.setCursorPosition(this.getPreviousCharacterIndex(this.getSelectionEnd()));
                    }

                    return true;

                case 205:
                    if (GuiScreen.isShiftKeyDown())
                    {
                        if (GuiScreen.isCtrlKeyDown())
                        {
                            this.setSelectionPos(this.getNthWordFromPos(1, this.getSelectionEnd()));
                        }
                        else
                        {
                            this.setSelectionPos(this.getNextCharacterIndex(this.getSelectionEnd()));
                        }
                    }
                    else if (GuiScreen.isCtrlKeyDown())
                    {
                        this.setCursorPosition(this.getNthWordFromCursor(1));
                    }
                    else
                    {
                        this.setCursorPosition(this.getNextCharacterIndex(this.getSelectionEnd()));
                    }

                    return true;

                case 207:
                    if (GuiScreen.isShiftKeyDown())
                    {
                        this.setSelectionPos(this.text.length());
                    }
                    else
                    {
                        this.setCursorPositionEnd();
                    }

                    return true;

                case 211:
                    if (GuiScreen.isCtrlKeyDown())
                    {
                        if (this.isEnabled)
                        {
                            this.deleteWords(1);
                        }
                    }
                    else if (this.isEnabled)
                    {
                        this.deleteFromCursor(1);
                    }

                    return true;

                default:
                    if (ChatAllowedCharacters.isAllowedCharacter(typedChar))
                    {
                        if (this.isEnabled)
                        {
                            this.writeText(Character.toString(typedChar));
                        }

                        return true;
                    }
                    else
                    {
                        return false;
                    }
            }
        }
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        boolean flag = mouseX >= this.xPosition && mouseX < this.xPosition + this.width && mouseY >= this.yPosition && mouseY < this.yPosition + this.height;

        if (this.canLoseFocus)
        {
            this.setFocused(flag);
        }

        if (this.isFocused && flag && mouseButton == 0)
        {
            int i = mouseX - this.xPosition;

            if (this.enableBackgroundDrawing)
            {
                i -= 4;
            }

            String s = this.fontRendererInstance.trimStringToWidth(this.text.substring(this.lineScrollOffset), this.getWidth());
            this.setCursorPosition(this.fontRendererInstance.trimStringToWidth(s, i).length() + this.lineScrollOffset);
        }
    }

    public void drawTextBox()
    {
        if (this.getVisible())
        {
            if (this.getEnableBackgroundDrawing())
            {
                drawRect(this.xPosition - 1, this.yPosition - 1, this.xPosition + this.width + 1, this.yPosition + this.height + 1, -6250336);
                drawRect(this.xPosition, this.yPosition, this.xPosition + this.width, this.yPosition + this.height, -16777216);
            }

            int textColor = this.isEnabled ? this.enabledColor : this.disabledColor;
            int cursorPosInLine = this.cursorPosition - this.lineScrollOffset;
            int selectionPosInLine = this.selectionEnd - this.lineScrollOffset;
            String visibleText = this.fontRendererInstance.trimStringToWidth(this.text.substring(this.lineScrollOffset), this.getWidth());
            boolean cursorInVisibleText = cursorPosInLine >= 0 && cursorPosInLine <= visibleText.length();
            boolean cursorVisible = this.isFocused && this.cursorCounter / 6 % 2 == 0 && cursorInVisibleText;
            int drawX = this.enableBackgroundDrawing ? this.xPosition + 4 : this.xPosition;
            int drawY = this.enableBackgroundDrawing ? this.yPosition + (this.height - 8) / 2 : this.yPosition;
            int currentX = drawX;

            if (selectionPosInLine > visibleText.length())
            {
                selectionPosInLine = visibleText.length();
            }

            if (visibleText.length() > 0)
            {
                String textBeforeCursor = cursorInVisibleText ? visibleText.substring(0, cursorPosInLine) : visibleText;
                currentX = this.fontRendererInstance.drawStringWithShadow(textBeforeCursor, (float)drawX, (float)drawY, textColor, 1.0F);
            }

            boolean cursorOnText = this.cursorPosition < this.text.length() || this.text.length() >= this.getMaxStringLength();
            int cursorX = currentX;

            if (!cursorInVisibleText)
            {
                cursorX = cursorPosInLine > 0 ? drawX + this.width : drawX;
            }
            else if (cursorOnText)
            {
                cursorX = currentX - 1;
                --currentX;
            }

            if (visibleText.length() > 0 && cursorInVisibleText && cursorPosInLine < visibleText.length())
            {
                currentX = this.fontRendererInstance.drawStringWithShadow(visibleText.substring(cursorPosInLine), (float)currentX, (float)drawY, textColor, 1.0F);
            }

            if (cursorVisible)
            {
                if (cursorOnText)
                {
                    Gui.drawRect(cursorX, drawY - 1, cursorX + 1, drawY + 1 + this.fontRendererInstance.FONT_HEIGHT, -3092272);
                }
                else
                {
                    this.fontRendererInstance.drawStringWithShadow("_", (float)cursorX, (float)drawY, textColor);
                }
            }

            if (selectionPosInLine != cursorPosInLine)
            {
                int selectionX = drawX + this.fontRendererInstance.getStringWidth(visibleText.substring(0, selectionPosInLine));
                this.drawCursorVertical(cursorX, drawY - 1, selectionX - 1, drawY + 1 + this.fontRendererInstance.FONT_HEIGHT);
            }
        }
    }

    private void drawCursorVertical(int left, int top, int right, int bottom)
    {
        if (left < right)
        {
            int i = left;
            left = right;
            right = i;
        }

        if (top < bottom)
        {
            int j = top;
            top = bottom;
            bottom = j;
        }

        if (right > this.xPosition + this.width)
        {
            right = this.xPosition + this.width;
        }

        if (left > this.xPosition + this.width)
        {
            left = this.xPosition + this.width;
        }

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        GlStateManager.color(0.0F, 0.0F, 255.0F, 255.0F);
        GlStateManager.disableTexture2D();
        GlStateManager.enableColorLogic();
        GlStateManager.colorLogicOp(5387);
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos((double)left, (double)bottom, 0.0D).endVertex();
        worldRenderer.pos((double)right, (double)bottom, 0.0D).endVertex();
        worldRenderer.pos((double)right, (double)top, 0.0D).endVertex();
        worldRenderer.pos((double)left, (double)top, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.disableColorLogic();
        GlStateManager.enableTexture2D();
    }

    public void setMaxStringLength(int maxLength)
    {
        this.maxStringLength = maxLength;

        if (this.text.length() > maxLength)
        {
            this.text = this.trimToMaxStringLength(this.text);
            this.cursorPosition = this.clampToCharacterBoundary(this.cursorPosition);
            this.selectionEnd = this.clampToCharacterBoundary(this.selectionEnd);
            this.lineScrollOffset = this.clampToCharacterBoundary(this.lineScrollOffset);
        }
    }

    public int getMaxStringLength()
    {
        return this.maxStringLength;
    }

    public int getCursorPosition()
    {
        return this.cursorPosition;
    }

    public boolean getEnableBackgroundDrawing()
    {
        return this.enableBackgroundDrawing;
    }

    public void setEnableBackgroundDrawing(boolean enableBackgroundDrawingIn)
    {
        this.enableBackgroundDrawing = enableBackgroundDrawingIn;
    }

    public void setTextColor(int color)
    {
        this.enabledColor = color;
    }

    public void setDisabledTextColour(int color)
    {
        this.disabledColor = color;
    }

    public void setFocused(boolean focused)
    {
        if (focused && !this.isFocused)
        {
            this.cursorCounter = 0;
        }

        this.isFocused = focused;
    }

    public boolean isFocused()
    {
        return this.isFocused;
    }

    public void setEnabled(boolean enabled)
    {
        this.isEnabled = enabled;
    }

    public int getSelectionEnd()
    {
        return this.selectionEnd;
    }

    public int getWidth()
    {
        return this.getEnableBackgroundDrawing() ? this.width - 8 : this.width;
    }

    public void setSelectionPos(int position)
    {
        int textLength = this.text.length();

        if (position > textLength)
        {
            position = textLength;
        }

        if (position < 0)
        {
            position = 0;
        }

        this.selectionEnd = this.clampToCharacterBoundary(position);

        if (this.fontRendererInstance != null)
        {
            if (this.lineScrollOffset > textLength)
            {
                this.lineScrollOffset = textLength;
            }

            this.lineScrollOffset = this.clampToCharacterBoundary(this.lineScrollOffset);
            int textBoxWidth = this.getWidth();
            String visibleText = this.fontRendererInstance.trimStringToWidth(this.text.substring(this.lineScrollOffset), textBoxWidth);
            int visibleTextEnd = visibleText.length() + this.lineScrollOffset;

            if (this.selectionEnd == this.lineScrollOffset)
            {
                this.lineScrollOffset -= this.fontRendererInstance.trimStringToWidth(this.text, textBoxWidth, true).length();
            }

            if (this.selectionEnd > visibleTextEnd)
            {
                this.lineScrollOffset += this.selectionEnd - visibleTextEnd;
            }
            else if (this.selectionEnd <= this.lineScrollOffset)
            {
                this.lineScrollOffset -= this.lineScrollOffset - this.selectionEnd;
            }

            this.lineScrollOffset = this.clampToCharacterBoundary(this.lineScrollOffset);
        }
    }

    public void setCanLoseFocus(boolean canLoseFocusIn)
    {
        this.canLoseFocus = canLoseFocusIn;
    }

    public boolean getVisible()
    {
        return this.visible;
    }

    public void setVisible(boolean visibleIn)
    {
        this.visible = visibleIn;
    }

    private String trimToMaxStringLength(String value)
    {
        return trimStringToLength(value, this.maxStringLength);
    }

    private static String trimStringToLength(String value, int maxLength)
    {
        if (maxLength <= 0)
        {
            return "";
        }

        if (value.length() <= maxLength)
        {
            return value;
        }

        int end = maxLength;

        if (end > 0 && end < value.length() && Character.isHighSurrogate(value.charAt(end - 1)) && Character.isLowSurrogate(value.charAt(end)))
        {
            --end;
        }

        return value.substring(0, end);
    }

    private int clampToCharacterBoundary(int position)
    {
        int length = this.text.length();
        position = MathHelper.clamp_int(position, 0, length);

        if (position > 0 && position < length && Character.isLowSurrogate(this.text.charAt(position)) && Character.isHighSurrogate(this.text.charAt(position - 1)))
        {
            return position + 1;
        }

        return position;
    }

    private int getPreviousCharacterIndex(int position)
    {
        position = MathHelper.clamp_int(position, 0, this.text.length());

        if (position > 1 && Character.isLowSurrogate(this.text.charAt(position - 1)) && Character.isHighSurrogate(this.text.charAt(position - 2)))
        {
            return position - 2;
        }

        return Math.max(position - 1, 0);
    }

    private int getNextCharacterIndex(int position)
    {
        int length = this.text.length();
        position = MathHelper.clamp_int(position, 0, length);

        if (position < length - 1 && Character.isHighSurrogate(this.text.charAt(position)) && Character.isLowSurrogate(this.text.charAt(position + 1)))
        {
            return position + 2;
        }

        return Math.min(position + 1, length);
    }
}
