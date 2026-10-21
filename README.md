# FTBC Xaero Compat

一个 Forge 1.20.1 的**客户端**模组，把 [FTB Chunks](https://www.curseforge.com/minecraft/mc-mods/ftb-chunks-forge) 的区块认领数据接入 [Xaero's World Map](https://www.curseforge.com/minecraft/mc-mods/xaeros-world-map) 和 [Xaero's Minimap](https://www.curseforge.com/minecraft/mc-mods/xaeros-minimap)。

## 功能

- 在 Xaero 世界地图和小地图上高亮显示 FTB Chunks 的认领区块，颜色使用 FTB Teams 的队伍颜色。
- 世界地图左下角有一个 FTB 图标按钮，单击在三种全局状态之间循环：
  1. 不显示 FTB 区块
  2. 显示 FTB 区块
  3. 显示 FTB 区块与强制加载
- 三种状态**同时作用于世界地图和小地图**；按钮右下角的小圆点显示当前状态（灰色/绿色/金色），鼠标悬停时会列出三种状态，当前状态以白色加粗显示。
- 强制加载区块使用金色斜线标记；只有第三种状态会显示这些斜线。
- 在世界地图右键菜单中直接：
  - 认领区块
  - 取消认领
  - 强加载
  - 取消强加载
- 支持右键单击单个区块，也支持按住右键拖拽框选多个区块。
- 大范围操作会弹出确认界面（阈值可配置）。
- 操作结果会显示在 Xaero 世界地图的消息框中；失败原因来自 FTB Chunks 服务端校验。

按钮优先放在 Xaero 左侧按钮列 `height - 80` 的空位（维度按钮与 hop 按钮之间），也就是截图中红框区域正上方；如果该位置已被占用，再动态扫描左下角其它 20×20 空位，因此不会覆盖 Xaero 自己的按钮、洞穴模式控件或其它模组按钮。

## 依赖

本模组只装在**客户端**即可，不要放进服务端的 `mods` 文件夹。服务端只需要原本的 FTB Chunks 及其依赖。

必需：

- Minecraft Forge 1.20.1（47.x）
- FTB Chunks（1.20.1，2001.x）
- FTB Teams（1.20.1，2001.x）
- FTB Library（1.20.1，2001.x）
- Architectury API（1.20.1，9.x）
- Xaero's World Map（1.20.1，1.45.0 或更高；按 1.45.0 的内部结构编译，并已用 1.47.0 静态核对）

可选：

- Xaero's Minimap（1.20.1，26.x；按 26.4.2 编译）。安装后小地图会使用与世界地图相同的显示状态。

## 安装

1. 安装 Forge 1.20.1 客户端。
2. 把 `ftbcxaerocompat-1.0.0.jar` 和上述依赖放入 `mods` 文件夹；如果已有旧版 `ftbuxaerocompat` 或其他版本，请先删除旧 jar，避免同一个模组 ID 被加载两次。
3. 启动游戏，进入世界后按 Xaero 世界地图的快捷键（默认 `M`）打开地图。

## 使用

- **显示控制**：世界地图左下角的 FTB 图标按钮按固定顺序循环：
  `不显示 FTB 区块 → 显示 FTB 区块 → 显示 FTB 区块与强制加载 → 不显示 FTB 区块`。
- 切换后世界地图和小地图会立即刷新，不需要重新打开地图，也不需要重新跑图。
- 不显示状态下，认领区块和强制加载斜线都会隐藏，但右键菜单中的认领/强加载操作仍然可用。
- **单个区块**：在地图上右键点击目标区块，在菜单里选择“认领”“取消认领”“强加载”等。
- **多个区块**：按住右键拖拽框选，松开右键后菜单会显示针对整个区域的操作。
- 只允许操作**当前所在维度**。FTB Chunks 的网络包使用玩家当前维度，跨维度操作会被禁用，避免误认领到错误维度。
- 是否能认领、认领数量上限、是否必须与已有领地相邻、强制加载上限等规则，仍然完全由 FTB Chunks 服务端配置和队伍权限决定。操作失败时，失败信息会显示在地图消息框。

## 配置

首次启动后生成 `config/ftbcxaerocompat-client.toml`：

```toml
[display]
    # FTB 区块显示状态：0 = 不显示，1 = 显示区块，2 = 显示区块与强制加载
    # 世界地图左下角的 FTB 按钮会修改并保存这个值
    displayMode = 1
    # 领地内部填充不透明度（0-255）
    fillOpacity = 72
    # 领地边界不透明度（0-255）
    borderOpacity = 190

[interaction]
    # 是否在世界地图右键菜单中加入 FTB Chunks 操作
    showContextMenu = true
    # 大范围操作是否弹出确认
    confirmLargeOperations = true
    # 超过多少个区块时弹出确认
    confirmThreshold = 256
```

默认值是 `displayMode = 1`，即显示认领区块、不显示强制加载斜线，保持旧版本默认可见认领区块的行为。状态会跨世界保存，因为它是客户端全局显示设置。

## 构建

需要 JDK 17。

Windows：

```bat
gradlew.bat build
```

Linux / macOS：

```bash
./gradlew build
```

产物位于 `build/libs/ftbcxaerocompat-1.0.0.jar`，构建后也会在项目根目录生成一份同名 jar。项目根目录的 `gradlew` / `gradlew.bat` 是 Gradle Wrapper，首次运行会自动下载 Gradle 8.1.1。

开发环境构建使用 `libs/` 下的 Xaero's World Map 1.45.0、Xaero's Minimap 26.4.2 与 XaeroLib 1.7.1 作为编译期依赖。它们不会被打进本模组的 jar，也不随本仓库上传（第三方模组文件）；请自行从对应发布页下载，并按上述版本号命名后放入 `libs/`。

## 实现说明

FTB Chunks 在玩家登录时会把所有对玩家可见的领地发送到客户端，并在领地变化时发送增量更新。本模组通过 Mixin 拦截 `FTBChunksClient#updateChunksFromServer`，在内存中维护一个轻量的领地索引：

- 高亮：向 Xaero 世界地图的 `HighlighterRegistry` 注册 `FTBChunksHighlighter`；安装 Xaero 小地图时，再向小地图自己的 `HighlighterRegistry` 注册 `FTBChunksMinimapHighlighter`。
- 全局显示状态：`DisplayMode` 读取 Forge 配置 `displayMode`，世界地图和小地图的两个高亮器都只读取这一份状态，因此不会出现两个地图开关不一致的问题；旧的“小地图纹理过渡”逻辑已删除。
- 地图按钮：在 `GuiMap#m_7856_` 末尾注入 `FTBChunksMapButton`，优先占用左侧按钮列 `height - 80` 的空位，再动态扫描其它空位。按钮使用 32×32 的 `FTB.png` 缩放绘制，并显示状态指示点；点击后保存配置并刷新世界地图和小地图的高亮缓存。
- 交互：在 `GuiMap#getRightClickOptions` 末尾追加菜单项，使用 FTB Chunks 已有的 `RequestChunkChangePacket` 发送认领/强加载请求。
- 结果：拦截 `ChunkChangeResponsePacket#handle`，把 FTB Chunks 的校验结果显示到 Xaero 地图的消息框。

因此不需要服务端安装本模组，也不会修改 FTB Chunks 的认领规则。

## 兼容性说明

本模组通过 Mixin 接入 Xaero 世界地图和 FTB Chunks 的内部类，没有官方 addon API，因此对版本较敏感：

- Xaero's World Map 按 **1.45.0** 的内部结构编译，1.45.0 与 1.47.0 的相关类、字段和方法已静态核对；1.46.x 预期兼容，1.48+ 可能发生变化。
- Xaero's Minimap 按 **26.4.2** 的内部结构编译；26.x 系列预期兼容。小地图集成使用独立、可选的 Mixin 配置，未安装小地图时不会加载相关类。
- FTB Chunks 按 **2001.3.8** 编译，2001.x 系列通常兼容。
- 如果游戏启动时日志出现 `Mixin apply failed`，请先确认 Xaero World Map、Xaero's Minimap 和 FTB Chunks 版本是否匹配；若只是部分功能缺失，可以查看 `latest.log` 中的 `ftbcxaerocompat` 相关警告。

## 许可

本项目使用 LGPL-3.0-only。

FTB Chunks、Xaero's World Map 及其相关代码分别归各自作者所有。本项目只通过公开/可调用的类与 Mixin 进行兼容，不包含它们的代码。
