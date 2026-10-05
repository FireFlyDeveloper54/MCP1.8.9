# MCP1.8.9 还原续审

日期：2026-10-05。对象为 `C:/Users/Admin/ideaProject/MCP1.8.9`，起点提交 `67e4a8c176796f77eb9b870aeeac19f1dbe963aa`。原版参考为 `C:/Users/Admin/Downloads/MCP-919-main`、其 1.8.9 client jar 和原配音频库字节码。

本轮继续执行“先还原、先提交，再改进 MCP，随后修 Naven 三分支”的顺序。下面区分源码恢复与运行验证；不能用编译通过代替游戏效果验证。

## 一、本轮新增确认项

| 编号 | 原改动造成的偏差 | 恢复内容 |
|---|---|---|
| R11 | 顶点颜色又乘当前颜色，改变顶点数组的原版颜色语义；光照结果未在顶点阶段截断 | 使用顶点颜色或当前颜色，按原版截断光照 RGB |
| R12 | 实体受伤、死亡、闪白先乘环境光再染色，黑暗中的染色偏亮；透明度仍乘当前颜色 | 先组合材质与染色，再乘环境光；染色链的 alpha 使用材质 alpha。独立记录启用状态，覆盖混合权重为零的情况 |
| R13 | 默认雾起点为 0.8、视距缩为 0.83，下界雾结束距离被放大；距离模式没有迁移 | 默认起点恢复 0.75、默认视距不缩短、下界结束距离恢复 `min(distance,192)*0.5`；GL3 默认与光影 shader 传入雾距离模式。保留手选 Fast/Fancy/Off |
| R14 | 默认 shader 无条件归一化法线，忽略原版 normalize/rescale 状态 | 按原状态处理法线，覆盖缩放的经验球和 TNT 等调用路径 |
| R15 | `pushAttrib/popAttrib` 为空；展示框设置标准光照后不能恢复之前的光源 | 恢复该接口所需的 enable 和 lighting 状态快照 |
| R16 | 删除/重链接 shader program 后保留按 program ID 缓存的 uniform locations | 使程序生命周期与 uniform 缓存失效同步，避免 ID 重用时错写 |
| A04 | 每个 Vorbis 帧直接对应一个 AL buffer，短帧时预缓冲不足，且首块就开播 | 按原配库聚合为 3 × 131072 字节 PCM 块，填满或到 EOF 后开播，仍按段读取 |
| A05 | 无衰减声源使用相对监听器坐标，后续却写世界坐标 | `NONE` 使用世界坐标，仅关闭 rolloff；恢复守卫攻击声音的定位 |
| A06 | 过期/新音频加载可重叠；取消不能尝试打断阻塞的输入 | 串行加载与清理，输入取消与 native decoder 关闭分开处理 |
| I01 | GLFW 物理键与字符分成两次 GUI 调用；创造搜索快捷键后还会写入字符，字符 repeat 无法过滤 | 同次可打印物理键和 BMP 字符合并；保留 IME 与非 BMP 独立字符事件，只按物理键分派快捷键 |
| I02 | 小于一个单位的滚轮增量被截断丢失 | 累积分数，满一个旧单位后提交离散滚轮事件 |
| I03 | 高 DPI 创建后的实际 framebuffer 尺寸未在创建主 FBO 前同步 | 创建窗口成功后立即同步实际宽高 |
| W07 | 默认世界自动保存从每 900 tick 变为每 4500 tick；玩家数据仍每 900 tick 保存 | 默认及重置值恢复 900，保留自选保存间隔 |
| V01 | 默认每层粒子上限从 4000 降至 400，20ms 预算超时后直接杀死尚未更新的粒子 | 恢复 4000 默认值，并逐个更新存活粒子 |
| V02 | mipmap 使用不同的混色算法，改变透明边缘和颜色 | 恢复原版 gamma 混色方法及透明颜色处理 |
| M01 | 三角函数替换、恒等式改写和操作顺序改变查表舍入；普通实体使用新 LookHelper | 恢复原版查表、平方根及直接相关调用的操作顺序，普通实体使用原版 LookHelper |
| M02 | Unicode 每字保留额外 0.5 像素；粗体增宽与计宽不符，fallback 阴影位移变化 | 恢复默认/Unicode 原版整数步进、粗体和阴影，保留主动自定义字体的度量扩展 |
| V03 | Alt+F3 的原版帧时间图被省略，OF 替代图没有相同尺寸、配色和帧率上限参考线 | 恢复原版图表，OF 图表仅在其选项开启时使用 |
| C01 | 原版 Realms、Twitch 及部分菜单/设置集成被删除 | 恢复源码、可达菜单、设置、客户端调用、原配依赖和 native 文件，适配 GLFW/GL3 与新版 Guava |
| C02 | Snooper 只剩空启动/停止，OS/JVM 信息收集及原版调度被省略 | 恢复该类原实现，继续由原有 `snooperEnabled` 设置控制发送 |

