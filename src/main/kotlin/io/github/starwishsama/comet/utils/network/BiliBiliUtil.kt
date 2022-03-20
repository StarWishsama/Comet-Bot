package io.github.starwishsama.comet.utils.network

private val pureNumberRegex by lazy { Regex("""^([aA][vV]\d+|[bB][vV]\w+|[eE][pP]\d+|[mM][dD]\d+|[sS]{2}\d+)$""") }
private val shortLinkRegex by lazy { Regex("""^(https?://)?(www\.)?b23\.tv/(\w+)$""") }
private val bvAvUrlRegex by lazy { Regex("""^(https?://)?(www\.)?bilibili\.com/video/([bB][vV]\w+|[aA][vV]\d+)""") }
private val dynamicPattern by lazy { Regex("""https://t.bilibili.com/(\d+)""") }

/**
 * 将链接或短链接返回为纯号码
 * @return 在成功时会返回, 否则为空
 */
fun parseBiliURL(input: String): String? {
    var s = input.filterNot { it.isWhitespace() }
    if (s.matches(pureNumberRegex)) return s

    if (shortLinkRegex.matches(s)) {
        s = NetUtil.getRedirectedURL(input) ?: return null
    }

    bvAvUrlRegex.find(s)?.groupValues?.getOrNull(3)?.let { return it }

    dynamicPattern.find(s)?.groupValues?.getOrNull(1)?.let { return it }

    return null
}