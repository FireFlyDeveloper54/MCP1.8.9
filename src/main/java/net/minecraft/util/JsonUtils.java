package net.minecraft.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

public class JsonUtils
{
    public static boolean isString(JsonObject jsonObject, String memberName)
    {
        return !isJsonPrimitive(jsonObject, memberName) ? false : jsonObject.getAsJsonPrimitive(memberName).isString();
    }

    public static boolean isString(JsonElement json)
    {
        return !json.isJsonPrimitive() ? false : json.getAsJsonPrimitive().isString();
    }

    public static boolean isBoolean(JsonObject jsonObject, String memberName)
    {
        return !isJsonPrimitive(jsonObject, memberName) ? false : jsonObject.getAsJsonPrimitive(memberName).isBoolean();
    }

    public static boolean isJsonArray(JsonObject jsonObject, String memberName)
    {
        return !hasField(jsonObject, memberName) ? false : jsonObject.get(memberName).isJsonArray();
    }

    public static boolean isJsonPrimitive(JsonObject jsonObject, String memberName)
    {
        return !hasField(jsonObject, memberName) ? false : jsonObject.get(memberName).isJsonPrimitive();
    }

    public static boolean hasField(JsonObject jsonObject, String memberName)
    {
        return jsonObject == null ? false : jsonObject.get(memberName) != null;
    }

    public static String getString(JsonElement json, String memberName)
    {
        if (json.isJsonPrimitive())
        {
            return json.getAsString();
        }
        else
        {
            throw new JsonSyntaxException("Expected " + memberName + " to be a string, was " + toString(json));
        }
    }

    public static String getString(JsonObject jsonObject, String memberName)
    {
        if (jsonObject.has(memberName))
        {
            return getString(jsonObject.get(memberName), memberName);
        }
        else
        {
            throw new JsonSyntaxException("Missing " + memberName + ", expected to find a string");
        }
    }

    public static String getString(JsonObject jsonObject, String memberName, String defaultValue)
    {
        return jsonObject.has(memberName) ? getString(jsonObject.get(memberName), memberName) : defaultValue;
    }

    public static boolean getBoolean(JsonElement json, String memberName)
    {
        if (json.isJsonPrimitive())
        {
            return json.getAsBoolean();
        }
        else
        {
            throw new JsonSyntaxException("Expected " + memberName + " to be a Boolean, was " + toString(json));
        }
    }

    public static boolean getBoolean(JsonObject jsonObject, String memberName)
    {
        if (jsonObject.has(memberName))
        {
            return getBoolean(jsonObject.get(memberName), memberName);
        }
        else
        {
            throw new JsonSyntaxException("Missing " + memberName + ", expected to find a Boolean");
        }
    }

    public static boolean getBoolean(JsonObject jsonObject, String memberName, boolean defaultValue)
    {
        return jsonObject.has(memberName) ? getBoolean(jsonObject.get(memberName), memberName) : defaultValue;
    }

    public static float getFloat(JsonElement json, String memberName)
    {
        if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isNumber())
        {
            return json.getAsFloat();
        }
        else
        {
            throw new JsonSyntaxException("Expected " + memberName + " to be a Float, was " + toString(json));
        }
    }

    public static float getFloat(JsonObject jsonObject, String memberName)
    {
        if (jsonObject.has(memberName))
        {
            return getFloat(jsonObject.get(memberName), memberName);
        }
        else
        {
            throw new JsonSyntaxException("Missing " + memberName + ", expected to find a Float");
        }
    }

    public static float getFloat(JsonObject jsonObject, String memberName, float defaultValue)
    {
        return jsonObject.has(memberName) ? getFloat(jsonObject.get(memberName), memberName) : defaultValue;
    }

    public static int getInt(JsonElement json, String memberName)
    {
        if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isNumber())
        {
            return json.getAsInt();
        }
        else
        {
            throw new JsonSyntaxException("Expected " + memberName + " to be a Int, was " + toString(json));
        }
    }

    public static int getInt(JsonObject jsonObject, String memberName)
    {
        if (jsonObject.has(memberName))
        {
            return getInt(jsonObject.get(memberName), memberName);
        }
        else
        {
            throw new JsonSyntaxException("Missing " + memberName + ", expected to find a Int");
        }
    }

    public static int getInt(JsonObject jsonObject, String memberName, int defaultValue)
    {
        return jsonObject.has(memberName) ? getInt(jsonObject.get(memberName), memberName) : defaultValue;
    }

    public static JsonObject getJsonObject(JsonElement json, String memberName)
    {
        if (json.isJsonObject())
        {
            return json.getAsJsonObject();
        }
        else
        {
            throw new JsonSyntaxException("Expected " + memberName + " to be a JsonObject, was " + toString(json));
        }
    }

    public static JsonObject getJsonObject(JsonObject base, String key)
    {
        if (base.has(key))
        {
            return getJsonObject(base.get(key), key);
        }
        else
        {
            throw new JsonSyntaxException("Missing " + key + ", expected to find a JsonObject");
        }
    }

    public static JsonObject getJsonObject(JsonObject jsonObject, String memberName, JsonObject defaultValue)
    {
        return jsonObject.has(memberName) ? getJsonObject(jsonObject.get(memberName), memberName) : defaultValue;
    }

    public static JsonArray getJsonArray(JsonElement json, String memberName)
    {
        if (json.isJsonArray())
        {
            return json.getAsJsonArray();
        }
        else
        {
            throw new JsonSyntaxException("Expected " + memberName + " to be a JsonArray, was " + toString(json));
        }
    }

    public static JsonArray getJsonArray(JsonObject jsonObject, String memberName)
    {
        if (jsonObject.has(memberName))
        {
            return getJsonArray(jsonObject.get(memberName), memberName);
        }
        else
        {
            throw new JsonSyntaxException("Missing " + memberName + ", expected to find a JsonArray");
        }
    }

    public static JsonArray getJsonArray(JsonObject jsonObject, String memberName, JsonArray defaultValue)
    {
        return jsonObject.has(memberName) ? getJsonArray(jsonObject.get(memberName), memberName) : defaultValue;
    }

    public static String toString(JsonElement json)
    {
        String jsonPreview = org.apache.commons.lang3.StringUtils.abbreviateMiddle(String.valueOf((Object)json), "...", 10);

        if (json == null)
        {
            return "null (missing)";
        }
        else if (json.isJsonNull())
        {
            return "null (json)";
        }
        else if (json.isJsonArray())
        {
            return "an array (" + jsonPreview + ")";
        }
        else if (json.isJsonObject())
        {
            return "an object (" + jsonPreview + ")";
        }
        else
        {
            if (json.isJsonPrimitive())
            {
                JsonPrimitive jsonPrimitive = json.getAsJsonPrimitive();

                if (jsonPrimitive.isNumber())
                {
                    return "a number (" + jsonPreview + ")";
                }

                if (jsonPrimitive.isBoolean())
                {
                    return "a boolean (" + jsonPreview + ")";
                }
            }

            return jsonPreview;
        }
    }
}
