package net.minecraft.server.management;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.io.File;

public class UserListBans extends UserList<GameProfile, UserListBansEntry>
{
    public UserListBans(File bansFile)
    {
        super(bansFile);
    }

    protected UserListEntry<GameProfile> createEntry(JsonObject entryData)
    {
        return new UserListBansEntry(entryData);
    }

    public boolean isBanned(GameProfile profile)
    {
        return this.hasEntry(profile);
    }

    public String[] getKeys()
    {
        String[] usernames = new String[this.getValues().size()];
        int index = 0;

        for (UserListBansEntry userListBansEntry : this.getValues().values())
        {
            usernames[index++] = ((GameProfile)userListBansEntry.getValue()).getName();
        }

        return usernames;
    }

    protected String getObjectKey(GameProfile obj)
    {
        return obj.getId().toString();
    }

    public GameProfile isUsernameBanned(String username)
    {
        for (UserListBansEntry userListBansEntry : this.getValues().values())
        {
            if (username.equalsIgnoreCase(((GameProfile)userListBansEntry.getValue()).getName()))
            {
                return (GameProfile)userListBansEntry.getValue();
            }
        }

        return null;
    }
}
