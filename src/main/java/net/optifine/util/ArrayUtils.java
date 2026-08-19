package net.optifine.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ArrayUtils
{
    public static boolean contains(Object[] arr, Object val)
    {
        if (arr == null)
        {
            return false;
        }
        else
        {
            for (int index = 0; index < arr.length; ++index)
            {
                Object element = arr[index];

                if (element == val)
                {
                    return true;
                }
            }

            return false;
        }
    }

    public static int[] addIntsToArray(int[] intArray, int[] copyFrom)
    {
        if (intArray != null && copyFrom != null)
        {
            int baseLength = intArray.length;
            int newLength = baseLength + copyFrom.length;
            int[] result = new int[newLength];
            System.arraycopy(intArray, 0, result, 0, baseLength);
            System.arraycopy(copyFrom, 0, result, baseLength, copyFrom.length);

            return result;
        }
        else
        {
            throw new NullPointerException("The given array is NULL");
        }
    }

    public static int[] addIntToArray(int[] intArray, int intValue)
    {
        return addIntsToArray(intArray, new int[] {intValue});
    }

    public static Object[] addObjectsToArray(Object[] arr, Object[] objs)
    {
        if (arr == null)
        {
            throw new NullPointerException("The given array is NULL");
        }
        else if (objs.length == 0)
        {
            return arr;
        }
        else
        {
            int baseLength = arr.length;
            int newLength = baseLength + objs.length;
            Object[] result = (Object[])((Object[])Array.newInstance(arr.getClass().getComponentType(), newLength));
            System.arraycopy(arr, 0, result, 0, baseLength);
            System.arraycopy(objs, 0, result, baseLength, objs.length);
            return result;
        }
    }

    public static Object[] addObjectToArray(Object[] arr, Object obj)
    {
        if (arr == null)
        {
            throw new NullPointerException("The given array is NULL");
        }
        else
        {
            int baseLength = arr.length;
            Object[] result = (Object[])((Object[])Array.newInstance(arr.getClass().getComponentType(), baseLength + 1));
            System.arraycopy(arr, 0, result, 0, baseLength);
            result[baseLength] = obj;
            return result;
        }
    }

    public static Object[] addObjectToArray(Object[] arr, Object obj, int index)
    {
        List values = new ArrayList(Arrays.asList(arr));
        values.add(index, obj);
        Object[] result = (Object[])((Object[])Array.newInstance(arr.getClass().getComponentType(), values.size()));
        return values.toArray(result);
    }

    public static String arrayToString(boolean[] arr, String separator)
    {
        if (arr == null)
        {
            return "";
        }
        else
        {
            StringBuffer buffer = new StringBuffer(arr.length * 5);

            for (int index = 0; index < arr.length; ++index)
            {
                boolean value = arr[index];

                if (index > 0)
                {
                    buffer.append(separator);
                }

                buffer.append(String.valueOf(value));
            }

            return buffer.toString();
        }
    }

    public static String arrayToString(float[] arr)
    {
        return arrayToString(arr, ", ");
    }

    public static String arrayToString(float[] arr, String separator)
    {
        if (arr == null)
        {
            return "";
        }
        else
        {
            StringBuffer buffer = new StringBuffer(arr.length * 5);

            for (int index = 0; index < arr.length; ++index)
            {
                float value = arr[index];

                if (index > 0)
                {
                    buffer.append(separator);
                }

                buffer.append(String.valueOf(value));
            }

            return buffer.toString();
        }
    }

    public static String arrayToString(float[] arr, String separator, String format)
    {
        if (arr == null)
        {
            return "";
        }
        else
        {
            StringBuffer stringBuffer = new StringBuffer(arr.length * 5);

            for (int i = 0; i < arr.length; ++i)
            {
                float f = arr[i];

                if (i > 0)
                {
                    stringBuffer.append(separator);
                }

                stringBuffer.append(String.format(format, new Object[] {Float.valueOf(f)}));
            }

            return stringBuffer.toString();
        }
    }

    public static String arrayToString(int[] arr)
    {
        return arrayToString(arr, ", ");
    }

    public static String arrayToString(int[] arr, String separator)
    {
        if (arr == null)
        {
            return "";
        }
        else
        {
            StringBuffer stringBuffer = new StringBuffer(arr.length * 5);

            for (int i = 0; i < arr.length; ++i)
            {
                int j = arr[i];

                if (i > 0)
                {
                    stringBuffer.append(separator);
                }

                stringBuffer.append(String.valueOf(j));
            }

            return stringBuffer.toString();
        }
    }

    public static String arrayToHexString(int[] arr, String separator)
    {
        if (arr == null)
        {
            return "";
        }
        else
        {
            StringBuffer stringBuffer = new StringBuffer(arr.length * 5);

            for (int i = 0; i < arr.length; ++i)
            {
                int j = arr[i];

                if (i > 0)
                {
                    stringBuffer.append(separator);
                }

                stringBuffer.append("0x");
                stringBuffer.append(Integer.toHexString(j));
            }

            return stringBuffer.toString();
        }
    }

    public static String arrayToString(Object[] arr)
    {
        return arrayToString(arr, ", ");
    }

    public static String arrayToString(Object[] arr, String separator)
    {
        if (arr == null)
        {
            return "";
        }
        else
        {
            StringBuffer stringBuffer = new StringBuffer(arr.length * 5);

            for (int i = 0; i < arr.length; ++i)
            {
                Object object = arr[i];

                if (i > 0)
                {
                    stringBuffer.append(separator);
                }

                stringBuffer.append(String.valueOf(object));
            }

            return stringBuffer.toString();
        }
    }

    public static Object[] collectionToArray(Collection coll, Class elementClass)
    {
        if (coll == null)
        {
            return null;
        }
        else if (elementClass == null)
        {
            return null;
        }
        else if (elementClass.isPrimitive())
        {
            throw new IllegalArgumentException("Can not make arrays with primitive elements (int, double), element class: " + elementClass);
        }
        else
        {
            Object[] aobject = (Object[])((Object[])Array.newInstance(elementClass, coll.size()));
            return coll.toArray(aobject);
        }
    }

    public static boolean equalsOne(int val, int[] vals)
    {
        for (int i = 0; i < vals.length; ++i)
        {
            if (vals[i] == val)
            {
                return true;
            }
        }

        return false;
    }

    public static boolean equalsOne(Object a, Object[] bs)
    {
        if (bs == null)
        {
            return false;
        }
        else
        {
            for (int i = 0; i < bs.length; ++i)
            {
                Object object = bs[i];

                if (equals(a, object))
                {
                    return true;
                }
            }

            return false;
        }
    }

    public static boolean equals(Object left, Object right)
    {
        return left == right ? true : (left == null ? false : left.equals(right));
    }

    public static boolean isSameOne(Object a, Object[] bs)
    {
        if (bs == null)
        {
            return false;
        }
        else
        {
            for (int i = 0; i < bs.length; ++i)
            {
                Object object = bs[i];

                if (a == object)
                {
                    return true;
                }
            }

            return false;
        }
    }

    public static Object[] removeObjectFromArray(Object[] arr, Object obj)
    {
        List list = new ArrayList(Arrays.asList(arr));
        list.remove(obj);
        Object[] aobject = collectionToArray(list, arr.getClass().getComponentType());
        return aobject;
    }

    public static int[] toPrimitive(Integer[] arr)
    {
        if (arr == null)
        {
            return null;
        }
        else if (arr.length == 0)
        {
            return new int[0];
        }
        else
        {
            int[] aint = new int[arr.length];

            for (int i = 0; i < aint.length; ++i)
            {
                aint[i] = arr[i].intValue();
            }

            return aint;
        }
    }
}
