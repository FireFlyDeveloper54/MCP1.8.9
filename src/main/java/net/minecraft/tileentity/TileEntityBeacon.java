package net.minecraft.tileentity;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStainedGlass;
import net.minecraft.block.BlockStainedGlassPane;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerBeacon;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.stats.AchievementList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ITickable;

public class TileEntityBeacon extends TileEntityLockable implements ITickable, IInventory
{
    public static final Potion[][] effectsList = new Potion[][] {{Potion.moveSpeed, Potion.digSpeed}, {Potion.resistance, Potion.jump}, {Potion.damageBoost}, {Potion.regeneration}};
    private final List<TileEntityBeacon.BeamSegment> beamSegments = Lists.<TileEntityBeacon.BeamSegment>newArrayList();
    private long beamRenderCounter;
    private float beamRenderScale;
    private boolean isComplete;
    private int levels = -1;
    private int primaryEffect;
    private int secondaryEffect;
    private ItemStack payment;
    private String customName;

    public void update()
    {
        if (this.worldObj.getTotalWorldTime() % 80L == 0L)
        {
            this.updateBeacon();
        }
    }

    public void updateBeacon()
    {
        this.updateSegmentColors();
        this.addEffectsToPlayers();
    }

    private void addEffectsToPlayers()
    {
        if (this.isComplete && this.levels > 0 && !this.worldObj.isRemote && this.primaryEffect > 0)
        {
            double doubleValue = (double)(this.levels * 10 + 10);
            int i = 0;

            if (this.levels >= 4 && this.primaryEffect == this.secondaryEffect)
            {
                i = 1;
            }

            int j = this.pos.getX();
            int k = this.pos.getY();
            int l = this.pos.getZ();
            AxisAlignedBB axisalignedbb = (new AxisAlignedBB((double)j, (double)k, (double)l, (double)(j + 1), (double)(k + 1), (double)(l + 1))).expand(doubleValue, doubleValue, doubleValue).addCoord(0.0D, (double)this.worldObj.getHeight(), 0.0D);
            List<EntityPlayer> list = this.worldObj.<EntityPlayer>getEntitiesWithinAABB(EntityPlayer.class, axisalignedbb);

            for (EntityPlayer entityPlayer : list)
            {
                entityPlayer.addPotionEffect(new PotionEffect(this.primaryEffect, 180, i, true, true));
            }

            if (this.levels >= 4 && this.primaryEffect != this.secondaryEffect && this.secondaryEffect > 0)
            {
                for (EntityPlayer entityplayer1 : list)
                {
                    entityplayer1.addPotionEffect(new PotionEffect(this.secondaryEffect, 180, 0, true, true));
                }
            }
        }
    }

