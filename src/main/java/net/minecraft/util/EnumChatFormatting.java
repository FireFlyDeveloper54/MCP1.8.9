package net.minecraft.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public enum EnumChatFormatting
{
    BLACK("BLACK", '0', 0),
    DARK_BLUE("DARK_BLUE", '1', 1),
    DARK_GREEN("DARK_GREEN", '2', 2),
    DARK_AQUA("DARK_AQUA", '3', 3),
    DARK_RED("DARK_RED", '4', 4),
    DARK_PURPLE("DARK_PURPLE", '5', 5),
    GOLD("GOLD", '6', 6),
    GRAY("GRAY", '7', 7),
    DARK_GRAY("DARK_GRAY", '8', 8),
    BLUE("BLUE", '9', 9),
    GREEN("GREEN", 'a', 10),
    AQUA("AQUA", 'b', 11),
    RED("RED", 'c', 12),
    LIGHT_PURPLE("LIGHT_PURPLE", 'd', 13),
    YELLOW("YELLOW", 'e', 14),
    WHITE("WHITE", 'f', 15),
    OBFUSCATED("OBFUSCATED", 'k', true),
    BOLD("BOLD", 'l', true),
    STRIKETHROUGH("STRIKETHROUGH", 'm', true),
    UNDERLINE("UNDERLINE", 'n', true),
    ITALIC("ITALIC", 'o', true),
    RESET("RESET", 'r', -1);

    private static final EnumChatFormatting[] VALUES = values();
    private static final EnumChatFormatting[] COLOR_INDEX_LOOKUP = new EnumChatFormatting[16];
    private static final Map<String, EnumChatFormatting> nameMapping = Maps.<String, EnumChatFormatting>newHashMap();
    private static final Pattern nameCleanupPattern = Pattern.compile("[^a-z]");
    private final String name;
    private final char formattingCode;
    private final boolean fancyStyling;
    private final String controlString;
    private final int colorIndex;

    private static String normalizeName(String text)
    {
        return nameCleanupPattern.matcher(text.toLowerCase(Locale.ROOT)).replaceAll("");
    }

    private EnumChatFormatting(String formattingName, char formattingCodeIn, int colorIndex)
    {
        this(formattingName, formattingCodeIn, false, colorIndex);
    }

    private EnumChatFormatting(String formattingName, char formattingCodeIn, boolean fancyStylingIn)
    {
        this(formattingName, formattingCodeIn, fancyStylingIn, -1);
    }

    private EnumChatFormatting(String formattingName, char formattingCodeIn, boolean fancyStylingIn, int colorIndex)
    {
        this.name = formattingName;
        this.formattingCode = formattingCodeIn;
        this.fancyStyling = fancyStylingIn;
        this.colorIndex = colorIndex;
        this.controlString = "\u00a7" + formattingCodeIn;
    }

    public int getColorIndex()
    {
        return this.colorIndex;
    }

    public boolean isFancyStyling()
    {
        return this.fancyStyling;
    }

    public boolean isColor()
    {
        return !this.fancyStyling && this != RESET;
    }

    public String getFriendlyName()
    {
        return this.name().toLowerCase(Locale.ROOT);
    }

    public String toString()
    {
        return this.controlString;
    }

    public static String getTextWithoutFormattingCodes(String text)
    {
        if (text == null)
        {
            return null;
        }

        StringBuilder strippedText = null;
        int copyStart = 0;

        for (int index = 0; index + 1 < text.length(); ++index)
        {
            if (text.charAt(index) == '\u00a7' && isFormattingCode(text.charAt(index + 1)))
            {
                if (strippedText == null)
                {
                    strippedText = new StringBuilder(text.length() - 2);
                }

                strippedText.append(text, copyStart, index);
                copyStart = ++index + 1;
            }
        }

        if (strippedText == null)
        {
            return text;
        }

        return strippedText.append(text, copyStart, text.length()).toString();
    }

    private static boolean isFormattingCode(char code)
    {
        char normalizedCode = code >= 'a' && code <= 'z' ? (char)(code - ('a' - 'A')) : code;
        return normalizedCode >= '0' && normalizedCode <= '9'
                || normalizedCode >= 'A' && normalizedCode <= 'F'
                || normalizedCode >= 'K' && normalizedCode <= 'O'
                || normalizedCode == 'R';
    }

    public static EnumChatFormatting getValueByName(String friendlyName)
    {
        return friendlyName == null ? null : nameMapping.get(normalizeName(friendlyName));
    }

    public static EnumChatFormatting getByColorIndex(int colorIndex)
    {
        if (colorIndex < 0)
        {
            return RESET;
        }

        return colorIndex < COLOR_INDEX_LOOKUP.length ? COLOR_INDEX_LOOKUP[colorIndex] : null;
    }

    public static Collection<String> getValidValues(boolean includeColors, boolean includeFancyStyling)
    {
        List<String> list = Lists.<String>newArrayList();

        for (EnumChatFormatting enumchatformatting : VALUES)
        {
            if ((!enumchatformatting.isColor() || includeColors) && (!enumchatformatting.isFancyStyling() || includeFancyStyling))
            {
                list.add(enumchatformatting.getFriendlyName());
            }
        }

        return list;
    }

    static {
        for (EnumChatFormatting enumchatformatting : VALUES)
        {
            nameMapping.put(normalizeName(enumchatformatting.name), enumchatformatting);

            if (enumchatformatting.colorIndex >= 0)
            {
                COLOR_INDEX_LOOKUP[enumchatformatting.colorIndex] = enumchatformatting;
            }
        }
    }
}
