package net.minecraft.client.gui;

import com.ibm.icu.text.ArabicShaping;
import com.ibm.icu.text.ArabicShapingException;
import com.ibm.icu.text.Bidi;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.src.Config;
import net.minecraft.util.ResourceLocation;
import net.optifine.CustomColors;
import net.optifine.render.GlBlendState;
import net.optifine.util.FontUtils;
import org.apache.commons.io.IOUtils;
import org.lwjgl.opengl.GL11;

public class FontRenderer implements IResourceManagerReloadListener
{
    private static final String FORMAT_CODES = "0123456789abcdefklmnor";
    private static final ResourceLocation[] unicodePageLocations = new ResourceLocation[256];
    private final int[] charWidth = new int[256];
    public int FONT_HEIGHT = 9;
    public Random fontRandom = new Random();
    private byte[] glyphWidth = new byte[65536];
    private int[] colorCode = new int[32];
    private ResourceLocation locationFontTexture;
    private final TextureManager renderEngine;
    private float posX;
    private float posY;
    private boolean unicodeFlag;
    private boolean bidiFlag;
    private float red;
    private float blue;
    private float green;
    private float alpha;
    private int textColor;
    private boolean randomStyle;
    private boolean boldStyle;
    private boolean italicStyle;
    private boolean underlineStyle;
    private boolean strikethroughStyle;
    public GameSettings gameSettings;
    public ResourceLocation locationFontTextureBase;
    public float offsetBold = 1.0F;
    private float[] charWidthFloat = new float[256];
    private float eastAsianGlyphYOffset;
    private boolean blend = false;
    private GlBlendState oldBlendState = new GlBlendState();
    private ResourceLocation lastBoundTexture;

    public FontRenderer(GameSettings gameSettingsIn, ResourceLocation location, TextureManager textureManagerIn, boolean unicode)
    {
        this.gameSettings = gameSettingsIn;
        this.locationFontTextureBase = location;
        this.locationFontTexture = location;
        this.renderEngine = textureManagerIn;
        this.unicodeFlag = unicode;
        this.locationFontTexture = FontUtils.getHdFontLocation(this.locationFontTextureBase);
        this.bindTexture(this.locationFontTexture);

        for (int i = 0; i < 32; ++i)
        {
            int j = (i >> 3 & 1) * 85;
            int k = (i >> 2 & 1) * 170 + j;
            int l = (i >> 1 & 1) * 170 + j;
            int intValue = (i >> 0 & 1) * 170 + j;

            if (i == 6)
            {
                k += 85;
            }

            if (gameSettingsIn.anaglyph)
            {
                int secondIntValue2 = (k * 30 + l * 59 + intValue * 11) / 100;
                int thirdIntValue2 = (k * 30 + l * 70) / 100;
                int fourthIntValue2 = (k * 30 + intValue * 70) / 100;
                k = secondIntValue2;
                l = thirdIntValue2;
                intValue = fourthIntValue2;
            }

            if (i >= 16)
            {
                k /= 4;
                l /= 4;
                intValue /= 4;
            }

            this.colorCode[i] = (k & 255) << 16 | (l & 255) << 8 | intValue & 255;
        }

        this.readGlyphSizes();
    }

    public void onResourceManagerReload(IResourceManager resourceManager)
    {
        this.locationFontTexture = FontUtils.getHdFontLocation(this.locationFontTextureBase);

        Arrays.fill(unicodePageLocations, null);

        this.readFontTexture();
        this.readGlyphSizes();
    }

