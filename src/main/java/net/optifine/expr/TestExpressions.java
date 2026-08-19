package net.optifine.expr;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class TestExpressions
{
    public static void main(String[] args) throws Exception
    {
        ExpressionParser expressionParser = new ExpressionParser((IExpressionResolver)null);

        while (true)
        {
            try
            {
                InputStreamReader inputStreamReader = new InputStreamReader(System.in);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String inputLine = bufferedReader.readLine();

                if (inputLine.length() <= 0)
                {
                    return;
                }

                IExpression expression = expressionParser.parse(inputLine);

                if (expression instanceof IExpressionFloat)
                {
                    IExpressionFloat floatExpression = (IExpressionFloat)expression;
                    float floatResult = floatExpression.eval();
                    System.out.println("" + floatResult);
                }

                if (expression instanceof IExpressionBool)
                {
                    IExpressionBool boolExpression = (IExpressionBool)expression;
                    boolean boolResult = boolExpression.eval();
                    System.out.println("" + boolResult);
                }
            }
            catch (Exception exception)
            {
                net.minecraft.src.Config.warn(exception.getClass().getName() + ": " + exception.getMessage(), exception);
            }
        }
    }
}
