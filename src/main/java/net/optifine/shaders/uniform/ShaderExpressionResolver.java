package net.optifine.shaders.uniform;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.world.biome.BiomeGenBase;
import net.optifine.expr.ConstantFloat;
import net.optifine.expr.IExpression;
import net.optifine.expr.IExpressionResolver;
import net.optifine.shaders.SMCLog;

public class ShaderExpressionResolver implements IExpressionResolver
{
    private Map<String, IExpression> mapExpressions = new HashMap();

    public ShaderExpressionResolver(Map<String, IExpression> map)
    {
        this.registerExpressions();

        for (String expressionName : map.keySet())
        {
            IExpression expression = (IExpression)map.get(expressionName);
            this.registerExpression(expressionName, expression);
        }
    }

    private void registerExpressions()
    {
        ShaderParameterFloat[] shaderFloatParameters = ShaderParameterFloat.VALUES;

        for (int parameterIndex = 0; parameterIndex < shaderFloatParameters.length; ++parameterIndex)
        {
            ShaderParameterFloat shaderParameterFloat = shaderFloatParameters[parameterIndex];
            this.addParameterFloat(this.mapExpressions, shaderParameterFloat);
        }

        ShaderParameterBool[] shaderBoolParameters = ShaderParameterBool.VALUES;

        for (int parameterIndex = 0; parameterIndex < shaderBoolParameters.length; ++parameterIndex)
        {
            ShaderParameterBool shaderParameterBool = shaderBoolParameters[parameterIndex];
            this.mapExpressions.put(shaderParameterBool.getName(), shaderParameterBool);
        }

        for (BiomeGenBase biome : BiomeGenBase.BIOME_ID_MAP.values())
        {
            String biomeName = biome.biomeName.trim();
            biomeName = "BIOME_" + biomeName.toUpperCase().replace(' ', '_');
            int biomeId = biome.biomeID;
            IExpression expression = new ConstantFloat((float)biomeId);
            this.registerExpression(biomeName, expression);
        }
    }

    private void addParameterFloat(Map<String, IExpression> map, ShaderParameterFloat shaderParameterFloat)
    {
        String[] indexNames1 = shaderParameterFloat.getIndexNames1();

        if (indexNames1 == null)
        {
            map.put(shaderParameterFloat.getName(), new ShaderParameterIndexed(shaderParameterFloat));
        }
        else
        {
            for (int index1 = 0; index1 < indexNames1.length; ++index1)
            {
                String indexName1 = indexNames1[index1];
                String[] indexNames2 = shaderParameterFloat.getIndexNames2();

                if (indexNames2 == null)
                {
                    map.put(shaderParameterFloat.getName() + "." + indexName1, new ShaderParameterIndexed(shaderParameterFloat, index1));
                }
                else
                {
                    for (int index2 = 0; index2 < indexNames2.length; ++index2)
                    {
                        String indexName2 = indexNames2[index2];
                        map.put(shaderParameterFloat.getName() + "." + indexName1 + "." + indexName2, new ShaderParameterIndexed(shaderParameterFloat, index1, index2));
                    }
                }
            }
        }
    }

    public boolean registerExpression(String name, IExpression expr)
    {
        if (this.mapExpressions.containsKey(name))
        {
            SMCLog.warning("Expression already defined: " + name);
            return false;
        }
        else
        {
            this.mapExpressions.put(name, expr);
            return true;
        }
    }

    public IExpression getExpression(String name)
    {
        return (IExpression)this.mapExpressions.get(name);
    }

    public boolean hasExpression(String name)
    {
        return this.mapExpressions.containsKey(name);
    }
}
