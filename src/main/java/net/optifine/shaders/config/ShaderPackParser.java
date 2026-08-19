package net.optifine.shaders.config;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.src.Config;
import net.optifine.expr.ExpressionFloatArrayCached;
import net.optifine.expr.ExpressionFloatCached;
import net.optifine.expr.ExpressionParser;
import net.optifine.expr.ExpressionType;
import net.optifine.expr.IExpression;
import net.optifine.expr.IExpressionBool;
import net.optifine.expr.IExpressionFloat;
import net.optifine.expr.IExpressionFloatArray;
import net.optifine.expr.ParseException;
import net.optifine.render.GlAlphaState;
import net.optifine.render.GlBlendState;
import net.optifine.shaders.IShaderPack;
import net.optifine.shaders.Program;
import net.optifine.shaders.SMCLog;
import net.optifine.shaders.ShaderUtils;
import net.optifine.shaders.Shaders;
import net.optifine.shaders.uniform.CustomUniform;
import net.optifine.shaders.uniform.CustomUniforms;
import net.optifine.shaders.uniform.ShaderExpressionResolver;
import net.optifine.shaders.uniform.UniformType;
import net.optifine.util.StrUtils;

public class ShaderPackParser
{
    private static final Pattern PATTERN_VERSION = Pattern.compile("^\\s*#version\\s+.*$");
    private static final Pattern PATTERN_INCLUDE = Pattern.compile("^\\s*#include\\s+\"([A-Za-z0-9_/\\.]+)\".*$");
    private static final Set<String> setConstNames = makeSetConstNames();
    private static final Map<String, Integer> mapAlphaFuncs = makeMapAlphaFuncs();
    private static final Map<String, Integer> mapBlendFactors = makeMapBlendFactors();

    public static ShaderOption[] parseShaderPackOptions(IShaderPack shaderPack, String[] programNames, List<Integer> listDimensions)
    {
        if (shaderPack == null)
        {
            return new ShaderOption[0];
        }
        else
        {
            Map<String, ShaderOption> optionMap = new HashMap();
            collectShaderOptions(shaderPack, "/shaders", programNames, optionMap);
            Iterator<Integer> iterator = listDimensions.iterator();

            while (iterator.hasNext())
            {
                int dimensionId = ((Integer)iterator.next()).intValue();
                String worldDir = "/shaders/world" + dimensionId;
                collectShaderOptions(shaderPack, worldDir, programNames, optionMap);
            }

            Collection<ShaderOption> collection = optionMap.values();
            ShaderOption[] shaderOptions = (ShaderOption[])((ShaderOption[])collection.toArray(new ShaderOption[collection.size()]));
            Comparator<ShaderOption> comparator = new Comparator<ShaderOption>()
            {
                public int compare(ShaderOption shaderOption, ShaderOption shaderOption2)
                {
                    return shaderOption.getName().compareToIgnoreCase(shaderOption2.getName());
                }
            };
            Arrays.sort(shaderOptions, comparator);
            return shaderOptions;
        }
    }

    private static void collectShaderOptions(IShaderPack shaderPack, String dir, String[] programNames, Map<String, ShaderOption> mapOptions)
    {
        for (int programIndex = 0; programIndex < programNames.length; ++programIndex)
        {
            String programName = programNames[programIndex];

            if (!programName.equals(""))
            {
                String vertexPath = dir + "/" + programName + ".vsh";
                String fragmentPath = dir + "/" + programName + ".fsh";
                collectShaderOptions(shaderPack, vertexPath, mapOptions);
                collectShaderOptions(shaderPack, fragmentPath, mapOptions);
            }
        }
    }

    private static void collectShaderOptions(IShaderPack sp, String path, Map<String, ShaderOption> mapOptions)
    {
        String[] lines = getLines(sp, path);

        for (int lineIndex = 0; lineIndex < lines.length; ++lineIndex)
        {
            String line = lines[lineIndex];
            ShaderOption shaderOption = getShaderOption(line, path);

            if (shaderOption != null && !shaderOption.getName().startsWith(ShaderMacros.getPrefixMacro()) && (!shaderOption.checkUsed() || isOptionUsed(shaderOption, lines)))
            {
                String optionName = shaderOption.getName();
                ShaderOption existingOption = (ShaderOption)mapOptions.get(optionName);

                if (existingOption != null)
                {
                    if (!Config.equals(existingOption.getValueDefault(), shaderOption.getValueDefault()))
                    {
                        Config.warn("Ambiguous shader option: " + shaderOption.getName());
                        Config.warn(" - in " + Config.arrayToString((Object[])existingOption.getPaths()) + ": " + existingOption.getValueDefault());
                        Config.warn(" - in " + Config.arrayToString((Object[])shaderOption.getPaths()) + ": " + shaderOption.getValueDefault());
                        existingOption.setEnabled(false);
                    }

                    if (existingOption.getDescription() == null || existingOption.getDescription().length() <= 0)
                    {
                        existingOption.setDescription(shaderOption.getDescription());
                    }

                    existingOption.addPaths(shaderOption.getPaths());
                }
                else
                {
                    mapOptions.put(optionName, shaderOption);
                }
            }
        }
    }

