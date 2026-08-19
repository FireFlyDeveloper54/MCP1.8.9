package net.minecraft.server.management;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.io.File;

public class UserListWhitelist extends UserList<GameProfile, UserListWhitelistEntry>
{
    public UserListWhitelist(File saveFile)
    {
        super(saveFile);
    }

    protected UserListEntry<GameProfile> createEntry(JsonObject entryData)
    {
        return new UserListWhitelistEntry(entryData);
    }

    public String[] getKeys()
    {
        String[] usernames = new String[this.getValues().size()];
        int index = 0;

        for (UserListWhitelistEntry userListWhitelistEntry : this.getValues().values())
        {
            usernames[index++] = ((GameProfile)userListWhitelistEntry.getValue()).getName();
        }

        return usernames;
    }

    protected String getObjectKey(GameProfile obj)
    {
        return obj.getId().toString();
    }

    public GameProfile getBannedProfile(String name)
    {
        for (UserListWhitelistEntry userListWhitelistEntry : this.getValues().values())
        {
            if (name.equalsIgnoreCase(((GameProfile)userListWhitelistEntry.getValue()).getName()))
            {
                return (GameProfile)userListWhitelistEntry.getValue();
            }
        }

        return null;
    }
}
