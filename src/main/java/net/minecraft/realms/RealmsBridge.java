package net.minecraft.realms;

import java.lang.reflect.Constructor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenRealmsProxy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RealmsBridge extends RealmsScreen
{
    private static final Logger LOGGER = LogManager.getLogger();
    private GuiScreen previousScreen;

    public void switchToRealms(GuiScreen previousScreen)
    {
        this.previousScreen = previousScreen;

        try
        {
            Class<?> oclass = Class.forName("com.mojang.realmsclient.RealmsMainScreen");
            Constructor<?> constructor = oclass.getDeclaredConstructor(new Class[] {RealmsScreen.class});
            constructor.setAccessible(true);
            Object object = constructor.newInstance(new Object[] {this});
            Minecraft.getMinecraft().displayGuiScreen(((RealmsScreen)object).getProxy());
        }
        catch (Exception exception)
        {
            // LOGGER.error((String)"Realms module missing", (Throwable)exception);
        }
    }

    public GuiScreenRealmsProxy getNotificationScreen(GuiScreen previousScreen)
    {
        try
        {
            this.previousScreen = previousScreen;
            Class<?> oclass = Class.forName("com.mojang.realmsclient.gui.screens.RealmsNotificationsScreen");
            Constructor<?> constructor = oclass.getDeclaredConstructor(new Class[] {RealmsScreen.class});
            constructor.setAccessible(true);
            Object object = constructor.newInstance(new Object[] {this});
            return ((RealmsScreen)object).getProxy();
        }
        catch (Exception exception)
        {
            // LOGGER.error((String)"Realms module missing", (Throwable)exception);
            return null;
        }
    }

    public void init()
    {
        Minecraft.getMinecraft().displayGuiScreen(this.previousScreen);
    }
}