    private static boolean isOptionUsed(ShaderOption so, String[] lines)
    {
        for (int lineIndex = 0; lineIndex < lines.length; ++lineIndex)
        {
            String line = lines[lineIndex];

            if (so.isUsedInLine(line))
            {
                return true;
            }
        }

        return false;
    }

    private static String[] getLines(IShaderPack sp, String path)
    {
        try
        {
            List<String> includeFiles = new ArrayList();
            String source = loadFile(path, sp, 0, includeFiles, 0);

            if (source == null)
            {
                return new String[0];
            }
            else
            {
                ByteArrayInputStream byteInputStream = new ByteArrayInputStream(source.getBytes());
                String[] lines = Config.readLines((InputStream)byteInputStream);
                return lines;
            }
        }
        catch (IOException iOException)
        {
            Config.dbg(iOException.getClass().getName() + ": " + iOException.getMessage());
            return new String[0];
        }
    }

    private static ShaderOption getShaderOption(String line, String path)
    {
        ShaderOption shaderOption = null;

        if (shaderOption == null)
        {
            shaderOption = ShaderOptionSwitch.parseOption(line, path);
        }

        if (shaderOption == null)
        {
            shaderOption = ShaderOptionVariable.parseOption(line, path);
        }

        if (shaderOption != null)
        {
            return shaderOption;
        }
        else
        {
            if (shaderOption == null)
            {
                shaderOption = ShaderOptionSwitchConst.parseOption(line, path);
            }

            if (shaderOption == null)
            {
                shaderOption = ShaderOptionVariableConst.parseOption(line, path);
            }

            return shaderOption != null && setConstNames.contains(shaderOption.getName()) ? shaderOption : null;
        }
    }

    private static Set<String> makeSetConstNames()
    {
        Set<String> set = new HashSet();
        set.add("shadowMapResolution");
        set.add("shadowMapFov");
        set.add("shadowDistance");
        set.add("shadowDistanceRenderMul");
        set.add("shadowIntervalSize");
        set.add("generateShadowMipmap");
        set.add("generateShadowColorMipmap");
        set.add("shadowHardwareFiltering");
        set.add("shadowHardwareFiltering0");
        set.add("shadowHardwareFiltering1");
        set.add("shadowtex0Mipmap");
        set.add("shadowtexMipmap");
        set.add("shadowtex1Mipmap");
        set.add("shadowcolor0Mipmap");
        set.add("shadowColor0Mipmap");
        set.add("shadowcolor1Mipmap");
        set.add("shadowColor1Mipmap");
        set.add("shadowtex0Nearest");
        set.add("shadowtexNearest");
        set.add("shadow0MinMagNearest");
        set.add("shadowtex1Nearest");
        set.add("shadow1MinMagNearest");
        set.add("shadowcolor0Nearest");
        set.add("shadowColor0Nearest");
        set.add("shadowColor0MinMagNearest");
        set.add("shadowcolor1Nearest");
        set.add("shadowColor1Nearest");
        set.add("shadowColor1MinMagNearest");
        set.add("wetnessHalflife");
        set.add("drynessHalflife");
        set.add("eyeBrightnessHalflife");
        set.add("centerDepthHalflife");
        set.add("sunPathRotation");
        set.add("ambientOcclusionLevel");
        set.add("superSamplingLevel");
        set.add("noiseTextureResolution");
        return set;
    }

    public static ShaderProfile[] parseProfiles(Properties props, ShaderOption[] shaderOptions)
    {
        String profilePrefix = "profile.";
        List<ShaderProfile> profiles = new ArrayList();

        for (Object o : props.keySet())
        {
            String propertyKey = (String) o;
            if (propertyKey.startsWith(profilePrefix))
            {
                String profileName = propertyKey.substring(profilePrefix.length());
                props.getProperty(propertyKey);
                Set<String> set = new HashSet();
                ShaderProfile shaderProfile = parseProfile(profileName, props, set, shaderOptions);

                if (shaderProfile != null)
                {
                    profiles.add(shaderProfile);
                }
            }
        }

        if (profiles.size() <= 0)
        {
            return null;
        }
        else
        {
            ShaderProfile[] shaderProfileArray = (ShaderProfile[])((ShaderProfile[])profiles.toArray(new ShaderProfile[profiles.size()]));
            return shaderProfileArray;
        }
    }