    private void readFontTexture()
    {
        BufferedImage bufferedImage;

        try
        {
            bufferedImage = TextureUtil.readBufferedImage(this.getResourceInputStream(this.locationFontTexture));
        }
        catch (IOException ioexception1)
        {
            throw new RuntimeException(ioexception1);
        }

        Properties properties = FontUtils.readFontProperties(this.locationFontTexture);
        this.blend = FontUtils.readBoolean(properties, "blend", false);
        int i = bufferedImage.getWidth();
        int j = bufferedImage.getHeight();
        int k = i / 16;
        int l = j / 16;
        float f = (float)i / 128.0F;
        float floatValue2 = Config.limit(f, 1.0F, 2.0F);
        this.offsetBold = 1.0F / floatValue2;
        float floatValue3 = FontUtils.readFloat(properties, "offsetBold", -1.0F);

        if (floatValue3 >= 0.0F)
        {
            this.offsetBold = floatValue3;
        }

        int[] aint = new int[i * j];
        bufferedImage.getRGB(0, 0, i, j, aint, 0, i);

        for (int index = 0; index < 256; ++index)
        {
            int secondIntValue2 = index % 16;
            int thirdIntValue2 = index / 16;
            int fourthIntValue2 = 0;

            for (fourthIntValue2 = k - 1; fourthIntValue2 >= 0; --fourthIntValue2)
            {
                int intValue2 = secondIntValue2 * k + fourthIntValue2;
                boolean flag = true;

                for (int index2 = 0; index2 < l && flag; ++index2)
                {
                    int intValue3 = (thirdIntValue2 * l + index2) * i;
                    int intValue4 = aint[intValue2 + intValue3];
                    int intValue5 = intValue4 >> 24 & 255;

                    if (intValue5 > 16)
                    {
                        flag = false;
                    }
                }

                if (!flag)
                {
                    break;
                }
            }

            if (index == 65)
            {
                index = index;
            }

            if (index == 32)
            {
                if (k <= 8)
                {
                    fourthIntValue2 = (int)(2.0F * f);
                }
                else
                {
                    fourthIntValue2 = (int)(1.5F * f);
                }
            }

            this.charWidthFloat[index] = (float)(fourthIntValue2 + 1) / f + 1.0F;
        }

        FontUtils.readCustomCharWidths(properties, this.charWidthFloat);

        for (int index3 = 0; index3 < this.charWidth.length; ++index3)
        {
            this.charWidth[index3] = Math.round(this.charWidthFloat[index3]);
        }
    }

    private void readGlyphSizes()
    {
        InputStream inputStream = null;

        try
        {
            inputStream = this.getResourceInputStream(new ResourceLocation("font/glyph_sizes.bin"));
            inputStream.read(this.glyphWidth);
        }
        catch (IOException iOException)
        {
            throw new RuntimeException(iOException);
        }
        finally
        {
            IOUtils.closeQuietly(inputStream);
        }
    }

    private float renderChar(char ch, boolean italic)
    {
        if (ch != 32 && ch != 160)
        {
            int i = "\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&\'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000".indexOf(ch);
            return i != -1 && !this.unicodeFlag ? this.renderDefaultChar(i, italic) : this.renderUnicodeChar(ch, italic);
        }
        else
        {
            return ch < 256 && !this.unicodeFlag ? this.charWidthFloat[ch] : 4.0F;
        }
    }

    private float renderDefaultChar(int ch, boolean italic)
    {
        int i = ch % 16 * 8;
        int j = ch / 16 * 8;
        int k = italic ? 1 : 0;
        this.bindTexture(this.locationFontTexture);
        float f = this.charWidthFloat[ch];
        float floatValue2 = 7.99F;
        GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
        GL11.glTexCoord2f((float)i / 128.0F, (float)j / 128.0F);
        GL11.glVertex3f(this.posX + (float)k, this.posY, 0.0F);
        GL11.glTexCoord2f((float)i / 128.0F, ((float)j + 7.99F) / 128.0F);
        GL11.glVertex3f(this.posX - (float)k, this.posY + 7.99F, 0.0F);
        GL11.glTexCoord2f(((float)i + floatValue2 - 1.0F) / 128.0F, (float)j / 128.0F);
        GL11.glVertex3f(this.posX + floatValue2 - 1.0F + (float)k, this.posY, 0.0F);
        GL11.glTexCoord2f(((float)i + floatValue2 - 1.0F) / 128.0F, ((float)j + 7.99F) / 128.0F);
        GL11.glVertex3f(this.posX + floatValue2 - 1.0F - (float)k, this.posY + 7.99F, 0.0F);
        GL11.glEnd();
        return f;
    }

    private ResourceLocation getUnicodePageLocation(int page)
    {
        if (unicodePageLocations[page] == null)
        {
            unicodePageLocations[page] = new ResourceLocation("textures/font/unicode_page_" + (page < 16 ? "0" : "") + Integer.toHexString(page) + ".png");
            unicodePageLocations[page] = FontUtils.getHdFontLocation(unicodePageLocations[page]);
        }

        return unicodePageLocations[page];
    }

