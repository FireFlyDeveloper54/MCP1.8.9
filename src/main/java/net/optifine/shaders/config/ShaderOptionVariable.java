package net.optifine.shaders.config;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.src.Config;
import net.optifine.shaders.Shaders;
import net.optifine.util.StrUtils;

public class ShaderOptionVariable extends ShaderOption
{
    private static final Pattern PATTERN_VARIABLE = Pattern.compile("^\\s*#define\\s+(\\w+)\\s+(-?[0-9\\.Ff]+|\\w+)\\s*(//.*)?$");

    public ShaderOptionVariable(String name, String description, String value, String[] values, String path)
    {
        super(name, description, value, values, value, path);
        this.setVisible(this.getValues().length > 1);
    }

    public String getSourceLine()
    {
        return "#define " + this.getName() + " " + this.getValue() + " // Shader option " + this.getValue();
    }

    public String getValueText(String val)
    {
        String prefixText = Shaders.translate("prefix." + this.getName(), "");
        String valueText = super.getValueText(val);
        String suffixText = Shaders.translate("suffix." + this.getName(), "");
        String displayText = prefixText + valueText + suffixText;
        return displayText;
    }

    public String getValueColor(String val)
    {
        String lowerValue = val.toLowerCase();
        return !lowerValue.equals("false") && !lowerValue.equals("off") ? "\u00a7a" : "\u00a7c";
    }

    public boolean matchesLine(String line)
    {
        Matcher matcher = PATTERN_VARIABLE.matcher(line);

        if (!matcher.matches())
        {
            return false;
        }
        else
        {
            String matchedName = matcher.group(1);
            return matchedName.matches(this.getName());
        }
    }

    public static ShaderOption parseOption(String line, String path)
    {
        Matcher matcher = PATTERN_VARIABLE.matcher(line);

        if (!matcher.matches())
        {
            return null;
        }
        else
        {
            String optionName = matcher.group(1);
            String value = matcher.group(2);
            String description = matcher.group(3);
            String valuesSegment = StrUtils.getSegment(description, "[", "]");

            if (valuesSegment != null && valuesSegment.length() > 0)
            {
                description = description.replace(valuesSegment, "").trim();
            }

            String[] values = parseValues(value, valuesSegment);

            if (optionName != null && optionName.length() > 0)
            {
                path = StrUtils.removePrefix(path, "/shaders/");
                ShaderOption shaderOption = new ShaderOptionVariable(optionName, description, value, values, path);
                return shaderOption;
            }
            else
            {
                return null;
            }
        }
    }

    public static String[] parseValues(String value, String valuesStr)
    {
        String[] defaultValues = new String[] {value};

        if (valuesStr == null)
        {
            return defaultValues;
        }
        else
        {
            valuesStr = valuesStr.trim();
            valuesStr = StrUtils.removePrefix(valuesStr, "[");
            valuesStr = StrUtils.removeSuffix(valuesStr, "]");
            valuesStr = valuesStr.trim();

            if (valuesStr.length() <= 0)
            {
                return defaultValues;
            }
            else
            {
                String[] parsedValues = Config.tokenize(valuesStr, " ");

                if (parsedValues.length <= 0)
                {
                    return defaultValues;
                }
                else
                {
                    if (!Arrays.asList(parsedValues).contains(value))
                    {
                        parsedValues = (String[])((String[])Config.addObjectToArray(parsedValues, value, 0));
                    }

                    return parsedValues;
                }
            }
        }
    }
}
