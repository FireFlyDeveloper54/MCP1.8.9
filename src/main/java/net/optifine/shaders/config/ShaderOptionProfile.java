package net.optifine.shaders.config;

import java.util.ArrayList;
import java.util.List;
import net.optifine.Lang;
import net.optifine.shaders.ShaderUtils;
import net.optifine.shaders.Shaders;

public class ShaderOptionProfile extends ShaderOption
{
    private ShaderProfile[] profiles = null;
    private ShaderOption[] options = null;
    private static final String NAME_PROFILE = "<profile>";
    private static final String VALUE_CUSTOM = "<custom>";

    public ShaderOptionProfile(ShaderProfile[] profiles, ShaderOption[] options)
    {
        super("<profile>", "", detectProfileName(profiles, options), getProfileNames(profiles), detectProfileName(profiles, options, true), (String)null);
        this.profiles = profiles;
        this.options = options;
    }

    public void nextValue()
    {
        super.nextValue();

        if (this.getValue().equals("<custom>"))
        {
            super.nextValue();
        }

        this.applyProfileOptions();
    }

    public void updateProfile()
    {
        ShaderProfile shaderProfile = this.getProfile(this.getValue());

        if (shaderProfile == null || !ShaderUtils.matchProfile(shaderProfile, this.options, false))
        {
            String profileName = detectProfileName(this.profiles, this.options);
            this.setValue(profileName);
        }
    }

    private void applyProfileOptions()
    {
        ShaderProfile shaderProfile = this.getProfile(this.getValue());

        if (shaderProfile != null)
        {
            String[] optionNames = shaderProfile.getOptions();

            for (int optionIndex = 0; optionIndex < optionNames.length; ++optionIndex)
            {
                String optionName = optionNames[optionIndex];
                ShaderOption shaderOption = this.getOption(optionName);

                if (shaderOption != null)
                {
                    String optionValue = shaderProfile.getValue(optionName);
                    shaderOption.setValue(optionValue);
                }
            }
        }
    }

    private ShaderOption getOption(String name)
    {
        for (int optionIndex = 0; optionIndex < this.options.length; ++optionIndex)
        {
            ShaderOption shaderOption = this.options[optionIndex];

            if (shaderOption.getName().equals(name))
            {
                return shaderOption;
            }
        }

        return null;
    }

    private ShaderProfile getProfile(String name)
    {
        for (int profileIndex = 0; profileIndex < this.profiles.length; ++profileIndex)
        {
            ShaderProfile shaderProfile = this.profiles[profileIndex];

            if (shaderProfile.getName().equals(name))
            {
                return shaderProfile;
            }
        }

        return null;
    }

    public String getNameText()
    {
        return Lang.get("of.shaders.profile");
    }

    public String getValueText(String val)
    {
        return val.equals("<custom>") ? Lang.get("of.general.custom", "<custom>") : Shaders.translate("profile." + val, val);
    }

    public String getValueColor(String val)
    {
        return val.equals("<custom>") ? "\u00a7c" : "\u00a7a";
    }

    public String getDescriptionText()
    {
        String profileComment = Shaders.translate("profile.comment", (String)null);

        if (profileComment != null)
        {
            return profileComment;
        }
        else
        {
            StringBuffer description = new StringBuffer();

            for (int profileIndex = 0; profileIndex < this.profiles.length; ++profileIndex)
            {
                String profileName = this.profiles[profileIndex].getName();

                if (profileName != null)
                {
                    String profileSpecificComment = Shaders.translate("profile." + profileName + ".comment", (String)null);

                    if (profileSpecificComment != null)
                    {
                        description.append(profileSpecificComment);

                        if (!profileSpecificComment.endsWith(". "))
                        {
                            description.append(". ");
                        }
                    }
                }
            }

            return description.toString();
        }
    }

    private static String detectProfileName(ShaderProfile[] profs, ShaderOption[] opts)
    {
        return detectProfileName(profs, opts, false);
    }

    private static String detectProfileName(ShaderProfile[] profs, ShaderOption[] opts, boolean def)
    {
        ShaderProfile shaderProfile = ShaderUtils.detectProfile(profs, opts, def);
        return shaderProfile == null ? "<custom>" : shaderProfile.getName();
    }

    private static String[] getProfileNames(ShaderProfile[] profs)
    {
        List<String> profileNames = new ArrayList();

        for (int profileIndex = 0; profileIndex < profs.length; ++profileIndex)
        {
            ShaderProfile shaderProfile = profs[profileIndex];
            profileNames.add(shaderProfile.getName());
        }

        profileNames.add("<custom>");
        String[] profileNameArray = (String[])((String[])profileNames.toArray(new String[profileNames.size()]));
        return profileNameArray;
    }
}
