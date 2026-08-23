package net.minecraft.client;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCharCallback;
import org.lwjgl.glfw.GLFWCursorEnterCallback;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWWindowFocusCallback;
import org.lwjgl.glfw.GLFWWindowSizeCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import net.minecraft.client.input.ImeSupport;
import net.minecraft.client.input.Keyboard;
import net.minecraft.client.settings.KeyBinding;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public final class GameWindow
{
    public static final class VideoMode
    {
        public final int width;
        public final int height;
        public final int bpp;
        public final int freq;

        public VideoMode(int width, int height)
        {
            this(width, height, 0, 0);
        }

        public VideoMode(int width, int height, int bpp, int freq)
        {
            this.width = width;
            this.height = height;
            this.bpp = bpp;
            this.freq = freq;
        }

        public int getWidth()
        {
            return this.width;
        }

        public int getHeight()
        {
            return this.height;
        }

        public int getBitsPerPixel()
        {
            return this.bpp;
        }

        public int getFrequency()
        {
            return this.freq;
        }

        public boolean equals(Object obj)
        {
            if (!(obj instanceof VideoMode))
            {
                return false;
            }

            VideoMode other = (VideoMode)obj;
            return other.width == this.width && other.height == this.height && other.bpp == this.bpp && other.freq == this.freq;
        }

        public int hashCode()
        {
            return this.width ^ this.height ^ this.bpp ^ this.freq;
        }

        public String toString()
        {
            return this.width + " x " + this.height + " x " + this.bpp + " @" + this.freq + "Hz";
        }
    }

    public static final class KeyEvent
    {
        public int key;
        public char character;
        public boolean pressed;
        public boolean repeat;
    }

    public static final class MouseEvent
    {
        public int button = -1;
        public boolean pressed;
        public int x;
        public int y;
        public int dx;
        public int dy;
        public int wheel;
    }

    private static long handle;
    private static VideoMode currentMode = new VideoMode(854, 480);
    private static VideoMode desktopMode;
    private static String title = "Minecraft 1.8.9";
    private static boolean created;
    private static boolean resized;
    private static boolean focused = true;
    private static boolean vsync;
    private static boolean fullscreen;
    private static int windowedX;
    private static int windowedY;
    private static int windowedWidth = 854;
    private static int windowedHeight = 480;
    private static boolean windowedBoundsValid;
    private static boolean resizable = true;
    private static ByteBuffer[] icons;
    private static boolean glfwInitialized;
    private static final boolean[] keysDown = new boolean[Keyboard.KEYBOARD_SIZE];
    private static final boolean[] mouseDown = new boolean[8];
    private static final ArrayDeque<KeyEvent> keyEvents = new ArrayDeque<KeyEvent>();
    private static final ArrayDeque<MouseEvent> mouseEvents = new ArrayDeque<MouseEvent>();
    private static KeyEvent currentKey = new KeyEvent();
    private static MouseEvent currentMouse = new MouseEvent();
    private static int mouseX;
    private static int mouseY;
    private static int mouseDx;
    private static int mouseDy;
    private static int mouseWheel;
    private static boolean grabbed;
    private static boolean mouseInside = true;
    private static boolean repeatEvents;
    private static int windowWidth = 854;
    private static int windowHeight = 480;
    private static int framebufferWidth = 854;
    private static int framebufferHeight = 480;
    private static GLFWErrorCallback errorCallback;
    private static GLFWWindowSizeCallback windowSizeCallback;
    private static GLFWFramebufferSizeCallback framebufferSizeCallback;
    private static GLFWWindowFocusCallback windowFocusCallback;
    private static GLFWKeyCallback keyCallback;
    private static GLFWCharCallback charCallback;
    private static GLFWMouseButtonCallback mouseButtonCallback;
    private static GLFWCursorPosCallback cursorPosCallback;
    private static GLFWScrollCallback scrollCallback;
    private static GLFWCursorEnterCallback cursorEnterCallback;

    private GameWindow()
    {
    }

    public static long getHandle()
    {
        return handle;
    }

    public static boolean isCreated()
    {
        return created;
    }

    public static int getWidth()
    {
        return Math.max(1, framebufferWidth);
    }

    public static int getHeight()
    {
        return Math.max(1, framebufferHeight);
    }

    public static VideoMode getVideoMode()
    {
        return currentMode;
    }

    public static VideoMode getDesktopMode()
    {
        ensureGlfw();
        return desktopMode != null ? desktopMode : currentMode;
    }

    public static boolean isFocused()
    {
        return focused;
    }

    public static boolean wasResized()
    {
        return resized;
    }

    public static boolean shouldClose()
    {
        return handle != NULL && glfwWindowShouldClose(handle);
    }

    public static String getGlfwVersion()
    {
        return Version.getVersion();
    }

    public static void setTitle(String titleIn)
    {
        title = titleIn;

        if (handle != NULL)
        {
            glfwSetWindowTitle(handle, titleIn);
        }
    }

    public static void setResizable(boolean resizableIn)
    {
        resizable = resizableIn;

        if (handle != NULL)
        {
            glfwSetWindowAttrib(handle, GLFW_RESIZABLE, resizableIn ? GLFW_TRUE : GLFW_FALSE);
        }
    }

    public static void setVsync(boolean vsyncIn)
    {
        vsync = vsyncIn;

        if (handle != NULL)
        {
            glfwSwapInterval(vsyncIn ? 1 : 0);
        }
    }

    public static void setIcon(ByteBuffer[] iconBuffers)
    {
        icons = iconBuffers;
        applyIcons();
    }

    public static void setSize(int width, int height)
    {
        currentMode = new VideoMode(width, height, currentMode.bpp, currentMode.freq);
        fullscreen = false;

        if (handle != NULL)
        {
            glfwSetWindowMonitor(handle, NULL, 0, 0, width, height, GLFW_DONT_CARE);
            centerWindow();
        }
    }

    public static void setVideoMode(VideoMode mode)
    {
        currentMode = mode;
        fullscreen = false;

        if (handle != NULL)
        {
            glfwSetWindowMonitor(handle, NULL, 0, 0, mode.width, mode.height, GLFW_DONT_CARE);
            centerWindow();
        }
    }

    public static void setFullscreen(boolean fullscreenIn)
    {
        ensureGlfw();

        if (handle == NULL)
        {
            fullscreen = fullscreenIn;

            if (fullscreenIn && desktopMode != null)
            {
                currentMode = desktopMode;
            }

            return;
        }

        if (fullscreenIn && !fullscreen)
        {
            saveWindowedBounds();
        }

        fullscreen = fullscreenIn;

        if (fullscreenIn)
        {
            long monitor = glfwGetPrimaryMonitor();
            GLFWVidMode vidMode = glfwGetVideoMode(monitor);
            glfwSetWindowMonitor(handle, monitor, 0, 0, vidMode.width(), vidMode.height(), vidMode.refreshRate());
            currentMode = new VideoMode(vidMode.width(), vidMode.height(), vidMode.redBits() + vidMode.greenBits() + vidMode.blueBits(), vidMode.refreshRate());
        }
        else
        {
            int width = windowedBoundsValid ? windowedWidth : Math.max(1, currentMode.width);
            int height = windowedBoundsValid ? windowedHeight : Math.max(1, currentMode.height);
            int x = windowedBoundsValid ? windowedX : 0;
            int y = windowedBoundsValid ? windowedY : 0;
            glfwSetWindowMonitor(handle, NULL, x, y, width, height, GLFW_DONT_CARE);
            currentMode = new VideoMode(width, height, currentMode.bpp, currentMode.freq);

            if (!windowedBoundsValid)
            {
                centerWindow();
            }
        }

        glfwSwapInterval(vsync ? 1 : 0);
        refreshFramebufferSize();
    }

    public static VideoMode[] getAvailableModes()
    {
        ensureGlfw();
        GLFWVidMode.Buffer modes = glfwGetVideoModes(glfwGetPrimaryMonitor());
        ArrayList<VideoMode> result = new ArrayList<VideoMode>();

        if (modes != null)
        {
            while (modes.hasRemaining())
            {
                GLFWVidMode vidMode = modes.get();
                result.add(new VideoMode(vidMode.width(), vidMode.height(), vidMode.redBits() + vidMode.greenBits() + vidMode.blueBits(), vidMode.refreshRate()));
            }
        }

        return result.toArray(new VideoMode[result.size()]);
    }

    public static void create(int width, int height, String windowTitle, int depthBits, int samples)
    {
        ensureGlfw();
        title = windowTitle;
        currentMode = new VideoMode(Math.max(1, width), Math.max(1, height), currentMode.bpp, currentMode.freq);

        if (handle != NULL)
        {
            glfwDestroyWindow(handle);
            handle = NULL;
            created = false;
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, resizable ? GLFW_TRUE : GLFW_FALSE);
        glfwWindowHint(GLFW_DOUBLEBUFFER, GLFW_TRUE);
        glfwWindowHint(GLFW_AUTO_ICONIFY, GLFW_TRUE);
        glfwWindowHint(GLFW_SCALE_TO_MONITOR, GLFW_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_COMPAT_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_FALSE);

        if (depthBits > 0)
        {
            glfwWindowHint(GLFW_DEPTH_BITS, depthBits);
        }

        if (samples > 0)
        {
            glfwWindowHint(GLFW_SAMPLES, samples);
        }

        long monitor = fullscreen ? glfwGetPrimaryMonitor() : NULL;
        handle = glfwCreateWindow(currentMode.width, currentMode.height, title, monitor, NULL);

        if (handle == NULL)
        {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        glfwMakeContextCurrent(handle);
        GL.createCapabilities();
        glfwSwapInterval(vsync ? 1 : 0);
        refreshFramebufferSize();

        if (fullscreen)
        {
            setFullscreen(true);
        }
        else
        {
            centerWindow();
            saveWindowedBounds();
        }

        windowSizeCallback = new GLFWWindowSizeCallback()
        {
            public void invoke(long window, int callbackWidth, int callbackHeight)
            {
                if (callbackWidth > 0 && callbackHeight > 0)
                {
                    windowWidth = callbackWidth;
                    windowHeight = callbackHeight;
                }
            }
        };
        glfwSetWindowSizeCallback(handle, windowSizeCallback);
        framebufferSizeCallback = new GLFWFramebufferSizeCallback()
        {
            public void invoke(long window, int callbackWidth, int callbackHeight)
            {
                applyFramebufferSize(callbackWidth, callbackHeight);
            }
        };
        glfwSetFramebufferSizeCallback(handle, framebufferSizeCallback);
        windowFocusCallback = new GLFWWindowFocusCallback()
        {
            public void invoke(long window, boolean callbackFocused)
            {
                focused = callbackFocused;

                if (!callbackFocused)
                {
                    releaseCapturedInput();
                }
            }
        };
        glfwSetWindowFocusCallback(handle, windowFocusCallback);
        keyCallback = new GLFWKeyCallback()
        {
            public void invoke(long window, int key, int scancode, int action, int mods)
            {
                if (ImeSupport.isComposing(window) && ImeSupport.shouldSuppressKey(key))
                {
                    return;
                }

                int lwjglKey = Keyboard.toLwjglKey(key);

                if (lwjglKey >= 0 && lwjglKey < keysDown.length)
                {
                    keysDown[lwjglKey] = action != GLFW_RELEASE;
                }

                KeyEvent event = new KeyEvent();
                event.key = lwjglKey;
                event.pressed = action != GLFW_RELEASE;
                event.repeat = action == GLFW_REPEAT;
                keyEvents.addLast(event);
            }
        };
        glfwSetKeyCallback(handle, keyCallback);
        charCallback = new GLFWCharCallback()
        {
            public void invoke(long window, int codepoint)
            {
                queueCharEvent(codepoint);
            }
        };
        glfwSetCharCallback(handle, charCallback);
        mouseButtonCallback = new GLFWMouseButtonCallback()
        {
            public void invoke(long window, int button, int action, int mods)
            {
                if (button >= 0 && button < mouseDown.length)
                {
                    mouseDown[button] = action == GLFW_PRESS;
                }

                MouseEvent event = new MouseEvent();
                event.button = button;
                event.pressed = action == GLFW_PRESS;
                event.x = mouseX;
                event.y = mouseY;
                mouseEvents.addLast(event);
            }
        };
        glfwSetMouseButtonCallback(handle, mouseButtonCallback);
        cursorPosCallback = new GLFWCursorPosCallback()
        {
            public void invoke(long window, double xpos, double ypos)
            {
                int x = mapMouseX(xpos);
                int y = mapMouseY(ypos);
                int dx = x - mouseX;
                int dy = y - mouseY;
                mouseX = x;
                mouseY = y;
                mouseDx += dx;
                mouseDy += dy;

                MouseEvent event = new MouseEvent();
                event.x = x;
                event.y = y;
                event.dx = dx;
                event.dy = dy;
                mouseEvents.addLast(event);
            }
        };
        glfwSetCursorPosCallback(handle, cursorPosCallback);
        scrollCallback = new GLFWScrollCallback()
        {
            public void invoke(long window, double xoffset, double yoffset)
            {
                int wheel = (int)yoffset;
                mouseWheel += wheel;
                MouseEvent event = new MouseEvent();
                event.x = mouseX;
                event.y = mouseY;
                event.wheel = wheel;
                mouseEvents.addLast(event);
            }
        };
        glfwSetScrollCallback(handle, scrollCallback);
        cursorEnterCallback = new GLFWCursorEnterCallback()
        {
            public void invoke(long window, boolean entered)
            {
                mouseInside = entered;
            }
        };
        glfwSetCursorEnterCallback(handle, cursorEnterCallback);

        if (glfwRawMouseMotionSupported())
        {
            glfwSetInputMode(handle, GLFW_RAW_MOUSE_MOTION, GLFW_TRUE);
        }

        applyIcons();
        applyCursorMode();
        glfwShowWindow(handle);
        created = true;
        focused = true;
        keyEvents.clear();
        mouseEvents.clear();
    }

    public static void destroy()
    {
        if (handle != NULL)
        {
            glfwDestroyWindow(handle);
            handle = NULL;
        }

        created = false;
        keyEvents.clear();
        mouseEvents.clear();
    }

    public static void update()
    {
        if (handle == NULL)
        {
            return;
        }

        resized = false;
        glfwSwapBuffers(handle);
        glfwPollEvents();
    }

    public static void sync(int fps)
    {
        FrameSync.sync(fps);
    }

    public static boolean isKeyDown(int key)
    {
        return key >= 0 && key < keysDown.length && keysDown[key];
    }

    public static void setRepeatEvents(boolean enable)
    {
        repeatEvents = enable;
    }

    public static boolean nextKeyEvent()
    {
        while (true)
        {
            KeyEvent event = keyEvents.pollFirst();

            if (event == null)
            {
                return false;
            }

            if (event.repeat && !repeatEvents)
            {
                continue;
            }

            currentKey = event;
            return true;
        }
    }

    public static int getEventKey()
    {
        return currentKey.key;
    }

    public static char getEventCharacter()
    {
        return currentKey.character;
    }

    public static boolean getEventKeyState()
    {
        return currentKey.pressed;
    }

    public static boolean isRepeatEvent()
    {
        return currentKey.repeat;
    }

    public static String getKeyName(int key)
    {
        return Keyboard.getKeyName(key);
    }

    public static long getTimeMillis()
    {
        ensureGlfw();
        return (long)(glfwGetTime() * 1000.0D);
    }

    public static boolean isButtonDown(int button)
    {
        return button >= 0 && button < mouseDown.length && mouseDown[button];
    }

    public static boolean nextMouseEvent()
    {
        MouseEvent event = mouseEvents.pollFirst();

        if (event == null)
        {
            return false;
        }

        currentMouse = event;
        return true;
    }

    public static int getEventButton()
    {
        return currentMouse.button;
    }

    public static boolean getEventButtonState()
    {
        return currentMouse.pressed;
    }

    public static int getEventX()
    {
        return currentMouse.x;
    }

    public static int getEventY()
    {
        return currentMouse.y;
    }

    public static int getEventDWheel()
    {
        return currentMouse.wheel;
    }

    public static int getMouseX()
    {
        return mouseX;
    }

    public static int getMouseY()
    {
        return mouseY;
    }

    public static int getDX()
    {
        int dx = mouseDx;
        mouseDx = 0;
        return dx;
    }

    public static int getDY()
    {
        int dy = mouseDy;
        mouseDy = 0;
        return dy;
    }

    public static int getDWheel()
    {
        int wheel = mouseWheel;
        mouseWheel = 0;
        return wheel;
    }

    public static boolean isGrabbed()
    {
        return grabbed;
    }

    public static boolean isMouseInsideWindow()
    {
        return mouseInside;
    }

    public static void setGrabbed(boolean grab)
    {
        grabbed = grab;
        applyCursorMode();
    }

    public static String getClipboard()
    {
        if (handle == NULL)
        {
            return "";
        }

        String text = glfwGetClipboardString(handle);
        return text != null ? text : "";
    }

    public static void setClipboard(String text)
    {
        if (handle != NULL && text != null)
        {
            glfwSetClipboardString(handle, text);
        }
    }

    public static boolean isImeComposing()
    {
        return handle != NULL && ImeSupport.isComposing(handle);
    }

    private static void queueCharEvent(int codepoint)
    {
        if (codepoint < 32 || codepoint == 127)
        {
            return;
        }

        char[] characters = Character.toChars(codepoint);

        for (int i = 0; i < characters.length; ++i)
        {
            KeyEvent event = new KeyEvent();
            event.key = Keyboard.KEY_NONE;
            event.character = characters[i];
            event.pressed = true;
            keyEvents.addLast(event);
        }
    }

    private static void saveWindowedBounds()
    {
        if (handle == NULL)
        {
            return;
        }

        try (MemoryStack stack = MemoryStack.stackPush())
        {
            IntBuffer x = stack.mallocInt(1);
            IntBuffer y = stack.mallocInt(1);
            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            glfwGetWindowPos(handle, x, y);
            glfwGetWindowSize(handle, width, height);
            windowedX = x.get(0);
            windowedY = y.get(0);
            windowedWidth = Math.max(1, width.get(0));
            windowedHeight = Math.max(1, height.get(0));
            windowedBoundsValid = true;
        }
    }

    private static void applyCursorMode()
    {
        if (handle == NULL)
        {
            return;
        }

        glfwSetInputMode(handle, GLFW_CURSOR, grabbed ? GLFW_CURSOR_DISABLED : GLFW_CURSOR_NORMAL);

        if (glfwRawMouseMotionSupported())
        {
            glfwSetInputMode(handle, GLFW_RAW_MOUSE_MOTION, grabbed ? GLFW_TRUE : GLFW_FALSE);
        }
    }

    public static void setCursorPosition(int x, int y)
    {
        mouseX = x;
        mouseY = y;

        if (handle != NULL)
        {
            double windowX = windowWidth <= 0 ? x : x * (double)windowWidth / (double)Math.max(1, framebufferWidth);
            double windowY = windowHeight <= 0 ? framebufferHeight - y : (double)windowHeight - y * (double)windowHeight / (double)Math.max(1, framebufferHeight);
            glfwSetCursorPos(handle, windowX, windowY);
        }
    }

    private static void applyFramebufferSize(int width, int height)
    {
        if (width <= 0 || height <= 0)
        {
            return;
        }

        framebufferWidth = width;
        framebufferHeight = height;
        currentMode = new VideoMode(width, height, currentMode.bpp, currentMode.freq);
        resized = true;
    }

    private static void refreshFramebufferSize()
    {
        if (handle == NULL)
        {
            return;
        }

        try (MemoryStack stack = MemoryStack.stackPush())
        {
            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            glfwGetWindowSize(handle, width, height);
            windowWidth = Math.max(1, width.get(0));
            windowHeight = Math.max(1, height.get(0));
            glfwGetFramebufferSize(handle, width, height);
            applyFramebufferSize(width.get(0), height.get(0));
        }
    }

    private static int mapMouseX(double xpos)
    {
        if (windowWidth <= 0)
        {
            return (int)xpos;
        }

        return (int)(xpos * (double)framebufferWidth / (double)windowWidth);
    }

    private static int mapMouseY(double ypos)
    {
        if (windowHeight <= 0)
        {
            return framebufferHeight - (int)ypos;
        }

        return framebufferHeight - (int)(ypos * (double)framebufferHeight / (double)windowHeight);
    }

    private static void releaseCapturedInput()
    {
        for (int key = 0; key < keysDown.length; ++key)
        {
            if (keysDown[key])
            {
                keysDown[key] = false;
                KeyEvent event = new KeyEvent();
                event.key = key;
                event.pressed = false;
                keyEvents.addLast(event);
            }
        }

        for (int button = 0; button < mouseDown.length; ++button)
        {
            if (mouseDown[button])
            {
                mouseDown[button] = false;
                MouseEvent event = new MouseEvent();
                event.button = button;
                event.pressed = false;
                event.x = mouseX;
                event.y = mouseY;
                mouseEvents.addLast(event);
            }
        }

        KeyBinding.unPressAllKeys();
    }

    private static void ensureGlfw()
    {
        if (glfwInitialized)
        {
            return;
        }

        if (errorCallback == null)
        {
            errorCallback = GLFWErrorCallback.createPrint(System.err);
        }

        glfwSetErrorCallback(errorCallback);

        if (!glfwInit())
        {
            throw new RuntimeException("Failed to initialize GLFW");
        }

        GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        desktopMode = new VideoMode(vidMode.width(), vidMode.height(), vidMode.redBits() + vidMode.greenBits() + vidMode.blueBits(), vidMode.refreshRate());
        glfwInitialized = true;
    }

    private static void centerWindow()
    {
        if (handle == NULL || desktopMode == null)
        {
            return;
        }

        try (MemoryStack stack = MemoryStack.stackPush())
        {
            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            glfwGetWindowSize(handle, width, height);
            glfwSetWindowPos(handle, Math.max(0, (desktopMode.width - width.get(0)) / 2), Math.max(0, (desktopMode.height - height.get(0)) / 2));
        }
    }

    private static void applyIcons()
    {
        if (handle == NULL || icons == null || icons.length == 0)
        {
            return;
        }

        GLFWImage.Buffer imageBuffer = GLFWImage.malloc(icons.length);

        try
        {
            int count = 0;

            for (int i = 0; i < icons.length; ++i)
            {
                ByteBuffer source = icons[i];

                if (source == null)
                {
                    continue;
                }

                ByteBuffer pixels = source.duplicate();
                pixels.rewind();

                if (pixels.remaining() < 4)
                {
                    continue;
                }

                int pixelCount = pixels.remaining() / 4;
                int size = (int)Math.round(Math.sqrt(pixelCount));

                if (size <= 0 || size * size != pixelCount)
                {
                    continue;
                }

                if (!pixels.isDirect())
                {
                    ByteBuffer direct = ByteBuffer.allocateDirect(pixels.remaining());
                    direct.put(pixels);
                    direct.flip();
                    pixels = direct;
                    icons[i] = direct;
                }

                imageBuffer.position(count);
                imageBuffer.width(size);
                imageBuffer.height(size);
                imageBuffer.pixels(pixels);
                ++count;
            }

            if (count > 0)
            {
                imageBuffer.position(0);
                imageBuffer.limit(count);
                glfwSetWindowIcon(handle, imageBuffer);
            }
        }
        catch (Throwable throwable)
        {
            throwable.printStackTrace();
        }
        finally
        {
            imageBuffer.free();
        }
    }

    private static final class FrameSync
    {
        private static final long NANOS_IN_SECOND = 1000L * 1000L * 1000L;
        private static long nextFrame;
        private static boolean initialised;
        private static final RunningAvg sleepDurations = new RunningAvg(10);
        private static final RunningAvg yieldDurations = new RunningAvg(10);

        public static void sync(int fps)
        {
            if (fps <= 0)
            {
                return;
            }

            if (!initialised)
            {
                initialise();
            }

            try
            {
                for (long t0 = getTime(), t1; nextFrame - t0 > sleepDurations.avg(); t0 = t1)
                {
                    Thread.sleep(1L);
                    sleepDurations.add((t1 = getTime()) - t0);
                }

                sleepDurations.dampenForLowResTicker();

                for (long t0 = getTime(), t1; nextFrame - t0 > yieldDurations.avg(); t0 = t1)
                {
                    Thread.yield();
                    yieldDurations.add((t1 = getTime()) - t0);
                }
            }
            catch (InterruptedException ignored)
            {
            }

            nextFrame = Math.max(nextFrame + NANOS_IN_SECOND / fps, getTime());
        }

        private static void initialise()
        {
            initialised = true;
            sleepDurations.init(1000 * 1000);
            yieldDurations.init((int)(-(getTime() - getTime()) * 1.333D));
            nextFrame = getTime();

            if (System.getProperty("os.name", "").startsWith("Win"))
            {
                Thread timerAccuracyThread = new Thread(new Runnable()
                {
                    public void run()
                    {
                        try
                        {
                            Thread.sleep(Long.MAX_VALUE);
                        }
                        catch (Exception ignored)
                        {
                        }
                    }
                });
                timerAccuracyThread.setName("LWJGL Timer");
                timerAccuracyThread.setDaemon(true);
                timerAccuracyThread.start();
            }
        }

        private static long getTime()
        {
            return (long)(glfwGetTime() * NANOS_IN_SECOND);
        }

        private static final class RunningAvg
        {
            private final long[] slots;
            private int offset;
            private static final long DAMPEN_THRESHOLD = 10 * 1000L * 1000L;
            private static final float DAMPEN_FACTOR = 0.9F;

            public RunningAvg(int slotCount)
            {
                this.slots = new long[slotCount];
            }

            public void init(long value)
            {
                while (this.offset < this.slots.length)
                {
                    this.slots[this.offset++] = value;
                }
            }

            public void add(long value)
            {
                this.slots[this.offset++ % this.slots.length] = value;
                this.offset %= this.slots.length;
            }

            public long avg()
            {
                long sum = 0L;
                for (int i = 0; i < this.slots.length; ++i)
                {
                    sum += this.slots[i];
                }
                return sum / this.slots.length;
            }

            public void dampenForLowResTicker()
            {
                if (this.avg() > DAMPEN_THRESHOLD)
                {
                    for (int i = 0; i < this.slots.length; ++i)
                    {
                        this.slots[i] = (long)(this.slots[i] * DAMPEN_FACTOR);
                    }
                }
            }
        }
    }
}