    private void updateSegmentColors()
    {
        int i = this.levels;
        int j = this.pos.getX();
        int k = this.pos.getY();
        int l = this.pos.getZ();
        this.levels = 0;
        this.beamSegments.clear();
        this.isComplete = true;
        TileEntityBeacon.BeamSegment tileentitybeacon$beamsegment = new TileEntityBeacon.BeamSegment(EntitySheep.getDyeRgb(EnumDyeColor.WHITE));
        this.beamSegments.add(tileentitybeacon$beamsegment);
        boolean flag = true;
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

        for (int index = k + 1; index < 256; ++index)
        {
            IBlockState iblockstate = this.worldObj.getBlockState(blockpos$mutableblockpos.set(j, index, l));
            float[] afloat;

            if (iblockstate.getBlock() == Blocks.stained_glass)
            {
                afloat = EntitySheep.getDyeRgb((EnumDyeColor)iblockstate.getValue(BlockStainedGlass.COLOR));
            }
            else
            {
                if (iblockstate.getBlock() != Blocks.stained_glass_pane)
                {
                    if (iblockstate.getBlock().getLightOpacity() >= 15 && iblockstate.getBlock() != Blocks.bedrock)
                    {
                        this.isComplete = false;
                        this.beamSegments.clear();
                        break;
                    }

                    tileentitybeacon$beamsegment.incrementHeight();
                    continue;
                }

                afloat = EntitySheep.getDyeRgb((EnumDyeColor)iblockstate.getValue(BlockStainedGlassPane.COLOR));
            }

            if (!flag)
            {
                afloat = new float[] {(tileentitybeacon$beamsegment.getColors()[0] + afloat[0]) / 2.0F, (tileentitybeacon$beamsegment.getColors()[1] + afloat[1]) / 2.0F, (tileentitybeacon$beamsegment.getColors()[2] + afloat[2]) / 2.0F};
            }

            if (Arrays.equals(afloat, tileentitybeacon$beamsegment.getColors()))
            {
                tileentitybeacon$beamsegment.incrementHeight();
            }
            else
            {
                tileentitybeacon$beamsegment = new TileEntityBeacon.BeamSegment(afloat);
                this.beamSegments.add(tileentitybeacon$beamsegment);
            }

            flag = false;
        }

        if (this.isComplete)
        {
            for (int outerIndex = 1; outerIndex <= 4; this.levels = outerIndex++)
            {
                int intValue2 = k - outerIndex;

                if (intValue2 < 0)
                {
                    break;
                }

                boolean flag1 = true;

                for (int innerIndex = j - outerIndex; innerIndex <= j + outerIndex && flag1; ++innerIndex)
                {
                    for (int nestedIndex = l - outerIndex; nestedIndex <= l + outerIndex; ++nestedIndex)
                    {
                        Block block = this.worldObj.getBlockState(blockpos$mutableblockpos.set(innerIndex, intValue2, nestedIndex)).getBlock();

                        if (block != Blocks.emerald_block && block != Blocks.gold_block && block != Blocks.diamond_block && block != Blocks.iron_block)
                        {
                            flag1 = false;
                            break;
                        }
                    }
                }

                if (!flag1)
                {
                    break;
                }
            }

            if (this.levels == 0)
            {
                this.isComplete = false;
            }
        }

        if (!this.worldObj.isRemote && this.levels == 4 && i < this.levels)
        {
            for (EntityPlayer entityPlayer : this.worldObj.getEntitiesWithinAABB(EntityPlayer.class, (new AxisAlignedBB((double)j, (double)k, (double)l, (double)j, (double)(k - 4), (double)l)).expand(10.0D, 5.0D, 10.0D)))
            {
                entityPlayer.triggerAchievement(AchievementList.fullBeacon);
            }
        }
    }

    public List<TileEntityBeacon.BeamSegment> getBeamSegments()
    {
        return this.beamSegments;
    }

    public float shouldBeamRender()
    {
        if (!this.isComplete)
        {
            return 0.0F;
        }
        else
        {
            int i = (int)(this.worldObj.getTotalWorldTime() - this.beamRenderCounter);
            this.beamRenderCounter = this.worldObj.getTotalWorldTime();

            if (i > 1)
            {
                this.beamRenderScale -= (float)i / 40.0F;

                if (this.beamRenderScale < 0.0F)
                {
                    this.beamRenderScale = 0.0F;
                }
            }

            this.beamRenderScale += 0.025F;

            if (this.beamRenderScale > 1.0F)
            {
                this.beamRenderScale = 1.0F;
            }

            return this.beamRenderScale;
        }
    }

    public Packet getDescriptionPacket()
    {
        NBTTagCompound nBTTagCompound = new NBTTagCompound();
        this.writeToNBT(nBTTagCompound);
        return new S35PacketUpdateTileEntity(this.pos, 3, nBTTagCompound);
    }

    public double getMaxRenderDistanceSquared()
    {
        return 65536.0D;
    }

