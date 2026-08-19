package net.minecraft.potion;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import net.minecraft.src.Config;
import net.minecraft.util.IntegerCache;
import net.optifine.CustomColors;

public class PotionHelper
{
    public static final String unusedString = null;
    public static final String sugarEffect = "-0+1-2-3&4-4+13";
    public static final String ghastTearEffect = "+0-1-2-3&4-4+13";
    public static final String spiderEyeEffect = "-0-1+2-3&4-4+13";
    public static final String fermentedSpiderEyeEffect = "-0+3-4+13";
    public static final String speckledMelonEffect = "+0-1+2-3&4-4+13";
    public static final String blazePowderEffect = "+0-1-2+3&4-4+13";
    public static final String magmaCreamEffect = "+0+1-2-3&4-4+13";
    public static final String redstoneEffect = "-5+6-7";
    public static final String glowstoneEffect = "+5-6-7";
    public static final String gunpowderEffect = "+14&13-13";
    public static final String goldenCarrotEffect = "-0+1+2-3+13&4-4";
    public static final String pufferfishEffect = "+0-1+2+3+13&4-4";
    public static final String rabbitFootEffect = "+0+1-2+3&4-4+13";
    private static final Map<Integer, String> potionRequirements = Maps.<Integer, String>newHashMap();
    private static final Map<Integer, String> potionAmplifiers = Maps.<Integer, String>newHashMap();
    private static final Map<Integer, Integer> DATAVALUE_COLORS = Maps.<Integer, Integer>newHashMap();
    private static final String[] potionPrefixes = new String[] {"potion.prefix.mundane", "potion.prefix.uninteresting", "potion.prefix.bland", "potion.prefix.clear", "potion.prefix.milky", "potion.prefix.diffuse", "potion.prefix.artless", "potion.prefix.thin", "potion.prefix.awkward", "potion.prefix.flat", "potion.prefix.bulky", "potion.prefix.bungling", "potion.prefix.buttered", "potion.prefix.smooth", "potion.prefix.suave", "potion.prefix.debonair", "potion.prefix.thick", "potion.prefix.elegant", "potion.prefix.fancy", "potion.prefix.charming", "potion.prefix.dashing", "potion.prefix.refined", "potion.prefix.cordial", "potion.prefix.sparkling", "potion.prefix.potent", "potion.prefix.foul", "potion.prefix.odorless", "potion.prefix.rank", "potion.prefix.harsh", "potion.prefix.acrid", "potion.prefix.gross", "potion.prefix.stinky"};

    public static boolean checkFlag(int value, int bitIndex)
    {
        return (value & 1 << bitIndex) != 0;
    }

    private static int getFlagSetValue(int dataValue, int bitIndex)
    {
        return checkFlag(dataValue, bitIndex) ? 1 : 0;
    }

    private static int getFlagUnsetValue(int dataValue, int bitIndex)
    {
        return checkFlag(dataValue, bitIndex) ? 0 : 1;
    }

    public static int getPotionPrefixIndex(int dataValue)
    {
        return getPotionPrefixIndexFlags(dataValue, 5, 4, 3, 2, 1);
    }

    public static int calcPotionLiquidColor(Collection<PotionEffect> potionEffects)
    {
        int i = 3694022;

        if (potionEffects != null && !potionEffects.isEmpty())
        {
            float f = 0.0F;
            float floatValue = 0.0F;
            float secondFloatValue = 0.0F;
            float thirdFloatValue = 0.0F;

            for (PotionEffect potioneffect : potionEffects)
            {
                if (potioneffect.getIsShowParticles())
                {
                    int j = Potion.potionTypes[potioneffect.getPotionID()].getLiquidColor();

                    if (Config.isCustomColors())
                    {
                        j = CustomColors.getPotionColor(potioneffect.getPotionID(), j);
                    }

                    for (int k = 0; k <= potioneffect.getAmplifier(); ++k)
                    {
                        f += (float)(j >> 16 & 255) / 255.0F;
                        floatValue += (float)(j >> 8 & 255) / 255.0F;
                        secondFloatValue += (float)(j >> 0 & 255) / 255.0F;
                        ++thirdFloatValue;
                    }
                }
            }

            if (thirdFloatValue == 0.0F)
            {
                return 0;
            }
            else
            {
                f = f / thirdFloatValue * 255.0F;
                floatValue = floatValue / thirdFloatValue * 255.0F;
                secondFloatValue = secondFloatValue / thirdFloatValue * 255.0F;
                return (int)f << 16 | (int)floatValue << 8 | (int)secondFloatValue;
            }
        }
        else
        {
            return Config.isCustomColors() ? CustomColors.getPotionColor(0, i) : i;
        }
    }

