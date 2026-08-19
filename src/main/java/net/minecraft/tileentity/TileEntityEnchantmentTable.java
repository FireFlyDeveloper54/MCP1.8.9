package net.minecraft.tileentity;

import java.util.Random;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerEnchantment;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ITickable;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IInteractionObject;

public class TileEntityEnchantmentTable extends TileEntity implements ITickable, IInteractionObject
{
    public int tickCount;
    public float pageFlip;
    public float pageFlipPrev;
    public float pageFlipTarget;
    public float pageFlipSpeed;
    public float bookSpread;
    public float bookSpreadPrev;
    public float bookRotation;
    public float bookRotationPrev;
    public float targetBookRotation;
    private static Random rand = new Random();
    private String customName;

    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);

        if (this.hasCustomName())
        {
            compound.setString("CustomName", this.customName);
        }
    }

    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        if (compound.hasKey("CustomName", 8))
        {
            this.customName = compound.getString("CustomName");
        }
    }

    public void update()
    {
        this.bookSpreadPrev = this.bookSpread;
        this.bookRotationPrev = this.bookRotation;
        EntityPlayer entityPlayer = this.worldObj.getClosestPlayer((double)((float)this.pos.getX() + 0.5F), (double)((float)this.pos.getY() + 0.5F), (double)((float)this.pos.getZ() + 0.5F), 3.0D);

        if (entityPlayer != null)
        {
            double xCoordinate = entityPlayer.posX - (double)((float)this.pos.getX() + 0.5F);
            double zCoordinate = entityPlayer.posZ - (double)((float)this.pos.getZ() + 0.5F);
            this.targetBookRotation = (float)MathHelper.atan2(zCoordinate, xCoordinate);
            this.bookSpread += 0.1F;

            if (this.bookSpread < 0.5F || rand.nextInt(40) == 0)
            {
                float floatValue = this.pageFlipTarget;

                while (true)
                {
                    this.pageFlipTarget += (float)(rand.nextInt(4) - rand.nextInt(4));

                    if (floatValue != this.pageFlipTarget)
                    {
                        break;
                    }
                }
            }
        }
        else
        {
            this.targetBookRotation += 0.02F;
            this.bookSpread -= 0.1F;
        }

        while (this.bookRotation >= (float)Math.PI)
        {
            this.bookRotation -= ((float)Math.PI * 2F);
        }

        while (this.bookRotation < -(float)Math.PI)
        {
            this.bookRotation += ((float)Math.PI * 2F);
        }

        while (this.targetBookRotation >= (float)Math.PI)
        {
            this.targetBookRotation -= ((float)Math.PI * 2F);
        }

        while (this.targetBookRotation < -(float)Math.PI)
        {
            this.targetBookRotation += ((float)Math.PI * 2F);
        }

        float secondFloatValue;

        for (secondFloatValue = this.targetBookRotation - this.bookRotation; secondFloatValue >= (float)Math.PI; secondFloatValue -= ((float)Math.PI * 2F))
        {
            ;
        }

        while (secondFloatValue < -(float)Math.PI)
        {
            secondFloatValue += ((float)Math.PI * 2F);
        }

        this.bookRotation += secondFloatValue * 0.4F;
        this.bookSpread = MathHelper.clamp_float(this.bookSpread, 0.0F, 1.0F);
        ++this.tickCount;
        this.pageFlipPrev = this.pageFlip;
        float f = (this.pageFlipTarget - this.pageFlip) * 0.4F;
        float floatValue4 = 0.2F;
        f = MathHelper.clamp_float(f, -floatValue4, floatValue4);
        this.pageFlipSpeed += (f - this.pageFlipSpeed) * 0.9F;
        this.pageFlip += this.pageFlipSpeed;
    }

    public String getName()
    {
        return this.hasCustomName() ? this.customName : "container.enchant";
    }

    public boolean hasCustomName()
    {
        return this.customName != null && this.customName.length() > 0;
    }

    public void setCustomName(String customNameIn)
    {
        this.customName = customNameIn;
    }

    public IChatComponent getDisplayName()
    {
        return (IChatComponent)(this.hasCustomName() ? new ChatComponentText(this.getName()) : new ChatComponentTranslation(this.getName(), new Object[0]));
    }

    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn)
    {
        return new ContainerEnchantment(playerInventory, this.worldObj, this.pos);
    }

    public String getGuiID()
    {
        return "minecraft:enchanting_table";
    }
}
