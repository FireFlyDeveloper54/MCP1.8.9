package net.optifine.shaders.config;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.src.Config;
import net.optifine.expr.ExpressionParser;
import net.optifine.expr.ExpressionType;
import net.optifine.expr.IExpression;
import net.optifine.expr.IExpressionBool;
import net.optifine.expr.IExpressionFloat;
import net.optifine.expr.IExpressionResolver;
import net.optifine.expr.ParseException;

public class MacroState
{
    private boolean active = true;
    private Deque<Boolean> dequeState = new ArrayDeque();
    private Deque<Boolean> dequeResolved = new ArrayDeque();
    private Map<String, String> mapMacroValues = new HashMap();
    private static final Pattern PATTERN_DIRECTIVE = Pattern.compile("\\s*#\\s*(\\w+)\\s*(.*)");
    private static final Pattern PATTERN_DEFINED = Pattern.compile("defined\\s+(\\w+)");
    private static final Pattern PATTERN_DEFINED_FUNC = Pattern.compile("defined\\s*\\(\\s*(\\w+)\\s*\\)");
    private static final Pattern PATTERN_MACRO = Pattern.compile("(\\w+)");
    private static final String DEFINE = "define";
    private static final String UNDEF = "undef";
    private static final String IFDEF = "ifdef";
    private static final String IFNDEF = "ifndef";
    private static final String IF = "if";
    private static final String ELSE = "else";
    private static final String ELIF = "elif";
    private static final String ENDIF = "endif";
    private static final List<String> MACRO_NAMES = Arrays.<String>asList(new String[] {"define", "undef", "ifdef", "ifndef", "if", "else", "elif", "endif"});

    public boolean processLine(String line)
    {
        Matcher directiveMatcher = PATTERN_DIRECTIVE.matcher(line);

        if (!directiveMatcher.matches())
        {
            return this.active;
        }
        else
        {
            String directiveName = directiveMatcher.group(1);
            String directiveParam = directiveMatcher.group(2);
            int commentIndex = directiveParam.indexOf("//");

            if (commentIndex >= 0)
            {
                directiveParam = directiveParam.substring(0, commentIndex);
            }

            boolean wasActive = this.active;
            this.processMacro(directiveName, directiveParam);
            this.active = !this.dequeState.contains(Boolean.FALSE);
            return this.active || wasActive;
        }
    }

    public static boolean isMacroLine(String line)
    {
        Matcher directiveMatcher = PATTERN_DIRECTIVE.matcher(line);

        if (!directiveMatcher.matches())
        {
            return false;
        }
        else
        {
            String directiveName = directiveMatcher.group(1);
            return MACRO_NAMES.contains(directiveName);
        }
    }

