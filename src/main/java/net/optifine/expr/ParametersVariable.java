package net.optifine.expr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ParametersVariable implements IParameters
{
    private ExpressionType[] first;
    private ExpressionType[] repeat;
    private ExpressionType[] last;
    private int maxCount;
    private static final ExpressionType[] EMPTY = new ExpressionType[0];

    public ParametersVariable()
    {
        this((ExpressionType[])null, (ExpressionType[])null, (ExpressionType[])null);
    }

    public ParametersVariable(ExpressionType[] first, ExpressionType[] repeat, ExpressionType[] last)
    {
        this(first, repeat, last, Integer.MAX_VALUE);
    }

    public ParametersVariable(ExpressionType[] first, ExpressionType[] repeat, ExpressionType[] last, int maxCount)
    {
        this.maxCount = Integer.MAX_VALUE;
        this.first = normalize(first);
        this.repeat = normalize(repeat);
        this.last = normalize(last);
        this.maxCount = maxCount;
    }

    private static ExpressionType[] normalize(ExpressionType[] exprs)
    {
        return exprs == null ? EMPTY : exprs;
    }

    public ExpressionType[] getFirst()
    {
        return this.first;
    }

    public ExpressionType[] getRepeat()
    {
        return this.repeat;
    }

    public ExpressionType[] getLast()
    {
        return this.last;
    }

    public int getCountRepeat()
    {
        return this.first == null ? 0 : this.first.length;
    }

    public ExpressionType[] getParameterTypes(IExpression[] arguments)
    {
        int fixedCount = this.first.length + this.last.length;
        int repeatableCount = arguments.length - fixedCount;
        int repeatCount = 0;

        for (int repeatOffset = 0; repeatOffset + this.repeat.length <= repeatableCount && fixedCount + repeatOffset + this.repeat.length <= this.maxCount; repeatOffset += this.repeat.length)
        {
            ++repeatCount;
        }

        List<ExpressionType> types = new ArrayList();
        types.addAll(Arrays.<ExpressionType>asList(this.first));

        for (int repeatIndex = 0; repeatIndex < repeatCount; ++repeatIndex)
        {
            types.addAll(Arrays.<ExpressionType>asList(this.repeat));
        }

        types.addAll(Arrays.<ExpressionType>asList(this.last));
        ExpressionType[] parameterTypes = (ExpressionType[])types.toArray(new ExpressionType[types.size()]);
        return parameterTypes;
    }

    public ParametersVariable first(ExpressionType... first)
    {
        return new ParametersVariable(first, this.repeat, this.last);
    }

    public ParametersVariable repeat(ExpressionType... repeat)
    {
        return new ParametersVariable(this.first, repeat, this.last);
    }

    public ParametersVariable last(ExpressionType... last)
    {
        return new ParametersVariable(this.first, this.repeat, last);
    }

    public ParametersVariable maxCount(int maxCount)
    {
        return new ParametersVariable(this.first, this.repeat, this.last, maxCount);
    }
}