    private void loadGlyphTexture(int page)
    {
        this.bindTexture(this.getUnicodePageLocation(page));
    }

    private float renderUnicodeChar(char ch, boolean italic)
    {
        if (this.glyphWidth[ch] == 0)
        {
            return 0.0F;
        }
        else
        {
            int i = ch / 256;
            this.loadGlyphTexture(i);
            int j = this.glyphWidth[ch] >>> 4;
            int k = this.glyphWidth[ch] & 15;
            float f = (float)j;
            float floatValue2 = (float)(k + 1);
            float floatValue3 = (float)(ch % 16 * 16) + f;
            float floatValue4 = (float)((ch & 255) / 16 * 16);
            float floatValue5 = floatValue2 - f - 0.02F;
            float floatValue6 = italic ? 1.0F : 0.0F;
            float glyphY = this.posY + this.getUnicodeGlyphYOffset(ch);
            GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
            GL11.glTexCoord2f(floatValue3 / 256.0F, floatValue4 / 256.0F);
            GL11.glVertex3f(this.posX + floatValue6, glyphY, 0.0F);
            GL11.glTexCoord2f(floatValue3 / 256.0F, (floatValue4 + 15.98F) / 256.0F);
            GL11.glVertex3f(this.posX - floatValue6, glyphY + 7.99F, 0.0F);
            GL11.glTexCoord2f((floatValue3 + floatValue5) / 256.0F, floatValue4 / 256.0F);
            GL11.glVertex3f(this.posX + floatValue5 / 2.0F + floatValue6, glyphY, 0.0F);
            GL11.glTexCoord2f((floatValue3 + floatValue5) / 256.0F, (floatValue4 + 15.98F) / 256.0F);
            GL11.glVertex3f(this.posX + floatValue5 / 2.0F - floatValue6, glyphY + 7.99F, 0.0F);
            GL11.glEnd();
            return (floatValue2 - f) / 2.0F + 1.0F;
        }
    }

    private float getUnicodeGlyphYOffset(char ch)
    {
        return isEastAsianGlyph(ch) ? this.eastAsianGlyphYOffset : 0.0F;
    }

    private static boolean isEastAsianGlyph(char ch)
    {
        return ch >= '\u2E80' && ch <= '\u9FFF' || ch >= '\uAC00' && ch <= '\uD7AF' || ch >= '\uF900' && ch <= '\uFAFF' || ch >= '\uFE30' && ch <= '\uFE4F' || ch >= '\uFF00' && ch <= '\uFFEF';
    }

    public int drawStringWithShadow(String text, float x, float y, int color)
    {
        return this.drawString(text, x, y, color, true);
    }

    public int drawStringWithShadow(String text, float x, float y, int color, float eastAsianYOffset)
    {
        return this.drawString(text, x, y, color, true, eastAsianYOffset);
    }

    public int drawString(String text, int x, int y, int color)
    {
        return this.drawString(text, (float)x, (float)y, color, false);
    }

    public int drawString(String text, float x, float y, int color, boolean dropShadow)
    {
        return this.drawString(text, x, y, color, dropShadow, 0.0F);
    }

    public int drawString(String text, float x, float y, int color, boolean dropShadow, float eastAsianYOffset)
    {
        this.enableAlpha();

        if (this.blend)
        {
            GlStateManager.getBlendState(this.oldBlendState);
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(770, 771);
        }

        this.resetStyles();
        this.eastAsianGlyphYOffset = eastAsianYOffset;
        int i;

        try
        {
            if (dropShadow)
            {
                i = this.renderString(text, x + 1.0F, y + 1.0F, color, true);
                i = Math.max(i, this.renderString(text, x, y, color, false));
            }
            else
            {
                i = this.renderString(text, x, y, color, false);
            }
        }
        finally
        {
            this.eastAsianGlyphYOffset = 0.0F;
        }

        if (this.blend)
        {
            GlStateManager.setBlendState(this.oldBlendState);
        }

        return i;
    }

    private String bidiReorder(String text)
    {
        try
        {
            Bidi bidi = new Bidi((new ArabicShaping(8)).shape(text), 127);
            bidi.setReorderingMode(0);
            return bidi.writeReordered(2);
        }
        catch (ArabicShapingException caughtArabicShapingException)
        {
            return text;
        }
    }

