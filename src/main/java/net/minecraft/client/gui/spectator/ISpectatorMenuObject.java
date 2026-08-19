package net.minecraft.client.gui.spectator;

import net.minecraft.util.IChatComponent;

public interface ISpectatorMenuObject
{
    void selectItem(SpectatorMenu menu);

    IChatComponent getSpectatorName();

    void renderIcon(float brightness, int alpha);

    boolean isEnabled();
}
