package net.minecraft.scoreboard;

import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;

public class ScoreHealthCriteria extends ScoreDummyCriteria
{
    public ScoreHealthCriteria(String name)
    {
        super(name);
    }

    public int setScore(List<EntityPlayer> players)
    {
        float healthValue = 0.0F;

        for (EntityPlayer entityPlayer : players)
        {
            healthValue += entityPlayer.getHealth() + entityPlayer.getAbsorptionAmount();
        }

        if (players.size() > 0)
        {
            healthValue /= (float)players.size();
        }

        return MathHelper.ceiling_float_int(healthValue);
    }

    public boolean isReadOnly()
    {
        return true;
    }

    public IScoreObjectiveCriteria.EnumRenderType getRenderType()
    {
        return IScoreObjectiveCriteria.EnumRenderType.HEARTS;
    }
}