    private void resetStyles()
    {
        this.randomStyle = false;
        this.boldStyle = false;
        this.italicStyle = false;
        this.underlineStyle = false;
        this.strikethroughStyle = false;
    }

    private void renderStringAtPos(String text, boolean shadow)
    {
        String lowercaseText = null;

        for (int i = 0; i < text.length(); ++i)
        {
            char character = text.charAt(i);

            if (character == 167 && i + 1 < text.length())
            {
                if (lowercaseText == null)
                {
                    lowercaseText = text.toLowerCase(Locale.ENGLISH);
                }

                int l = FORMAT_CODES.indexOf(lowercaseText.charAt(i + 1));

                if (l < 16)
                {
                    this.randomStyle = false;
                    this.boldStyle = false;
                    this.strikethroughStyle = false;
                    this.underlineStyle = false;
                    this.italicStyle = false;

                    if (l < 0 || l > 15)
                    {
                        l = 15;
                    }

                    if (shadow)
                    {
                        l += 16;
                    }

                    int secondIntValue = this.colorCode[l];

                    if (Config.isCustomColors())
                    {
                        secondIntValue = CustomColors.getTextColor(l, secondIntValue);
                    }

                    this.textColor = secondIntValue;
                    this.setColor((float)(secondIntValue >> 16) / 255.0F, (float)(secondIntValue >> 8 & 255) / 255.0F, (float)(secondIntValue & 255) / 255.0F, this.alpha);
                }
                else if (l == 16)
                {
                    this.randomStyle = true;
                }
                else if (l == 17)
                {
                    this.boldStyle = true;
                }
                else if (l == 18)
                {
                    this.strikethroughStyle = true;
                }
                else if (l == 19)
                {
                    this.underlineStyle = true;
                }
                else if (l == 20)
                {
                    this.italicStyle = true;
                }
                else if (l == 21)
                {
                    this.randomStyle = false;
                    this.boldStyle = false;
                    this.strikethroughStyle = false;
                    this.underlineStyle = false;
                    this.italicStyle = false;
                    this.setColor(this.red, this.blue, this.green, this.alpha);
                }

                ++i;
            }
            else
            {
                int j = "\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&\'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000".indexOf(character);

                if (this.randomStyle && j != -1)
                {
                    int k = this.getCharWidth(character);
                    char secondCharacter;

                    while (true)
                    {
                        j = this.fontRandom.nextInt("\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&\'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000".length());
                        secondCharacter = "\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&\'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000".charAt(j);

                        if (k == this.getCharWidth(secondCharacter))
                        {
                            break;
                        }
                    }

                    character = secondCharacter;
                }

                float secondFloatValue = j != -1 && !this.unicodeFlag ? this.offsetBold : 0.5F;
                boolean flag = (character == 0 || j == -1 || this.unicodeFlag) && shadow;

                if (flag)
                {
                    this.posX -= secondFloatValue;
                    this.posY -= secondFloatValue;
                }

                float f = this.renderChar(character, this.italicStyle);

                if (flag)
                {
                    this.posX += secondFloatValue;
                    this.posY += secondFloatValue;
                }

                if (this.boldStyle)
                {
                    this.posX += secondFloatValue;

                    if (flag)
                    {
                        this.posX -= secondFloatValue;
                        this.posY -= secondFloatValue;
                    }

                    this.renderChar(character, this.italicStyle);
                    this.posX -= secondFloatValue;

                    if (flag)
                    {
                        this.posX += secondFloatValue;
                        this.posY += secondFloatValue;
                    }

                    f += secondFloatValue;
                }

                this.doDraw(f);
            }
        }
    }

