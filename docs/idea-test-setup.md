# kura-networking 的 IDEA 测试配置

完整指南见 [`../../kura/docs/testing/idea-test-setup.md`](../../kura/docs/testing/idea-test-setup.md)
（前置要求、Maven 缓存、如何新增配置）。本仓库已内置共享运行配置：

| 配置 | 类型 | 用途 |
| --- | --- | --- |
| Kura network enumeration JUnit | JUnit | 网络接口枚举单测（mock 服务，不依赖真实 D-Bus） |

说明：涉及真实内核 / NetworkManager D-Bus / 防火墙行为的验收不在 Mac 单测
范围内；本仓库保留 dbus 5.2 虚拟线程与 Update/Activate 定制行为，回归以本
仓库现有 JUnit 为准。Maven 缓存缺 plc4x 时先在 `plc4x-yofc` 目录
`mvn install -DskipTests`。环境检查可执行 `../kura/tools/ide/setup-idea.sh`。
