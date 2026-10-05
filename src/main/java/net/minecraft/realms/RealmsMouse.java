package net.minecraft.realms;

import net.minecraft.client.GameWindow;

public final class RealmsMouse
{
    private RealmsMouse() {}

    public static boolean isButtonDown(int button)
    {
        return GameWindow.isButtonDown(button);
    }
}