    protected void doDraw(float charWidth)
    {
        if (this.strikethroughStyle)
        {
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldRenderer = tessellator.getWorldRenderer();
            GlStateManager.disableTexture2D();
            worldRenderer.begin(7, DefaultVertexFormats.POSITION);
            worldRenderer.pos((double)this.posX, (double)(this.posY + (float)(this.FONT_HEIGHT / 2)), 0.0D).endVertex();
            worldRenderer.pos((double)(this.posX + charWidth), (double)(this.posY + (float)(this.FONT_HEIGHT / 2)), 0.0D).endVertex();
            worldRenderer.pos((double)(this.posX + charWidth), (double)(this.posY + (float)(this.FONT_HEIGHT / 2) - 1.0F), 0.0D).endVertex();
            worldRenderer.pos((double)this.posX, (double)(this.posY + (float)(this.FONT_HEIGHT / 2) - 1.0F), 0.0D).endVertex();
            tessellator.draw();
            GlStateManager.enableTexture2D();
        }

        if (this.underlineStyle)
        {
            Tessellator tessellator1 = Tessellator.getInstance();
            WorldRenderer worldrenderer1 = tessellator1.getWorldRenderer();
            GlStateManager.disableTexture2D();
            worldrenderer1.begin(7, DefaultVertexFormats.POSITION);
            int i = this.underlineStyle ? -1 : 0;
            worldrenderer1.pos((double)(this.posX + (float)i), (double)(this.posY + (float)this.FONT_HEIGHT), 0.0D).endVertex();
            worldrenderer1.pos((double)(this.posX + charWidth), (double)(this.posY + (float)this.FONT_HEIGHT), 0.0D).endVertex();
            worldrenderer1.pos((double)(this.posX + charWidth), (double)(this.posY + (float)this.FONT_HEIGHT - 1.0F), 0.0D).endVertex();
            worldrenderer1.pos((double)(this.posX + (float)i), (double)(this.posY + (float)this.FONT_HEIGHT - 1.0F), 0.0D).endVertex();
            tessellator1.draw();
            GlStateManager.enableTexture2D();
        }

        this.posX += charWidth;
    }

    private int renderStringAligned(String text, int x, int y, int width, int color, boolean dropShadow)
    {
        if (this.bidiFlag)
        {
            int i = this.getStringWidth(this.bidiReorder(text));
            x = x + width - i;
        }

        return this.renderString(text, (float)x, (float)y, color, dropShadow);
    }

    private int renderString(String text, float x, float y, int color, boolean dropShadow)
    {
        if (text == null)
        {
            return 0;
        }
        else
        {
            if (this.bidiFlag)
            {
                text = this.bidiReorder(text);
            }

            if ((color & -67108864) == 0)
            {
                color |= -16777216;
            }

            if (dropShadow)
            {
                color = (color & 16579836) >> 2 | color & -16777216;
            }

            this.red = (float)(color >> 16 & 255) / 255.0F;
            this.blue = (float)(color >> 8 & 255) / 255.0F;
            this.green = (float)(color & 255) / 255.0F;
            this.alpha = (float)(color >> 24 & 255) / 255.0F;
            this.setColor(this.red, this.blue, this.green, this.alpha);
            this.posX = x;
            this.posY = y;
            this.lastBoundTexture = null;
            this.renderStringAtPos(text, dropShadow);
            return (int)this.posX;
        }
    }

    public int getStringWidth(String text)
    {
        if (text == null)
        {
            return 0;
        }
        else
        {
            float f = 0.0F;
            boolean flag = false;

            for (int i = 0; i < text.length(); ++i)
            {
                char character = text.charAt(i);
                float floatValue = this.getCharWidthFloat(character);

                if (floatValue < 0.0F && i < text.length() - 1)
                {
                    ++i;
                    character = text.charAt(i);

                    if (character != 108 && character != 76)
                    {
                        if (character == 114 || character == 82)
                        {
                            flag = false;
                        }
                    }
                    else
                    {
                        flag = true;
                    }

                    floatValue = 0.0F;
                }

                f += floatValue;

                if (flag && floatValue > 0.0F)
                {
                    f += this.unicodeFlag ? 1.0F : this.offsetBold;
                }
            }

            return Math.round(f);
        }
    }

    public int getCharWidth(char character)
    {
        return Math.round(this.getCharWidthFloat(character));
    }

    private float getCharWidthFloat(char character)
    {
        if (character == 167)
        {
            return -1.0F;
        }
        else if (character != 32 && character != 160)
        {
            int i = "\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&\'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000".indexOf(character);

            if (character > 0 && i != -1 && !this.unicodeFlag)
            {
                return this.charWidthFloat[i];
            }
            else if (this.glyphWidth[character] != 0)
            {
                int j = this.glyphWidth[character] >>> 4;
                int k = this.glyphWidth[character] & 15;

                if (k > 7)
                {
                    k = 15;
                    j = 0;
                }

                ++k;
                return (float)((k - j) / 2 + 1);
            }
            else
            {
                return 0.0F;
            }
        }
        else
        {
            return this.charWidthFloat[32];
        }
    }

