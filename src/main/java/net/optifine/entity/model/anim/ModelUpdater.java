package net.optifine.entity.model.anim;

public class ModelUpdater
{
    private ModelVariableUpdater[] modelVariableUpdaters;

    public ModelUpdater(ModelVariableUpdater[] modelVariableUpdaters)
    {
        this.modelVariableUpdaters = modelVariableUpdaters;
    }

    public void update()
    {
        for (int updaterIndex = 0; updaterIndex < this.modelVariableUpdaters.length; ++updaterIndex)
        {
            ModelVariableUpdater modelVariableUpdater = this.modelVariableUpdaters[updaterIndex];
            modelVariableUpdater.update();
        }
    }

    public boolean initialize(IModelResolver mr)
    {
        for (int updaterIndex = 0; updaterIndex < this.modelVariableUpdaters.length; ++updaterIndex)
        {
            ModelVariableUpdater modelVariableUpdater = this.modelVariableUpdaters[updaterIndex];

            if (!modelVariableUpdater.initialize(mr))
            {
                return false;
            }
        }

        return true;
    }
}
