package net.minecraft.server.management;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.util.UUID;

public class UserListOpsEntry extends UserListEntry<GameProfile>
{
    private final int permissionLevel;
    private final boolean bypassesPlayerLimit;

    public UserListOpsEntry(GameProfile player, int permissionLevelIn, boolean bypassesPlayerLimitIn)
    {
        super(player);
        this.permissionLevel = permissionLevelIn;
        this.bypassesPlayerLimit = bypassesPlayerLimitIn;
    }

    public UserListOpsEntry(JsonObject json)
    {
        super(constructProfile(json), json);
        this.permissionLevel = json.has("level") ? json.get("level").getAsInt() : 0;
        this.bypassesPlayerLimit = json.has("bypassesPlayerLimit") && json.get("bypassesPlayerLimit").getAsBoolean();
    }

    public int getPermissionLevel()
    {
        return this.permissionLevel;
    }

    public boolean bypassesPlayerLimit()
    {
        return this.bypassesPlayerLimit;
    }

    protected void onSerialization(JsonObject data)
    {
        if (this.getValue() != null)
        {
            data.addProperty("uuid", ((GameProfile)this.getValue()).getId() == null ? "" : ((GameProfile)this.getValue()).getId().toString());
            data.addProperty("name", ((GameProfile)this.getValue()).getName());
            super.onSerialization(data);
            data.addProperty("level", (Number)Integer.valueOf(this.permissionLevel));
            data.addProperty("bypassesPlayerLimit", Boolean.valueOf(this.bypassesPlayerLimit));
        }
    }

    private static GameProfile constructProfile(JsonObject json)
    {
        if (json.has("uuid") && json.has("name"))
        {
            String uuidText = json.get("uuid").getAsString();
            UUID uUID;

            try
            {
                uUID = UUID.fromString(uuidText);
            }
            catch (Throwable caughtThrowable)
            {
                return null;
            }

            return new GameProfile(uUID, json.get("name").getAsString());
        }
        else
        {
            return null;
        }
    }
}
