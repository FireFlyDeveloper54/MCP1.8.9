package net.optifine.util;

import java.util.Comparator;
import org.lwjgl.opengl.DisplayMode;

public class DisplayModeComparator implements Comparator
{
    public int compare(Object object, Object secondObject)
    {
        DisplayMode displayMode = (DisplayMode)object;
        DisplayMode displaymode1 = (DisplayMode)secondObject;
        return displayMode.getWidth() != displaymode1.getWidth() ? displayMode.getWidth() - displaymode1.getWidth() : (displayMode.getHeight() != displaymode1.getHeight() ? displayMode.getHeight() - displaymode1.getHeight() : (displayMode.getBitsPerPixel() != displaymode1.getBitsPerPixel() ? displayMode.getBitsPerPixel() - displaymode1.getBitsPerPixel() : (displayMode.getFrequency() != displaymode1.getFrequency() ? displayMode.getFrequency() - displaymode1.getFrequency() : 0)));
    }
}
