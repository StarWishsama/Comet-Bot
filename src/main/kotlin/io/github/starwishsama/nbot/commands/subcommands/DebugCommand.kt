package io.github.starwishsama.nbot.commands.subcommands

import cn.hutool.http.HttpRequest
import io.github.starwishsama.nbot.BotInstance
import io.github.starwishsama.nbot.commands.CommandProps
import io.github.starwishsama.nbot.commands.interfaces.UniversalCommand
import io.github.starwishsama.nbot.enums.UserLevel
import io.github.starwishsama.nbot.objects.BotUser
import io.github.starwishsama.nbot.util.BotUtils
import javafx.scene.effect.Light
import net.mamoe.mirai.message.ContactMessage
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.uploadAsImage
import net.mamoe.mirai.utils.ExternalImage
import java.io.File

class DebugCommand : UniversalCommand {
    override suspend fun execute(message: ContactMessage, args: List<String>, user: BotUser): MessageChain {
        val client = BotInstance.client

        if (args.isNotEmpty() && BotUtils.isNoCoolDown(message.sender.id)) {
            when (args[0]) {
                "image" -> {
                    val map = mutableMapOf<String, String>()
                    map["user-agent"] = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.163 Safari/537.36"
                    val stream = HttpRequest.get("https://i.loli.net/2020/04/14/INmrZVhiyK5dgbk.jpg")
                        .setFollowRedirects(true)
                        .timeout(150_000)
                        .addHeaders(map)
                        .execute().bodyStream()
                    return stream.uploadAsImage(message.subject).asMessageChain()
                }
                "xml" ->{
                    message.quoteReply("Test")
                    return XmlMessage(15,"<?xml version='1.0' encoding='UTF-8' standalone='yes' ?><msg serviceID=\"15\" templateID=\"1\" action=\"web\" brief=\"测试\" sourceMsgId=\"0\" url=\"https://space.bilibili.com/410484677\" flag=\"0\" adverSign=\"0\" multiMsgFlag=\"0\"><item layout=\"0\" mode=\"1\" advertiser_id=\"0\" aid=\"0\"><summary>测试</summary><hr hidden=\"false\" style=\"0\" /></item><item layout=\"2\" mode=\"1\" advertiser_id=\"0\" aid=\"0\"><picture cover=\"https://i2.hdslb.com/bfs/face/ec24bb376f1448219295eb80db2a537b0c4d87bd.jpg\" w=\"0\" h=\"0\" needRoundView=\"0\" /><title>测试</title><summary>测试</summary></item><source name=\"\" icon=\"\" action=\"\" appid=\"-1\" /></msg>").asMessageChain()
                }
                "json" -> {
                    message.quoteReply("等1下")
                    return LightApp("{\n" +
                            "    \"app\": \"com.tencent.structmsg\",\n" +
                            "    \"desc\": \"音乐\",\n" +
                            "    \"view\": \"music\",\n" +
                            "    \"ver\": \"0.0.0.1\",\n" +
                            "    \"prompt\": \"[分享] Shiny Smily Story\",\n" +
                            "    \"meta\": {\n" +
                            "        \"music\": {\n" +
                            "            \"action\": \"\",\n" +
                            "            \"android_pkg_name\": \"\",\n" +
                            "            \"app_type\": 1,\n" +
                            "            \"appid\": 100497308,\n" +
                            "            \"desc\": \"HOLOLIVE Idol Project\",\n" +
                            "            \"jumpUrl\": \"http://url.cn/500SgnR?_wv=1\",\n" +
                            "            \"musicUrl\": \"http://url.cn/5ndGtwZ\",\n" +
                            "            \"preview\": \"http://y.gtimg.cn/music/photo_new/T002R150x150M000003EDKWq0V5WXs_1.jpg\",\n" +
                            "            \"sourceMsgId\": \"0\",\n" +
                            "            \"source_icon\": \"\",\n" +
                            "            \"source_url\": \"\",\n" +
                            "            \"tag\": \"QQ音乐\",\n" +
                            "            \"title\": \"Shiny Smily Story\"\n" +
                            "        }\n" +
                            "    }\n" +
                            "}").asMessageChain()
                }
                "bili" -> {
                    when (args[1]){
                        "search", "搜索" -> {
                            val searchResult = client.appAPI.searchUser(keyword = args[2]).await()
                            return if (searchResult.data.items.isNotEmpty()) {
                                val item = searchResult.data.items[0]
                                (item.title + "\n粉丝数: " + item.fans + "\n最近投递的视频: " + (if (!item.avItems.isNullOrEmpty()) item.avItems[0].title else "没有投稿过视频") + "\n直播状态: " + (if (item.liveStatus == 1) "直播中" else  "未直播")).toMessage()
                                    .asMessageChain()
                            } else {
                                "Bot > 账号不存在".toMessage().asMessageChain()
                            }
                        }
                        "coin", "投币" -> {
                            if (user.level == UserLevel.OWNER) {
                                val avNumber = args[2].toLong()
                                if (avNumber != 0L) {
                                    val response = client.appAPI.addCoin(aid = avNumber, multiply = 2).await()
                                    return if (response.code == 0) {
                                        "Bot > 投币成功".toMessage().asMessageChain()
                                    } else {
                                        ("Bot > 投币可能失败, 响应内容为 " + response.data).toMessage().asMessageChain()
                                    }
                                }
                            }
                        }
                    }
                }
                "help" -> return getHelp().toMessage().asMessageChain()
                else -> return "Bot > 命令不存在\n请注意: 这里的命令随时会被删除.".toMessage().asMessageChain()
            }
        }
        return EmptyMessageChain
    }

    override fun getProps(): CommandProps {
        return CommandProps("debug", null, "nbot.commands.debug", UserLevel.ADMIN)
    }

    override fun getHelp(): String = "直接开 IDE 看会死掉吗"
}