    public static boolean getAreAmbient(Collection<PotionEffect> potionEffects)
    {
        for (PotionEffect potionEffect : potionEffects)
        {
            if (!potionEffect.getIsAmbient())
            {
                return false;
            }
        }

        return true;
    }

    public static int getLiquidColor(int dataValue, boolean bypassCache)
    {
        Integer integer = IntegerCache.getInteger(dataValue);

        if (!bypassCache)
        {
            Integer cachedColor = DATAVALUE_COLORS.get(integer);

            if (cachedColor != null)
            {
                return cachedColor.intValue();
            }
            else
            {
                int i = calcPotionLiquidColor(getPotionEffects(integer.intValue(), false));
                DATAVALUE_COLORS.put(integer, Integer.valueOf(i));
                return i;
            }
        }
        else
        {
            return calcPotionLiquidColor(getPotionEffects(integer.intValue(), true));
        }
    }

    public static String getPotionPrefix(int dataValue)
    {
        int i = getPotionPrefixIndex(dataValue);
        return potionPrefixes[i];
    }

    private static int getPotionEffect(boolean requireUnset, boolean scaleByValue, boolean negate, int operation, int bitIndex, int scale, int dataValue)
    {
        int i = 0;

        if (requireUnset)
        {
            i = getFlagUnsetValue(dataValue, bitIndex);
        }
        else if (operation != -1)
        {
            if (operation == 0 && countSetFlags(dataValue) == bitIndex)
            {
                i = 1;
            }
            else if (operation == 1 && countSetFlags(dataValue) > bitIndex)
            {
                i = 1;
            }
            else if (operation == 2 && countSetFlags(dataValue) < bitIndex)
            {
                i = 1;
            }
        }
        else
        {
            i = getFlagSetValue(dataValue, bitIndex);
        }

        if (scaleByValue)
        {
            i *= scale;
        }

        if (negate)
        {
            i *= -1;
        }

        return i;
    }

    private static int countSetFlags(int dataValue)
    {
        int i;

        for (i = 0; dataValue > 0; ++i)
        {
            dataValue &= dataValue - 1;
        }

        return i;
    }

    private static int parsePotionEffects(String expression, int startIndex, int endIndex, int dataValue)
    {
        if (startIndex < expression.length() && endIndex >= 0 && startIndex < endIndex)
        {
            int i = expression.indexOf(124, startIndex);

            if (i >= 0 && i < endIndex)
            {
                int fourthIntValue = parsePotionEffects(expression, startIndex, i - 1, dataValue);

                if (fourthIntValue > 0)
                {
                    return fourthIntValue;
                }
                else
                {
                    int intValue2 = parsePotionEffects(expression, i + 1, endIndex, dataValue);
                    return intValue2 > 0 ? intValue2 : 0;
                }
            }
            else
            {
                int j = expression.indexOf(38, startIndex);

                if (j >= 0 && j < endIndex)
                {
                    int intValue3 = parsePotionEffects(expression, startIndex, j - 1, dataValue);

                    if (intValue3 <= 0)
                    {
                        return 0;
                    }
                    else
                    {
                        int intValue4 = parsePotionEffects(expression, j + 1, endIndex, dataValue);
                        return intValue4 <= 0 ? 0 : (intValue3 > intValue4 ? intValue3 : intValue4);
                    }
                }
                else
                {
                    boolean flag = false;
                    boolean flag1 = false;
                    boolean flag2 = false;
                    boolean flag3 = false;
                    boolean flag4 = false;
                    int k = -1;
                    int l = 0;
                    int intValue5 = 0;
                    int secondIntValue2 = 0;

                    for (int nestedIndex = startIndex; nestedIndex < endIndex; ++nestedIndex)
                    {
                        char character = expression.charAt(nestedIndex);

                        if (character >= 48 && character <= 57)
                        {
                            if (flag)
                            {
                                intValue5 = character - 48;
                                flag1 = true;
                            }
                            else
                            {
                                l = l * 10;
                                l = l + (character - 48);
                                flag2 = true;
                            }
                        }
                        else if (character == 42)
                        {
                            flag = true;
                        }
                        else if (character == 33)
                        {
                            if (flag2)
                            {
                                secondIntValue2 += getPotionEffect(flag3, flag1, flag4, k, l, intValue5, dataValue);
                                flag3 = false;
                                flag4 = false;
                                flag = false;
                                flag1 = false;
                                flag2 = false;
                                intValue5 = 0;
                                l = 0;
                                k = -1;
                            }

                            flag3 = true;
                        }
                        else if (character == 45)
                        {
                            if (flag2)
                            {
                                secondIntValue2 += getPotionEffect(flag3, flag1, flag4, k, l, intValue5, dataValue);
                                flag3 = false;
                                flag4 = false;
                                flag = false;
                                flag1 = false;
                                flag2 = false;
                                intValue5 = 0;
                                l = 0;
                                k = -1;
                            }

                            flag4 = true;
                        }
                        else if (character != 61 && character != 60 && character != 62)
                        {
                            if (character == 43 && flag2)
                            {
                                secondIntValue2 += getPotionEffect(flag3, flag1, flag4, k, l, intValue5, dataValue);
                                flag3 = false;
                                flag4 = false;
                                flag = false;
                                flag1 = false;
                                flag2 = false;
                                intValue5 = 0;
                                l = 0;
                                k = -1;
                            }
                        }
                        else
                        {
                            if (flag2)
                            {
                                secondIntValue2 += getPotionEffect(flag3, flag1, flag4, k, l, intValue5, dataValue);
                                flag3 = false;
                                flag4 = false;
                                flag = false;
                                flag1 = false;
                                flag2 = false;
                                intValue5 = 0;
                                l = 0;
                                k = -1;
                            }

                            if (character == 61)
                            {
                                k = 0;
                            }
                            else if (character == 60)
                            {
                                k = 2;
                            }
                            else if (character == 62)
                            {
                                k = 1;
                            }
                        }
                    }

                    if (flag2)
                    {
                        secondIntValue2 += getPotionEffect(flag3, flag1, flag4, k, l, intValue5, dataValue);
                    }

                    return secondIntValue2;
                }
            }
        }
        else
        {
            return 0;
        }
    }