    public static Map<String, IExpressionBool> parseProgramConditions(Properties props, ShaderOption[] shaderOptions)
    {
        String programPrefix = "program.";
        Pattern pattern = Pattern.compile("program\\.([^.]+)\\.enabled");
        Map<String, IExpressionBool> programConditions = new HashMap();

        for (Object o : props.keySet())
        {
            String propertyKey = (String) o;
            Matcher matcher = pattern.matcher(propertyKey);

            if (matcher.matches())
            {
                String programName = matcher.group(1);
                String expressionText = props.getProperty(propertyKey).trim();
                IExpressionBool condition = parseOptionExpression(expressionText, shaderOptions);

                if (condition == null)
                {
                    SMCLog.severe("Error parsing program condition: " + propertyKey);
                }
                else
                {
                    programConditions.put(programName, condition);
                }
            }
        }

        return programConditions;
    }

    private static IExpressionBool parseOptionExpression(String val, ShaderOption[] shaderOptions)
    {
        try
        {
            ShaderOptionResolver shaderOptionResolver = new ShaderOptionResolver(shaderOptions);
            ExpressionParser expressionParser = new ExpressionParser(shaderOptionResolver);
            IExpressionBool expressionBool = expressionParser.parseBool(val);
            return expressionBool;
        }
        catch (ParseException parseException)
        {
            SMCLog.warning(parseException.getClass().getName() + ": " + parseException.getMessage());
            return null;
        }
    }

    public static Set<String> parseOptionSliders(Properties props, ShaderOption[] shaderOptions)
    {
        Set<String> sliderOptions = new HashSet();
        String sliders = props.getProperty("sliders");

        if (sliders == null)
        {
            return sliderOptions;
        }
        else
        {
            String[] optionNames = Config.tokenize(sliders, " ");

            for (int optionIndex = 0; optionIndex < optionNames.length; ++optionIndex)
            {
                String optionName = optionNames[optionIndex];
                ShaderOption shaderOption = ShaderUtils.getShaderOption(optionName, shaderOptions);

                if (shaderOption == null)
                {
                    Config.warn("Invalid shader option: " + optionName);
                }
                else
                {
                    sliderOptions.add(optionName);
                }
            }

            return sliderOptions;
        }
    }

    private static ShaderProfile parseProfile(String name, Properties props, Set<String> parsedProfiles, ShaderOption[] shaderOptions)
    {
        String profilePrefix = "profile.";
        String profileKey = profilePrefix + name;

        if (parsedProfiles.contains(profileKey))
        {
            Config.warn("[Shaders] Profile already parsed: " + name);
            return null;
        }
        else
        {
            parsedProfiles.add(name);
            ShaderProfile shaderProfile = new ShaderProfile(name);
            String profileProperty = props.getProperty(profileKey);
            String[] profileTokens = Config.tokenize(profileProperty, " ");

            for (int tokenIndex = 0; tokenIndex < profileTokens.length; ++tokenIndex)
            {
                String token = profileTokens[tokenIndex];

                if (token.startsWith(profilePrefix))
                {
                    String includedProfileName = token.substring(profilePrefix.length());
                    ShaderProfile includedProfile = parseProfile(includedProfileName, props, parsedProfiles, shaderOptions);

                    if (shaderProfile != null)
                    {
                        shaderProfile.addOptionValues(includedProfile);
                        shaderProfile.addDisabledPrograms(includedProfile.getDisabledPrograms());
                    }
                }
                else
                {
                    String[] optionTokens = Config.tokenize(token, ":=");

                    if (optionTokens.length == 1)
                    {
                        String optionName = optionTokens[0];
                        boolean enabled = true;

                        if (optionName.startsWith("!"))
                        {
                            enabled = false;
                            optionName = optionName.substring(1);
                        }

                        String programPrefix = "program.";

                        if (optionName.startsWith(programPrefix))
                        {
                            String programName = optionName.substring(programPrefix.length());

                            if (!Shaders.isProgramPath(programName))
                            {
                                Config.warn("Invalid program: " + programName + " in profile: " + shaderProfile.getName());
                            }
                            else if (enabled)
                            {
                                shaderProfile.removeDisabledProgram(programName);
                            }
                            else
                            {
                                shaderProfile.addDisabledProgram(programName);
                            }
                        }
                        else
                        {
                            ShaderOption shaderOptionSwitch = ShaderUtils.getShaderOption(optionName, shaderOptions);

                            if (!(shaderOptionSwitch instanceof ShaderOptionSwitch))
                            {
                                Config.warn("[Shaders] Invalid option: " + optionName);
                            }
                            else
                            {
                                shaderProfile.addOptionValue(optionName, String.valueOf(enabled));
                                shaderOptionSwitch.setVisible(true);
                            }
                        }
                    }
                    else if (optionTokens.length != 2)
                    {
                        Config.warn("[Shaders] Invalid option value: " + token);
                    }
                    else
                    {
                        String optionName = optionTokens[0];
                        String optionValue = optionTokens[1];
                        ShaderOption shaderOption = ShaderUtils.getShaderOption(optionName, shaderOptions);

                        if (shaderOption == null)
                        {
                            Config.warn("[Shaders] Invalid option: " + token);
                        }
                        else if (!shaderOption.isValidValue(optionValue))
                        {
                            Config.warn("[Shaders] Invalid value: " + token);
                        }
                        else
                        {
                            shaderOption.setVisible(true);
                            shaderProfile.addOptionValue(optionName, optionValue);
                        }
                    }
                }
            }

            return shaderProfile;
        }
    }

