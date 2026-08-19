package net.optifine.shaders.config;

import java.util.HashMap;
import java.util.Map;
import net.optifine.expr.IExpression;
import net.optifine.expr.IExpressionResolver;

public class ShaderOptionResolver implements IExpressionResolver
{
    private Map<String, ExpressionShaderOptionSwitch> mapOptions = new HashMap();

    public ShaderOptionResolver(ShaderOption[] options)
    {
        for (int optionIndex = 0; optionIndex < options.length; ++optionIndex)
        {
            ShaderOption shaderOption = options[optionIndex];

            if (shaderOption instanceof ShaderOptionSwitch)
            {
                ShaderOptionSwitch shaderOptionSwitch = (ShaderOptionSwitch)shaderOption;
                ExpressionShaderOptionSwitch expressionShaderOptionSwitch = new ExpressionShaderOptionSwitch(shaderOptionSwitch);
                this.mapOptions.put(shaderOption.getName(), expressionShaderOptionSwitch);
            }
        }
    }

    public IExpression getExpression(String name)
    {
        ExpressionShaderOptionSwitch expressionShaderOptionSwitch = (ExpressionShaderOptionSwitch)this.mapOptions.get(name);
        return expressionShaderOptionSwitch;
    }
}
