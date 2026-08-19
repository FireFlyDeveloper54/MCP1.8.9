package net.optifine.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class Json
{
    public static float getFloat(JsonObject obj, String field, float def)
    {
        JsonElement jsonElement = obj.get(field);
        return jsonElement == null ? def : jsonElement.getAsFloat();
    }

    public static boolean getBoolean(JsonObject obj, String field, boolean def)
    {
        JsonElement jsonElement = obj.get(field);
        return jsonElement == null ? def : jsonElement.getAsBoolean();
    }

    public static String getString(JsonObject jsonObj, String field)
    {
        return getString(jsonObj, field, (String)null);
    }

    public static String getString(JsonObject jsonObj, String field, String def)
    {
        JsonElement jsonElement = jsonObj.get(field);
        return jsonElement == null ? def : jsonElement.getAsString();
    }

    public static float[] parseFloatArray(JsonElement jsonElement, int len)
    {
        return parseFloatArray(jsonElement, len, (float[])null);
    }

    public static float[] parseFloatArray(JsonElement jsonElement, int len, float[] def)
    {
        if (jsonElement == null)
        {
            return def;
        }
        else
        {
            JsonArray jsonArray = jsonElement.getAsJsonArray();

            if (jsonArray.size() != len)
            {
                throw new JsonParseException("Wrong array length: " + jsonArray.size() + ", should be: " + len + ", array: " + jsonArray);
            }
            else
            {
                float[] floatArray = new float[jsonArray.size()];

                for (int index = 0; index < floatArray.length; ++index)
                {
                    floatArray[index] = jsonArray.get(index).getAsFloat();
                }

                return floatArray;
            }
        }
    }

    public static int[] parseIntArray(JsonElement jsonElement, int len)
    {
        return parseIntArray(jsonElement, len, (int[])null);
    }

    public static int[] parseIntArray(JsonElement jsonElement, int len, int[] def)
    {
        if (jsonElement == null)
        {
            return def;
        }
        else
        {
            JsonArray jsonArray = jsonElement.getAsJsonArray();

            if (jsonArray.size() != len)
            {
                throw new JsonParseException("Wrong array length: " + jsonArray.size() + ", should be: " + len + ", array: " + jsonArray);
            }
            else
            {
                int[] intArray = new int[jsonArray.size()];

                for (int index = 0; index < intArray.length; ++index)
                {
                    intArray[index] = jsonArray.get(index).getAsInt();
                }

                return intArray;
            }
        }
    }
}
