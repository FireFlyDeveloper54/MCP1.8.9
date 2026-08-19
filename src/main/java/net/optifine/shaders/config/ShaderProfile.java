package net.optifine.shaders.config;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class ShaderProfile
{
    private String name = null;
    private Map<String, String> mapOptionValues = new LinkedHashMap();
    private Set<String> disabledPrograms = new LinkedHashSet();

    public ShaderProfile(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return this.name;
    }

    public void addOptionValue(String option, String value)
    {
        this.mapOptionValues.put(option, value);
    }

    public void addOptionValues(ShaderProfile prof)
    {
        if (prof != null)
        {
            this.mapOptionValues.putAll(prof.mapOptionValues);
        }
    }

    public void applyOptionValues(ShaderOption[] options)
    {
        for (int optionIndex = 0; optionIndex < options.length; ++optionIndex)
        {
            ShaderOption shaderOption = options[optionIndex];
            String optionName = shaderOption.getName();
            String optionValue = (String)this.mapOptionValues.get(optionName);

            if (optionValue != null)
            {
                shaderOption.setValue(optionValue);
            }
        }
    }

    public String[] getOptions()
    {
        Set<String> optionNameSet = this.mapOptionValues.keySet();
        String[] optionNames = (String[])((String[])optionNameSet.toArray(new String[optionNameSet.size()]));
        return optionNames;
    }

    public String getValue(String key)
    {
        return (String)this.mapOptionValues.get(key);
    }

    public void addDisabledProgram(String program)
    {
        this.disabledPrograms.add(program);
    }

    public void removeDisabledProgram(String program)
    {
        this.disabledPrograms.remove(program);
    }

    public Collection<String> getDisabledPrograms()
    {
        return new LinkedHashSet(this.disabledPrograms);
    }

    public void addDisabledPrograms(Collection<String> programs)
    {
        this.disabledPrograms.addAll(programs);
    }

    public boolean isProgramDisabled(String program)
    {
        return this.disabledPrograms.contains(program);
    }
}
