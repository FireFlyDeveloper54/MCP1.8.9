package net.minecraft.entity;

import java.util.Collection;
import java.util.UUID;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.BaseAttributeMap;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SharedMonsterAttributes
{
    private static final Logger logger = LogManager.getLogger();
    public static final IAttribute maxHealth = (new RangedAttribute((IAttribute)null, "generic.maxHealth", 20.0D, 0.0D, 1024.0D)).setDescription("Max Health").setShouldWatch(true);
    public static final IAttribute followRange = (new RangedAttribute((IAttribute)null, "generic.followRange", 32.0D, 0.0D, 2048.0D)).setDescription("Follow Range");
    public static final IAttribute knockbackResistance = (new RangedAttribute((IAttribute)null, "generic.knockbackResistance", 0.0D, 0.0D, 1.0D)).setDescription("Knockback Resistance");
    public static final IAttribute movementSpeed = (new RangedAttribute((IAttribute)null, "generic.movementSpeed", 0.699999988079071D, 0.0D, 1024.0D)).setDescription("Movement Speed").setShouldWatch(true);
    public static final IAttribute attackDamage = new RangedAttribute((IAttribute)null, "generic.attackDamage", 2.0D, 0.0D, 2048.0D);

    public static NBTTagList writeBaseAttributeMapToNBT(BaseAttributeMap map)
    {
        NBTTagList nBTTagList = new NBTTagList();

        for (IAttributeInstance iattributeinstance : map.getAllAttributes())
        {
            nBTTagList.appendTag(writeAttributeInstanceToNBT(iattributeinstance));
        }

        return nBTTagList;
    }

    private static NBTTagCompound writeAttributeInstanceToNBT(IAttributeInstance instance)
    {
        NBTTagCompound nBTTagCompound = new NBTTagCompound();
        IAttribute iattribute = instance.getAttribute();
        nBTTagCompound.setString("Name", iattribute.getAttributeUnlocalizedName());
        nBTTagCompound.setDouble("Base", instance.getBaseValue());
        Collection<AttributeModifier> collection = instance.getAllModifiers();

        if (collection != null && !collection.isEmpty())
        {
            NBTTagList nBTTagList = new NBTTagList();

            for (AttributeModifier attributeModifier : collection)
            {
                if (attributeModifier.isSaved())
                {
                    nBTTagList.appendTag(writeAttributeModifierToNBT(attributeModifier));
                }
            }

            nBTTagCompound.setTag("Modifiers", nBTTagList);
        }

        return nBTTagCompound;
    }

    private static NBTTagCompound writeAttributeModifierToNBT(AttributeModifier modifier)
    {
        NBTTagCompound nBTTagCompound = new NBTTagCompound();
        nBTTagCompound.setString("Name", modifier.getName());
        nBTTagCompound.setDouble("Amount", modifier.getAmount());
        nBTTagCompound.setInteger("Operation", modifier.getOperation());
        nBTTagCompound.setLong("UUIDMost", modifier.getID().getMostSignificantBits());
        nBTTagCompound.setLong("UUIDLeast", modifier.getID().getLeastSignificantBits());
        return nBTTagCompound;
    }

    public static void setAttributeModifiers(BaseAttributeMap map, NBTTagList list)
    {
        for (int i = 0; i < list.tagCount(); ++i)
        {
            NBTTagCompound nBTTagCompound = list.getCompoundTagAt(i);
            IAttributeInstance iattributeinstance = map.getAttributeInstanceByName(nBTTagCompound.getString("Name"));

            if (iattributeinstance != null)
            {
                applyModifiersToAttributeInstance(iattributeinstance, nBTTagCompound);
            }
            else
            {
                logger.warn("Ignoring unknown attribute \'" + nBTTagCompound.getString("Name") + "\'");
            }
        }
    }

    private static void applyModifiersToAttributeInstance(IAttributeInstance instance, NBTTagCompound compound)
    {
        instance.setBaseValue(compound.getDouble("Base"));

        if (compound.hasKey("Modifiers", 9))
        {
            NBTTagList nBTTagList = compound.getTagList("Modifiers", 10);

            for (int i = 0; i < nBTTagList.tagCount(); ++i)
            {
                AttributeModifier attributeModifier = readAttributeModifierFromNBT(nBTTagList.getCompoundTagAt(i));

                if (attributeModifier != null)
                {
                    AttributeModifier attributemodifier1 = instance.getModifier(attributeModifier.getID());

                    if (attributemodifier1 != null)
                    {
                        instance.removeModifier(attributemodifier1);
                    }

                    instance.applyModifier(attributeModifier);
                }
            }
        }
    }

    public static AttributeModifier readAttributeModifierFromNBT(NBTTagCompound compound)
    {
        UUID uUID = new UUID(compound.getLong("UUIDMost"), compound.getLong("UUIDLeast"));

        try
        {
            return new AttributeModifier(uUID, compound.getString("Name"), compound.getDouble("Amount"), compound.getInteger("Operation"));
        }
        catch (Exception exception)
        {
            logger.warn("Unable to create attribute: " + exception.getMessage());
            return null;
        }
    }
}
