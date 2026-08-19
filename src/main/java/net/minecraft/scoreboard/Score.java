package net.minecraft.scoreboard;

import java.util.Comparator;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;

public class Score
{
    public static final Comparator<Score> scoreComparator = new Comparator<Score>()
    {
        public int compare(Score firstScore, Score secondScore)
        {
            return firstScore.getScorePoints() > secondScore.getScorePoints() ? 1 : (firstScore.getScorePoints() < secondScore.getScorePoints() ? -1 : secondScore.getPlayerName().compareToIgnoreCase(firstScore.getPlayerName()));
        }
    };
    private final Scoreboard theScoreboard;
    private final ScoreObjective theScoreObjective;
    private final String scorePlayerName;
    private int scorePoints;
    private boolean locked;
    private boolean forceUpdate;

    public Score(Scoreboard theScoreboardIn, ScoreObjective theScoreObjectiveIn, String scorePlayerNameIn)
    {
        this.theScoreboard = theScoreboardIn;
        this.theScoreObjective = theScoreObjectiveIn;
        this.scorePlayerName = scorePlayerNameIn;
        this.forceUpdate = true;
    }

    public void increseScore(int amount)
    {
        if (this.theScoreObjective.getCriteria().isReadOnly())
        {
            throw new IllegalStateException("Cannot modify read-only score");
        }
        else
        {
            this.setScorePoints(this.getScorePoints() + amount);
        }
    }

    public void decreaseScore(int amount)
    {
        if (this.theScoreObjective.getCriteria().isReadOnly())
        {
            throw new IllegalStateException("Cannot modify read-only score");
        }
        else
        {
            this.setScorePoints(this.getScorePoints() - amount);
        }
    }

    public void incrementScore()
    {
        if (this.theScoreObjective.getCriteria().isReadOnly())
        {
            throw new IllegalStateException("Cannot modify read-only score");
        }
        else
        {
            this.increseScore(1);
        }
    }

    public int getScorePoints()
    {
        return this.scorePoints;
    }

    public void setScorePoints(int points)
    {
        int previousPoints = this.scorePoints;
        this.scorePoints = points;

        if (previousPoints != points || this.forceUpdate)
        {
            this.forceUpdate = false;
            this.getScoreScoreboard().onScoreUpdated(this);
        }
    }

    public ScoreObjective getObjective()
    {
        return this.theScoreObjective;
    }

    public String getPlayerName()
    {
        return this.scorePlayerName;
    }

    public Scoreboard getScoreScoreboard()
    {
        return this.theScoreboard;
    }

    public boolean isLocked()
    {
        return this.locked;
    }

    public void setLocked(boolean locked)
    {
        this.locked = locked;
    }

    public void setScoreFromPlayers(List<EntityPlayer> players)
    {
        this.setScorePoints(this.theScoreObjective.getCriteria().setScore(players));
    }
}
