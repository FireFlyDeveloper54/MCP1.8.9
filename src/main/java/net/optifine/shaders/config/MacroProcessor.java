package net.optifine.shaders.config;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import net.minecraft.src.Config;
import net.optifine.shaders.Shaders;

public class MacroProcessor
{
    public static InputStream process(InputStream in, String path) throws IOException
    {
        String shaderSource = Config.readInputStream(in, "ASCII");
        String macroHeader = getMacroHeader(shaderSource);

        if (!macroHeader.isEmpty())
        {
            shaderSource = macroHeader + shaderSource;

            if (Shaders.saveFinalShaders)
            {
                String preprocessedPath = path.replace(':', '/') + ".pre";
                Shaders.saveShader(preprocessedPath, shaderSource);
            }

            shaderSource = process(shaderSource);
        }

        if (Shaders.saveFinalShaders)
        {
            String shaderPath = path.replace(':', '/');
            Shaders.saveShader(shaderPath, shaderSource);
        }

        byte[] shaderBytes = shaderSource.getBytes("ASCII");
        ByteArrayInputStream shaderInputStream = new ByteArrayInputStream(shaderBytes);
        return shaderInputStream;
    }

    public static String process(String strIn) throws IOException
    {
        StringReader stringReader = new StringReader(strIn);
        BufferedReader bufferedReader = new BufferedReader(stringReader);
        MacroState macroState = new MacroState();
        StringBuilder output = new StringBuilder();

        while (true)
        {
            String line = bufferedReader.readLine();

            if (line == null)
            {
                line = output.toString();
                return line;
            }

            if (macroState.processLine(line) && !MacroState.isMacroLine(line))
            {
                output.append(line);
                output.append("\n");
            }
        }
    }

    private static String getMacroHeader(String str) throws IOException
    {
        StringBuilder macroHeader = new StringBuilder();
        List<ShaderOption> macroOptions = null;
        List<ShaderMacro> remainingMacros = null;
        StringReader stringReader = new StringReader(str);
        BufferedReader bufferedReader = new BufferedReader(stringReader);

        while (true)
        {
            String line = bufferedReader.readLine();

            if (line == null)
            {
                return macroHeader.toString();
            }

            if (MacroState.isMacroLine(line))
            {
                if (macroHeader.length() == 0)
                {
                    macroHeader.append(ShaderMacros.getFixedMacroLines());
                }

                if (remainingMacros == null)
                {
                    remainingMacros = new ArrayList(Arrays.asList(ShaderMacros.getExtensions()));
                }

                Iterator iterator = remainingMacros.iterator();

                while (iterator.hasNext())
                {
                    ShaderMacro shaderMacro = (ShaderMacro)iterator.next();

                    if (line.contains(shaderMacro.getName()))
                    {
                        macroHeader.append(shaderMacro.getSourceLine());
                        macroHeader.append("\n");
                        iterator.remove();
                    }
                }
            }
        }
    }

    private static List<ShaderOption> getMacroOptions()
    {
        List<ShaderOption> macroOptions = new ArrayList();
        ShaderOption[] shaderOptions = Shaders.getShaderPackOptions();

        for (int optionIndex = 0; optionIndex < shaderOptions.length; ++optionIndex)
        {
            ShaderOption shaderOption = shaderOptions[optionIndex];
            String sourceLine = shaderOption.getSourceLine();

            if (sourceLine != null && sourceLine.startsWith("#"))
            {
                macroOptions.add(shaderOption);
            }
        }

        return macroOptions;
    }
}
