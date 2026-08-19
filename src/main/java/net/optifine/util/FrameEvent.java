package net.optifine.util;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;

public class FrameEvent
{
    private static Map<String, Integer> mapEventFrames = new HashMap();

    public static boolean isActive(String name, int frameInterval)
    {
        synchronized (mapEventFrames)
        {
            int currentFrame = Minecraft.getMinecraft().entityRenderer.frameCount;
            Integer lastFrameObject = (Integer)mapEventFrames.get(name);

            if (lastFrameObject == null)
            {
                lastFrameObject = Integer.valueOf(currentFrame);
                mapEventFrames.put(name, lastFrameObject);
            }

            int lastFrame = lastFrameObject.intValue();

            if (currentFrame > lastFrame && currentFrame < lastFrame + frameInterval)
            {
                return false;
            }
            else
            {
                mapEventFrames.put(name, Integer.valueOf(currentFrame));
                return true;
            }
        }
    }
}
