package net.minecraft.client.stream;

import net.minecraft.entity.EntityLivingBase;

public class MetadataCombat extends Metadata
{
    public MetadataCombat(EntityLivingBase player, EntityLivingBase opponent)
    {
        super("player_combat");
        this.addPayloadEntry("player", player.getName());

        if (opponent != null)
        {
            this.addPayloadEntry("primary_opponent", opponent.getName());
        }

        if (opponent != null)
        {
            this.setDescription("Combat between " + player.getName() + " and " + opponent.getName());
        }
        else
        {
            this.setDescription("Combat between " + player.getName() + " and others");
        }
    }
}
