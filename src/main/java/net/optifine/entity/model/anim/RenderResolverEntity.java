package net.optifine.entity.model.anim;

import net.optifine.expr.IExpression;

public class RenderResolverEntity implements IRenderResolver
{
    public IExpression getParameter(String name)
    {
        RenderEntityParameterBool renderEntityParameterBool = RenderEntityParameterBool.parse(name);

        if (renderEntityParameterBool != null)
        {
            return renderEntityParameterBool;
        }
        else
        {
            RenderEntityParameterFloat renderEntityParameterFloat = RenderEntityParameterFloat.parse(name);
            return renderEntityParameterFloat != null ? renderEntityParameterFloat : null;
        }
    }
}
