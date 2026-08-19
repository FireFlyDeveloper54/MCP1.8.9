package net.optifine.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.client.settings.KeyBinding;

public class KeyUtils
{
    public static void fixKeyConflicts(KeyBinding[] keys, KeyBinding[] keysPrio)
    {
        Set<Integer> priorityKeyCodes = new HashSet();

        for (int index = 0; index < keysPrio.length; ++index)
        {
            KeyBinding priorityKey = keysPrio[index];
            priorityKeyCodes.add(Integer.valueOf(priorityKey.getKeyCode()));
        }

        Set<KeyBinding> conflictingKeys = new HashSet(Arrays.asList(keys));
        conflictingKeys.removeAll(Arrays.asList(keysPrio));

        for (KeyBinding keyBinding : conflictingKeys)
        {
            Integer keyCode = Integer.valueOf(keyBinding.getKeyCode());

            if (priorityKeyCodes.contains(keyCode))
            {
                keyBinding.setKeyCode(0);
            }
        }
    }
}
