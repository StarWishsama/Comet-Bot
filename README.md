## 彗星 Bot (Comet)

项目名来源于 [Comet](https://music.163.com/#/song?id=22717199)

由高性能机器人框架 [Mirai](https://github.com/mamoe/mirai) 强力驱动

本项目处于开发阶段, 部分功能可能无法使用 (除了写在下面的功能) ~~反正也没人用~~

交流群：725656262

**一切开发旨在学习，请勿用于非法用途**

[![Codacy Badge](https://app.codacy.com/project/badge/Grade/6d5507c8dc364b9a9cb2a04bd8d04e64)](https://www.codacy.com/gh/StarWishsama/Comet-Bot/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=StarWishsama/Comet-Bot&amp;utm_campaign=Badge_Grade)
[![Kotlin Gradle](https://github.com/StarWishsama/Comet-Bot/workflows/Kotlin%20Gradle/badge.svg)](https://github.com/StarWishsama/Comet-Bot/actions/)
[![LICENSE](https://img.shields.io/github/license/StarWishsama/Comet-Bot.svg?style=popout)](https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE)
[![Issues](https://img.shields.io/github/issues/StarWishsama/Comet-Bot.svg?style=popout)](https://github.com/StarWishsama/Comet-Bot/issues)
![Stars](https://img.shields.io/github/stars/starwishsama/Comet-Bot)
[![Release](https://img.shields.io/github/v/release/StarWishSama/Comet-Bot?include_prereleases)](https://github.com/StarWishsama/Comet-Bot/releases)

## 🎉 它能干什么?

* 以图搜图 (支持 ascii2d/SauceNao)
* 彩虹六号战绩查询
* 打卡
* 签到
* 哔哩哔哩UP主信息/动态查询
* 点歌 (卡片/纯文本样式)
* 事件概率占卜
* 明日方舟抽卡模拟器
* 去你大爷的小程序 (将小程序转换为文本)
    - 哔哩哔哩
* 单推小助手
    - 支持订阅哔哩哔哩用户动态/开播提醒
    - 支持订阅推特用户最新消息
* rCon 功能, 支持连接到有 rCon 功能的游戏
* 查询推特用户信息/推文
* 自动推送最新推文到指定群聊
* 禁言/踢出群员
* 群抽奖
* Github WebHook 推送
* 能不能好好说话
* 企鹅物流 [BETA]
* 小鸡词典
* 还在开发中...

## ☑ To-Do 列表

详见 [Issues](https://github.com/StarWishsama/Comet-Bot/issues)

## 💽 如何使用

### Mirai-Console 插件版 (实验性)

- 实验性的支持, 可能存在无法使用的恶性 Bug, 欢迎反馈问题

~~你可以在[这里](https://github.com/StarWishsama/Comet-Bot/tree/mirai-console)找到插件版本的 Comet, 可以如下编译

或是从 [Github Actions](https://github.com/StarWishsama/Comet-Bot/actions?query=branch%3Amirai-console) 中下载~~

自 0.6.3 起, Releases 中已提供插件版下载

### 自编译

- 注意: 请使用 JDK 11 或更高版本打包, **推荐**使用 JDK 11+

1. 编译

* Clone 或者下载这个项目.

 ```bash
 git clone https://github.com/StarWishsama/Comet-Bot.git
 ```

本项目使用 Gradle 作为包管理系统, 还使用了 ShadowJar 引入依赖. 你可以在项目文件夹中打开终端/cmd/或其他等效软件 输入 ./gradlew clean shadowjar 等待提示成功后, 可以在
./build/libs 下找到编译成功的 jar

### Releases 下载

1. 在[此处](https://github.com/StarWishsama/Comet-Bot/releases)下载最新版本

2. 使用 cmd 启动 Bot, 按照提示登录
   ```java -jar jar的路径```

3. 提示启动完成后, 在机器人加入的群内发送 /help 就可开始了解 Comet 的各项功能了

## 📜 协议

**一切开发旨在学习，请勿用于非法用途**

本项目使用 [AGPLv3](https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE) 协议

------

    Copyright (C) 2018-2021 StarWishsama
    
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.
    
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.
    
    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

## 🔈 鸣谢

> [IntelliJ IDEA](https://zh.wikipedia.org/zh-hans/IntelliJ_IDEA) 是一个功能强大、符合人体工程学且智能的 IDE, 适用于 JVM 平台语言的开发.

特别感谢 [JetBrains](https://www.jetbrains.com/?from=comet-bot)
为开源项目提供免费的 [IntelliJ IDEA](https://www.jetbrains.com/idea/?from=comet-bot) 等 IDE 的授权  
[<img src=".github/jetbrains.png" width="200"/>](https://www.jetbrains.com/?from=comet-bot)

> [PRTS Wiki](http://prts.wiki/) 玩家自由构筑的明日方舟中文 Wiki

特别感谢 PRTS Wiki 提供的干员数据以及干员立绘

![](http://prts.wiki/ak.png?8efd0)

特别感谢 [Kengxxiao](https://github.com/Kengxxiao/) 的项目 [ArknightsGameData](https://github.com/Kengxxiao/ArknightsGameData)
提供明日方舟游戏数据资源

特别感谢 [企鹅物流](https://penguin-stats.io/) 提供的明日方舟物品掉落数据

![](https://penguin.upyun.galvincdn.com/logos/penguin_stats_logo.png)

