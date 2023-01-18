# 欢迎来到 Comet
Comet 是一款支持多 IM 平台的机器人，目前主要支持 QQ、Telegram 两大平台。
Comet 使用 Kotlin + Java **17** 开发，因此在使用前**必须**安装 Java 环境。

## 使用 Comet

### 直接下载使用
Comet 目前处于重构开发阶段，目前提供两种方式供下载：

1. GitHub Actions
   在 Action CI 中，提供了最新构建版本，其中包含着最新问题修复和最新问题。
   不建议在生产环境中使用该版本。

欲通过 GitHub Actions 下载，请打开 [Action](https://github.com/StarWishsama/Comet-Bot/actions)。
![图片](https://user-images.githubusercontent.com/25561848/204084431-2ef500ff-0068-41d0-8188-c64e1758304b.png)
选择最新含有 `dev` 字样的构建，单击进入下载 `Artifact` 下的压缩包：
![图片](https://user-images.githubusercontent.com/25561848/204084460-5e3db3f7-b285-4cde-9bea-497b4a7c62fc.png)

GitHub Actions 总是提供全量 Comet，包含了所有组件。

2. GitHub Release
   尽管新 Comet 仍在~摸鱼~积极开发中，但仍会不定时提供 Release 版本下载。
   欲通过 Release 下载 Comet，请打开 [Releases](https://github.com/StarWishsama/Comet-Bot/releases)

![图片](https://user-images.githubusercontent.com/25561848/204084518-0f53957c-af80-4774-98b0-2a4250e51b4d.png)

下载最顶部的发行版中的 `Assets`，并按需下载你需要的 Comet 组件，关于组件我们会在下面详细解释。

## 启动 Comet
1. 在任意位置解压 Comet, 并在这个位置下打开终端
2. 输入 `java -jar comet-console.jar`
3. 启动 Comet
4. 输入 /login 登录指定平台账号 (QQ, Telegram)
5. 提示登录成功后, 在机器人加入的群内发送 /help 就可开始了解 Comet 的各项功能了

得益于 Comet 的模块化设计，Comet 支持多个 IM 平台的同时服务。
但 Comet-Console 本体并不提供任何 IM 平台的服务，需要安装 Comet 组件。
查阅此处了解更多：[Comet 组件](./comet-module.md)

### 使用 Docker (实验性)
Comet 现在支持在 Docker 容器中运行。

可以使用 `docker pull noraincity/comet-bot` 或是 `docker pull ghcr.io/starwishsama/comet-bot:latest` 获取每日推送的最新镜像。

请注意：在容器中运行的 Comet 并不支持以普通方式输入命令，需要配置好 WebAPI 操作。
且无法支持完成 Mirai 后续验证码操作，需要预先映射好相关文件以备登录。