    public static Map<String, ScreenShaderOptions> parseGuiScreens(Properties props, ShaderProfile[] shaderProfiles, ShaderOption[] shaderOptions)
    {
        Map<String, ScreenShaderOptions> screenOptionsMap = new HashMap();
        parseGuiScreen("screen", props, screenOptionsMap, shaderProfiles, shaderOptions);
        return screenOptionsMap.isEmpty() ? null : screenOptionsMap;
    }

    private static boolean parseGuiScreen(String key, Properties props, Map<String, ScreenShaderOptions> screenOptionsMap, ShaderProfile[] shaderProfiles, ShaderOption[] shaderOptions)
    {
        String propertyValue = props.getProperty(key);

        if (propertyValue == null)
        {
            return false;
        }
        else
        {
            List<ShaderOption> options = new ArrayList();
            Set<String> seenOptions = new HashSet();
            String[] optionKeys = Config.tokenize(propertyValue, " ");

            for (int optionIndex = 0; optionIndex < optionKeys.length; ++optionIndex)
            {
                String optionKey = optionKeys[optionIndex];

                if (optionKey.equals("<empty>"))
                {
                    options.add((ShaderOption)null);
                }
                else if (seenOptions.contains(optionKey))
                {
                    Config.warn("[Shaders] Duplicate option: " + optionKey + ", key: " + key);
                }
                else
                {
                    seenOptions.add(optionKey);

                    if (optionKey.equals("<profile>"))
                    {
                        if (shaderProfiles == null)
                        {
                            Config.warn("[Shaders] Option profile can not be used, no profiles defined: " + optionKey + ", key: " + key);
                        }
                        else
                        {
                            ShaderOptionProfile shaderOptionProfile = new ShaderOptionProfile(shaderProfiles, shaderOptions);
                            options.add(shaderOptionProfile);
                        }
                    }
                    else if (optionKey.equals("*"))
                    {
                        ShaderOption restOption = new ShaderOptionRest("<rest>");
                        options.add(restOption);
                    }
                    else if (optionKey.startsWith("[") && optionKey.endsWith("]"))
                    {
                        String screenName = StrUtils.removePrefixSuffix(optionKey, "[", "]");

                        if (!screenName.matches("^[a-zA-Z0-9_]+$"))
                        {
                            Config.warn("[Shaders] Invalid screen: " + optionKey + ", key: " + key);
                        }
                        else if (!parseGuiScreen("screen." + screenName, props, screenOptionsMap, shaderProfiles, shaderOptions))
                        {
                            Config.warn("[Shaders] Invalid screen: " + optionKey + ", key: " + key);
                        }
                        else
                        {
                            ShaderOptionScreen shaderOptionScreen = new ShaderOptionScreen(screenName);
                            options.add(shaderOptionScreen);
                        }
                    }
                    else
                    {
                        ShaderOption shaderOption = ShaderUtils.getShaderOption(optionKey, shaderOptions);

                        if (shaderOption == null)
                        {
                            Config.warn("[Shaders] Invalid option: " + optionKey + ", key: " + key);
                            options.add((ShaderOption)null);
                        }
                        else
                        {
                            shaderOption.setVisible(true);
                            options.add(shaderOption);
                        }
                    }
                }
            }

            ShaderOption[] optionArray = (ShaderOption[])((ShaderOption[])options.toArray(new ShaderOption[options.size()]));
            String columnsText = props.getProperty(key + ".columns");
            int columns = Config.parseInt(columnsText, 2);
            ScreenShaderOptions screenOptions = new ScreenShaderOptions(key, optionArray, columns);
            screenOptionsMap.put(key, screenOptions);
            return true;
        }
    }