    public static List<PotionEffect> getPotionEffects(int dataValue, boolean includeAllPotions)
    {
        List<PotionEffect> list = null;

        for (Potion potion : Potion.potionTypes)
        {
            if (potion != null && (!potion.isUsable() || includeAllPotions))
            {
                String s = potionRequirements.get(Integer.valueOf(potion.getId()));

                if (s != null)
                {
                    int i = parsePotionEffects(s, 0, s.length(), dataValue);

                    if (i > 0)
                    {
                        int j = 0;
                        String stringValue = potionAmplifiers.get(Integer.valueOf(potion.getId()));

                        if (stringValue != null)
                        {
                            j = parsePotionEffects(stringValue, 0, stringValue.length(), dataValue);

                            if (j < 0)
                            {
                                j = 0;
                            }
                        }

                        if (potion.isInstant())
                        {
                            i = 1;
                        }
                        else
                        {
                            i = 1200 * (i * 3 + (i - 1) * 2);
                            i = i >> j;
                            i = (int)Math.round((double)i * potion.getEffectiveness());

                            if ((dataValue & 16384) != 0)
                            {
                                i = (int)Math.round((double)i * 0.75D + 0.5D);
                            }
                        }

                        if (list == null)
                        {
                            list = Lists.<PotionEffect>newArrayList();
                        }

                        PotionEffect potioneffect = new PotionEffect(potion.getId(), i, j);

                        if ((dataValue & 16384) != 0)
                        {
                            potioneffect.setSplashPotion(true);
                        }

                        list.add(potioneffect);
                    }
                }
            }
        }

        return list;
    }

    private static int brewBitOperations(int dataValue, int bitIndex, boolean requiredUnset, boolean allowToggle, boolean requirePresent)
    {
        if (requirePresent)
        {
            if (!checkFlag(dataValue, bitIndex))
            {
                return 0;
            }
        }
        else if (requiredUnset)
        {
            dataValue &= ~(1 << bitIndex);
        }
        else if (allowToggle)
        {
            if ((dataValue & 1 << bitIndex) == 0)
            {
                dataValue |= 1 << bitIndex;
            }
            else
            {
                dataValue &= ~(1 << bitIndex);
            }
        }
        else
        {
            dataValue |= 1 << bitIndex;
        }

        return dataValue;
    }