    public String trimStringToWidth(String text, int width)
    {
        return this.trimStringToWidth(text, width, false);
    }

    public String trimStringToWidth(String text, int width, boolean reverse)
    {
        StringBuilder stringBuilder = new StringBuilder();
        float f = 0.0F;
        int i = reverse ? text.length() - 1 : 0;
        int j = reverse ? -1 : 1;
        boolean flag = false;
        boolean flag1 = false;

        for (int k = i; k >= 0 && k < text.length() && f < (float)width; k += j)
        {
            char character = text.charAt(k);
            float floatValue2 = this.getCharWidthFloat(character);

            if (flag)
            {
                flag = false;

                if (character != 108 && character != 76)
                {
                    if (character == 114 || character == 82)
                    {
                        flag1 = false;
                    }
                }
                else
                {
                    flag1 = true;
                }
            }
            else if (floatValue2 < 0.0F)
            {
                flag = true;
            }
            else
            {
                f += floatValue2;

                if (flag1)
                {
                    ++f;
                }
            }

            if (f > (float)width)
            {
                break;
            }

            if (reverse)
            {
                stringBuilder.insert(0, (char)character);
            }
            else
            {
                stringBuilder.append(character);
            }
        }

        return trimDanglingSurrogate(stringBuilder.toString());
    }

    private String trimStringNewline(String text)
    {
        while (text != null && text.endsWith("\n"))
        {
            text = text.substring(0, text.length() - 1);
        }

        return text;
    }

    public void drawSplitString(String str, int x, int y, int wrapWidth, int textColor)
    {
        this.drawSplitString(str, x, y, wrapWidth, textColor, 0.0F);
    }

    public void drawSplitString(String str, int x, int y, int wrapWidth, int textColor, float eastAsianYOffset)
    {
        if (this.blend)
        {
            GlStateManager.getBlendState(this.oldBlendState);
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(770, 771);
        }

        this.resetStyles();
        this.textColor = textColor;
        str = this.trimStringNewline(str);

        this.eastAsianGlyphYOffset = eastAsianYOffset;

        try
        {
            this.renderSplitString(str, x, y, wrapWidth, false);
        }
        finally
        {
            this.eastAsianGlyphYOffset = 0.0F;
        }

        if (this.blend)
        {
            GlStateManager.setBlendState(this.oldBlendState);
        }
    }

    private void renderSplitString(String str, int x, int y, int wrapWidth, boolean addShadow)
    {
        for (String s : this.listFormattedStringToWidth(str, wrapWidth))
        {
            this.renderStringAligned(s, x, y, wrapWidth, this.textColor, addShadow);
            y += this.FONT_HEIGHT;
        }
    }

    public int splitStringWidth(String str, int maxLength)
    {
        return this.FONT_HEIGHT * this.listFormattedStringToWidth(str, maxLength).size();
    }

    public void setUnicodeFlag(boolean unicodeFlagIn)
    {
        this.unicodeFlag = unicodeFlagIn;
    }

    public boolean getUnicodeFlag()
    {
        return this.unicodeFlag;
    }

    public void setBidiFlag(boolean bidiFlagIn)
    {
        this.bidiFlag = bidiFlagIn;
    }

    public List<String> listFormattedStringToWidth(String str, int wrapWidth)
    {
        return Arrays.<String>asList(this.wrapFormattedStringToWidth(str, wrapWidth).split("\n"));
    }

    String wrapFormattedStringToWidth(String str, int wrapWidth)
    {
        if (str.length() <= 1)
        {
            return str;
        }
        else
        {
            int i = this.sizeStringToWidth(str, wrapWidth);

            if (str.length() <= i)
            {
                return str;
            }
            else
            {
                i = getSafeWrapIndex(str, i);

                if (str.length() <= i)
                {
                    return str;
                }

                String s = str.substring(0, i);
                char character = str.charAt(i);
                boolean flag = character == 32 || character == 10;
                String stringValue = getFormatFromString(s) + str.substring(i + (flag ? 1 : 0));
                return s + "\n" + this.wrapFormattedStringToWidth(stringValue, wrapWidth);
            }
        }
    }

