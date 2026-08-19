package net.optifine.shaders.uniform;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.optifine.expr.IExpression;
import net.optifine.expr.IExpressionCached;

public class CustomUniforms
{
    private CustomUniform[] uniforms;
    private IExpressionCached[] expressionsCached;

    public CustomUniforms(CustomUniform[] uniforms, Map<String, IExpression> mapExpressions)
    {
        this.uniforms = uniforms;
        List<IExpressionCached> cachedExpressions = new ArrayList();

        for (String expressionName : mapExpressions.keySet())
        {
            IExpression expression = (IExpression)mapExpressions.get(expressionName);

            if (expression instanceof IExpressionCached)
            {
                IExpressionCached cachedExpression = (IExpressionCached)expression;
                cachedExpressions.add(cachedExpression);
            }
        }

        this.expressionsCached = (IExpressionCached[])((IExpressionCached[])cachedExpressions.toArray(new IExpressionCached[cachedExpressions.size()]));
    }

    public void setProgram(int program)
    {
        for (int uniformIndex = 0; uniformIndex < this.uniforms.length; ++uniformIndex)
        {
            CustomUniform customUniform = this.uniforms[uniformIndex];
            customUniform.setProgram(program);
        }
    }

    public void update()
    {
        this.resetCache();

        for (int uniformIndex = 0; uniformIndex < this.uniforms.length; ++uniformIndex)
        {
            CustomUniform customUniform = this.uniforms[uniformIndex];
            customUniform.update();
        }
    }

    private void resetCache()
    {
        for (int expressionIndex = 0; expressionIndex < this.expressionsCached.length; ++expressionIndex)
        {
            IExpressionCached cachedExpression = this.expressionsCached[expressionIndex];
            cachedExpression.reset();
        }
    }

    public void reset()
    {
        for (int uniformIndex = 0; uniformIndex < this.uniforms.length; ++uniformIndex)
        {
            CustomUniform customUniform = this.uniforms[uniformIndex];
            customUniform.reset();
        }
    }
}
