package net.minecraft.world.gen.layer;

public class GenLayerFuzzyZoom extends GenLayerZoom
{
    public GenLayerFuzzyZoom(long baseSeedIn, GenLayer parent)
    {
        super(baseSeedIn, parent);
    }

    protected int selectModeOrRandom(int first, int second, int third, int fourth)
    {
        return this.selectRandom(new int[] {first, second, third, fourth});
    }
}