    public static int applyIngredient(int dataValue, String expression)
    {
        int i = 0;
        int j = expression.length();
        boolean flag = false;
        boolean flag1 = false;
        boolean flag2 = false;
        boolean flag3 = false;
        int k = 0;

        for (int l = i; l < j; ++l)
        {
            char character = expression.charAt(l);

            if (character >= 48 && character <= 57)
            {
                k = k * 10;
                k = k + (character - 48);
                flag = true;
            }
            else if (character == 33)
            {
                if (flag)
                {
                    dataValue = brewBitOperations(dataValue, k, flag2, flag1, flag3);
                    flag3 = false;
                    flag1 = false;
                    flag2 = false;
                    flag = false;
                    k = 0;
                }

                flag1 = true;
            }
            else if (character == 45)
            {
                if (flag)
                {
                    dataValue = brewBitOperations(dataValue, k, flag2, flag1, flag3);
                    flag3 = false;
                    flag1 = false;
                    flag2 = false;
                    flag = false;
                    k = 0;
                }

                flag2 = true;
            }
            else if (character == 43)
            {
                if (flag)
                {
                    dataValue = brewBitOperations(dataValue, k, flag2, flag1, flag3);
                    flag3 = false;
                    flag1 = false;
                    flag2 = false;
                    flag = false;
                    k = 0;
                }
            }
            else if (character == 38)
            {
                if (flag)
                {
                    dataValue = brewBitOperations(dataValue, k, flag2, flag1, flag3);
                    flag3 = false;
                    flag1 = false;
                    flag2 = false;
                    flag = false;
                    k = 0;
                }

                flag3 = true;
            }
        }

        if (flag)
        {
            dataValue = brewBitOperations(dataValue, k, flag2, flag1, flag3);
        }

        return dataValue & 32767;
    }

    public static int getPotionPrefixIndexFlags(int dataValue, int seventhIntValue, int sixthIntValue, int fifthIntValue, int thirdIntValue, int secondIntValue)
    {
        return (checkFlag(dataValue, seventhIntValue) ? 16 : 0) | (checkFlag(dataValue, sixthIntValue) ? 8 : 0) | (checkFlag(dataValue, fifthIntValue) ? 4 : 0) | (checkFlag(dataValue, thirdIntValue) ? 2 : 0) | (checkFlag(dataValue, secondIntValue) ? 1 : 0);
    }

    static
    {
        potionRequirements.put(Integer.valueOf(Potion.regeneration.getId()), "0 & !1 & !2 & !3 & 0+6");
        potionRequirements.put(Integer.valueOf(Potion.moveSpeed.getId()), "!0 & 1 & !2 & !3 & 1+6");
        potionRequirements.put(Integer.valueOf(Potion.fireResistance.getId()), "0 & 1 & !2 & !3 & 0+6");
        potionRequirements.put(Integer.valueOf(Potion.heal.getId()), "0 & !1 & 2 & !3");
        potionRequirements.put(Integer.valueOf(Potion.poison.getId()), "!0 & !1 & 2 & !3 & 2+6");
        potionRequirements.put(Integer.valueOf(Potion.weakness.getId()), "!0 & !1 & !2 & 3 & 3+6");
        potionRequirements.put(Integer.valueOf(Potion.harm.getId()), "!0 & !1 & 2 & 3");
        potionRequirements.put(Integer.valueOf(Potion.moveSlowdown.getId()), "!0 & 1 & !2 & 3 & 3+6");
        potionRequirements.put(Integer.valueOf(Potion.damageBoost.getId()), "0 & !1 & !2 & 3 & 3+6");
        potionRequirements.put(Integer.valueOf(Potion.nightVision.getId()), "!0 & 1 & 2 & !3 & 2+6");
        potionRequirements.put(Integer.valueOf(Potion.invisibility.getId()), "!0 & 1 & 2 & 3 & 2+6");
        potionRequirements.put(Integer.valueOf(Potion.waterBreathing.getId()), "0 & !1 & 2 & 3 & 2+6");
        potionRequirements.put(Integer.valueOf(Potion.jump.getId()), "0 & 1 & !2 & 3 & 3+6");
        potionAmplifiers.put(Integer.valueOf(Potion.moveSpeed.getId()), "5");
        potionAmplifiers.put(Integer.valueOf(Potion.digSpeed.getId()), "5");
        potionAmplifiers.put(Integer.valueOf(Potion.damageBoost.getId()), "5");
        potionAmplifiers.put(Integer.valueOf(Potion.regeneration.getId()), "5");
        potionAmplifiers.put(Integer.valueOf(Potion.harm.getId()), "5");
        potionAmplifiers.put(Integer.valueOf(Potion.heal.getId()), "5");
        potionAmplifiers.put(Integer.valueOf(Potion.resistance.getId()), "5");
        potionAmplifiers.put(Integer.valueOf(Potion.poison.getId()), "5");
        potionAmplifiers.put(Integer.valueOf(Potion.jump.getId()), "5");
    }
}
