package net.minecraft.server.management;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import java.io.File;

public class UserListOps extends UserList<GameProfile, UserListOpsEntry>
{
    public UserListOps(File saveFile)
    {
        super(saveFile);
    }

    protected UserListEntry<GameProfile> createEntry(JsonObject entryData)
    {
        return new UserListOpsEntry(entryData);
    }

    public String[] getKeys()
    {
        String[] usernames = new String[this.getValues().size()];
        int index = 0;

        for (UserListOpsEntry userListOpsEntry : this.getValues().values())
        {
            usernames[index++] = ((GameProfile)userListOpsEntry.getValue()).getName();
        }

        return usernames;
    }

    public boolean bypassesPlayerLimit(GameProfile profile)
    {
        UserListOpsEntry userListOpsEntry = (UserListOpsEntry)this.getEntry(profile);
        return userListOpsEntry != null ? userListOpsEntry.bypassesPlayerLimit() : false;
    }

    protected String getObjectKey(GameProfile obj)
    {
        return obj.getId().toString();
    }

    public GameProfile getGameProfileFromName(String username)
    {
        for (UserListOpsEntry userListOpsEntry : this.getValues().values())
        {
            if (username.equalsIgnoreCase(((GameProfile)userListOpsEntry.getValue()).getName()))
            {
                return (GameProfile)userListOpsEntry.getValue();
            }
        }

        return null;
    }
}
