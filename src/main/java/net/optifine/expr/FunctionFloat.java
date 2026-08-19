package net.optifine.expr;

import net.optifine.shaders.uniform.Smoother;

public class FunctionFloat implements IExpressionFloat
{
    private FunctionType type;
    private IExpression[] arguments;
    private int smoothId = -1;

    public FunctionFloat(FunctionType type, IExpression[] arguments)
    {
        this.type = type;
        this.arguments = arguments;
    }

    public float eval()
    {
        IExpression[] args = this.arguments;

        switch (this.type)
        {
            case SMOOTH:
                IExpression firstArgument = args[0];

                if (!(firstArgument instanceof ConstantFloat))
                {
                    float value = evalFloat(args, 0);
                    float fadeUp = args.length > 1 ? evalFloat(args, 1) : 1.0F;
                    float fadeDown = args.length > 2 ? evalFloat(args, 2) : fadeUp;

                    if (this.smoothId < 0)
                    {
                        this.smoothId = Smoother.getNextId();
                    }

                    float smoothValue = Smoother.getSmoothValue(this.smoothId, value, fadeUp, fadeDown);
                    return smoothValue;
                }

            default:
                return this.type.evalFloat(this.arguments);
        }
    }

    private static float evalFloat(IExpression[] exprs, int index)
    {
        IExpressionFloat floatExpression = (IExpressionFloat)exprs[index];
        float value = floatExpression.eval();
        return value;
    }

    public ExpressionType getExpressionType()
    {
        return ExpressionType.FLOAT;
    }

    public String toString()
    {
        return "" + this.type + "()";
    }
}
