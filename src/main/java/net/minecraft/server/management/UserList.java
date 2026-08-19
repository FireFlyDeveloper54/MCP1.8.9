package net.minecraft.server.management;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UserList<K, V extends UserListEntry<K>>
{
    protected static final Logger logger = LogManager.getLogger();
    protected final Gson gson;
    private final File saveFile;
    private final Map<String, V> values = Maps.<String, V>newHashMap();
    private boolean lanServer = true;
    private static final ParameterizedType saveFileFormat = new ParameterizedType()
    {
        public Type[] getActualTypeArguments()
        {
            return new Type[] {UserListEntry.class};
        }
        public Type getRawType()
        {
            return List.class;
        }
        public Type getOwnerType()
        {
            return null;
        }
    };

    public UserList(File saveFile)
    {
        this.saveFile = saveFile;
        GsonBuilder gsonBuilder = (new GsonBuilder()).setPrettyPrinting();
        gsonBuilder.registerTypeHierarchyAdapter(UserListEntry.class, new UserList.Serializer());
        this.gson = gsonBuilder.create();
    }

    public boolean isLanServer()
    {
        return this.lanServer;
    }

    public void setLanServer(boolean state)
    {
        this.lanServer = state;
    }

    public void addEntry(V entry)
    {
        this.values.put(this.getObjectKey(entry.getValue()), entry);

        try
        {
            this.writeChanges();
        }
        catch (IOException iOException)
        {
            logger.warn((String)"Could not save the list after adding a user.", (Throwable)iOException);
        }
    }

    public V getEntry(K obj)
    {
        this.removeExpired();
        return (V)((UserListEntry)this.values.get(this.getObjectKey(obj)));
    }

    public void removeEntry(K entry)
    {
        this.values.remove(this.getObjectKey(entry));

        try
        {
            this.writeChanges();
        }
        catch (IOException iOException)
        {
            logger.warn((String)"Could not save the list after removing a user.", (Throwable)iOException);
        }
    }

    public String[] getKeys()
    {
        return (String[])this.values.keySet().toArray(new String[this.values.size()]);
    }

    protected String getObjectKey(K obj)
    {
        return obj.toString();
    }

    protected boolean hasEntry(K entry)
    {
        return this.values.containsKey(this.getObjectKey(entry));
    }

    private void removeExpired()
    {
        List<K> expiredValues = Lists.<K>newArrayList();

        for (V entry : this.values.values())
        {
            if (entry.hasBanExpired())
            {
                expiredValues.add(entry.getValue());
            }
        }

        for (K value : expiredValues)
        {
            this.values.remove(value);
        }
    }

    protected UserListEntry<K> createEntry(JsonObject entryData)
    {
        return new UserListEntry((Object)null, entryData);
    }

    protected Map<String, V> getValues()
    {
        return this.values;
    }

    public void writeChanges() throws IOException
    {
        Collection<V> collection = this.values.values();
        String serializedList = this.gson.toJson((Object)collection);
        BufferedWriter bufferedWriter = null;

        try
        {
            bufferedWriter = Files.newWriter(this.saveFile, Charsets.UTF_8);
            bufferedWriter.write(serializedList);
        }
        finally
        {
            IOUtils.closeQuietly((Writer)bufferedWriter);
        }
    }

    class Serializer implements JsonDeserializer<UserListEntry<K>>, JsonSerializer<UserListEntry<K>>
    {
        private Serializer()
        {
        }

        public JsonElement serialize(UserListEntry<K> entry, Type typeOfSrc, JsonSerializationContext context)
        {
            JsonObject jsonObject = new JsonObject();
            entry.onSerialization(jsonObject);
            return jsonObject;
        }

        public UserListEntry<K> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            if (json.isJsonObject())
            {
                JsonObject jsonObject = json.getAsJsonObject();
                UserListEntry<K> userListEntry = UserList.this.createEntry(jsonObject);
                return userListEntry;
            }
            else
            {
                return null;
            }
        }
    }
}
