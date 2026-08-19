package net.optifine.shaders.config;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.optifine.util.StrUtils;

public class ShaderOptionVariableConst extends ShaderOptionVariable
{
    private String type = null;
    private static final Pattern PATTERN_CONST = Pattern.compile("^\\s*const\\s*(float|int)\\s*([A-Za-z0-9_]+)\\s*=\\s*(-?[0-9\\.]+f?F?)\\s*;\\s*(//.*)?$");

    public ShaderOptionVariableConst(String name, String type, String description, String value, String[] values, String path)
    {
        super(name, description, value, values, path);
        this.type = type;
    }

    public String getSourceLine()
    {
        return "const " + this.type + " " + this.getName() + " = " + this.getValue() + "; // Shader option " + this.getValue();
    }

    public boolean matchesLine(String line)
    {
        Matcher matcher = PATTERN_CONST.matcher(line);

        if (!matcher.matches())
        {
            return false;
        }
        else
        {
            String matchedName = matcher.group(2);
            return matchedName.matches(this.getName());
        }
    }

    public static ShaderOption parseOption(String line, String path)
    {
        Matcher matcher = PATTERN_CONST.matcher(line);

        if (!matcher.matches())
        {
            return null;
        }
        else
        {
            String typeName = matcher.group(1);
            String optionName = matcher.group(2);
            String value = matcher.group(3);
            String description = matcher.group(4);
            String valuesSegment = StrUtils.getSegment(description, "[", "]");

            if (valuesSegment != null && valuesSegment.length() > 0)
            {
                description = description.replace(valuesSegment, "").trim();
            }

            String[] values = parseValues(value, valuesSegment);

            if (optionName != null && optionName.length() > 0)
            {
                path = StrUtils.removePrefix(path, "/shaders/");
                ShaderOption shaderOption = new ShaderOptionVariableConst(optionName, typeName, description, value, values, path);
                return shaderOption;
            }
            else
            {
                return null;
            }
        }
    }
}
