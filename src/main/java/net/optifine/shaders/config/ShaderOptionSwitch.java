package net.optifine.shaders.config;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.src.Config;
import net.optifine.Lang;
import net.optifine.util.StrUtils;

public class ShaderOptionSwitch extends ShaderOption
{
    private static final Pattern PATTERN_DEFINE = Pattern.compile("^\\s*(//)?\\s*#define\\s+([A-Za-z0-9_]+)\\s*(//.*)?$");
    private static final Pattern PATTERN_IFDEF = Pattern.compile("^\\s*#if(n)?def\\s+([A-Za-z0-9_]+)(\\s*)?$");

    public ShaderOptionSwitch(String name, String description, String value, String path)
    {
        super(name, description, value, new String[] {"false", "true"}, value, path);
    }

    public String getSourceLine()
    {
        return isTrue(this.getValue()) ? "#define " + this.getName() + " // Shader option ON" : "//#define " + this.getName() + " // Shader option OFF";
    }

    public String getValueText(String val)
    {
        String valueText = super.getValueText(val);
        return valueText != val ? valueText : (isTrue(val) ? Lang.getOn() : Lang.getOff());
    }

    public String getValueColor(String val)
    {
        return isTrue(val) ? "\u00a7a" : "\u00a7c";
    }

    public static ShaderOption parseOption(String line, String path)
    {
        Matcher matcher = PATTERN_DEFINE.matcher(line);

        if (!matcher.matches())
        {
            return null;
        }
        else
        {
            String commentPrefix = matcher.group(1);
            String optionName = matcher.group(2);
            String description = matcher.group(3);

            if (optionName != null && optionName.length() > 0)
            {
                boolean disabled = Config.equals(commentPrefix, "//");
                boolean enabled = !disabled;
                path = StrUtils.removePrefix(path, "/shaders/");
                ShaderOption shaderOption = new ShaderOptionSwitch(optionName, description, String.valueOf(enabled), path);
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
        Matcher matcher = PATTERN_DEFINE.matcher(line);

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

    public boolean checkUsed()
    {
        return true;
    }

    public boolean isUsedInLine(String line)
    {
        Matcher matcher = PATTERN_IFDEF.matcher(line);

        if (matcher.matches())
        {
            String matchedName = matcher.group(2);

            if (matchedName.equals(this.getName()))
            {
                return true;
            }
        }

        return false;
    }

    public static boolean isTrue(String val)
    {
        return Boolean.valueOf(val).booleanValue();
    }
}
