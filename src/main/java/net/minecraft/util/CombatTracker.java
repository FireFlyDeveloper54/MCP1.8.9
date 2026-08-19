package net.minecraft.util;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

public class CombatTracker
{
    private final List<CombatEntry> combatEntries = Lists.<CombatEntry>newArrayList();
    private final EntityLivingBase fighter;
    private int lastDamageTick;
    private int combatStartTick;
    private int combatEndTick;
    private boolean inCombat;
    private boolean hasDamage;
    private String lastEnvironment;
    private final BlockPos.MutableBlockPos blockSamplePos = new BlockPos.MutableBlockPos();

    public CombatTracker(EntityLivingBase fighterIn)
    {
        this.fighter = fighterIn;
    }

    public void updateFallState()
    {
        this.clearEnvironment();

        if (this.fighter.isOnLadder())
        {
            this.blockSamplePos.set(MathHelper.floor_double(this.fighter.posX), MathHelper.floor_double(this.fighter.getEntityBoundingBox().minY), MathHelper.floor_double(this.fighter.posZ));
            Block block = this.fighter.worldObj.getBlockState(this.blockSamplePos).getBlock();

            if (block == Blocks.ladder)
            {
                this.lastEnvironment = "ladder";
            }
            else if (block == Blocks.vine)
            {
                this.lastEnvironment = "vines";
            }
        }
        else if (this.fighter.isInWater())
        {
            this.lastEnvironment = "water";
        }
    }

    public void trackDamage(DamageSource damageSrc, float healthIn, float damageAmount)
    {
        this.reset();
        this.updateFallState();
        CombatEntry combatEntry = new CombatEntry(damageSrc, this.fighter.ticksExisted, healthIn, damageAmount, this.lastEnvironment, this.fighter.fallDistance);
        this.combatEntries.add(combatEntry);
        this.lastDamageTick = this.fighter.ticksExisted;
        this.hasDamage = true;

        if (combatEntry.isLivingDamageSrc() && !this.inCombat && this.fighter.isEntityAlive())
        {
            this.inCombat = true;
            this.combatStartTick = this.fighter.ticksExisted;
            this.combatEndTick = this.combatStartTick;
            this.fighter.sendEnterCombat();
        }
    }

    public IChatComponent getDeathMessage()
    {
        if (this.combatEntries.size() == 0)
        {
            return new ChatComponentTranslation("death.attack.generic", new Object[] {this.fighter.getDisplayName()});
        }
        else
        {
            CombatEntry combatEntry = this.selectBestCombatEntry();
            CombatEntry finalEntry = this.combatEntries.get(this.combatEntries.size() - 1);
            IChatComponent finalDamageSourceName = finalEntry.getDamageSrcDisplayName();
            Entity finalAttacker = finalEntry.getDamageSrc().getEntity();
            IChatComponent deathMessage;

            if (combatEntry != null && finalEntry.getDamageSrc() == DamageSource.fall)
            {
                IChatComponent assistDamageSourceName = combatEntry.getDamageSrcDisplayName();

                if (combatEntry.getDamageSrc() != DamageSource.fall && combatEntry.getDamageSrc() != DamageSource.outOfWorld)
                {
                    if (assistDamageSourceName != null && (finalDamageSourceName == null || !assistDamageSourceName.equals(finalDamageSourceName)))
                    {
                        Entity assistAttacker = combatEntry.getDamageSrc().getEntity();
                        ItemStack assistItem = assistAttacker instanceof EntityLivingBase ? ((EntityLivingBase)assistAttacker).getHeldItem() : null;

                        if (assistItem != null && assistItem.hasDisplayName())
                        {
                            deathMessage = new ChatComponentTranslation("death.fell.assist.item", new Object[] {this.fighter.getDisplayName(), assistDamageSourceName, assistItem.getChatComponent()});
                        }
                        else
                        {
                            deathMessage = new ChatComponentTranslation("death.fell.assist", new Object[] {this.fighter.getDisplayName(), assistDamageSourceName});
                        }
                    }
                    else if (finalDamageSourceName != null)
                    {
                        ItemStack finalItem = finalAttacker instanceof EntityLivingBase ? ((EntityLivingBase)finalAttacker).getHeldItem() : null;

                        if (finalItem != null && finalItem.hasDisplayName())
                        {
                            deathMessage = new ChatComponentTranslation("death.fell.finish.item", new Object[] {this.fighter.getDisplayName(), finalDamageSourceName, finalItem.getChatComponent()});
                        }
                        else
                        {
                            deathMessage = new ChatComponentTranslation("death.fell.finish", new Object[] {this.fighter.getDisplayName(), finalDamageSourceName});
                        }
                    }
                    else
                    {
                        deathMessage = new ChatComponentTranslation("death.fell.killer", new Object[] {this.fighter.getDisplayName()});
                    }
                }
                else
                {
                    deathMessage = new ChatComponentTranslation("death.fell.accident." + this.getFallType(combatEntry), new Object[] {this.fighter.getDisplayName()});
                }
            }
            else
            {
                deathMessage = finalEntry.getDamageSrc().getDeathMessage(this.fighter);
            }

            return deathMessage;
        }
    }

