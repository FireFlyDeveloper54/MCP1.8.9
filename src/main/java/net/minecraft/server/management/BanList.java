package net.minecraft.server.management;

import com.google.gson.JsonObject;
import java.io.File;
import java.net.SocketAddress;

public class BanList extends UserList<String, IPBanEntry>
{
    public BanList(File bansFile)
    {
        super(bansFile);
    }

    protected UserListEntry<String> createEntry(JsonObject entryData)
    {
        return new IPBanEntry(entryData);
    }

    public boolean isBanned(SocketAddress address)
    {
        String addressText = this.addressToString(address);
        return this.hasEntry(addressText);
    }

    public IPBanEntry getBanEntry(SocketAddress address)
    {
        String addressText = this.addressToString(address);
        return (IPBanEntry)this.getEntry(addressText);
    }

    private String addressToString(SocketAddress address)
    {
        String addressText = address.toString();

        if (addressText.contains("/"))
        {
            addressText = addressText.substring(addressText.indexOf(47) + 1);
        }

        if (addressText.contains(":"))
        {
            addressText = addressText.substring(0, addressText.indexOf(58));
        }

        return addressText;
    }
}
