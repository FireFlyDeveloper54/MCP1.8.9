package net.minecraft.nbt;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import java.util.UUID;
import net.minecraft.util.StringUtils;

public final class NBTUtil
{
    public static GameProfile readGameProfileFromNBT(NBTTagCompound compound)
    {
        String s = null;
        String text2 = null;

        if (compound.hasKey("Name", 8))
        {
            s = compound.getString("Name");
        }

        if (compound.hasKey("Id", 8))
        {
            text2 = compound.getString("Id");
        }

        if (StringUtils.isNullOrEmpty(s) && StringUtils.isNullOrEmpty(text2))
        {
            return null;
        }
        else
        {
            UUID uUID;

            try
            {
                uUID = UUID.fromString(text2);
            }
            catch (Throwable caughtThrowable)
            {
                uUID = null;
            }

            GameProfile gameprofile = new GameProfile(uUID, s);

            if (compound.hasKey("Properties", 10))
            {
                NBTTagCompound nBTTagCompound = compound.getCompoundTag("Properties");

                for (String text3 : nBTTagCompound.getKeySet())
                {
                    NBTTagList nBTTagList = nBTTagCompound.getTagList(text3, 10);

                    for (int i = 0; i < nBTTagList.tagCount(); ++i)
                    {
                        NBTTagCompound nbttagcompound1 = nBTTagList.getCompoundTagAt(i);
                        String text4 = nbttagcompound1.getString("Value");

                        if (nbttagcompound1.hasKey("Signature", 8))
                        {
                            gameprofile.getProperties().put(text3, new Property(text3, text4, nbttagcompound1.getString("Signature")));
                        }
                        else
                        {
                            gameprofile.getProperties().put(text3, new Property(text3, text4));
                        }
                    }
                }
            }

            return gameprofile;
        }
    }

    public static NBTTagCompound writeGameProfile(NBTTagCompound tagCompound, GameProfile profile)
    {
        if (!StringUtils.isNullOrEmpty(profile.getName()))
        {
            tagCompound.setString("Name", profile.getName());
        }

        if (profile.getId() != null)
        {
            tagCompound.setString("Id", profile.getId().toString());
        }

        if (!profile.getProperties().isEmpty())
        {
            NBTTagCompound nBTTagCompound = new NBTTagCompound();

            for (String s : profile.getProperties().keySet())
            {
                NBTTagList nBTTagList = new NBTTagList();

                for (Property property : profile.getProperties().get(s))
                {
                    NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                    nbttagcompound1.setString("Value", property.getValue());

                    if (property.hasSignature())
                    {
                        nbttagcompound1.setString("Signature", property.getSignature());
                    }

                    nBTTagList.appendTag(nbttagcompound1);
                }

                nBTTagCompound.setTag(s, nBTTagList);
            }

            tagCompound.setTag("Properties", nBTTagCompound);
        }

        return tagCompound;
    }

    public static boolean compareTags(NBTBase firstTag, NBTBase secondTag, boolean allowListOrder)
    {
        if (firstTag == secondTag)
        {
            return true;
        }
        else if (firstTag == null)
        {
            return true;
        }
        else if (secondTag == null)
        {
            return false;
        }
        else if (!firstTag.getClass().equals(secondTag.getClass()))
        {
            return false;
        }
        else if (firstTag instanceof NBTTagCompound)
        {
            NBTTagCompound firstCompound = (NBTTagCompound)firstTag;
            NBTTagCompound secondCompound = (NBTTagCompound)secondTag;

            for (String key : firstCompound.getKeySet())
            {
                NBTBase firstChildTag = firstCompound.getTag(key);

                if (!compareTags(firstChildTag, secondCompound.getTag(key), allowListOrder))
                {
                    return false;
                }
            }

            return true;
        }
        else if (firstTag instanceof NBTTagList && allowListOrder)
        {
            NBTTagList firstList = (NBTTagList)firstTag;
            NBTTagList secondList = (NBTTagList)secondTag;

            if (firstList.tagCount() == 0)
            {
                return secondList.tagCount() == 0;
            }
            else
            {
                for (int i = 0; i < firstList.tagCount(); ++i)
                {
                    NBTBase element = firstList.get(i);
                    boolean matched = false;

                    for (int j = 0; j < secondList.tagCount(); ++j)
                    {
                        if (compareTags(element, secondList.get(j), allowListOrder))
                        {
                            matched = true;
                            break;
                        }
                    }

                    if (!matched)
                    {
                        return false;
                    }
                }

                return true;
            }
        }
        else
        {
            return firstTag.equals(secondTag);
        }
    }
}
