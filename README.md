# MCP 1.8.9

IDEA 下的 Minecraft 1.8.9 客户端工作区（MCP 映射，无 Gradle）。

## 环境

- Minecraft 1.8.9 + OptiFine HD_U_M6_pre2
- 源码按 Java 8 编译（`--release 8`）
- 运行支持 JDK 8–25（JDK 16+ 由 `Start` 补 `--add-opens`）
- 窗口：LWJGL 3.3.6 + GLFW + OpenGL 3.3 **compatibility**
- 按键：GLFW 在 `GameWindow` 里翻成 LWJGL2 键码，游戏逻辑仍用 `ESC=1`、`F3=61`、`W=17`

## 运行

IDEA 运行配置 `Start`，工作目录 `run/`。Make 后再跑。

LWJGL3 的 Windows natives 已经打在 `libraries/common/lwjgl/*-natives-windows.jar` 里，不需要再设 `java.library.path`，也不要再用 `natives/` 下的 LWJGL2 dll。

旧的 `run/options.txt` 会把 `useVbo` 锁成 false。要代码默认值（VBO 开），删掉 `run/` 后重开即可。

## 相对原版

- OptiFine、BetterFps、EntityCulling、区块光照/路径相关优化
- 声音走 LWJGL3 OpenAL Soft；默认输出设备变化时用 `alcReopenDeviceSOFT` 重开设备，失败再整引擎重载
- 剪贴板走 GLFW；全屏会记住窗口位置
- 字体走 Tessellator，不再 `glBegin`。中文走 unicode 页，不会因为输入过汉字就把 ASCII 永久切到 unicode 字体
- 输入法组词时（Windows IMM）不把 Enter/空格/数字当成聊天快捷键

## 渲染

游戏侧立即模式已经迁走：字体、OptiFine composite 全屏四边形、实体 `ModelRenderer` 默认编成 VBO，天空/星星在 `useVbo=true` 时走 VBO。Tessellator 上传会绑一个默认 VAO，并在客户端数组绘制前解绑 `ARRAY_BUFFER`。

窗口仍是 **OpenGL 3.3 compatibility**，不是 core。OptiFine 光影要用 `gl_Vertex` / `gl_ModelViewProjectionMatrix` / `ftransform` 这类兼容 GLSL，矩阵栈和固定管线还在。关 VBO 时的区块 display list、OptiFine 云的 list、带 `ModelSprite` 的模型仍走 list。

## 鼠标抓住 vs raw mouse

这是两件不同的事：

1. **抓住鼠标（cursor grab）**  
   进游戏、关掉 GUI 时 `MouseHelper.grabMouseCursor()` → `GameWindow.setGrabbed(true)` → `GLFW_CURSOR_DISABLED`。系统光标藏起来，相对位移用来转视角。打开背包/聊天时 `ungrabMouseCursor()`，光标回到普通 `GLFW_CURSOR_NORMAL`，才能点按钮。

2. **Raw mouse motion**  
   `GLFW_RAW_MOUSE_MOTION` 只在抓住鼠标时打开。它让 GLFW 走操作系统的 raw input（Windows 上是 `RIDEV_INPUTSINK`/`WM_INPUT` 那套），**绕过鼠标加速、提高指针精度、桌面 DPI 缩放**。不抓鼠标时必须关掉，否则 GUI 指针会和系统光标对不上。

不是所有机器都支持：`glfwRawMouseMotionSupported()` 为 false 时不会开，退回普通相对位移，游戏照样能转视角，只是加速曲线跟桌面鼠标设置走。
