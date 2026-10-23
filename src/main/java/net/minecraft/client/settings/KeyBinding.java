package net.minecraft.client.settings;

import com.google.common.collect.Maps;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.GameWindow;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.IntHashMap;

public class KeyBinding implements Comparable<KeyBinding>
{
    private static final List<KeyBinding> keybindArray = Lists.<KeyBinding>newArrayList();
    private static final IntHashMap<KeyBinding> hash = new IntHashMap();
    private static final Set<String> keybindSet = Sets.<String>newHashSet();
    private static final Map<String, Integer> CATEGORY_ORDER = Maps.<String, Integer>newHashMap();
    private final String keyDescription;
    private final int keyCodeDefault;
    private final String keyCategory;
    private int keyCode;
    private boolean pressed;
    private int pressTime;

    public static void onTick(int keyCode)
    {
        if (keyCode != 0)
        {
            KeyBinding keyBinding = (KeyBinding)hash.lookup(keyCode);

            if (keyBinding != null)
            {
                ++keyBinding.pressTime;
            }
        }
    }

    public static void setKeyBindState(int keyCode, boolean pressed)
    {
        if (keyCode != 0)
        {
            KeyBinding keyBinding = (KeyBinding)hash.lookup(keyCode);

            if (keyBinding != null)
            {
                keyBinding.pressed = pressed;
            }
        }
    }

    public static void updateKeyBindState()
    {
        for (KeyBinding keyBinding : keybindArray)
        {
            setKeyBindState(keyBinding.keyCode, GameWindow.isKeyDown(keyBinding.keyCode));
        }
    }

    public static void unPressAllKeys()
    {
        for (KeyBinding keyBinding : keybindArray)
        {
            keyBinding.unpressKey();
        }
    }

    public static void resetKeyBindingArrayAndHash()
    {
        hash.clearMap();

        for (KeyBinding keyBinding : keybindArray)
        {
            hash.addKey(keyBinding.keyCode, keyBinding);
        }
    }

    public static Set<String> getKeybinds()
    {
        return keybindSet;
    }

    public KeyBinding(String description, int keyCode, String category)
    {
        this.keyDescription = description;
        this.keyCode = keyCode;
        this.keyCodeDefault = keyCode;
        this.keyCategory = category;
        keybindArray.add(this);
        hash.addKey(keyCode, this);
        keybindSet.add(category);
    }

    public boolean isKeyDown()
    {
        return this.pressed;
    }

    public String getKeyCategory()
    {
        return this.keyCategory;
    }

    public boolean isPressed()
    {
        if (this.pressTime == 0)
        {
            return false;
        }
        else
        {
            --this.pressTime;
            return true;
        }
    }

    private void unpressKey()
    {
        this.pressTime = 0;
        this.pressed = false;
    }

    public String getKeyDescription()
    {
        return this.keyDescription;
    }

    public int getKeyCodeDefault()
    {
        return this.keyCodeDefault;
    }

    public int getKeyCode()
    {
        return this.keyCode;
    }

    public void setKeyCode(int keyCode)
    {
        this.keyCode = keyCode;
    }

    public int compareTo(KeyBinding otherKeyBinding)
    {
        if (this.keyCategory.equals(otherKeyBinding.keyCategory))
        {
            return I18n.format(this.keyDescription, new Object[0]).compareTo(I18n.format(otherKeyBinding.keyDescription, new Object[0]));
        }

        Integer categoryOrder = CATEGORY_ORDER.get(this.keyCategory);
        Integer otherCategoryOrder = CATEGORY_ORDER.get(otherKeyBinding.keyCategory);

        if (categoryOrder != null && otherCategoryOrder != null)
        {
            return categoryOrder.compareTo(otherCategoryOrder);
        }
        else if (categoryOrder != null)
        {
            return -1;
        }
        else if (otherCategoryOrder != null)
        {
            return 1;
        }
        else
        {
            int localizedCategoryComparison = I18n.format(this.keyCategory, new Object[0]).compareTo(I18n.format(otherKeyBinding.keyCategory, new Object[0]));
            return localizedCategoryComparison != 0 ? localizedCategoryComparison : this.keyCategory.compareTo(otherKeyBinding.keyCategory);
        }
    }

    static
    {
        CATEGORY_ORDER.put("key.categories.movement", Integer.valueOf(1));
        CATEGORY_ORDER.put("key.categories.gameplay", Integer.valueOf(2));
        CATEGORY_ORDER.put("key.categories.inventory", Integer.valueOf(3));
        CATEGORY_ORDER.put("key.categories.creative", Integer.valueOf(4));
        CATEGORY_ORDER.put("key.categories.multiplayer", Integer.valueOf(5));
        CATEGORY_ORDER.put("key.categories.ui", Integer.valueOf(6));
        CATEGORY_ORDER.put("key.categories.misc", Integer.valueOf(7));
    }
}
