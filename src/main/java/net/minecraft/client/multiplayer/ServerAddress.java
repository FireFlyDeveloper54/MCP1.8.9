package net.minecraft.client.multiplayer;

import java.net.IDN;
import java.util.Hashtable;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

public class ServerAddress
{
    private final String ipAddress;
    private final int serverPort;

    private ServerAddress(String address, int port)
    {
        this.ipAddress = address;
        this.serverPort = port;
    }

    public String getIP()
    {
        return IDN.toASCII(this.ipAddress);
    }

    public int getPort()
    {
        return this.serverPort;
    }

    public static ServerAddress fromString(String address)
    {
        if (address == null)
        {
            return null;
        }
        else
        {
            String[] astring = address.split(":");

            if (address.startsWith("["))
            {
                int i = address.indexOf("]");

                if (i > 0)
                {
                    String s = address.substring(1, i);
                    String portSuffix = address.substring(i + 1).trim();

                    if (portSuffix.startsWith(":") && portSuffix.length() > 0)
                    {
                        portSuffix = portSuffix.substring(1);
                        astring = new String[] {s, portSuffix};
                    }
                    else
                    {
                        astring = new String[] {s};
                    }
                }
            }

            if (astring.length > 2)
            {
                astring = new String[] {address};
            }

            String host = astring[0];
            int j = astring.length > 1 ? parseIntWithDefault(astring[1], 25565) : 25565;

            if (j == 25565)
            {
                String[] astring1 = getServerAddress(host);
                host = astring1[0];
                j = parseIntWithDefault(astring1[1], 25565);
            }

            return new ServerAddress(host, j);
        }
    }

    private static String[] getServerAddress(String address)
    {
        try
        {
            String s = "com.sun.jndi.dns.DnsContextFactory";
            Class.forName("com.sun.jndi.dns.DnsContextFactory");
            Hashtable hashtable = new Hashtable();
            hashtable.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
            hashtable.put("java.naming.provider.url", "dns:");
            hashtable.put("com.sun.jndi.dns.timeout.retries", "1");
            DirContext dircontext = new InitialDirContext(hashtable);
            Attributes attributes = dircontext.getAttributes("_minecraft._tcp." + address, new String[] {"SRV"});
            String[] astring = attributes.get("srv").get().toString().split(" ", 4);
            return new String[] {astring[3], astring[2]};
        }
        catch (Throwable caughtThrowable)
        {
            return new String[] {address, Integer.toString(25565)};
        }
    }

    private static int parseIntWithDefault(String value, int defaultValue)
    {
        try
        {
            return Integer.parseInt(value.trim());
        }
        catch (Exception caughtException)
        {
            return defaultValue;
        }
    }
}
