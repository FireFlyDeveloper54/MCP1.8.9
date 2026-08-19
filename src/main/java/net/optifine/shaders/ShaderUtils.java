package net.optifine.shaders;

import net.minecraft.src.Config;
import net.optifine.shaders.config.ShaderOption;
import net.optifine.shaders.config.ShaderProfile;

public class ShaderUtils
{
    public static ShaderOption getShaderOption(String name, ShaderOption[] opts)
    {
        if (opts == null)
        {
            return null;
        }
        else
        {
            for (int optionIndex = 0; optionIndex < opts.length; ++optionIndex)
            {
                ShaderOption shaderOption = opts[optionIndex];

                if (shaderOption.getName().equals(name))
                {
                    return shaderOption;
                }
            }

            return null;
        }
    }

    public static ShaderProfile detectProfile(ShaderProfile[] profs, ShaderOption[] opts, boolean def)
    {
        if (profs == null)
        {
            return null;
        }
        else
        {
            for (int profileIndex = 0; profileIndex < profs.length; ++profileIndex)
            {
                ShaderProfile shaderProfile = profs[profileIndex];

                if (matchProfile(shaderProfile, opts, def))
                {
                    return shaderProfile;
                }
            }

            return null;
        }
    }

    public static boolean matchProfile(ShaderProfile prof, ShaderOption[] opts, boolean def)
    {
        if (prof == null)
        {
            return false;
        }
        else if (opts == null)
        {
            return false;
        }
        else
        {
            String[] profileOptionNames = prof.getOptions();

            for (int optionIndex = 0; optionIndex < profileOptionNames.length; ++optionIndex)
            {
                String profileOptionName = profileOptionNames[optionIndex];
                ShaderOption shaderOption = getShaderOption(profileOptionName, opts);

                if (shaderOption != null)
                {
                    String currentValue = def ? shaderOption.getValueDefault() : shaderOption.getValue();
                    String profileValue = prof.getValue(profileOptionName);

                    if (!Config.equals(currentValue, profileValue))
                    {
                        return false;
                    }
                }
            }

            return true;
        }
    }
}
