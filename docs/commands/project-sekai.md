# 世界计划

## 主命令
> 主命令: projectsekai
> 可用别名: pjsk, 啤酒烧烤

查询 Project Sekai: Colorful Stage 游戏相关的信息。
目前支持查询账号打歌进度、谱面查询、活动排名查询。

在绑定账号后，直接输入主命令即可直接查询活动排名数据。

## 子命令
### bind
> 可用别名: 绑定
> 命令示例: /pjsk bind -i 账号ID

用于绑定你的 Project Sekai 账号，目前仅支持日服账号。
请注意，请不要尝试绑定他人账号，如果想查询他人账号信息可以另外查询，无需绑定。

### event
> 可用别名: 活动排名, 活排
> 命令示例: /pjsk event (排名位置)

查询指定排名或是自己的活动排名数据，数据来源于 [Unibot API](https://docs.unipjsk.com/) 和 [Project Sekai Profile](https://profile.pjsekai.moe/)

### pred
> 可用别名: prediction, 预测, 预测线
> 命令示例: /pjsk pred

查询当前活动不同位置活动积分的预测值，数据来源于 [33Kit](https://3-3.dev/)。

### best30
> 可用别名: b30
> 命令示例: /pjsk b30

查询你的 Best30 曲目，按照 PJSK Profile 提供的歌曲难度排名。

### chart
> 可用别名: 谱面, 谱面预览
> 命令示例: /pjsk chart [歌曲名称] (-d 难度)

预览歌曲的谱面，数据来源于 [プロセカ譜面保管所](https://sdvx.in/prsk.html)，谱面数据并非实时更新，新歌会出现查询失败的情况。

难度可选值为：
```text
Master, ma, 大师
Expert, ex, 专家
```
且大小写不敏感。

### music
> 可用别名: 音乐, 查音乐
> 命令示例: /pjsk music [音乐名]

查询歌曲的相关信息，包括 BPM，等级，key 数量，作者等。

### info
> 可用别名: 查询
> 命令示例: /pjsk info (用户ID)

查询自己或他人的账号信息。
