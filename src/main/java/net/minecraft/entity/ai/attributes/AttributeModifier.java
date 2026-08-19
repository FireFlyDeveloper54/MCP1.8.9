package net.minecraft.entity.ai.attributes;

import io.netty.util.internal.ThreadLocalRandom;
import java.util.UUID;
import net.minecraft.util.MathHelper;
import org.apache.commons.lang3.Validate;

public class AttributeModifier
{
    private final double amount;
    private final int operation;
    private final String name;
    private final UUID id;
    private boolean isSaved;

    public AttributeModifier(String nameIn, double amountIn, int operationIn)
    {
        this(MathHelper.getRandomUuid(ThreadLocalRandom.current()), nameIn, amountIn, operationIn);
    }

    public AttributeModifier(UUID idIn, String nameIn, double amountIn, int operationIn)
    {
        this.isSaved = true;
        this.id = idIn;
        this.name = nameIn;
        this.amount = amountIn;
        this.operation = operationIn;
        Validate.notEmpty(nameIn, "Modifier name cannot be empty", new Object[0]);
        Validate.inclusiveBetween(0L, 2L, (long)operationIn, "Invalid operation");
    }

    public UUID getID()
    {
        return this.id;
    }

    public String getName()
    {
        return this.name;
    }

    public int getOperation()
    {
        return this.operation;
    }

    public double getAmount()
    {
        return this.amount;
    }

    public boolean isSaved()
    {
        return this.isSaved;
    }

    public AttributeModifier setSaved(boolean saved)
    {
        this.isSaved = saved;
        return this;
    }

    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }
        else if (other != null && this.getClass() == other.getClass())
        {
            AttributeModifier attributeModifier = (AttributeModifier)other;

            if (this.id != null)
            {
                if (!this.id.equals(attributeModifier.id))
                {
                    return false;
                }
            }
            else if (attributeModifier.id != null)
            {
                return false;
            }

            return true;
        }
        else
        {
            return false;
        }
    }

    public int hashCode()
    {
        return this.id != null ? this.id.hashCode() : 0;
    }

    public String toString()
    {
        return "AttributeModifier{amount=" + this.amount + ", operation=" + this.operation + ", name=\'" + this.name + '\'' + ", id=" + this.id + ", serialize=" + this.isSaved + '}';
    }
}