    private int validateEffectId(int effectId)
    {
        if (effectId >= 0 && effectId < Potion.potionTypes.length && Potion.potionTypes[effectId] != null)
        {
            Potion potion = Potion.potionTypes[effectId];
            return potion != Potion.moveSpeed && potion != Potion.digSpeed && potion != Potion.resistance && potion != Potion.jump && potion != Potion.damageBoost && potion != Potion.regeneration ? 0 : effectId;
        }
        else
        {
            return 0;
        }
    }

    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        this.primaryEffect = this.validateEffectId(compound.getInteger("Primary"));
        this.secondaryEffect = this.validateEffectId(compound.getInteger("Secondary"));
        this.levels = compound.getInteger("Levels");
    }

    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        compound.setInteger("Primary", this.primaryEffect);
        compound.setInteger("Secondary", this.secondaryEffect);
        compound.setInteger("Levels", this.levels);
    }

    public int getSizeInventory()
    {
        return 1;
    }

    public ItemStack getStackInSlot(int index)
    {
        return index == 0 ? this.payment : null;
    }

    public ItemStack decrStackSize(int index, int count)
    {
        if (index == 0 && this.payment != null)
        {
            if (count >= this.payment.stackSize)
            {
                ItemStack itemStack = this.payment;
                this.payment = null;
                return itemStack;
            }
            else
            {
                this.payment.stackSize -= count;
                return new ItemStack(this.payment.getItem(), count, this.payment.getMetadata());
            }
        }
        else
        {
            return null;
        }
    }

    public ItemStack removeStackFromSlot(int index)
    {
        if (index == 0 && this.payment != null)
        {
            ItemStack itemStack = this.payment;
            this.payment = null;
            return itemStack;
        }
        else
        {
            return null;
        }
    }

    public void setInventorySlotContents(int index, ItemStack stack)
    {
        if (index == 0)
        {
            this.payment = stack;
        }
    }

    public String getName()
    {
        return this.hasCustomName() ? this.customName : "container.beacon";
    }

    public boolean hasCustomName()
    {
        return this.customName != null && this.customName.length() > 0;
    }

    public void setName(String name)
    {
        this.customName = name;
    }

    public int getInventoryStackLimit()
    {
        return 1;
    }

    public boolean isUseableByPlayer(EntityPlayer player)
    {
        return this.worldObj.getTileEntity(this.pos) != this ? false : player.getDistanceSq((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) <= 64.0D;
    }

    public void openInventory(EntityPlayer player)
    {
    }

    public void closeInventory(EntityPlayer player)
    {
    }

    public boolean isItemValidForSlot(int index, ItemStack stack)
    {
        return stack.getItem() == Items.emerald || stack.getItem() == Items.diamond || stack.getItem() == Items.gold_ingot || stack.getItem() == Items.iron_ingot;
    }

    public String getGuiID()
    {
        return "minecraft:beacon";
    }

    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn)
    {
        return new ContainerBeacon(playerInventory, this);
    }

    public int getField(int id)
    {
        switch (id)
        {
            case 0:
                return this.levels;

            case 1:
                return this.primaryEffect;

            case 2:
                return this.secondaryEffect;

            default:
                return 0;
        }
    }

    public void setField(int id, int value)
    {
        switch (id)
        {
            case 0:
                this.levels = value;
                break;

            case 1:
                this.primaryEffect = this.validateEffectId(value);
                break;

            case 2:
                this.secondaryEffect = this.validateEffectId(value);
        }
    }

    public int getFieldCount()
    {
        return 3;
    }

    public void clear()
    {
        this.payment = null;
    }

    public boolean receiveClientEvent(int id, int type)
    {
        if (id == 1)
        {
            this.updateBeacon();
            return true;
        }
        else
        {
            return super.receiveClientEvent(id, type);
        }
    }

    public static class BeamSegment
    {
        private final float[] colors;
        private int height;

        public BeamSegment(float[] colorsIn)
        {
            this.colors = colorsIn;
            this.height = 1;
        }

        protected void incrementHeight()
        {
            ++this.height;
        }

        public float[] getColors()
        {
            return this.colors;
        }

        public int getHeight()
        {
            return this.height;
        }
    }
}
