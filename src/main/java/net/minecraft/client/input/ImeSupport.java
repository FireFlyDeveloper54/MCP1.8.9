package net.minecraft.client.input;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWNativeWin32;

public final class ImeSupport
{
    private static final boolean WINDOWS = System.getProperty("os.name", "").toLowerCase().contains("win");
    private static final int GCS_COMPSTR = 0x0008;
    private static Boolean available;

    private ImeSupport()
    {
    }

    public static boolean isComposing(long window)
    {
        if (!WINDOWS || window == 0L)
        {
            return false;
        }

        if (available != null && !available.booleanValue())
        {
            return false;
        }

        try
        {
            long hwnd = GLFWNativeWin32.glfwGetWin32Window(window);

            if (hwnd == 0L)
            {
                available = Boolean.FALSE;
                return false;
            }

            HWND windowHandle = new HWND(Pointer.createConstant(hwnd));
            HANDLE context = Imm32.INSTANCE.ImmGetContext(windowHandle);
            available = Boolean.TRUE;

            if (context == null || context.getPointer() == null || Pointer.nativeValue(context.getPointer()) == 0L)
            {
                return false;
            }

            try
            {
                return Imm32.INSTANCE.ImmGetCompositionStringW(context, GCS_COMPSTR, Pointer.NULL, 0) > 0;
            }
            finally
            {
                Imm32.INSTANCE.ImmReleaseContext(windowHandle, context);
            }
        }
        catch (Throwable ignored)
        {
            available = Boolean.FALSE;
            return false;
        }
    }

    public static boolean shouldSuppressKey(int glfwKey)
    {
        switch (glfwKey)
        {
            case GLFW.GLFW_KEY_ENTER:
            case GLFW.GLFW_KEY_KP_ENTER:
            case GLFW.GLFW_KEY_SPACE:
            case GLFW.GLFW_KEY_TAB:
            case GLFW.GLFW_KEY_ESCAPE:
            case GLFW.GLFW_KEY_BACKSPACE:
            case GLFW.GLFW_KEY_UP:
            case GLFW.GLFW_KEY_DOWN:
            case GLFW.GLFW_KEY_LEFT:
            case GLFW.GLFW_KEY_RIGHT:
                return true;
            default:
                return glfwKey >= GLFW.GLFW_KEY_0 && glfwKey <= GLFW.GLFW_KEY_9;
        }
    }

    private interface Imm32 extends StdCallLibrary
    {
        Imm32 INSTANCE = Native.load("imm32", Imm32.class, W32APIOptions.DEFAULT_OPTIONS);

        HANDLE ImmGetContext(HWND hWnd);

        boolean ImmReleaseContext(HWND hWnd, HANDLE hIMC);

        int ImmGetCompositionStringW(HANDLE hIMC, int dwIndex, Pointer lpBuf, int dwBufLen);
    }
}
