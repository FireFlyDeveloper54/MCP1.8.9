package net.minecraft.util;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;

public class EnumTypeAdapterFactory implements TypeAdapterFactory
{
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type)
    {
        Class<T> oclass = (Class<T>)type.getRawType();

        if (!oclass.isEnum())
        {
            return null;
        }
        else
        {
            final Map<String, T> map = Maps.<String, T>newHashMap();

            for (T t : oclass.getEnumConstants())
            {
                map.put(this.normalizeEnumName(t), t);
            }

            return new TypeAdapter<T>()
            {
                public void write(JsonWriter out, T value) throws IOException
                {
                    if (value == null)
                    {
                        out.nullValue();
                    }
                    else
                    {
                        out.value(EnumTypeAdapterFactory.this.normalizeEnumName(value));
                    }
                }
                public T read(JsonReader in) throws IOException
                {
                    if (in.peek() == JsonToken.NULL)
                    {
                        in.nextNull();
                        return (T)null;
                    }
                    else
                    {
                        return (T)map.get(in.nextString());
                    }
                }
            };
        }
    }

    private String normalizeEnumName(Object value)
    {
        return value instanceof Enum ? ((Enum)value).name().toLowerCase(Locale.US) : value.toString().toLowerCase(Locale.US);
    }
}
