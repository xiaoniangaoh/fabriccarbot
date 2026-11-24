<<<<<<< HEAD
# FabricCarbot

Fabric 端 Carpet 假人管理模组，面向 Minecraft 26.3，功能参照 MCDReforged 的 BotKikai：
支持**中文别名**、**登记坐标/维度/朝向**、**按别名一键召唤** Carpet 假人。

## 依赖

| 组件 | 版本 |
|------|------|
| Minecraft | 26.3 |
| Fabric Loader | 0.20.1 |
| Fabric API | 0.167.0+26.3 |
| Carpet | 26.3+v261008（或同版本任意构建） |
| Java | 25+（本机 26） |

## 安装

1. 安装 [Fabric Loader 0.20.1](https://fabricmc.net/use/) 与 Minecraft 26.3。
2. 放入 `fabric-carpet-26.3+v261008.jar`（[Modrinth](https://modrinth.com/mod/carpet)）。
3. 放入 `fabric-api-0.167.0+26.3.jar`。
4. 放入本模组构建产物：`build/libs/fabriccarbot-1.0.0.jar`。
5. 启动服务端/单人游戏即可。

## 命令

所有命令需要 2 级（OP）权限：

| 命令 | 作用 |
|------|------|
| `/carbot add <botName> <中文别名>` | 在当前位置、维度、朝向登记假人 |
| `/carbot spawn <别名>` | 按别名在登记位置召唤 Carpet 假人（内部调用 `/player <name> spawn at x y z facing yaw pitch in <dim>`） |
| `/carbot kill <别名>` | 移除该假人 |
| `/carbot list` | 列出所有登记记录 |
| `/carbot remove <别名>` | 删除登记记录（不移除已召唤的假人） |

### 示例

```
/carbot add Steve 小明
/carbot spawn 小明
/carbot kill 小明
/carbot list
/carbot remove 小明
```

## 数据持久化

登记记录保存在 `config/fabriccarbot/bots.json`，服务端重启后自动加载。

## 构建

```bash
gradle build
```

产物位于 `build/libs/fabriccarbot-1.0.0.jar`。

## 实现说明

- 作为 Carpet 扩展（`CarpetExtension`）注册，命令通过 `CarpetServer.manageExtension` 挂载。
- 假人实体由 Carpet 的 `/player <name> spawn at x y z yaw pitch in <dim>` 原生能力创建，本模组只负责别名映射与坐标记忆。
- 别名不区分大小写（英文部分），中文别名原样匹配。
=======
# fabriccarbot
Fabric 26.3 Carpet bot management mod with Chinese aliases and coordinate registration - alternative to BotKikai for Fabric
>>>>>>> 27c0fe51a370817b4b3d2368daa45f453687bd1f
