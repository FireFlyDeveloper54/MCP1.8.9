package net.minecraft.realms;

import net.minecraft.client.GameWindow;

public final class RealmsKeyboard
{
    private RealmsKeyboard() {}

    public static void enableRepeatEvents(boolean enabled)
    {
        GameWindow.setRepeatEvents(enabled);
    }

    public static boolean isKeyDown(int key)
    {
        return GameWindow.isKeyDown(key);
    }
}
