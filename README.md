# Minecraft 1.8 Premium modification Source

本仓库是一个基于 **Minecraft 1.8.9** 版本的 **MCP (Minecraft Coder Pack)** 客户端开发项目。

## 项目概述

- **Minecraft 版本**：1.8.9
- **构建工具**：MCP 官方工具链
- **用途**：个人客户端开发、模组开发
- **环境**：JDK 21、Windows、OpenGL 3.3 Core Profile

## 依赖版本

- 渲染与窗口：LWJGL 3.4.3（GLFW、OpenGL、OpenAL、STB、Jemalloc）
- 网络：Netty 4.2.19.Final
- 常用库：Guava 33.7.2-jre、Gson 2.14.0
- 日志：Log4j 2.26.1
- 系统信息：OSHI 7.7.1、JNA 5.19.1、SLF4J 2.0.20
- Apache Commons：Codec 1.23.0、Compress 1.28.0、IO 2.22.0、Lang 3.21.0、Text 1.15.0、Logging 1.4.0
- 兼容版本：HttpClient 4.5.14、HttpCore 4.4.16、jopt-simple 5.0.4

Minecraft Authlib、ICU4J、Realms 和 Twitch 组件使用 1.8.9 对应版本；Realms JAR 保留项目的 GL3 适配版。

 ## 本项目相较于原版的改动

  - 加入了Optifine和BetterFPS（已移除Forge反射调用）
  - 重写了部分渲染减少性能占用
  - 几乎恢复了所有被混淆的字段
  - 移除了Him



**Love from FireFlyDeveloper54 with❤️ Claude-Code & Mojang Studios**
