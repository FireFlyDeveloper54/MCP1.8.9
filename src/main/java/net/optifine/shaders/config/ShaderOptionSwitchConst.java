package net.optifine.shaders.config;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.optifine.util.StrUtils;

public class ShaderOptionSwitchConst extends ShaderOptionSwitch
{
    private static final Pattern PATTERN_CONST = Pattern.compile("^\\s*const\\s*bool\\s*([A-Za-z0-9_]+)\\s*=\\s*(true|false)\\s*;\\s*(//.*)?$");

    public ShaderOptionSwitchConst(String name, String description, String value, String path)
    {
        super(name, description, value, path);
    }

    public String getSourceLine()
    {
        return "const bool " + this.getName() + " = " + this.getValue() + "; // Shader option " + this.getValue();
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
            String optionName = matcher.group(1);
            String value = matcher.group(2);
            String description = matcher.group(3);

            if (optionName != null && optionName.length() > 0)
            {
                path = StrUtils.removePrefix(path, "/shaders/");
                ShaderOption shaderOption = new ShaderOptionSwitchConst(optionName, description, value, path);
                shaderOption.setVisible(false);
                return shaderOption;
            }
            else
            {
                return null;
            }
        }
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
            String matchedName = matcher.group(1);
            return matchedName.matches(this.getName());
        }
    }

    public boolean checkUsed()
    {
        return false;
    }
}
