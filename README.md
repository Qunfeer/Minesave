# Minesave

**用 Git 给 Minecraft 存档做备份与回档 · Git-based backup & rollback for Minecraft saves**

[![License](https://img.shields.io/badge/license-Apache--2.0-blue.svg)](LICENSE)
![Java](https://img.shields.io/badge/Java-17%2B-orange.svg)
![JavaFX](https://img.shields.io/badge/JavaFX-21-blueviolet.svg)

---

## 中文

### 简介

Minesave 是一个 Minecraft 存档备份工具。它为每一个存档目录初始化一个本地 Git 仓库：
「备份」就是一次 `git commit`，「回档」就是 `git reset --hard` 回到某一次提交。
所有操作都在你自己的电脑上完成——不联网、不注册、不上传任何存档数据。

### 功能

- **一键备份** —— 选中存档，填写备注，程序自动 `add` + `commit`。
- **一键回档** —— 在历史列表中选中任意一次备份，一键回到那个时间点。
- **多目录管理** —— 内置「版本管理」窗口，可以同时管理多个 `.minecraft` 目录及其下的存档。
- **崩溃防护** —— 备份/回档开始前写入 `operation.lock` 标记，正常结束即删除。如果程序中途被强制结束（断电、任务管理器强杀），下次启动会检测到未完成的操作并提示恢复，避免存档停留在半完成状态。
- **全部离线** —— 不依赖任何网络服务，存档只留在本机。

### 环境要求

- JDK 17 或更高版本
- 已在 Windows 上测试

### 构建与运行

```bash
# 直接运行
./mvnw javafx:run

# 打包成可执行 jar
./mvnw clean package
java -jar target/Minesave-1.0-TEST.jar
```

> Windows 下请使用 `mvnw.cmd` 代替 `./mvnw`。

### 使用方式

1. 点击菜单 **文件 → 版本管理**，添加你的 `.minecraft` 目录（程序会读取其中的 `saves` 文件夹）。
2. 回到主界面，左上角选择这个目录，右侧选择要管理的具体存档。
3. 点 **备份**，填写备注，当前存档状态即被记录成一次提交。
4. 需要回到以前的状态时，在左侧历史列表选中那次备份，点 **恢复**。

> ⚠️ 回档是 `git reset --hard`，会覆盖存档目录里现有的文件。做破坏性操作前，建议先备份一次。

### 项目结构

```
src/main/java/com/qunfeer/minesave/
├── Launcher.java                     # 入口
├── Controller/
│   ├── GitUtil.java                  # Git 操作封装（commit / reset / log / 恢复中断）
│   ├── GitView.java                  # 提交在界面上的展示形式
│   ├── OperationGuard.java           # 中断操作的标记与检测
│   ├── Version.java                  # 一个受管理的存档目录
│   └── VersionsManager.java          # 目录列表的持久化（versions.dat）
├── GUI/Main/
│   ├── MainApplication.java          # JavaFX 应用入口
│   ├── MainController.java           # 主界面逻辑
│   └── VersionManageController.java  # 版本管理弹窗
└── exception/NoValueException.java
```

### 技术栈

Java 17+ · JavaFX 21（FXML）· [JGit](https://www.eclipse.org/jgit/) 7.7 · Maven（shade 打包）

---

## English

### What is this?

Minesave is a backup tool for Minecraft save files. It initializes a local Git repository inside
each save folder — a *backup* is a `git commit`, and a *rollback* is a `git reset --hard` back to a
chosen commit. Everything happens on your own machine: no network, no sign-up, no uploads.

### Features

- **One-click backup** — pick a save, type a note, and the app runs `add` + `commit`.
- **One-click rollback** — select any previous backup in the history list and jump back to it.
- **Multiple directories** — a built-in manager window lets you keep several `.minecraft`
  folders and all their saves under control.
- **Crash guard** — an `operation.lock` marker is written before a backup/rollback and removed on
  completion. If the process is killed mid-operation (power loss, Task Manager), the next launch
  detects the unfinished operation and offers to recover, so a save is never left half-restored.
- **Fully offline** — no network service involved; your saves never leave your computer.

### Requirements

- JDK 17 or newer
- Tested on Windows

### Build & Run

```bash
# run directly
./mvnw javafx:run

# build an executable jar
./mvnw clean package
java -jar target/Minesave-1.0-TEST.jar
```

> On Windows use `mvnw.cmd` instead of `./mvnw`.

### Usage

1. Open **File → Version Manager** and add your `.minecraft` directory (the app reads its `saves` folder).
2. Back on the main window, pick that directory on the left, then the save you want on the right.
3. Click **Backup**, enter a note, and the current state of the save is recorded as a commit.
4. To go back in time, select a backup in the history list and click **Restore**.

> ⚠️ Restore performs `git reset --hard` and overwrites the files currently in the save folder.
> Always make a fresh backup before a destructive operation.

### License

Licensed under the [Apache License 2.0](LICENSE).

Copyright 2026 Qunfeer

---

## Contact

- Author: **Qunfeer**
- Email: <qunfeer@163.com>
