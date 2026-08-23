package net.minecraft.util;

import net.minecraft.client.GameWindow;

public class MouseHelper
{
    public int deltaX;
    public int deltaY;

    public void grabMouseCursor()
    {
        GameWindow.setGrabbed(true);
        this.deltaX = 0;
        this.deltaY = 0;
    }

    public void ungrabMouseCursor()
    {
        GameWindow.setCursorPosition(GameWindow.getWidth() / 2, GameWindow.getHeight() / 2);
        GameWindow.setGrabbed(false);
    }

    public void mouseXYChange()
    {
        this.deltaX = GameWindow.getDX();
        this.deltaY = GameWindow.getDY();
    }
}
