package net.minecraft.realms;

import net.minecraft.client.renderer.GlStateManager;

public final class RealmsGL13
{
    private RealmsGL13() {}

    public static void glActiveTexture(int texture)
    {
        GlStateManager.setActiveTexture(texture);
    }
}