以上源码修复已收敛并通过全量编译；本表不是运行测试结果。

## 二、前序确认项的源码修复

- R01–R10：VBO/VAO 绑定缓存、常量 lightmap、纹理矩阵、TexGen、模型 padding、即时上传偏移、全屏模式与退出边界、云颜色、GLSL 指令顺序、shader pack 矩阵与亮度。
- A01–A03：解码失败回滚、真正的流式播放、已停止通道复用和普通/流式声源池。
- W01–W06：传送门坐标复制、发光不透明方块和区块边界光照、生成条件逐 tick 更新、视距队列、服务端卸载缓存、客户端跨线程区块读取。
- O01/L01：强制可见超时不再自动续期；语言切换仍通知全部资源监听器。
- B01：两套反编译源码共享的 `saveExtraData()` 死循环按原版字节码恢复退出分支。

额外恢复 Unicode 检测、染色玻璃板 blockstate/UV、标题标语及被删除的语言键。默认关闭额外实体遮挡剔除，保留设置供主动选择；原版并不存在这条剔除捷径，它还会跳过实体阴影/火焰/调试框。

## 三、版本与边界

当前 MCP 仍是 Java 21、IDEA 源码工程、LWJGL 3.3.6、OpenGL 3.3 core。还原通过兼容实现保留原版行为，不能把 LWJGL 2 固定管线调用直接复制进 core context。

雾距离模式与法线处理依据本地原版调用及 [Khronos NV_fog_distance 规范](https://registry.khronos.org/OpenGL/extensions/NV/NV_fog_distance.txt)、[OpenGL 2.1 规范](https://registry.khronos.org/OpenGL/specs/gl/glspec21.pdf)。无 NV 扩展时采用标准允许的眼平面距离；旧固定管线的具体插值精度仍可能因驱动而异。

Realms 1.7.59 的全部 154 个类保留。适配脚本只改类引用与描述符，原服务实现和资源保留；补齐 commons-compress，并用直接执行器适配旧 Guava 两参数回调。没有向 Realms、Twitch 或 Snooper 服务发起验证请求，服务可用性不在静态结论内。

任意第三方 `InputStream` 未必会响应 interrupt/close；取消机制可以发起关闭并隔离旧工作线程，不能承诺所有自定义资源输入立即终止。

## 四、验证与剩余工作

- 最新第 5 轮 Java 21 全量编译包含全部 2006 个 Java 文件，退出码 0；6 个 warning 为 J2ObjC 注解类缺失及现有 finalize 弃用提示。
- 编译后 Realms 静态成员链接检查覆盖 154 个类、3298 个非 JDK 引用，0 个未解析引用。
- `git diff --check` 通过。
- 未运行测试或启动游戏；GLSL 的驱动编译、画面、窗口切换、音频播放和在线服务仍未实测。
- 预先存在的 `Start.java` accessToken 修改保留，提交时排除。
- 游戏日志与日志配置未修改。
- MCP 还原提交、其后的独立改进阶段，以及 Naven main/noauth/Recode-NoAuth 修复尚未完成。

四边形 flat provoking vertex 的兼容差异仍是候选：所查原版 flat 四边形调用没有同一面不同顶点颜色的明确触发，渐变调用使用 smooth；因此没有为了候选更换三角形对角线。它不计入确认缺陷，仍需运行场景证据。

完整执行进度见 [RESTORATION_PROGRESS.md](C:/Users/Admin/ideaProject/MCP1.8.9/RESTORATION_PROGRESS.md)。前序源码审计见 [MCP189_VS_MCP919_AUDIT.md](C:/Users/Admin/ideaProject/Naven-1.8.9-Private/MCP189_VS_MCP919_AUDIT.md)，其记录的是起点提交的快照。
