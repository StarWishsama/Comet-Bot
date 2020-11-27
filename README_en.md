# Comet-Bot

This project powered by high efficiency bot framework, [Mirai](https://github.com/mamoe/mirai).

This bot is under heavy develop now, Many features may **not work as expected**.

**All development is for learning, DO NOT use it for illegal purposes**

中文版本: [README](https://github.com/StarWishsama/Comet-Bot/blob/mirai/README_zhCN.md)

[![Codacy Badge](https://app.codacy.com/project/badge/Grade/b26348aabf51452195dbc14846accd86)](https://www.codacy.com/manual/StarWishsama/Comet-Bot?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=StarWishsama/Comet-Bot&amp;utm_campaign=Badge_Grade)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=StarWishsama_Nameless-Bot&metric=alert_status)](https://sonarcloud.io/dashboard?id=StarWishsama_Nameless-Bot)
[![Kotlin Gradle](https://github.com/StarWishsama/Comet-Bot/workflows/Kotlin%20Gradle/badge.svg)](https://github.com/StarWishsama/Comet-Bot/actions/)
[![LICENSE](https://img.shields.io/github/license/StarWishsama/Comet-Bot.svg?style=popout)](https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE)
[![Issues](https://img.shields.io/github/issues/StarWishsama/Comet-Bot.svg?style=popout)](https://github.com/StarWishsama/Comet-Bot/issues)
![Stars](https://img.shields.io/github/stars/starwishsama/Comet-Bot)
[![Release](https://img.shields.io/github/v/release/StarWishSama/Comet-Bot?include_prereleases)](https://github.com/StarWishsama/Comet-Bot/releases)

## 🎉 Features
* Search picture original source by picture
* Rainbow Six: Siege game data info lookup
* Clock in & Check-In
* Bilibili user info/dynamic/streaming notice
* Search music and share as LightApp style
* Arknights / PCR draw simulator
* Auto convert QQ LightApp message to text for PC user(s)
* VTuber monitor (BiliBili/Twitter)
* Twitter user's dynamic Pusher
* RCON feature
* Mute/Kick group member
* Group drawing
* WIP

## ☑ To-Do 
Development Roadmap: [Project Page](https://github.com/StarWishsama/Comet-Bot/projects/2)

## 💽 How to use

### Build it yourself

- Attention: Please use JDK 8 or higher version to build, use AdoptOpenJDK 11 is recommended.

1. Compile
 * do git clone or download it simply.

 ```bash
 git clone https://github.com/StarWishsama/Comet-Bot.git
 ```

 This project use [Gradle](https://gradle.org/) as build tool, and used shadowjar to compile dependencies.
 You can do ./gradlew shadowjar in cmd or something else to build.
 Until gradle shows build successful, You can find compiled jar in ./build/libs.

### Download on Github Releases
1. Click [here](https://github.com/StarWishsama/Comet-Bot/releases) to download latest version.


2. Use cmd or something else to start Comet, Follow the instruction to log in.
3. When Comet shows started success, Send /v in group which bot has joined to check whether it works properly.

## 📜 License 
**All development is for learning, DO NOT use it for illegal purposes**

This project uses [AGPL v3.0](https://github.com/StarWishsama/Comet-Bot/blob/master/LICENSE)

------

    Copyright (C) 2018-2020 StarWishsama
    
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
    
## 🔈 Special Thanks
> [IntelliJ IDEA](https://zh.wikipedia.org/zh-hans/IntelliJ_IDEA) is a capable, ergonomic and intelligent IDE, suitable for development of JVM platform language.

Big thanks to [JetBrains](https://www.jetbrains.com/?from=comet-bot) for allocating free open-source licences for Jetbrains' IDEs such as [IntelliJ IDEA](https://www.jetbrains.com/idea/?from=comet-bot) 
[<img src=".github/jetbrains.png" width="200"/>](https://www.jetbrains.com/?from=comet-bot)

> [PRTS Wiki](http://prts.wiki/) A Chinese ArkNights wiki built by players freely

Big thanks to PRTS Wiki for providing ArkNights operator data and art