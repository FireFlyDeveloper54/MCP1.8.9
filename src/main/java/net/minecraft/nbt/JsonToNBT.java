package net.minecraft.nbt;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.Stack;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JsonToNBT
{
    private static final Logger logger = LogManager.getLogger();
    private static final Pattern numericListPattern = Pattern.compile("\\[[-+\\d|,\\s]+\\]");

    public static NBTTagCompound getTagFromJson(String jsonString) throws NBTException
    {
        jsonString = jsonString.trim();

        if (!jsonString.startsWith("{"))
        {
            throw new NBTException("Invalid tag encountered, expected \'{\' as first char.");
        }
        else if (countTopLevelTags(jsonString) != 1)
        {
            throw new NBTException("Encountered multiple top tags, only one expected");
        }
        else
        {
            return (NBTTagCompound)parseTag("tag", jsonString).parse();
        }
    }

    static int countTopLevelTags(String input) throws NBTException
    {
        int i = 0;
        boolean flag = false;
        Stack<Character> stack = new Stack();

        for (int j = 0; j < input.length(); ++j)
        {
            char thirdCharacter = input.charAt(j);

            if (thirdCharacter == 34)
            {
                if (isEscapedQuote(input, j))
                {
                    if (!flag)
                    {
                        throw new NBTException("Illegal use of \\\": " + input);
                    }
                }
                else
                {
                    flag = !flag;
                }
            }
            else if (!flag)
            {
                if (thirdCharacter != 123 && thirdCharacter != 91)
                {
                    if (thirdCharacter == 125 && (stack.isEmpty() || ((Character)stack.pop()).charValue() != 123))
                    {
                        throw new NBTException("Unbalanced curly brackets {}: " + input);
                    }

                    if (thirdCharacter == 93 && (stack.isEmpty() || ((Character)stack.pop()).charValue() != 91))
                    {
                        throw new NBTException("Unbalanced square brackets []: " + input);
                    }
                }
                else
                {
                    if (stack.isEmpty())
                    {
                        ++i;
                    }

                    stack.push(Character.valueOf(thirdCharacter));
                }
            }
        }

        if (flag)
        {
            throw new NBTException("Unbalanced quotation: " + input);
        }
        else if (!stack.isEmpty())
        {
            throw new NBTException("Unbalanced brackets: " + input);
        }
        else
        {
            if (i == 0 && !input.isEmpty())
            {
                i = 1;
            }

            return i;
        }
    }

    static JsonToNBT.NbtElement parsePair(String... pair) throws NBTException
    {
        return parseTag(pair[0], pair[1]);
    }

    static JsonToNBT.NbtElement parseTag(String name, String value) throws NBTException
    {
        value = value.trim();

        if (value.startsWith("{"))
        {
            value = value.substring(1, value.length() - 1);
            JsonToNBT.Compound compound;
            String element;

            for (compound = new JsonToNBT.Compound(name); value.length() > 0; value = value.substring(element.length() + 1))
            {
                element = extractElement(value, true);

                if (element.length() > 0)
                {
                    boolean quoted = false;
                    compound.elements.add(parsePrimitive(element, quoted));
                }

                if (value.length() < element.length() + 1)
                {
                    break;
                }

                char fifthCharacter = value.charAt(element.length());

                if (fifthCharacter != 44 && fifthCharacter != 123 && fifthCharacter != 125 && fifthCharacter != 91 && fifthCharacter != 93)
                {
                    throw new NBTException("Unexpected token \'" + fifthCharacter + "\' at: " + value.substring(element.length()));
                }
            }

            return compound;
        }
        else if (value.startsWith("[") && !numericListPattern.matcher(value).matches())
        {
            value = value.substring(1, value.length() - 1);
            JsonToNBT.List list;
            String element;

            for (list = new JsonToNBT.List(name); value.length() > 0; value = value.substring(element.length() + 1))
            {
                element = extractElement(value, false);

                if (element.length() > 0)
                {
                    boolean quoted = true;
                    list.elements.add(parsePrimitive(element, quoted));
                }

                if (value.length() < element.length() + 1)
                {
                    break;
                }

                char secondCharacter = value.charAt(element.length());

                if (secondCharacter != 44 && secondCharacter != 123 && secondCharacter != 125 && secondCharacter != 91 && secondCharacter != 93)
                {
                    throw new NBTException("Unexpected token \'" + secondCharacter + "\' at: " + value.substring(element.length()));
                }
            }

            return list;
        }
        else
        {
            return new JsonToNBT.Primitive(name, value);
        }
    }

    private static JsonToNBT.NbtElement parsePrimitive(String text, boolean preferQuoted) throws NBTException
    {
        String name = getName(text, preferQuoted);
        String value = getValue(text, preferQuoted);
        return parsePair(new String[] {name, value});
    }

    private static String extractElement(String text, boolean hasNameValueSeparator) throws NBTException
    {
        int i = findSeparator(text, ':');
        int j = findSeparator(text, ',');

        if (hasNameValueSeparator)
        {
            if (i == -1)
            {
                throw new NBTException("Unable to locate name/value separator for string: " + text);
            }

            if (j != -1 && j < i)
            {
                throw new NBTException("Name error at: " + text);
            }
        }
        else if (i == -1 || i > j)
        {
            i = -1;
        }

        return trimQuotedValue(text, i);
    }

    private static String trimQuotedValue(String text, int separatorIndex) throws NBTException
    {
        Stack<Character> stack = new Stack();
        int i = separatorIndex + 1;
        boolean insideQuotes = false;
        boolean foundQuotedValue = false;
        boolean passedNonWhitespace = false;

        for (int lastQuoteIndex = 0; i < text.length(); ++i)
        {
            char fourthCharacter = text.charAt(i);

            if (fourthCharacter == 34)
            {
                if (isEscapedQuote(text, i))
                {
                    if (!insideQuotes)
                    {
                        throw new NBTException("Illegal use of \\\": " + text);
                    }
                }
                else
                {
                    insideQuotes = !insideQuotes;

                    if (insideQuotes && !passedNonWhitespace)
                    {
                        foundQuotedValue = true;
                    }

                    if (!insideQuotes)
                    {
                        lastQuoteIndex = i;
                    }
                }
            }
            else if (!insideQuotes)
            {
                if (fourthCharacter != 123 && fourthCharacter != 91)
                {
                    if (fourthCharacter == 125 && (stack.isEmpty() || ((Character)stack.pop()).charValue() != 123))
                    {
                        throw new NBTException("Unbalanced curly brackets {}: " + text);
                    }

                    if (fourthCharacter == 93 && (stack.isEmpty() || ((Character)stack.pop()).charValue() != 91))
                    {
                        throw new NBTException("Unbalanced square brackets []: " + text);
                    }

                    if (fourthCharacter == 44 && stack.isEmpty())
                    {
                        return text.substring(0, i);
                    }
                }
                else
                {
                    stack.push(Character.valueOf(fourthCharacter));
                }
            }

            if (!Character.isWhitespace(fourthCharacter))
            {
                if (!insideQuotes && foundQuotedValue && lastQuoteIndex != i)
                {
                    return text.substring(0, lastQuoteIndex + 1);
                }

                passedNonWhitespace = true;
            }
        }

        return text.substring(0, i);
    }

    private static String getName(String text, boolean quoted) throws NBTException {
        if (quoted)
        {
            text = text.trim();

            if (text.startsWith("{") || text.startsWith("["))
            {
                return "";
            }
        }

        int i = findSeparator(text, ':');

        if (i == -1)
        {
            if (quoted)
            {
                return "";
            }
            else
            {
                throw new NBTException("Unable to locate name/value separator for string: " + text);
            }
        }
        else
        {
            return text.substring(0, i).trim();
        }
    }

    private static String getValue(String text, boolean quoted) throws NBTException {
        if (quoted)
        {
            text = text.trim();

            if (text.startsWith("{") || text.startsWith("["))
            {
                return text;
            }
        }

        int i = findSeparator(text, ':');

        if (i == -1)
        {
            if (quoted)
            {
                return text;
            }
            else
            {
                throw new NBTException("Unable to locate name/value separator for string: " + text);
            }
        }
        else
        {
            return text.substring(i + 1).trim();
        }
    }

    private static int findSeparator(String text, char separator)
    {
        int i = 0;

        for (boolean flag = true; i < text.length(); ++i)
        {
            char character = text.charAt(i);

            if (character == 34)
            {
                if (!isEscapedQuote(text, i))
                {
                    flag = !flag;
                }
            }
            else if (flag)
            {
                if (character == separator)
                {
                    return i;
                }

                if (character == 123 || character == 91)
                {
                    return -1;
                }
            }
        }

        return -1;
    }

    private static boolean isEscapedQuote(String text, int index)
    {
        return index > 0 && text.charAt(index - 1) == 92 && !isEscapedQuote(text, index - 1);
    }

    abstract static class NbtElement
    {
        protected String name;

        public abstract NBTBase parse() throws NBTException;
    }

    static class Compound extends JsonToNBT.NbtElement
    {
        protected java.util.List<JsonToNBT.NbtElement> elements = Lists.<JsonToNBT.NbtElement>newArrayList();

        public Compound(String nameIn)
        {
            this.name = nameIn;
        }

        public NBTBase parse() throws NBTException
        {
            NBTTagCompound nbttagcompound = new NBTTagCompound();

            for (JsonToNBT.NbtElement element : this.elements)
            {
                nbttagcompound.setTag(element.name, element.parse());
            }

            return nbttagcompound;
        }
    }

    static class List extends JsonToNBT.NbtElement
    {
        protected java.util.List<JsonToNBT.NbtElement> elements = Lists.<JsonToNBT.NbtElement>newArrayList();

        public List(String json)
        {
            this.name = json;
        }

        public NBTBase parse() throws NBTException
        {
            NBTTagList nbttaglist = new NBTTagList();

            for (JsonToNBT.NbtElement element : this.elements)
            {
                nbttaglist.appendTag(element.parse());
            }

            return nbttaglist;
        }
    }

    static class Primitive extends JsonToNBT.NbtElement
    {
        private static final Pattern DOUBLE = Pattern.compile("[-+]?[0-9]*\\.?[0-9]+[d|D]");
        private static final Pattern FLOAT = Pattern.compile("[-+]?[0-9]*\\.?[0-9]+[f|F]");
        private static final Pattern BYTE = Pattern.compile("[-+]?[0-9]+[b|B]");
        private static final Pattern LONG = Pattern.compile("[-+]?[0-9]+[l|L]");
        private static final Pattern SHORT = Pattern.compile("[-+]?[0-9]+[s|S]");
        private static final Pattern INTEGER = Pattern.compile("[-+]?[0-9]+");
        private static final Pattern DOUBLE_UNTYPED = Pattern.compile("[-+]?[0-9]*\\.?[0-9]+");
        private static final Splitter SPLITTER = Splitter.on(',').omitEmptyStrings();
        protected String value;

        public Primitive(String nameIn, String valueIn)
        {
            this.name = nameIn;
            this.value = valueIn;
        }

        public NBTBase parse() throws NBTException
        {
            try
            {
                if (DOUBLE.matcher(this.value).matches())
                {
                    return new NBTTagDouble(Double.parseDouble(this.value.substring(0, this.value.length() - 1)));
                }

                if (FLOAT.matcher(this.value).matches())
                {
                    return new NBTTagFloat(Float.parseFloat(this.value.substring(0, this.value.length() - 1)));
                }

                if (BYTE.matcher(this.value).matches())
                {
                    return new NBTTagByte(Byte.parseByte(this.value.substring(0, this.value.length() - 1)));
                }

                if (LONG.matcher(this.value).matches())
                {
                    return new NBTTagLong(Long.parseLong(this.value.substring(0, this.value.length() - 1)));
                }

                if (SHORT.matcher(this.value).matches())
                {
                    return new NBTTagShort(Short.parseShort(this.value.substring(0, this.value.length() - 1)));
                }

                if (INTEGER.matcher(this.value).matches())
                {
                    return new NBTTagInt(Integer.parseInt(this.value));
                }

                if (DOUBLE_UNTYPED.matcher(this.value).matches())
                {
                    return new NBTTagDouble(Double.parseDouble(this.value));
                }

                if (this.value.equalsIgnoreCase("true") || this.value.equalsIgnoreCase("false"))
                {
                    return new NBTTagByte((byte)(Boolean.parseBoolean(this.value) ? 1 : 0));
                }
            }
            catch (NumberFormatException caughtNumberFormatException)
            {
                this.value = this.value.replace("\\\"", "\"");
                return new NBTTagString(this.value);
            }

            if (this.value.startsWith("[") && this.value.endsWith("]"))
            {
                String s = this.value.substring(1, this.value.length() - 1);
                String[] astring = (String[])Iterables.toArray(SPLITTER.split(s), String.class);

                try
                {
                    int[] aint = new int[astring.length];

                    for (int j = 0; j < astring.length; ++j)
                    {
                        aint[j] = Integer.parseInt(astring[j].trim());
                    }

                    return new NBTTagIntArray(aint);
                }
                catch (NumberFormatException caughtNumberFormatException)
                {
                    return new NBTTagString(this.value);
                }
            }
            else
            {
                if (this.value.startsWith("\"") && this.value.endsWith("\""))
                {
                    this.value = this.value.substring(1, this.value.length() - 1);
                }

                this.value = this.value.replace("\\\"", "\"");
                StringBuilder stringbuilder = new StringBuilder();

                for (int i = 0; i < this.value.length(); ++i)
                {
                    if (i < this.value.length() - 1 && this.value.charAt(i) == 92 && this.value.charAt(i + 1) == 92)
                    {
                        stringbuilder.append('\\');
                        ++i;
                    }
                    else
                    {
                        stringbuilder.append(this.value.charAt(i));
                    }
                }

                return new NBTTagString(stringbuilder.toString());
            }
        }
    }
}
