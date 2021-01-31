package io.github.starwishsama.comet.test

import com.github.salomonbrys.kotson.fromJson
import com.google.gson.GsonBuilder
import io.github.starwishsama.comet.objects.pojo.youtube.SearchChannelResult

class TestSearchChannelResult

val GSON = GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create()

fun main() {
    val testJson = "{\n" +
            "  \"kind\": \"youtube#channelListResponse\",\n" +
            "  \"etag\": \"gSlmHO988MiNC1yWWmFzrAVlnd0\",\n" +
            "  \"pageInfo\": {\n" +
            "    \"totalResults\": 1,\n" +
            "    \"resultsPerPage\": 5\n" +
            "  },\n" +
            "  \"items\": [{\n" +
            "    \"kind\": \"youtube#channel\",\n" +
            "    \"etag\": \"9J7G7lJBbuw-RPiIEu0Fo75eLRA\",\n" +
            "    \"id\": \"UC4YaOt1yT-ZeyB0OmxHgolA\",\n" +
            "    \"snippet\": {\n" +
            "      \"title\": \"A.I.Channel\",\n" +
            "      \"description\": \"はじめまして！ みんなと、あなたと繋がりたい！キズナアイです(o・v・o)♪\\nチャンネル登録よろしくお願いしますლ(´ڡ`ლ)\",\n" +
            "      \"customUrl\": \"aichannel\",\n" +
            "      \"publishedAt\": \"2016-10-19T06:03:24Z\",\n" +
            "      \"thumbnails\": {\n" +
            "        \"default\": {\n" +
            "          \"url\": \"https://yt3.ggpht.com/ytc/AAUvwnhGnnDhdjO7gAkYmd5dvOdKQzgmU6lJfXZfC6CIoA=s88-c-k-c0x00ffffff-no-rj-mo\",\n" +
            "          \"width\": 88,\n" +
            "          \"height\": 88\n" +
            "        },\n" +
            "        \"medium\": {\n" +
            "          \"url\": \"https://yt3.ggpht.com/ytc/AAUvwnhGnnDhdjO7gAkYmd5dvOdKQzgmU6lJfXZfC6CIoA=s240-c-k-c0x00ffffff-no-rj-mo\",\n" +
            "          \"width\": 240,\n" +
            "          \"height\": 240\n" +
            "        },\n" +
            "        \"high\": {\n" +
            "          \"url\": \"https://yt3.ggpht.com/ytc/AAUvwnhGnnDhdjO7gAkYmd5dvOdKQzgmU6lJfXZfC6CIoA=s800-c-k-c0x00ffffff-no-rj-mo\",\n" +
            "          \"width\": 800,\n" +
            "          \"height\": 800\n" +
            "        }\n" +
            "      },\n" +
            "      \"localized\": {\n" +
            "        \"title\": \"A.I.Channel\",\n" +
            "        \"description\": \"はじめまして！ みんなと、あなたと繋がりたい！キズナアイです(o・v・o)♪\\nチャンネル登録よろしくお願いしますლ(´ڡ`ლ)\"\n" +
            "      },\n" +
            "      \"country\": \"JP\"\n" +
            "    },\n" +
            "    \"contentDetails\": {\n" +
            "      \"relatedPlaylists\": {\n" +
            "        \"likes\": \"\",\n" +
            "        \"favorites\": \"\",\n" +
            "        \"uploads\": \"UU4YaOt1yT-ZeyB0OmxHgolA\"\n" +
            "      }\n" +
            "    },\n" +
            "    \"statistics\": {\n" +
            "      \"viewCount\": \"359352019\",\n" +
            "      \"subscriberCount\": \"2910000\",\n" +
            "      \"hiddenSubscriberCount\": false,\n" +
            "      \"videoCount\": \"985\"\n" +
            "    }\n" +
            "  }]\n" +
            "}"

    println(GSON.fromJson<SearchChannelResult>(testJson))
}