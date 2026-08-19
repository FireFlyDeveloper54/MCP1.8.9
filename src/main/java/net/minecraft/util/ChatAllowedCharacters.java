package net.minecraft.util;

public class ChatAllowedCharacters
{
    public static final char[] allowedCharactersArray = new char[] {'/', '\n', '\r', '\t', '\u0000', '\f', '`', '?', '*', '\\', '<', '>', '|', '\"', ':'};

    public static boolean isAllowedCharacter(char character)
    {
        return character != 167 && character >= 32 && character != 127;
    }

    public static boolean isAllowedCodePoint(int codePoint)
    {
        if (!Character.isValidCodePoint(codePoint))
        {
            return false;
        }

        if (codePoint <= Character.MAX_VALUE)
        {
            return isAllowedCharacter((char)codePoint);
        }

        char[] characters = Character.toChars(codePoint);
        return isAllowedCharacter(characters[0]) && isAllowedCharacter(characters[1]);
    }

    public static String filterAllowedCharacters(String input)
    {
        StringBuilder stringBuilder = new StringBuilder(input.length());

        for (int index = 0; index < input.length(); ++index)
        {
            char character = input.charAt(index);

            if (isAllowedCharacter(character))
            {
                stringBuilder.append(character);
            }
        }

        return stringBuilder.toString();
    }
}
