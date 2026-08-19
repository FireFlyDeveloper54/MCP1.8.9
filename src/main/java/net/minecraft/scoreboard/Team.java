package net.minecraft.scoreboard;

import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Map;

public abstract class Team
{
    public boolean isSameTeam(Team other)
    {
        return other == null ? false : this == other;
    }

    public abstract String getRegisteredName();

    public abstract String formatString(String input);

    public abstract boolean getSeeFriendlyInvisiblesEnabled();

    public abstract boolean getAllowFriendlyFire();

    public abstract Team.EnumVisible getNameTagVisibility();

    public abstract Collection<String> getMembershipCollection();

    public abstract Team.EnumVisible getDeathMessageVisibility();

    public static enum EnumVisible
    {
        ALWAYS("always", 0),
        NEVER("never", 1),
        HIDE_FOR_OTHER_TEAMS("hideForOtherTeams", 2),
        HIDE_FOR_OWN_TEAM("hideForOwnTeam", 3);

        public static final Team.EnumVisible[] VALUES = values();
        private static final Map<String, Team.EnumVisible> NAME_LOOKUP = Maps.<String, Team.EnumVisible>newHashMap();
        public final String internalName;
        public final int id;

        public static String[] getNames()
        {
            return (String[])NAME_LOOKUP.keySet().toArray(new String[NAME_LOOKUP.size()]);
        }

        public static Team.EnumVisible getByName(String name)
        {
            return NAME_LOOKUP.get(name);
        }

        private EnumVisible(String internalName, int id)
        {
            this.internalName = internalName;
            this.id = id;
        }

        static {
            for (Team.EnumVisible team$enumvisible : VALUES)
            {
                NAME_LOOKUP.put(team$enumvisible.internalName, team$enumvisible);
            }
        }
    }
}
