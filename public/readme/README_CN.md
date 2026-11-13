# TintedPaper

**TintedPaper 是一个 AI-driven（人工智能驱动）的 Paper 分支，专注于 Minecraft 数据包原生优化、性能、原版机制和稳定性。**

[English](../../README.md) | **中文**

## AI 驱动开发

Codex、Claude 等 AI 代理参与代码分析、实现、上游比较和回归测试。更改仍需维护者审查和验证；使用 AI 不代表代码已经正确，也不代表性能提升已经得到测量。

本项目正在将 [DatapackOptimizer](https://github.com/KiyroPrower/DatapackOptimizer) 从插件迁移为原生服务器补丁。每个迁移项都会与当前服务器实现比较，复用现有优化并排除过时补丁。

## 当前状态

- 目标版本：Minecraft **26.3**，需要 **Java 25**。
- 迁移尚未完成。已验证的工作和待办项见[迁移报告](../../docs/DPO-MIGRATION.md)及[迁移清单](../../docs/dpo-port-decisions.json)。
- 插件、数据包兼容性及 TPS/MSPT 性能变化仍需运行时验证。

## 构建与支持

- [构建与开发构件](https://github.com/KiyroPrower/TintedPaper/actions/workflows/build-263.yml)
- [发行版本](https://github.com/KiyroPrower/TintedPaper/releases)
- [问题反馈与功能建议](https://github.com/KiyroPrower/TintedPaper/issues)
- [贡献指南](../../CONTRIBUTING.md)

CI 上传开发构件，不会自动发布稳定版或部署服务器。

## 本地构建

```powershell
.\gradlew.bat applyAllPatches
.\gradlew.bat :leaf-server:test createLeafclipJar
```

Linux/macOS 使用 `./gradlew`。为保持补丁系统兼容性，内部模块与构建任务保留原有标识。构件位于 `leaf-server/build/libs/`。

## 许可证与上游致谢

TintedPaper 基于 [Paper](https://github.com/PaperMC/Paper)、[上游实现](https://github.com/Winds-Studio/Leaf)，以及 Gale、Pufferfish、Purpur、Moonrise、Lithium 和补丁头中注明的其他项目。原作者和许可证声明保持不变。

完整条款见 [LICENSE.md](../../LICENSE.md)、[许可证目录](../../licenses)及各补丁头。项目更名不会转移上游工作的作者身份。
