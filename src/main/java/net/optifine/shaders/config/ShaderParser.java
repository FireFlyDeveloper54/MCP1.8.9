package net.optifine.shaders.config;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShaderParser
{
    public static Pattern PATTERN_UNIFORM = Pattern.compile("\\s*uniform\\s+\\w+\\s+(\\w+).*");
    public static Pattern PATTERN_ATTRIBUTE = Pattern.compile("\\s*attribute\\s+\\w+\\s+(\\w+).*");
    public static Pattern PATTERN_CONST_INT = Pattern.compile("\\s*const\\s+int\\s+(\\w+)\\s*=\\s*([-+.\\w]+)\\s*;.*");
    public static Pattern PATTERN_CONST_FLOAT = Pattern.compile("\\s*const\\s+float\\s+(\\w+)\\s*=\\s*([-+.\\w]+)\\s*;.*");
    public static Pattern PATTERN_CONST_VEC4 = Pattern.compile("\\s*const\\s+vec4\\s+(\\w+)\\s*=\\s*(.+)\\s*;.*");
    public static Pattern PATTERN_CONST_BOOL = Pattern.compile("\\s*const\\s+bool\\s+(\\w+)\\s*=\\s*(\\w+)\\s*;.*");
    public static Pattern PATTERN_PROPERTY = Pattern.compile("\\s*(/\\*|//)?\\s*([A-Z]+):\\s*(\\w+)\\s*(\\*/.*|\\s*)");
    public static Pattern PATTERN_EXTENSION = Pattern.compile("\\s*#\\s*extension\\s+(\\w+)\\s*:\\s*(\\w+).*");
    public static Pattern PATTERN_DEFERRED_FSH = Pattern.compile(".*deferred[0-9]*\\.fsh");
    public static Pattern PATTERN_COMPOSITE_FSH = Pattern.compile(".*composite[0-9]*\\.fsh");
    public static Pattern PATTERN_FINAL_FSH = Pattern.compile(".*final\\.fsh");
    public static Pattern PATTERN_DRAW_BUFFERS = Pattern.compile("[0-7N]*");

    public static ShaderLine parseLine(String line)
    {
        Matcher matcher = PATTERN_UNIFORM.matcher(line);

        if (matcher.matches())
        {
            return new ShaderLine(1, matcher.group(1), "", line);
        }
        else
        {
            Matcher attributeMatcher = PATTERN_ATTRIBUTE.matcher(line);

            if (attributeMatcher.matches())
            {
                return new ShaderLine(2, attributeMatcher.group(1), "", line);
            }
            else
            {
                Matcher propertyMatcher = PATTERN_PROPERTY.matcher(line);

                if (propertyMatcher.matches())
                {
                    return new ShaderLine(6, propertyMatcher.group(2), propertyMatcher.group(3), line);
                }
                else
                {
                    Matcher constIntMatcher = PATTERN_CONST_INT.matcher(line);

                    if (constIntMatcher.matches())
                    {
                        return new ShaderLine(3, constIntMatcher.group(1), constIntMatcher.group(2), line);
                    }
                    else
                    {
                        Matcher constFloatMatcher = PATTERN_CONST_FLOAT.matcher(line);

                        if (constFloatMatcher.matches())
                        {
                            return new ShaderLine(4, constFloatMatcher.group(1), constFloatMatcher.group(2), line);
                        }
                        else
                        {
                            Matcher constBoolMatcher = PATTERN_CONST_BOOL.matcher(line);

                            if (constBoolMatcher.matches())
                            {
                                return new ShaderLine(5, constBoolMatcher.group(1), constBoolMatcher.group(2), line);
                            }
                            else
                            {
                                Matcher extensionMatcher = PATTERN_EXTENSION.matcher(line);

                                if (extensionMatcher.matches())
                                {
                                    return new ShaderLine(7, extensionMatcher.group(1), extensionMatcher.group(2), line);
                                }
                                else
                                {
                                    Matcher constVec4Matcher = PATTERN_CONST_VEC4.matcher(line);
                                    return constVec4Matcher.matches() ? new ShaderLine(8, constVec4Matcher.group(1), constVec4Matcher.group(2), line) : null;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static int getIndex(String uniform, String prefix, int minIndex, int maxIndex)
    {
        if (uniform.length() != prefix.length() + 1)
        {
            return -1;
        }
        else if (!uniform.startsWith(prefix))
        {
            return -1;
        }
        else
        {
            int parsedIndex = uniform.charAt(prefix.length()) - 48;
            return parsedIndex >= minIndex && parsedIndex <= maxIndex ? parsedIndex : -1;
        }
    }

    public static int getShadowDepthIndex(String uniform)
    {
        return uniform.equals("shadow") ? 0 : (uniform.equals("watershadow") ? 1 : getIndex(uniform, "shadowtex", 0, 1));
    }

    public static int getShadowColorIndex(String uniform)
    {
        return uniform.equals("shadowcolor") ? 0 : getIndex(uniform, "shadowcolor", 0, 1);
    }

    public static int getDepthIndex(String uniform)
    {
        return getIndex(uniform, "depthtex", 0, 2);
    }

    public static int getColorIndex(String uniform)
    {
        int gauxIndex = getIndex(uniform, "gaux", 1, 4);
        return gauxIndex > 0 ? gauxIndex + 3 : getIndex(uniform, "colortex", 4, 7);
    }

    public static boolean isDeferred(String filename)
    {
        return PATTERN_DEFERRED_FSH.matcher(filename).matches();
    }

    public static boolean isComposite(String filename)
    {
        return PATTERN_COMPOSITE_FSH.matcher(filename).matches();
    }

    public static boolean isFinal(String filename)
    {
        return PATTERN_FINAL_FSH.matcher(filename).matches();
    }

    public static boolean isValidDrawBuffers(String str)
    {
        return PATTERN_DRAW_BUFFERS.matcher(str).matches();
    }
}