    private void processMacro(String name, String param)
    {
        StringTokenizer stringTokenizer = new StringTokenizer(param, " \t");
        String macroName = stringTokenizer.hasMoreTokens() ? stringTokenizer.nextToken() : "";
        String macroValue = stringTokenizer.hasMoreTokens() ? stringTokenizer.nextToken("").trim() : "";

        if (name.equals("define"))
        {
            this.mapMacroValues.put(macroName, macroValue);
        }
        else if (name.equals("undef"))
        {
            this.mapMacroValues.remove(macroName);
        }
        else if (name.equals("ifdef"))
        {
            boolean isDefined = this.mapMacroValues.containsKey(macroName);
            this.dequeState.add(Boolean.valueOf(isDefined));
            this.dequeResolved.add(Boolean.valueOf(isDefined));
        }
        else if (name.equals("ifndef"))
        {
            boolean isNotDefined = !this.mapMacroValues.containsKey(macroName);
            this.dequeState.add(Boolean.valueOf(isNotDefined));
            this.dequeResolved.add(Boolean.valueOf(isNotDefined));
        }
        else if (name.equals("if"))
        {
            boolean ifResult = this.eval(param);
            this.dequeState.add(Boolean.valueOf(ifResult));
            this.dequeResolved.add(Boolean.valueOf(ifResult));
        }
        else if (!this.dequeState.isEmpty())
        {
            if (name.equals("elif"))
            {
                boolean previousBranchActive = ((Boolean)this.dequeState.removeLast()).booleanValue();
                boolean alreadyResolved = ((Boolean)this.dequeResolved.removeLast()).booleanValue();

                if (alreadyResolved)
                {
                    this.dequeState.add(Boolean.valueOf(false));
                    this.dequeResolved.add(Boolean.valueOf(alreadyResolved));
                }
                else
                {
                    boolean elifResult = this.eval(param);
                    this.dequeState.add(Boolean.valueOf(elifResult));
                    this.dequeResolved.add(Boolean.valueOf(elifResult));
                }
            }
            else if (name.equals("else"))
            {
                boolean previousBranchActive = ((Boolean)this.dequeState.removeLast()).booleanValue();
                boolean alreadyResolved = ((Boolean)this.dequeResolved.removeLast()).booleanValue();
                boolean elseActive = !alreadyResolved;
                this.dequeState.add(Boolean.valueOf(elseActive));
                this.dequeResolved.add(Boolean.valueOf(true));
            }
            else if (name.equals("endif"))
            {
                this.dequeState.removeLast();
                this.dequeResolved.removeLast();
            }
        }
    }

    private boolean eval(String str)
    {
        Matcher definedMatcher = PATTERN_DEFINED.matcher(str);
        str = definedMatcher.replaceAll("defined_$1");
        Matcher definedFuncMatcher = PATTERN_DEFINED_FUNC.matcher(str);
        str = definedFuncMatcher.replaceAll("defined_$1");
        boolean replacedMacro = false;
        int iterationCount = 0;

        while (true)
        {
            replacedMacro = false;
            Matcher macroMatcher = PATTERN_MACRO.matcher(str);

            while (macroMatcher.find())
            {
                String macroName = macroMatcher.group();

                if (macroName.length() > 0)
                {
                    char firstChar = macroName.charAt(0);

                    if ((Character.isLetter(firstChar) || firstChar == 95) && this.mapMacroValues.containsKey(macroName))
                    {
                        String macroValue = (String)this.mapMacroValues.get(macroName);

                        if (macroValue == null)
                        {
                            macroValue = "1";
                        }

                        int macroStart = macroMatcher.start();
                        int macroEnd = macroMatcher.end();
                        str = str.substring(0, macroStart) + " " + macroValue + " " + str.substring(macroEnd);
                        replacedMacro = true;
                        ++iterationCount;
                        break;
                    }
                }
            }

            if (!replacedMacro || iterationCount >= 100)
            {
                break;
            }
        }

        if (iterationCount >= 100)
        {
            Config.warn("Too many iterations: " + iterationCount + ", when resolving: " + str);
            return true;
        }
        else
        {
            try
            {
                IExpressionResolver expressionResolver = new MacroExpressionResolver(this.mapMacroValues);
                ExpressionParser expressionParser = new ExpressionParser(expressionResolver);
                IExpression expression = expressionParser.parse(str);

                if (expression.getExpressionType() == ExpressionType.BOOL)
                {
                    IExpressionBool boolExpression = (IExpressionBool)expression;
                    boolean boolValue = boolExpression.eval();
                    return boolValue;
                }
                else if (expression.getExpressionType() == ExpressionType.FLOAT)
                {
                    IExpressionFloat floatExpression = (IExpressionFloat)expression;
                    float expressionValue = floatExpression.eval();
                    boolean expressionAsBool = expressionValue != 0.0F;
                    return expressionAsBool;
                }
                else
                {
                    throw new ParseException("Not a boolean or float expression: " + expression.getExpressionType());
                }
            }
            catch (ParseException parseException)
            {
                Config.warn("Invalid macro expression: " + str);
                Config.warn("Error: " + parseException.getMessage());
                return false;
            }
        }
    }
}
