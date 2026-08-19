package net.minecraft.client.stream;

import net.minecraft.stats.Achievement;

public class MetadataAchievement extends Metadata
{
    public MetadataAchievement(Achievement achievement)
    {
        super("achievement");
        this.addPayloadEntry("achievement_id", achievement.statId);
        this.addPayloadEntry("achievement_name", achievement.getStatName().getUnformattedText());
        this.addPayloadEntry("achievement_description", achievement.getDescription());
        this.setDescription("Achievement \'" + achievement.getStatName().getUnformattedText() + "\' obtained!");
    }
}
