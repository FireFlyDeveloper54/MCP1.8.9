package net.optifine.shaders.config;

import java.util.Map;
import net.minecraft.src.Config;
import net.optifine.expr.ConstantFloat;
import net.optifine.expr.FunctionBool;
import net.optifine.expr.FunctionType;
import net.optifine.expr.IExpression;
import net.optifine.expr.IExpressionResolver;

public class MacroExpressionResolver implements IExpressionResolver
{
    private Map<String, String> mapMacroValues = null;

    public MacroExpressionResolver(Map<String, String> mapMacroValues)
    {
        this.mapMacroValues = mapMacroValues;
    }

    public IExpression getExpression(String name)
    {
        String definedPrefix = "defined_";

        if (name.startsWith(definedPrefix))
        {
            String macroName = name.substring(definedPrefix.length());
            return this.mapMacroValues.containsKey(macroName) ? new FunctionBool(FunctionType.TRUE, (IExpression[])null) : new FunctionBool(FunctionType.FALSE, (IExpression[])null);
        }
        else
        {
            while (this.mapMacroValues.containsKey(name))
            {
                String macroValue = (String)this.mapMacroValues.get(name);

                if (macroValue == null || macroValue.equals(name))
                {
                    break;
                }

                name = macroValue;
            }

            int macroIntValue = Config.parseInt(name, Integer.MIN_VALUE);

            if (macroIntValue == Integer.MIN_VALUE)
            {
                Config.warn("Unknown macro value: " + name);
                return new ConstantFloat(0.0F);
            }
            else
            {
                return new ConstantFloat((float)macroIntValue);
            }
        }
    }
}
