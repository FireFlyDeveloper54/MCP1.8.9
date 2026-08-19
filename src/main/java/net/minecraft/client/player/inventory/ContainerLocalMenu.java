package net.minecraft.client.player.inventory;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.LockCode;

public class ContainerLocalMenu extends InventoryBasic implements ILockableContainer
{
    private String guiID;
    private Map<Integer, Integer> fieldValues = Maps.<Integer, Integer>newHashMap();

    public ContainerLocalMenu(String id, IChatComponent title, int slotCount)
    {
        super(title, slotCount);
        this.guiID = id;
    }

    public int getField(int id)
    {
        Integer fieldValue = this.fieldValues.get(Integer.valueOf(id));
        return fieldValue == null ? 0 : fieldValue.intValue();
    }

    public void setField(int id, int value)
    {
        this.fieldValues.put(Integer.valueOf(id), Integer.valueOf(value));
    }

    public int getFieldCount()
    {
        return this.fieldValues.size();
    }

    public boolean isLocked()
    {
        return false;
    }

    public void setLockCode(LockCode code)
    {
    }

    public LockCode getLockCode()
    {
        return LockCode.EMPTY_CODE;
    }

    public String getGuiID()
    {
        return this.guiID;
    }

    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn)
    {
        throw new UnsupportedOperationException();
    }
}