    public static BufferedReader resolveIncludes(BufferedReader reader, String filePath, IShaderPack shaderPack, int fileIndex, List<String> listFiles, int includeLevel) throws IOException
    {
        String parentDir = "/";
        int slashIndex = filePath.lastIndexOf("/");

        if (slashIndex >= 0)
        {
            parentDir = filePath.substring(0, slashIndex);
        }

        CharArrayWriter outputWriter = new CharArrayWriter();
        int macroInsertPosition = -1;
        Set<ShaderMacro> usedMacros = new LinkedHashSet();
        int lineNumber = 1;

        while (true)
        {
            String line = reader.readLine();

            if (line == null)
            {
                char[] outputChars = outputWriter.toCharArray();

                if (macroInsertPosition >= 0 && usedMacros.size() > 0)
                {
                    StringBuilder macroHeader = new StringBuilder();

                    for (ShaderMacro shaderMacro : usedMacros)
                    {
                        macroHeader.append("#define ");
                        macroHeader.append(shaderMacro.getName());
                        macroHeader.append(" ");
                        macroHeader.append(shaderMacro.getValue());
                        macroHeader.append("\n");
                    }

                    String macroHeaderText = macroHeader.toString();
                    StringBuilder outputBuilder = new StringBuilder(new String(outputChars));
                    outputBuilder.insert(macroInsertPosition, macroHeaderText);
                    String outputText = outputBuilder.toString();
                    outputChars = outputText.toCharArray();
                }

                CharArrayReader outputReader = new CharArrayReader(outputChars);
                return new BufferedReader(outputReader);
            }

            if (macroInsertPosition < 0)
            {
                Matcher versionMatcher = PATTERN_VERSION.matcher(line);

                if (versionMatcher.matches())
                {
                    String macroLines = ShaderMacros.getFixedMacroLines() + ShaderMacros.getOptionMacroLines();
                    String versionBlock = line + "\n" + macroLines;
                    String lineDirective = "#line " + (lineNumber + 1) + " " + fileIndex;
                    line = versionBlock + lineDirective;
                    macroInsertPosition = outputWriter.size() + versionBlock.length();
                }
            }

            Matcher includeMatcher = PATTERN_INCLUDE.matcher(line);

            if (includeMatcher.matches())
            {
                String includePath = includeMatcher.group(1);
                boolean absoluteInclude = includePath.startsWith("/");
                String resolvedIncludePath = absoluteInclude ? "/shaders" + includePath : parentDir + "/" + includePath;

                if (!listFiles.contains(resolvedIncludePath))
                {
                    listFiles.add(resolvedIncludePath);
                }

                int includeFileIndex = listFiles.indexOf(resolvedIncludePath) + 1;
                line = loadFile(resolvedIncludePath, shaderPack, includeFileIndex, listFiles, includeLevel);

                if (line == null)
                {
                    throw new IOException("Included file not found: " + filePath);
                }

                if (line.endsWith("\n"))
                {
                    line = line.substring(0, line.length() - 1);
                }

                String includeLineDirective = "#line 1 " + includeFileIndex + "\n";

                if (line.startsWith("#version "))
                {
                    includeLineDirective = "";
                }

                line = includeLineDirective + line + "\n" + "#line " + (lineNumber + 1) + " " + fileIndex;
            }

            if (macroInsertPosition >= 0 && line.contains(ShaderMacros.getPrefixMacro()))
            {
                ShaderMacro[] lineMacros = findMacros(line, ShaderMacros.getExtensions());

                for (int macroIndex = 0; macroIndex < lineMacros.length; ++macroIndex)
                {
                    ShaderMacro lineMacro = lineMacros[macroIndex];
                    usedMacros.add(lineMacro);
                }
            }

            outputWriter.write(line);
            outputWriter.write("\n");
            ++lineNumber;
        }
    }