    public EntityLivingBase getBestAttacker()
    {
        EntityLivingBase bestLivingAttacker = null;
        EntityPlayer bestPlayerAttacker = null;
        float bestLivingDamage = 0.0F;
        float bestPlayerDamage = 0.0F;

        for (CombatEntry combatEntry : this.combatEntries)
        {
            if (combatEntry.getDamageSrc().getEntity() instanceof EntityPlayer && (bestPlayerAttacker == null || combatEntry.getDamageAmountInternal() > bestPlayerDamage))
            {
                bestPlayerDamage = combatEntry.getDamageAmountInternal();
                bestPlayerAttacker = (EntityPlayer)combatEntry.getDamageSrc().getEntity();
            }

            if (combatEntry.getDamageSrc().getEntity() instanceof EntityLivingBase && (bestLivingAttacker == null || combatEntry.getDamageAmountInternal() > bestLivingDamage))
            {
                bestLivingDamage = combatEntry.getDamageAmountInternal();
                bestLivingAttacker = (EntityLivingBase)combatEntry.getDamageSrc().getEntity();
            }
        }

        if (bestPlayerAttacker != null && bestPlayerDamage >= bestLivingDamage / 3.0F)
        {
            return bestPlayerAttacker;
        }
        else
        {
            return bestLivingAttacker;
        }
    }

    private CombatEntry selectBestCombatEntry()
    {
        CombatEntry bestFallEntry = null;
        CombatEntry bestEnvironmentEntry = null;
        int bestEnvironmentDamage = 0;
        float bestFallDamage = 0.0F;

        for (int index = 0; index < this.combatEntries.size(); ++index)
        {
            CombatEntry currentEntry = this.combatEntries.get(index);
            CombatEntry previousEntry = index > 0 ? this.combatEntries.get(index - 1) : null;

            if ((currentEntry.getDamageSrc() == DamageSource.fall || currentEntry.getDamageSrc() == DamageSource.outOfWorld) && currentEntry.getDamageAmount() > 0.0F && (bestFallEntry == null || currentEntry.getDamageAmount() > bestFallDamage))
            {
                if (index > 0)
                {
                    bestFallEntry = previousEntry;
                }
                else
                {
                    bestFallEntry = currentEntry;
                }

                bestFallDamage = currentEntry.getDamageAmount();
            }

            if (currentEntry.getEnvironment() != null && (bestEnvironmentEntry == null || currentEntry.getDamageAmountInternal() > (float)bestEnvironmentDamage))
            {
                bestEnvironmentEntry = currentEntry;
                bestEnvironmentDamage = (int)currentEntry.getDamageAmountInternal();
            }
        }

        if (bestFallDamage > 5.0F && bestFallEntry != null)
        {
            return bestFallEntry;
        }
        else if (bestEnvironmentDamage > 5 && bestEnvironmentEntry != null)
        {
            return bestEnvironmentEntry;
        }
        else
        {
            return null;
        }
    }

    private String getFallType(CombatEntry entry)
    {
        return entry.getEnvironment() == null ? "generic" : entry.getEnvironment();
    }

    public int getCombatDuration()
    {
        return this.inCombat ? this.fighter.ticksExisted - this.combatStartTick : this.combatEndTick - this.combatStartTick;
    }

    private void clearEnvironment()
    {
        this.lastEnvironment = null;
    }

    public void reset()
    {
        int timeoutTicks = this.inCombat ? 300 : 100;

        if (this.hasDamage && (!this.fighter.isEntityAlive() || this.fighter.ticksExisted - this.lastDamageTick > timeoutTicks))
        {
            boolean wasInCombat = this.inCombat;
            this.hasDamage = false;
            this.inCombat = false;
            this.combatEndTick = this.fighter.ticksExisted;

            if (wasInCombat)
            {
                this.fighter.sendEndCombat();
            }

            this.combatEntries.clear();
        }
    }

    public EntityLivingBase getFighter()
    {
        return this.fighter;
    }
}
