package net.minecraft.client.stream;

import net.minecraft.entity.EntityLivingBase;

public class MetadataPlayerDeath extends Metadata
{
    public MetadataPlayerDeath(EntityLivingBase player, EntityLivingBase killer)
    {
        super("player_death");

        if (player != null)
        {
            this.addPayloadEntry("player", player.getName());
        }

        if (killer != null)
        {
            this.addPayloadEntry("killer", killer.getName());
        }
    }
}