    private int sizeStringToWidth(String str, int wrapWidth)
    {
        int i = str.length();
        float f = 0.0F;
        int j = 0;
        int k = -1;

        for (boolean flag = false; j < i; ++j)
        {
            char thirdCharacter = str.charAt(j);

            switch (thirdCharacter)
            {
                case '\n':
                    --j;
                    break;

                case ' ':
                    k = j;

                default:
                    f += (float)this.getCharWidth(thirdCharacter);

                    if (flag)
                    {
                        ++f;
                    }

                    break;

                case '\u00a7':
                    if (j < i - 1)
                    {
                        ++j;
                        char fourthCharacter = str.charAt(j);

                        if (fourthCharacter != 108 && fourthCharacter != 76)
                        {
                            if (fourthCharacter == 114 || fourthCharacter == 82 || isFormatColor(fourthCharacter))
                            {
                                flag = false;
                            }
                        }
                        else
                        {
                            flag = true;
                        }
                    }
            }

            if (thirdCharacter == 10)
            {
                ++j;
                k = j;
                break;
            }

            if (Math.round(f) > wrapWidth)
            {
                break;
            }
        }

        int result = j != i && k != -1 && k < j ? k : j;

        if (result > 0 && result < str.length() && Character.isHighSurrogate(str.charAt(result - 1)) && Character.isLowSurrogate(str.charAt(result)))
        {
            --result;
        }

        return result;
    }

    private static String trimDanglingSurrogate(String text)
    {
        int start = 0;
        int end = text.length();

        if (end > 0 && Character.isHighSurrogate(text.charAt(end - 1)))
        {
            --end;
        }

        if (start < end && Character.isLowSurrogate(text.charAt(start)))
        {
            ++start;
        }

        return start == 0 && end == text.length() ? text : text.substring(start, end);
    }

    private static int getSafeWrapIndex(String text, int index)
    {
        if (index <= 0)
        {
            return Character.charCount(text.codePointAt(0));
        }

        if (index < text.length() && Character.isHighSurrogate(text.charAt(index - 1)) && Character.isLowSurrogate(text.charAt(index)))
        {
            --index;

            if (index <= 0)
            {
                return Character.charCount(text.codePointAt(0));
            }
        }

        return index;
    }

    private static boolean isFormatColor(char colorChar)
    {
        return colorChar >= 48 && colorChar <= 57 || colorChar >= 97 && colorChar <= 102 || colorChar >= 65 && colorChar <= 70;
    }

    private static boolean isFormatSpecial(char formatChar)
    {
        return formatChar >= 107 && formatChar <= 111 || formatChar >= 75 && formatChar <= 79 || formatChar == 114 || formatChar == 82;
    }

    public static String getFormatFromString(String text)
    {
        String s = "";
        int i = -1;
        int j = text.length();

        while ((i = text.indexOf(167, i + 1)) != -1)
        {
            if (i < j - 1)
            {
                char character = text.charAt(i + 1);

                if (isFormatColor(character))
                {
                    s = "\u00a7" + character;
                }
                else if (isFormatSpecial(character))
                {
                    s = s + "\u00a7" + character;
                }
            }
        }

        return s;
    }

    public boolean getBidiFlag()
    {
        return this.bidiFlag;
    }

    public int getColorCode(char character)
    {
        int i = "0123456789abcdef".indexOf(character);

        if (i >= 0 && i < this.colorCode.length)
        {
            int j = this.colorCode[i];

            if (Config.isCustomColors())
            {
                j = CustomColors.getTextColor(i, j);
            }

            return j;
        }
        else
        {
            return 16777215;
        }
    }

    protected void setColor(float red, float green, float blue, float alpha)
    {
        GlStateManager.color(red, green, blue, alpha);
    }

    protected void enableAlpha()
    {
        GlStateManager.enableAlpha();
    }

    protected void bindTexture(ResourceLocation location)
    {
        if (!location.equals(this.lastBoundTexture))
        {
            this.renderEngine.bindTexture(location);
            this.lastBoundTexture = location;
        }
    }

    protected InputStream getResourceInputStream(ResourceLocation location) throws IOException
    {
        return Minecraft.getMinecraft().getResourceManager().getResource(location).getInputStream();
    }
}