    private static ShaderMacro[] findMacros(String line, ShaderMacro[] macros)
    {
        List<ShaderMacro> foundMacros = new ArrayList();

        for (int macroIndex = 0; macroIndex < macros.length; ++macroIndex)
        {
            ShaderMacro shaderMacro = macros[macroIndex];

            if (line.contains(shaderMacro.getName()))
            {
                foundMacros.add(shaderMacro);
            }
        }

        ShaderMacro[] macroArray = (ShaderMacro[])foundMacros.toArray(new ShaderMacro[foundMacros.size()]);
        return macroArray;
    }

    private static String loadFile(String filePath, IShaderPack shaderPack, int fileIndex, List<String> listFiles, int includeLevel) throws IOException
    {
        if (includeLevel >= 10)
        {
            throw new IOException("#include depth exceeded: " + includeLevel + ", file: " + filePath);
        }
        else
        {
            ++includeLevel;
            InputStream inputStream = shaderPack.getResourceAsStream(filePath);

            if (inputStream == null)
            {
                return null;
            }
            else
            {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "ASCII");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                bufferedReader = resolveIncludes(bufferedReader, filePath, shaderPack, fileIndex, listFiles, includeLevel);
                CharArrayWriter outputWriter = new CharArrayWriter();

                while (true)
                {
                    String line = bufferedReader.readLine();

                    if (line == null)
                    {
                        return outputWriter.toString();
                    }

                    outputWriter.write(line);
                    outputWriter.write("\n");
                }
            }
        }
    }

    public static CustomUniforms parseCustomUniforms(Properties props)
    {
        String uniformPrefix = "uniform";
        String variablePrefix = "variable";
        String uniformKeyPrefix = uniformPrefix + ".";
        String variableKeyPrefix = variablePrefix + ".";
        Map<String, IExpression> expressions = new HashMap();
        List<CustomUniform> uniforms = new ArrayList();

        for (Object o : props.keySet())
        {
            String propertyKey = (String) o;
            String[] keyParts = Config.tokenize(propertyKey, ".");

            if (keyParts.length == 3)
            {
                String kind = keyParts[0];
                String type = keyParts[1];
                String name = keyParts[2];
                String source = props.getProperty(propertyKey).trim();

                if (expressions.containsKey(name))
                {
                    SMCLog.warning("Expression already defined: " + name);
                }
                else if (kind.equals(uniformPrefix) || kind.equals(variablePrefix))
                {
                    SMCLog.info("Custom " + kind + ": " + name);
                    CustomUniform customUniform = parseCustomUniform(kind, name, type, source, expressions);

                    if (customUniform != null)
                    {
                        expressions.put(name, customUniform.getExpression());

                        if (!kind.equals(variablePrefix))
                        {
                            uniforms.add(customUniform);
                        }
                    }
                }
            }
        }

        if (uniforms.size() <= 0)
        {
            return null;
        }
        else
        {
            CustomUniform[] customUniformArray = (CustomUniform[])((CustomUniform[])uniforms.toArray(new CustomUniform[uniforms.size()]));
            CustomUniforms customUniforms = new CustomUniforms(customUniformArray, expressions);
            return customUniforms;
        }
    }

    private static CustomUniform parseCustomUniform(String kind, String name, String type, String src, Map<String, IExpression> mapExpressions)
    {
        try
        {
            UniformType uniformType = UniformType.parse(type);

            if (uniformType == null)
            {
                SMCLog.warning("Unknown " + kind + " type: " + uniformType);
                return null;
            }
            else
            {
                ShaderExpressionResolver expressionResolver = new ShaderExpressionResolver(mapExpressions);
                ExpressionParser expressionParser = new ExpressionParser(expressionResolver);
                IExpression expression = expressionParser.parse(src);
                ExpressionType expressionType = expression.getExpressionType();

                if (!uniformType.matchesExpressionType(expressionType))
                {
                    SMCLog.warning("Expression type does not match " + kind + " type, expression: " + expressionType + ", " + kind + ": " + uniformType + " " + name);
                    return null;
                }
                else
                {
                    expression = makeExpressionCached(expression);
                    CustomUniform customUniform = new CustomUniform(name, uniformType, expression);
                    return customUniform;
                }
            }
        }
        catch (ParseException parseException)
        {
            SMCLog.warning(parseException.getClass().getName() + ": " + parseException.getMessage());
            return null;
        }
    }

    private static IExpression makeExpressionCached(IExpression expr)
    {
        return (IExpression)(expr instanceof IExpressionFloat ? new ExpressionFloatCached((IExpressionFloat)expr) : (expr instanceof IExpressionFloatArray ? new ExpressionFloatArrayCached((IExpressionFloatArray)expr) : expr));
    }

    public static void parseAlphaStates(Properties props)
    {
        for (Object o : props.keySet())
        {
            String propertyKey = (String) o;
            String[] keyParts = Config.tokenize(propertyKey, ".");

            if (keyParts.length == 2)
            {
                String propertyType = keyParts[0];
                String programName = keyParts[1];

                if (propertyType.equals("alphaTest"))
                {
                    Program program = Shaders.getProgram(programName);

                    if (program == null)
                    {
                        SMCLog.severe("Invalid program name: " + programName);
                    }
                    else
                    {
                        String propertyValue = props.getProperty(propertyKey).trim();
                        GlAlphaState glAlphaState = parseAlphaState(propertyValue);

                        if (glAlphaState != null)
                        {
                            program.setAlphaState(glAlphaState);
                        }
                    }
                }
            }
        }
    }

    private static GlAlphaState parseAlphaState(String str)
    {
        String[] tokens = Config.tokenize(str, " ");

        if (tokens.length == 1)
        {
            String alphaMode = tokens[0];

            if (alphaMode.equals("off") || alphaMode.equals("false"))
            {
                return new GlAlphaState(false);
            }
        }
        else if (tokens.length == 2)
        {
            String funcName = tokens[0];
            String refValueText = tokens[1];
            Integer alphaFunc = (Integer)mapAlphaFuncs.get(funcName);
            float refValue = Config.parseFloat(refValueText, -1.0F);

            if (alphaFunc != null && refValue >= 0.0F)
            {
                return new GlAlphaState(true, alphaFunc.intValue(), refValue);
            }
        }

        SMCLog.severe("Invalid alpha test: " + str);
        return null;
    }

    public static void parseBlendStates(Properties props)
    {
        for (Object o : props.keySet())
        {
            String propertyKey = (String) o;
            String[] keyParts = Config.tokenize(propertyKey, ".");

            if (keyParts.length == 2)
            {
                String propertyType = keyParts[0];
                String programName = keyParts[1];

                if (propertyType.equals("blend"))
                {
                    Program program = Shaders.getProgram(programName);

                    if (program == null)
                    {
                        SMCLog.severe("Invalid program name: " + programName);
                    }
                    else
                    {
                        String propertyValue = props.getProperty(propertyKey).trim();
                        GlBlendState glBlendState = parseBlendState(propertyValue);

                        if (glBlendState != null)
                        {
                            program.setBlendState(glBlendState);
                        }
                    }
                }
            }
        }
    }

    private static GlBlendState parseBlendState(String str)
    {
        String[] tokens = Config.tokenize(str, " ");

        if (tokens.length == 1)
        {
            String blendMode = tokens[0];

            if (blendMode.equals("off") || blendMode.equals("false"))
            {
                return new GlBlendState(false);
            }
        }
        else if (tokens.length == 2 || tokens.length == 4)
        {
            String srcFactorName = tokens[0];
            String dstFactorName = tokens[1];
            String srcAlphaFactorName = srcFactorName;
            String dstAlphaFactorName = dstFactorName;

            if (tokens.length == 4)
            {
                srcAlphaFactorName = tokens[2];
                dstAlphaFactorName = tokens[3];
            }

            Integer srcFactor = (Integer)mapBlendFactors.get(srcFactorName);
            Integer dstFactor = (Integer)mapBlendFactors.get(dstFactorName);
            Integer srcAlphaFactor = (Integer)mapBlendFactors.get(srcAlphaFactorName);
            Integer dstAlphaFactor = (Integer)mapBlendFactors.get(dstAlphaFactorName);

            if (srcFactor != null && dstFactor != null && srcAlphaFactor != null && dstAlphaFactor != null)
            {
                return new GlBlendState(true, srcFactor.intValue(), dstFactor.intValue(), srcAlphaFactor.intValue(), dstAlphaFactor.intValue());
            }
        }

        SMCLog.severe("Invalid blend mode: " + str);
        return null;
    }

    public static void parseRenderScales(Properties props)
    {
        for (Object o : props.keySet())
        {
            String propertyKey = (String) o;
            String[] keyParts = Config.tokenize(propertyKey, ".");

            if (keyParts.length == 2)
            {
                String propertyType = keyParts[0];
                String programName = keyParts[1];

                if (propertyType.equals("scale"))
                {
                    Program program = Shaders.getProgram(programName);

                    if (program == null)
                    {
                        SMCLog.severe("Invalid program name: " + programName);
                    }
                    else
                    {
                        String propertyValue = props.getProperty(propertyKey).trim();
                        RenderScale renderScale = parseRenderScale(propertyValue);

                        if (renderScale != null)
                        {
                            program.setRenderScale(renderScale);
                        }
                    }
                }
            }
        }
    }

    private static RenderScale parseRenderScale(String str)
    {
        String[] tokens = Config.tokenize(str, " ");
        float scale = Config.parseFloat(tokens[0], -1.0F);
        float offsetX = 0.0F;
        float offsetY = 0.0F;

        if (tokens.length > 1)
        {
            if (tokens.length != 3)
            {
                SMCLog.severe("Invalid render scale: " + str);
                return null;
            }

            offsetX = Config.parseFloat(tokens[1], -1.0F);
            offsetY = Config.parseFloat(tokens[2], -1.0F);
        }

        if (Config.between(scale, 0.0F, 1.0F) && Config.between(offsetX, 0.0F, 1.0F) && Config.between(offsetY, 0.0F, 1.0F))
        {
            return new RenderScale(scale, offsetX, offsetY);
        }
        else
        {
            SMCLog.severe("Invalid render scale: " + str);
            return null;
        }
    }

    public static void parseBuffersFlip(Properties props)
    {
        for (Object o : props.keySet())
        {
            String propertyKey = (String) o;
            String[] keyParts = Config.tokenize(propertyKey, ".");

            if (keyParts.length == 3)
            {
                String propertyType = keyParts[0];
                String programName = keyParts[1];
                String bufferName = keyParts[2];

                if (propertyType.equals("flip"))
                {
                    Program program = Shaders.getProgram(programName);

                    if (program == null)
                    {
                        SMCLog.severe("Invalid program name: " + programName);
                    }
                    else
                    {
                        Boolean[] buffersFlip = program.getBuffersFlip();
                        int bufferIndex = Shaders.getBufferIndexFromString(bufferName);

                        if (bufferIndex >= 0 && bufferIndex < buffersFlip.length)
                        {
                            String propertyValue = props.getProperty(propertyKey).trim();
                            Boolean flipValue = Config.parseBoolean(propertyValue, (Boolean)null);

                            if (flipValue == null)
                            {
                                SMCLog.severe("Invalid boolean value: " + propertyValue);
                            }
                            else
                            {
                                buffersFlip[bufferIndex] = flipValue;
                            }
                        }
                        else
                        {
                            SMCLog.severe("Invalid buffer name: " + bufferName);
                        }
                    }
                }
            }
        }
    }

    private static Map<String, Integer> makeMapAlphaFuncs()
    {
        Map<String, Integer> map = new HashMap();
        map.put("NEVER", Integer.valueOf(512));
        map.put("LESS", Integer.valueOf(513));
        map.put("EQUAL", Integer.valueOf(514));
        map.put("LEQUAL", Integer.valueOf(515));
        map.put("GREATER", Integer.valueOf(516));
        map.put("NOTEQUAL", Integer.valueOf(517));
        map.put("GEQUAL", Integer.valueOf(518));
        map.put("ALWAYS", Integer.valueOf(519));
        return Collections.<String, Integer>unmodifiableMap(map);
    }

    private static Map<String, Integer> makeMapBlendFactors()
    {
        Map<String, Integer> map = new HashMap();
        map.put("ZERO", Integer.valueOf(0));
        map.put("ONE", Integer.valueOf(1));
        map.put("SRC_COLOR", Integer.valueOf(768));
        map.put("ONE_MINUS_SRC_COLOR", Integer.valueOf(769));
        map.put("DST_COLOR", Integer.valueOf(774));
        map.put("ONE_MINUS_DST_COLOR", Integer.valueOf(775));
        map.put("SRC_ALPHA", Integer.valueOf(770));
        map.put("ONE_MINUS_SRC_ALPHA", Integer.valueOf(771));
        map.put("DST_ALPHA", Integer.valueOf(772));
        map.put("ONE_MINUS_DST_ALPHA", Integer.valueOf(773));
        map.put("CONSTANT_COLOR", Integer.valueOf(32769));
        map.put("ONE_MINUS_CONSTANT_COLOR", Integer.valueOf(32770));
        map.put("CONSTANT_ALPHA", Integer.valueOf(32771));
        map.put("ONE_MINUS_CONSTANT_ALPHA", Integer.valueOf(32772));
        map.put("SRC_ALPHA_SATURATE", Integer.valueOf(776));
        return Collections.<String, Integer>unmodifiableMap(map);
    }
}
